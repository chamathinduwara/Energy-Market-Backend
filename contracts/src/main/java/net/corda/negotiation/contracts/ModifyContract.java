package net.corda.negotiation.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import net.corda.negotiation.states.ModifyState;
import net.corda.negotiation.states.ProposalState;
import org.jetbrains.annotations.NotNull;

import static net.corda.core.contracts.ContractsDSL.requireThat;
public class ModifyContract implements Contract {
    public static String ID = "net.corda.negotiation.contracts.ModifyContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        final CommandWithParties<CommandData> command = tx.getCommands().get(0);

        if (command.getValue() instanceof Commands.Modify){
            requireThat(require -> {
                require.using("There is exactly one input", tx.getInputStates().size() == 1);
                require.using("The single input is of type ModifyState", tx.inputsOfType(ModifyState.class).size() == 1);
                require.using("There is exactly one output", tx.getOutputs().size() == 1);
                require.using("The single output is of type ModifyState", tx.outputsOfType(ModifyState.class).size() == 1);
                require.using("There is exactly one command", tx.getCommands().size() == 1);
                require.using("There is no timestamp", tx.getTimeWindow() == null);

                ModifyState output = tx.outputsOfType(ModifyState.class).get(0);
                ProposalState input = tx.inputsOfType(ProposalState.class).get(0);

                require.using("The buyer is unmodified in the output", input.getBuyer().equals(output.getBuyer()));
                require.using("The seller is unmodified in the output", input.getSeller().equals(output.getSeller()));

                require.using("The Proposer is a required signer", command.getSigners().contains(input.getProposer().getOwningKey()));
                require.using("The Proposee is a required signer", command.getSigners().contains(input.getProposee().getOwningKey()));

                return null;
            });
        }
    }

    public interface Commands extends CommandData{
        class Modify implements Commands{};
    }
}
