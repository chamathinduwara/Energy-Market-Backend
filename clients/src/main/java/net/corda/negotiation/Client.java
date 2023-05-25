package net.corda.negotiation;


import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.utilities.NetworkHostAndPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static net.corda.core.utilities.NetworkHostAndPort.parse;
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    public static void main(String[] args) {

        if (args.length != 3) throw new IllegalArgumentException("Usage: Client <node address> <rpc username> <rpc password>");
        final NetworkHostAndPort nodeAddress = parse(args[0]);
        final String rpcUsername = args[1];
        final String rpcPassword = args[2];
        final CordaRPCClient client = new CordaRPCClient(nodeAddress);
        final CordaRPCConnection clientConnection = client.start(rpcUsername, rpcPassword);
        final CordaRPCOps proxy = clientConnection.getProxy();

        final List<NodeInfo> nodes = proxy.networkMapSnapshot();
        System.out.println("\n-- Here is the networkMap snapshot --");
        logger.info("{}", nodes);

        CordaX500Name name = proxy.nodeInfo().getLegalIdentities().get(0).getName();
        System.out.println("\n-- Here is the node info of the node that the client connected to --");
        logger.info("{}", name);

        clientConnection.close();

    }
}