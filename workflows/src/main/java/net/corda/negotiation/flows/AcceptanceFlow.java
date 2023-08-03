package net.corda.negotiation.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;


import net.corda.core.transactions.LedgerTransaction;
import net.corda.negotiation.contracts.ProposalAndTradeContract;
import net.corda.negotiation.states.ModifyState;

import net.corda.negotiation.states.TradeState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;


import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;


import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.security.SignatureException;
import java.util.List;

public class AcceptanceFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {

        private UniqueIdentifier proposalId;
        private ProgressTracker progressTracker = new ProgressTracker();

        public Initiator(UniqueIdentifier proposalId) {
            this.proposalId = proposalId;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {


            List<StateAndRef<ModifyState>> ProposalStateAndRefs = getServiceHub().getVaultService().queryBy(ModifyState.class).getStates();


            StateAndRef<ModifyState> inputStateAndRef = ProposalStateAndRefs.stream().filter(ProposalStateAndRef -> {
                ModifyState modifyState  = ProposalStateAndRef.getState().getData();
                return modifyState.getLinearId().equals(proposalId);
            }).findAny().orElseThrow(() -> new IllegalArgumentException("Proposal not found"));

            ModifyState input = inputStateAndRef.getState().getData();

            Party counterparty = (getOurIdentity().getOwningKey().equals(input.getProposer().getOwningKey()))? input.getProposee() : input.getProposer();
            //Creating the output
            TradeState output = new TradeState(input.getAmount(), input.getUnitPrice(), input.getBuyer(), input.getSeller(), input.getLinearId());

            //Creating the command
            List<PublicKey> requiredSigners = ImmutableList.of(input.getProposer().getOwningKey(), input.getProposee().getOwningKey());
            Command command = new Command(new ProposalAndTradeContract.Commands.Accept(), requiredSigners);

           //Building the transaction
            Party notary = inputStateAndRef.getState().getNotary();
            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addInputState(inputStateAndRef)
                    .addOutputState(output, ProposalAndTradeContract.ID)
                    .addCommand(command);

            //Signing the transaction ourselves
            SignedTransaction partStx = getServiceHub().signInitialTransaction(txBuilder);

            //Gathering the counterparty's signature

            FlowSession counterpartySession = initiateFlow(counterparty);
            SignedTransaction fullyStx = subFlow(new CollectSignaturesFlow(partStx, ImmutableList.of(counterpartySession)));

            // Finalising the1 transaction
            SignedTransaction finalisedTx  = subFlow(new FinalityFlow(fullyStx, ImmutableList.of(counterpartySession)));
            return finalisedTx;
        }
    }

    @InitiatedBy(Initiator.class)
    public static class Responder extends FlowLogic<SignedTransaction>{
        private FlowSession counterpartySession;

        public Responder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            SignTransactionFlow signTransactionFlow = new SignTransactionFlow(counterpartySession){

                @Override
                protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {
                    try {
                        LedgerTransaction ledgerTx = stx.toLedgerTransaction(getServiceHub(), false);
                        Party proposer = ledgerTx.inputsOfType(ModifyState.class).get(0).getProposer();
                        if(!proposer.equals(counterpartySession.getCounterparty())){
                            throw new FlowException("Only the proposer can accept a proposal.");
                        }
                    } catch (SignatureException e) {
                        throw new FlowException("Check transaction failed");
                    }
                }
            };
            SecureHash txId = subFlow(signTransactionFlow).getId();
            SignedTransaction finalisedTx = subFlow(new ReceiveFinalityFlow(counterpartySession, txId));
            return finalisedTx;
        }
    }
}


