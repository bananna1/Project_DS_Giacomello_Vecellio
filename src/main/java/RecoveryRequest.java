import akka.actor.ActorRef;

public class RecoveryRequest extends ExternalRequest {
    // INSERIRE CAMPI SPECIFICI DEL RECOVERY
    public RecoveryRequest(ActorRef bootStrappingPeer, ActorRef client) {
        super(Ring.Node.ExternalRequestType.Recovery, bootStrappingPeer, client);
    }
}
