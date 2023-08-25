import akka.actor.*;

public class Request {
    private int id;
    private int key;
    private Node.RequestType type;
    private ActorRef client;

    public Request(int id, int key, Node.RequestType read, ActorRef client) {
        this.id = id;
        this.key = key;
        this.type = read;
        this.client = client;
    }
    
    public int getID () {
        return id;
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
