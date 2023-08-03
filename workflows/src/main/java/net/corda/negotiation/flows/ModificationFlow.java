package net.corda.negotiation.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;


import net.corda.core.internal.FetchDataFlow;
import net.corda.negotiation.contracts.ProposalAndTradeContract;
import net.corda.negotiation.states.ModifyState;
import net.corda.negotiation.states.ProposalState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;

import net.corda.core.transactions.LedgerTransaction;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.security.SignatureException;
import java.util.List;

public class ModificationFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction>{
        private UniqueIdentifier proposalId;
        private Double newAmount;
        private Double newUnitPrice;
        private ProgressTracker progressTracker = new ProgressTracker();

        public Initiator(UniqueIdentifier proposalId, Double newAmount, Double newUnitPrice) {
            this.proposalId = proposalId;
            this.newAmount = newAmount;
            this.newUnitPrice = newUnitPrice;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            List<StateAndRef<ProposalState>> ProposalStateAndRefs = getServiceHub().getVaultService().queryBy(ProposalState.class).getStates();

            StateAndRef<ProposalState> inputStateAndRef = ProposalStateAndRefs.stream().filter(ProposalStateAndRef ->{
                ProposalState proposalState = ProposalStateAndRef.getState().getData();
                return proposalState.getLinearId().equals(proposalId);
            }).findAny().orElseThrow(() -> new IllegalArgumentException("proposal not found"));


            ProposalState input = inputStateAndRef.getState().getData();

            //Creating the output
            Party counterparty = (getOurIdentity().getOwningKey().equals(input.getProposee().getOwningKey()))? input.getProposer() : input.getProposee();

            if (!getOurIdentity().getOwningKey().equals(input.getProposee().getOwningKey())){
                throw new IllegalArgumentException("you are not the proposee");
            } else if (!counterparty.getOwningKey().equals(input.getProposer().getOwningKey())) {
                throw new IllegalArgumentException("counterparty is not the proposer");
            }

            ModifyState output = new ModifyState(newAmount, newUnitPrice, input.getBuyer(), input.getSeller(), counterparty, getOurIdentity(), input.getLinearId());

//
            //Creating the command
            List<PublicKey> requiredSigners = ImmutableList.of(counterparty.getOwningKey(), getOurIdentity().getOwningKey());
            Command command = new Command(new ProposalAndTradeContract.Commands.Modify(), requiredSigners);

            //Building the transaction
            Party notary = inputStateAndRef.getState().getNotary();
            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addInputState(inputStateAndRef)
                    .addOutputState(output, ProposalAndTradeContract.ID)
                    .addCommand(command);

            //Signing the transaction ourselves
            SignedTransaction partStx = getServiceHub().signInitialTransaction(txBuilder);

            //Gathering the counterparty's signatures
            FlowSession counterpartySession = initiateFlow(counterparty);
            SignedTransaction fullyStx = subFlow(new CollectSignaturesFlow(partStx, ImmutableList.of(counterpartySession)));

            //Finalising the transaction
            SignedTransaction finalTx = subFlow(new FinalityFlow(fullyStx,ImmutableList.of(counterpartySession)));
            return finalTx;
        }
    }

    @InitiatedBy(Initiator.class)
    public static class Responder extends FlowLogic<SignedTransaction> {
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
                        Party proposee = ledgerTx.inputsOfType(ProposalState.class).get(0).getProposee();
                        if(!proposee.equals(counterpartySession.getCounterparty())){
                            throw new FlowException("Only the proposee can modify a proposal.");
                        }
                    } catch (SignatureException e) {
                        throw new FlowException();
                    }
                }
            };
            SecureHash txId = subFlow(signTransactionFlow).getId();
            SignedTransaction finalisedTx = subFlow(new ReceiveFinalityFlow(counterpartySession, txId));
            return finalisedTx;
        }
    }
}
