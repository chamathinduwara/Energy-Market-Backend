package net.corda.negotiation.flows;



import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;


import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.negotiation.contracts.ProposalAndTradeContract;
import net.corda.negotiation.states.KillState;

import net.corda.negotiation.states.ProposalState;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.List;


public class KillProposalFlow {
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {
        private final UniqueIdentifier proposalId;

        public Initiator(UniqueIdentifier proposalId) {
            this.proposalId = proposalId;
        }


        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {

            List<StateAndRef<ProposalState>> ProposalStateAndRefs = getServiceHub().getVaultService().queryBy(ProposalState.class).getStates();


            StateAndRef<ProposalState> inputStateAndRef = ProposalStateAndRefs.stream().filter(ProposalStateAndRef -> {
                ProposalState proposalState = ProposalStateAndRef.getState().getData();
                return proposalState.getLinearId().equals(proposalId);
            }).findAny().orElseThrow(() -> new IllegalArgumentException("Proposal not found"));

            ProposalState input = inputStateAndRef.getState().getData();
            Party counterparty = (getOurIdentity().getOwningKey().equals(input.getProposee().getOwningKey()))? input.getProposer() : input.getProposee();
            KillState output = new KillState(input.getAmount(), input.getUnitPrice(), input.getBuyer(), input.getSeller(), input.getProposer(), input.getProposee(), input.getLinearId());

            List<PublicKey> requiredSigners = ImmutableList.of(counterparty.getOwningKey(), getOurIdentity().getOwningKey());
            Command command = new Command(new ProposalAndTradeContract.Commands.Kill(), requiredSigners);

            TransactionBuilder tx = new TransactionBuilder(getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0))
                    .addInputState(inputStateAndRef)
                    .addOutputState(output, ProposalAndTradeContract.ID)
                    .addCommand(command);

            SignedTransaction partSTx = getServiceHub().signInitialTransaction(tx);

            FlowSession counterPartySession = initiateFlow(counterparty);

            SignedTransaction fullyStx = subFlow(new CollectSignaturesFlow(partSTx, ImmutableList.of(counterPartySession)));

            //Finalising the transaction
            SignedTransaction finalTx = subFlow(new FinalityFlow(fullyStx,ImmutableList.of(counterPartySession)));
            return finalTx;
        }
    }

    @InitiatedBy(Initiator.class)
    public static class Responder extends FlowLogic<SignedTransaction>{

        private final FlowSession counterpartySession;

        public Responder(FlowSession counterPartySession) {
            this.counterpartySession = counterPartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            SignTransactionFlow signTransactionFlow = new SignTransactionFlow(counterpartySession){
                @Override
                protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {

                }
            };
            SecureHash txId = subFlow(signTransactionFlow).getId();
            SignedTransaction finalisedTx = subFlow(new ReceiveFinalityFlow(counterpartySession, txId));
            return finalisedTx;
        }
    }
}

