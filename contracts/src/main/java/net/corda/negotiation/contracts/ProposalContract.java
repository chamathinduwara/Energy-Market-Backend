package net.corda.negotiation.contracts;

import com.google.common.collect.ImmutableSet;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import net.corda.negotiation.states.ProposalState;
import org.jetbrains.annotations.NotNull;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class ProposalContract implements Contract {
    public static String ID = "net.corda.samples.negotiation.contracts.ProposalContract";


    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        final CommandWithParties<CommandData> command = tx.getCommands().get(0);
        if(command.getValue() instanceof Commands.Propose){
            requireThat(require ->{
                require.using("There are no input", tx.getInputs().isEmpty());
                require.using("Only one output state should be created.", tx.getOutputs().size() == 1);
                require.using("The single output is of type ProposalState", tx.outputsOfType(ProposalState.class).size()==1);
                require.using("There is exactly one command", tx.getCommands().size() == 1);
                require.using("There is no timestamp", tx.getTimeWindow() == null);

                ProposalState output = tx.outputsOfType(ProposalState.class).get(0);
                require.using("The buyer and seller are the proposer and the proposee", ImmutableSet.of(output.getBuyer(), output.getSeller()).equals(ImmutableSet.of(output.getProposer(), output.getProposee())));
                require.using("The proposer is a required signer", command.getSigners().contains(output.getProposer().getOwningKey()));
                require.using("The proposee is a required signer", command.getSigners().contains(output.getProposee().getOwningKey()));
                return null;
            });
        }else {
            throw new IllegalArgumentException("Command of incorrect type");
        }
    }


    public interface Commands extends CommandData {

        class Propose implements Commands{

        };
    }
}

