import akka.actor.*;

public class ExternalRequest {
    private Peer joiningPeer;
    private ActorRef bootStrappingPeer;
    private ActorRef client;
    private Ring.Node.ExternalRequestType type;
    private int id = 0;
    private static int count = 0;
    private int nResponses = 0;
    private int storageSize = 0;

    public ExternalRequest(Peer joiningPeer, Ring.Node.ExternalRequestType type, ActorRef bootStrappingPeer, ActorRef client) {
        this.joiningPeer = joiningPeer;
        this.type = type;
        this.bootStrappingPeer = bootStrappingPeer;
        this.client = client;
        this.id = ++count;
    }
    
    public Ring.Node.ExternalRequestType getType () {
        return type;
    }

    public ActorRef getClient() {
        return client;
    }
    public int getID() {
        return id;
    }
    public int getnResponses() {
        return nResponses;
    }
    public void incrementnResponses() {
        nResponses++;
    }
    public void setStorageSize(int storageSize) {
        this.storageSize = storageSize;
    }
    public int getStorageSize() {
        return storageSize;
    }
    public Peer getJoiningPeer() {
        return joiningPeer;
    }

}
