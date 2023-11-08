import akka.actor.AbstractActor;
import akka.actor.Props;

public class Client extends AbstractActor {
    
    private int id;                     // ID of the client
    
    /**
     * Constructor
     * @param id ID of the client
     */
    public Client (int id) {
        this.id = id;
    }

    /**
     * Method used to create a new client
     * @param id ID of the client
     * @return Props for the client
     */
    static public Props props(int id) {
        return Props.create(Client.class, () -> new Client(id));
    }

    /**
     * Method used to get the ID
     * @return ID of the client
     */
    public int getId() {
        return this.id;
    }

    /**
     * Method used to send a message to the client for ReturnValueMsg
     * @param msg Instance of ReturnValueMsg
     */
    public void onReturnValueMsg(Ring.ReturnValueMsg msg) {
        System.out.println("Return message - Client: " + this.id + ", id request: " + msg.requestID + ", request type " + msg.requestType + ", value: " + msg.item.getValue() + "; version: " + msg.item.getVersion());
    }

    /**
     * Method used to send an error message to the client
     * @param msg Instance of ErrorMsg
     */
    public void onErrorMsg(Ring.ErrorMsg msg) {
        System.out.println(msg.error);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Ring.ReturnValueMsg.class, this::onReturnValueMsg)
                .match(Ring.ErrorMsg.class, this::onErrorMsg)
                .build();
    }
}