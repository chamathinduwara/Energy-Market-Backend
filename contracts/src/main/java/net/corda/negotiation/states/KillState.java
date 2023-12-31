package net.corda.negotiation.states;

import com.google.common.collect.ImmutableList;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.negotiation.contracts.ProposalAndTradeContract;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@BelongsToContract(ProposalAndTradeContract.class)
public class KillState implements LinearState {
    private Double amount;
    private Double unitPrice;
    private Party buyer;
    private Party seller;
    private Party proposer;
    private Party proposee;
    private UniqueIdentifier linearId;

    @ConstructorForDeserialization
    public KillState(Double amount,Double unitPrice, Party buyer, Party seller, Party proposer, Party proposee, UniqueIdentifier linearId) {
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.buyer = buyer;
        this.seller = seller;
        this.proposer = proposer;
        this.proposee = proposee;
        this.linearId = linearId;
    }

    public KillState(Double amount,Double unitPrice, Party buyer, Party seller, Party proposer, Party proposee) {
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.buyer = buyer;
        this.seller = seller;
        this.proposer = proposer;
        this.proposee = proposee;
        this.linearId = new UniqueIdentifier();
    }

    public Double getAmount() {
        return amount;
    }

    public Double getUnitPrice(){return unitPrice;}

    public Party getBuyer() {
        return buyer;
    }

    public Party getSeller() {
        return seller;
    }

    public Party getProposer() {
        return proposer;
    }

    public Party getProposee() {
        return proposee;
    }

    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(proposer, proposee);
    }
}
