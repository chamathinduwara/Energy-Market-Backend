package net.corda.negotiation.webserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.corda.client.jackson.JacksonSupport;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.identity.Party;

import net.corda.core.transactions.SignedTransaction;
import net.corda.negotiation.flows.AcceptanceFlow;
import net.corda.negotiation.flows.ModificationFlow;
import net.corda.negotiation.flows.ProposalFlow;
import net.corda.negotiation.states.ProposalState;

import net.corda.negotiation.states.TradeState;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
@RequestMapping("/")
public class Controller {

    private static final Logger logger = LoggerFactory.getLogger(RestController.class);

    private final CordaRPCOps proxy;

    private final CordaX500Name me;

    public Controller(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
        this.me = proxy.nodeInfo().getLegalIdentities().get(0).getName();
    }

    public String toDisplayString(X500Name name){
        return BCStyle.INSTANCE.toString(name);
    }

    private boolean isNotary(NodeInfo nodeInfo) {
        return !proxy.notaryIdentities()
                .stream().filter(el -> nodeInfo.isLegalIdentity(el))
                .collect(Collectors.toList()).isEmpty();
    }

    private boolean isMe(NodeInfo nodeInfo){
        return nodeInfo.getLegalIdentities().get(0).getName().equals(me);}

    private boolean isNetworkMap(NodeInfo nodeInfo){
        return nodeInfo.getLegalIdentities().get(0).getName().getOrganisation().equals("Network Map Service");
    }

    @Configuration
    class Plugin {
        @Bean
        public ObjectMapper registerModule() {
            return JacksonSupport.createNonRpcMapper();
        }
    }

    @GetMapping(value = "/status", produces = TEXT_PLAIN_VALUE)
    private String status() {
        return "200";
    }

    @GetMapping(value = "/servertime", produces = TEXT_PLAIN_VALUE)
    private String serverTime() {
        return (LocalDateTime.ofInstant(proxy.currentNodeTime(), ZoneId.of("UTC"))).toString();
    }

    @GetMapping(value = "/addresses", produces = TEXT_PLAIN_VALUE)
    private String addresses() {
        return proxy.nodeInfo().getAddresses().toString();
    }

    @GetMapping(value = "/identities", produces = TEXT_PLAIN_VALUE)
    private String identities() {
        return proxy.nodeInfo().getLegalIdentities().toString();
    }

    @GetMapping(value = "/platformversion", produces = TEXT_PLAIN_VALUE)
    private String platformVersion() {
        return Integer.toString(proxy.nodeInfo().getPlatformVersion());
    }

    @GetMapping(value = "/peers", produces = APPLICATION_JSON_VALUE)
    public HashMap<String, List<String>> getPeers() {
        HashMap<String, List<String>> myMap = new HashMap<>();

        // Find all nodes that are not notaries, ourself, or the network map.
        Stream<NodeInfo> filteredNodes = proxy.networkMapSnapshot().stream()
                .filter(el -> !isNotary(el) && !isMe(el) && !isNetworkMap(el));
        // Get their names as strings
        List<String> nodeNames = filteredNodes.map(el -> el.getLegalIdentities().get(0).getName().toString())
                .collect(Collectors.toList());

        myMap.put("peers", nodeNames);
        return myMap;
    }

    @GetMapping(value = "/notaries", produces = TEXT_PLAIN_VALUE)
    private String notaries() {
        return proxy.notaryIdentities().toString();
    }

    @GetMapping(value = "/flows", produces = TEXT_PLAIN_VALUE)
    private String flows() {
        return proxy.registeredFlows().toString();
    }

    @GetMapping(value = "/states", produces = TEXT_PLAIN_VALUE)
    private String states() {
        return proxy.vaultQuery(ContractState.class).getStates().toString();
    }

    @GetMapping(value = "/me",produces = APPLICATION_JSON_VALUE)
    private HashMap<String, String> whoami(){
        HashMap<String, String> myMap = new HashMap<>();
        myMap.put("me", me.toString());
        return myMap;
    }

    @GetMapping(value = "/ious",produces = APPLICATION_JSON_VALUE)
    public List<StateAndRef<ProposalState>> getIOUs() {
        // Filter by state type: IOU.
        return proxy.vaultQuery(ProposalState.class).getStates();
    }
    @GetMapping(value = "/transaction-iou",produces = APPLICATION_JSON_VALUE)
    public List<StateAndRef<TradeState>> getTradeIOUs() {
        // Filter by state type: IOU.
        return proxy.vaultQuery(TradeState.class).getStates();
    }

    @PostMapping(value = "create-iou",  produces =  TEXT_PLAIN_VALUE , headers =  "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<String> issuIOU(HttpServletRequest request) throws IllegalArgumentException{
        Boolean isBuyer = Boolean.valueOf(request.getParameter("isBuyer"));
        Double amount = Double.valueOf(request.getParameter("iouValue"));
        Double rate = Double.valueOf(request.getParameter("rate"));
        Double unitPrice = Double.valueOf(request.getParameter("unitPrice"));
        String party = request.getParameter("partyName");

        CordaX500Name partyX500Name =CordaX500Name.parse(party);
        Party otherParty = proxy.wellKnownPartyFromX500Name(partyX500Name);

        try {
            SignedTransaction result = proxy.startTrackedFlowDynamic(ProposalFlow.Initiator.class, isBuyer, amount, rate, unitPrice, otherParty).getReturnValue().get();
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Transaction id "+ result.getId() +" committed to ledger.\n " + result.getTx().getOutput(0));
        }
        catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping(value = "modify-iou",  produces =  TEXT_PLAIN_VALUE , headers =  "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<String> modufyIOU(HttpServletRequest request) throws IllegalArgumentException{
        String uniqueId =  request.getParameter(("proposalId"));
        Double newAmount = Double.valueOf(request.getParameter("newValue"));
        Double newUnitPrice = Double.valueOf(request.getParameter("newUnitPrice"));
        Double newRate = Double.valueOf(request.getParameter("newRate"));

        UniqueIdentifier proposalId = UniqueIdentifier.Companion.fromString(uniqueId);

        try{
            SignedTransaction result = proxy.startTrackedFlowDynamic(ModificationFlow.Initiator.class, proposalId, newAmount, newUnitPrice, newRate).getReturnValue().get();
            return  ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Transaction id"+ result.getId() + "committed to ledger. \n" + result.getTx().getOutput(0));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

    }

    @PostMapping(value = "accept-iou",  produces =  TEXT_PLAIN_VALUE , headers =  "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<String> acceptIOU(HttpServletRequest request) throws IllegalArgumentException{
        String uniqueId = request.getParameter("proposalId");

        UniqueIdentifier proposalId = UniqueIdentifier.Companion.fromString(uniqueId);

        try{
            SignedTransaction result = proxy.startTrackedFlowDynamic(AcceptanceFlow.Initiator.class, proposalId).getReturnValue().get();
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(("Transaction id") + result.getId() + "committed to ledger. \n" + result.getTx().getOutput(0));
        }catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

    }


}
