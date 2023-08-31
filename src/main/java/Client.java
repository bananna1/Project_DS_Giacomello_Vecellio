import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

public class Client extends AbstractActor {
    private int id;
    public Client (int id) {
        this.id = id;
    }
    static public Props props(int id) {
        return Props.create(Client.class, () -> new Client(id));
    }
    public void onReturnValueMsg(Ring.ReturnValueMsg msg) {
        System.out.println("Client: " + this.id + ", id request: " + msg.requestID + ", request type " + msg.requestType + ", value: " + msg.item.getValue() + "; version: " + msg.item.getVersion());
    }
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
