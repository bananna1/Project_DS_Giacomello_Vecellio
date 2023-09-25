import akka.actor.*;

public class ExternalRequest {

    private ActorRef client;
    private Ring.Node.ExternalRequestType type;
    private int id;
    private static int count = 0;
    private int readResponses = 0;

    public ExternalRequest(Ring.Node.ExternalRequestType type, ActorRef client) {
        this.type = type;
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
        return readResponses;
    }
    public void incrementnResponses() {
        readResponses++;
    }


}
