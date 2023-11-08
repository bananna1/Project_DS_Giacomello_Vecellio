import akka.actor.ActorRef;

public class RecoveryRequest extends ExternalRequest {
    
    /**
     * Constructor of the recovery request
     * @param client Client that request the recovery
     */
    public RecoveryRequest(ActorRef client) {
        super(Ring.Node.ExternalRequestType.Recovery, client);
    }
}
