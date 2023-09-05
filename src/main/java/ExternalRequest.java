import akka.actor.*;

public class ExternalRequest {
    private Ring.Node joiningNode;
    private ActorRef bootStrappingPeer;
    private Ring.Node.ExternalRequestType type;
    private int id = 0;
    private static int count = 0;

    public ExternalRequest(Ring.Node joiningNode, Ring.Node.ExternalRequestType type, ActorRef bootStrappingPeer) {
        this.joiningNode = joiningNode;
        this.type = type;
        this.bootStrappingPeer = bootStrappingPeer;
        this.id = ++count;
    }

    public int getKey () {
        return joiningNode.getID();
    }
    
    public Ring.Node.ExternalRequestType getType () {
        return type;
    }


}
