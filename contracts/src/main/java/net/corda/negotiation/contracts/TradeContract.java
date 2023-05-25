package net.corda.negotiation.contracts;

import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

public class TradeContract implements Contract {
    public static String ID = "net.corda.negotiation.contracts.TradeContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {

    }
}
