import akka.actor.*;

public class Request {
    private int key;
    private Ring.Node.RequestType type;
    private ActorRef client;
    private ActorRef owner;
    private String newValue;
    private Item currBest = null;
    private int nResponses = 0;
    private int okResponses = 0;
    private int id = 0;
    private static int count = 0;

    public Request(int key, Ring.Node.RequestType type, ActorRef client, String newValue) {
        this.key = key;
        this.type = type;
        this.client = client;
        this.newValue = newValue;
        this.id = ++count;
    }

    public int getKey () {
        return key;
    }
    
    public Ring.Node.RequestType getType () {
        return type;
    }

    public void setClient(ActorRef client) { // FORSE NON SERVE
        this.client = client;
    }
    public ActorRef getClient() {
        return client;
    }
    public String getNewValue() {
        return newValue;
    }
    public Item getCurrBest() {
        return currBest;
    }
    public int getID() {
        return id;
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
    public int getOkResponses() {
        return okResponses;
    }
    public void incrementOkResponses() {
        okResponses ++;
    }
    public void setOwner(ActorRef owner) {
        this.owner = owner;
    }
    public ActorRef getOwner() {
        return owner;
    }
}
