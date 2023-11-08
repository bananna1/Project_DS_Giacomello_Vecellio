import akka.actor.*;

public class Request {
    private int key;                                // Key of the item
    private Ring.Node.RequestType type;             // Type of the request
    private ActorRef client;                        // Client that ask for the request
    private ActorRef owner;                         // Owner of the request
    private String newValue;                        // New value if it is an update
    private Item currBest = null;                   // Best version found
    private int nResponses = 0;                     // Number of responses
    private int okResponses = 0;                    // Number of ACK
    private int id = 0;                             // ID of the request
    private static int count = 0;                   // Count for the ID

    /**
     * Constructor of the request
     * @param key Key of the item
     * @param type Type of the request
     * @param client Client that ask for the request
     * @param newValue New value if it is an update
     */
    public Request(int key, Ring.Node.RequestType type, ActorRef client, String newValue) {
        this.key = key;
        this.type = type;
        this.client = client;
        this.newValue = newValue;
        this.id = ++count;
    }

    /**
     * Method to get the key of the item
     * @return Key of the item
     */
    public int getKey () {
        return key;
    }
        
    /** 
     * Method to get the type of the request
     * @return Type of the request
     */
    public Ring.Node.RequestType getType () {
        return type;
    }

    /**
     * Method to get the client
     * @return Client that ask for the request
     */
    public ActorRef getClient() {
        return client;
    }
    
    /**
     * Method to get the new value
     * @return New value if it is an update
     */
    public String getNewValue() {
        return newValue;
    }
    
    /**
     * Method to get the item with the best version
     * @return Item with the lastest version
     */
    public Item getCurrBest() {
        return currBest;
    }

    /**
     * Method to get the ID of the request
     * @return ID of the request
     */
    public int getID() {
        return id;
    }

    /**
     * Method to set the item with the best version
     * @param newItem Item with the lastest version
     */
    public void setCurrBest(Item newItem) {
        currBest = newItem;
    }
    
    /**
     * Method to get the number of responses
     * @return Number of responses
     */
    public int getnResponses() {
        return nResponses;
    }
    
    /**
     * Method used to increment the number of responses
     */
    public void incrementnResponses() {
        nResponses++;
    }

    /**
     * Method to get the number of ACKs
     * @return Number of ACKs
     */
    public int getOkResponses() {
        return okResponses;
    }
    
    /**
     * Method used to increment the number of ACKs
     */
    public void incrementOkResponses() {
        okResponses ++;
    }

    /**
     * Method used to set the owner of the request
     * @param owner Owner of the request
     */
    public void setOwner(ActorRef owner) {
        this.owner = owner;
    }

    /**
     * Method used to get the owner of the request
     * @return Owner of the request
     */
    public ActorRef getOwner() {
        return owner;
    }
}
