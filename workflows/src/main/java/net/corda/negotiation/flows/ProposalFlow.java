package net.corda.negotiation.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;

import net.corda.negotiation.contracts.ProposalAndTradeContract;

import net.corda.negotiation.states.ProposalState;

import net.corda.core.contracts.Command;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.List;
import net.corda.core.identity.CordaX500Name;

public class ProposalFlow {
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {
        private Boolean isBuyer;
        private Double amount;
        private Double unitPrice;
        private Party counterparty;

//        private String notaryName;

        public Initiator(Boolean isBuyer, Double amount,Double unitPrice, Party counterparty) {
            this.isBuyer = isBuyer;
            this.amount = amount;
            this.unitPrice = unitPrice;
            this.counterparty = counterparty;
        }

//        public void setNotaryName(String notaryName) {
//            this.notaryName = notaryName;
//        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            Party buyer, seller;
            if(isBuyer){
                buyer = getOurIdentity();
                seller = counterparty;
            }else {
                throw new FlowException("Invalid initiator");
            }
            ProposalState output = new ProposalState(amount, unitPrice, buyer, seller,buyer, seller);

            //Creating the command
            ProposalAndTradeContract.Commands.Propose commandType = new ProposalAndTradeContract.Commands.Propose();

//  collecting public keys
            List<PublicKey> requiredSigners = ImmutableList.of(getOurIdentity().getOwningKey(), counterparty.getOwningKey());
            Command command = new Command(commandType, requiredSigners);

            //Building the Proposal

            // Obtain a reference to a notary we wish to use.
            /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
            final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=moratuwa,C=SL"));

            if (notary == null) {
                throw new FlowException("Notary not found");
            }
            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(output, ProposalAndTradeContract.ID)
                    .addCommand(command);

            //Signing the transaction ourselves
            SignedTransaction partStx = getServiceHub().signInitialTransaction(txBuilder);

            //Gather counterparty sigs
            FlowSession counterpartySession = initiateFlow(counterparty);
            SignedTransaction fullyStx = subFlow(new CollectSignaturesFlow(partStx, ImmutableList.of(counterpartySession)));

            //Finalise the transaction
            SignedTransaction finalisedTx = subFlow(new FinalityFlow(fullyStx, ImmutableList.of(counterpartySession)));
//            return finalisedTx.getTx().outputsOfType(ProposalState.class).get(0).getLinearId();
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

                }
            };
            SecureHash txId = subFlow(signTransactionFlow).getId();

            SignedTransaction finalisedTx = subFlow(new ReceiveFinalityFlow(counterpartySession, txId));
            return finalisedTx;
        }
    }
}
