import akka.actor.*;

public class Request {
    private int key;
    private Node.RequestType type;
    private ActorRef client;
    private String newValue;

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
}
