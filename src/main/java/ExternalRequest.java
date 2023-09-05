import akka.actor.*;

public class ExternalRequest {
    private Peer joiningPeer;
    private ActorRef bootStrappingPeer;
    private Ring.Node.ExternalRequestType type;
    private int id = 0;
    private static int count = 0;

    public ExternalRequest(Peer joiningPeer, Ring.Node.ExternalRequestType type, ActorRef bootStrappingPeer) {
        this.joiningPeer = joiningPeer;
        this.type = type;
        this.bootStrappingPeer = bootStrappingPeer;
        this.id = ++count;
    }

    public int getKey () {
        return joiningPeer.getID();
    }
    
    public Ring.Node.ExternalRequestType getType () {
        return type;
    }


}
