package net.corda.negotiation.states;

import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;


public class TradeStateTests {

    private Double amount = 10.0;
    private Double unitPrice = 1.0;
    private Double rate = 0.5;
    private final Party buyer = new TestIdentity(new CordaX500Name("Bob", "", "GB")).getParty();
    private final Party seller = new TestIdentity(new CordaX500Name("Charlie", "", "GB")).getParty();

    @Test
    public void constructorTest() {

        UniqueIdentifier tempId = new UniqueIdentifier();
        TradeState ts = new TradeState(amount,unitPrice, rate, buyer, seller, tempId);

        assertEquals(ts.getAmount(), amount);
        assertEquals(ts.getUnitPrice(),unitPrice);
        assertEquals(ts.getRate(),rate);
        assertEquals(ts.getBuyer(), buyer);
        assertEquals(ts.getSeller(), seller);
        assertEquals(ts.getLinearId(), tempId);

        assertNotEquals(ts.getAmount(), 0);
        assertNotEquals(ts.getAmount(), -5);
        assertNotEquals(ts.getAmount(), null);
    }

    @Test
    public void linearIdTest() {
        TradeState ts = new TradeState(amount,unitPrice,rate, buyer, seller);

        // ensure ID is generated with shorter constructor stub
        assertTrue(ts.getLinearId() instanceof UniqueIdentifier);
    }

    @Test
    public void participantTest() {
        TradeState ts = new TradeState(amount,unitPrice,rate, buyer, seller);

        // ensure participants are generated correctly
        assertTrue(ts.getParticipants().contains(buyer));
        assertTrue(ts.getParticipants().contains(seller));
    }

}
