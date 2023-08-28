import akka.actor.*;
import scala.concurrent.duration.Duration;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class Request {
    private int key;
    private Node.RequestType type;
    private ActorRef client;
    private ActorRef coordinator;
    private String newValue;
    private Item currBest = null;
    private int nResponses = 0;

<<<<<<< Updated upstream
    final static int TIMEOUT = 2000;

    public Request(int key, Node.RequestType type, ActorRef client, String newValue) {
=======
    public Request(int key, Node.RequestType type, ActorRef client, ActorRef coordinator, String newValue) {
>>>>>>> Stashed changes
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
