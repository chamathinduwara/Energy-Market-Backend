package net.corda.negotiation.webserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.corda.client.jackson.JacksonSupport;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.transactions.SignedTransaction;
import net.corda.negotiation.flows.*;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("api/")
public class PostController {
    private static final Logger logger = LoggerFactory.getLogger(RestController.class);

    private final CordaRPCOps proxy;

    private final CordaX500Name me;

    public PostController(NodeRPCConnection rpc) {
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
    static
    class Plugin {
        @Bean
        public ObjectMapper registerModule() {
            return JacksonSupport.createNonRpcMapper();
        }
    }


    @PostMapping(value = "create-proposal", headers = "Content-type=application/json")
    public APIResponse<Void> createProposal(@RequestBody Forms.ProposalForm proposalFrom) throws ExecutionException, InterruptedException {

        try{
            String party = proposalFrom.getPartyName();
            CordaX500Name partyX500Name =CordaX500Name.parse(party);
            Party otherParty = proxy.wellKnownPartyFromX500Name(partyX500Name);

            SignedTransaction result = proxy
                    .startTrackedFlowDynamic(ProposalFlow.Initiator.class, proposalFrom.getBuyer(), proposalFrom.getUnitAmount(), proposalFrom.getUnitPrice(), otherParty )
                    .getReturnValue()
                    .get();
            return APIResponse.success();
        } catch (Exception e){
            return APIResponse.error(e.getMessage());
        }

    }

    @PostMapping(value = "modify-proposal", headers =  "Content-Type=application/json")
    public APIResponse<Void> modufyIOU(@RequestBody Forms.ModifyForm modifyForm) throws IllegalArgumentException{
        try{
            UniqueIdentifier proposalId = UniqueIdentifier.Companion.fromString(modifyForm.getProposalId());
            SignedTransaction result = proxy.startTrackedFlowDynamic(ModificationFlow.Initiator.class, proposalId, modifyForm.getNewValue(), modifyForm.getNewUnitPrice())
                    .getReturnValue()
                    .get();
            return  APIResponse.success();


        } catch (Exception e) {
            return APIResponse.error(e.getMessage());
        }

    }

    @PostMapping(value = "accept-proposal", headers =  "Content-Type=application/json")
    public APIResponse<Void> acceptIOU(@RequestBody Forms.AcceptForm acceptForm) throws IllegalArgumentException{

        UniqueIdentifier proposalId = UniqueIdentifier.Companion.fromString(acceptForm.getProposalId());

        try{
            SignedTransaction result = proxy.startTrackedFlowDynamic(AcceptanceFlow.Initiator.class, proposalId)
                    .getReturnValue()
                    .get();
            return APIResponse.success();

        }catch (Exception e){
            return APIResponse.error(e.getMessage());
        }

    }

    @PostMapping(value = "kill-proposal", headers = "Content-Type=application/json")
    public APIResponse<Void> killProposal(@RequestBody Forms.KillProposalForm killProposalForm) throws IllegalArgumentException{

        UniqueIdentifier proposalId = UniqueIdentifier.Companion.fromString((killProposalForm.getProposalId()));

        try{
            SignedTransaction result = proxy.startTrackedFlowDynamic(KillProposalFlow.Initiator.class, proposalId).getReturnValue().get();
            return APIResponse.success();
        }catch (Exception e){
            return APIResponse.error(e.getMessage());
        }
    }

    @PostMapping(value = "kill-modify", headers = "Content-Type=application/json")
    public APIResponse<Void> killMOdify(@RequestBody Forms.KillModifyForm killModifyForm) throws IllegalArgumentException{

        UniqueIdentifier proposalId = UniqueIdentifier.Companion.fromString((killModifyForm.getProposalId()));

        try{
            SignedTransaction result = proxy.startTrackedFlowDynamic(KillModificationFlow.Initiator.class, proposalId).getReturnValue().get();
            return APIResponse.success();
        }catch (Exception e){
            return APIResponse.error(e.getMessage());
        }
    }


}
