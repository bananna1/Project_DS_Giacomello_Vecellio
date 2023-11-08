import akka.actor.*;

public class ExternalRequest {

    private ActorRef client;                                // Client that request the operation
    private Ring.Node.ExternalRequestType type;             // Type of the request
    private int id;                                         // ID of the request
    private static int count = 0;                           // Count for the ID
    private int readResponses = 0;                          // Read responses for a request

    /**
     * Constructor for an external request
     * @param type Request type
     * @param client Client that request the operation
     */
    public ExternalRequest(Ring.Node.ExternalRequestType type, ActorRef client) {
        this.type = type;
        this.client = client;
        this.id = ++count;
    }
    
    /**
     * Method used to get the type
     * @return Request type
     */
    public Ring.Node.ExternalRequestType getType () {
        return type;
    }

    /**
     * Method used to get the client
     * @return ID of the client
     */
    public ActorRef getClient() {
        return client;
    }

    /**
     * Method used to get the ID of the request
     * @return ID of the request
     */
    public int getID() {
        return id;
    }
    
    /**
     * Method used to get the number of read responses
     * @return Number of read responses
     */
    public int getnResponses() {
        return readResponses;
    }

    /**
     * Method used to increment the number of read responses
     */
    public void incrementnResponses() {
        readResponses++;
    }
}
