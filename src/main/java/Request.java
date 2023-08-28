import akka.actor.*;

public class Request {
    private int key;
    private Node.RequestType type;
    private ActorRef client;
    private String newValue;
    private Item currBest = null;
    private int nResponses = 0;

    final static int TIMEOUT = 2000;

    public Request(int key, Node.RequestType type, ActorRef client, String newValue) {
        this.key = key;
        this.type = type;
        this.client = client;
        this.newValue = newValue;
    }

    public int getKey () {
        return key;
    }
    
    public Node.RequestType getType () {
        return type;
    }

    public void setClient(ActorRef client) { // FORSE NON SERVE
        this.client = client;
    }
    public ActorRef getClient() {
        return this.client;
    }
    public String getNewValue() {
        return newValue;
    }
    public Item getCurrBest() {
        return currBest;
    }
    public void setCurrBest(Item newItem) {
        currBest = newItem;
    }
    public int getnResponses() {
        return nResponses;
    }
    public void incrementnResponses() {
        nResponses++;
    }
}
