package net.corda.negotiation.webserver;

import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.client.rpc.RPCConnection;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.NetworkHostAndPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class NodeRPCConnection implements AutoCloseable{

    @Value("${config.rpc.host}")
    private String host;

    @Value("${config.rpc.username}")
    private String username;

    @Value("${config.rpc.password}")
    private String password;

    @Value("${config.rpc.port}")
    private Integer rpcPort;

    private CordaRPCConnection rpcConnection;

    CordaRPCOps proxy;

    @PostConstruct
    public void initialiseNodeRPCConnection() {
        NetworkHostAndPort rpcAddress = new NetworkHostAndPort(host, rpcPort);
        CordaRPCClient rpcClient = new CordaRPCClient(rpcAddress);
        rpcConnection = rpcClient.start(username, password);
        proxy = rpcConnection.getProxy();
    }
    @PreDestroy
    public void close() throws Exception {

    }
}
