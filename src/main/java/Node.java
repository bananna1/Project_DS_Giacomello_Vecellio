import akka.actor.*;
import scala.concurrent.duration.Duration;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;

import java.util.Collections;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;


public abstract class Node extends AbstractActor{

    private int id;                                                         // Node ID
    private ActorRef actor;
    private Hashtable<Integer, Item> values = new Hashtable<>();            // list of keys and values
    private List<Node> peers = new ArrayList<>();                           // list of peer banks

    private boolean isCoordinator = false;                                  // the node is the coordinator

    private int nResponses = 0;

    private Request currRequest;

    private ArrayList<Request> activeRequests = new ArrayList<>();

    private Queue<Request> requestQueue = new LinkedList<>();

    public final int N = 4;

    public final int read_quorum = N / 2 + 1;
    public final int write_quorum = N / 2 + 1;

    // Start message that sends the list of participants to everyone
    public static class StartMessage implements Serializable {
        public final List<Node> group;
        public StartMessage(List<Node> group) {
              this.group = Collections.unmodifiableList(new ArrayList<>(group));
        }
    }

    public enum RequestType {
        Read,
        Update
    }

    public static class Timeout implements Serializable {
        Request request;
        public Timeout(Request request) {
            this.request = request;
        }
    }

    public static class Timeout implements Serializable {
        Request request;

        public Timeout(Request request) {
            this.request = request;
        }
    }

    public static class UpdateValueMsg implements Serializable {
        public final int key;
        public final String value;
        public UpdateValueMsg(int key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    public static class GetValueMsg implements Serializable {
        public final int key;
        public GetValueMsg(int key) {
            this.key = key;
        }
    }

    public static class RequestAccessMsg implements Serializable {
        public final Request request;
        public RequestAccessMsg(Request request) {
            this.request = request;
        }
    }

    public static class RequestValueMsg implements Serializable {
        public final Request request;
        public RequestValueMsg(Request request) {
            this.request = request;
        }
    }

    public static class AccessResponseMsg implements Serializable {
        public final boolean accessGranted;
        public final Request request;
        public AccessResponseMsg(boolean accessGranted, Request request) {
            this.accessGranted = accessGranted;
            this.request = request;
        }
    }

    public static class ValueResponseMsg implements Serializable {
        public final Item item;
        public final Request request;
        public ValueResponseMsg(Item item, Request request) {
            this.item = item;
            this.request = request;
        }
    }

    public static class ReturnValueMsg implements Serializable {
        public final Item item;
        public ReturnValueMsg(Item item) {
            this.item = item;
        }
    }

    public static class ChangeValueMsg implements Serializable {
        public final Request request;
        public final int newVersion;
        public ChangeValueMsg(Request request, int newVersion) {
            this.request = request;
            this.newVersion = newVersion;
        }
    }

    public static class ErrorMsg implements Serializable {
        public final String error;
        public ErrorMsg(String error) {
            this.error = error;
        }
    }

    /*-- Actor constructors --------------------------------------------------- */
    public Node(int id /*boolean isCoordinator, Node next, Node previous*/){
        super();
        this.id = id;

        /*
        this.next = next;
        this.previous = previous;
         */
    }
    public int getID() {
        return this.id;
    }
    public ActorRef getActor() {
        return this.actor;
    }
    public void removeValue (int key) {
        values.remove(key);
    }

    public void addValue (int key, String value, int version) {
        values.put(key, new Item(value, version));
    }

    void setGroup(StartMessage sm) {
        peers = new ArrayList<>();
        for (Node b: sm.group) {
            this.peers.add(b);
        }
        //print("starting with " + sm.group.size() + " peer(s)");
    }
    /*
    public void updatePrevious(Node newPrev) {
        this.previous = newPrev;
    }

    public void updateNext(Node newNext) {
        this.previous = newNext;
    }
     */

    private int getIndexOfFirstNode (int key) {
        int index = 0;
        for (int i = 0; i < peers.size(); i++) {
            if (peers.get(i).getID() > key) {
                index = i;
                break;
                // If we're not able to find a node whose ID is greater than the key,
                // then the first node to store the value is necessarily the node with the lowest ID (aka index = 0)
            }
        }
        return index;
    }

    public void onStartMessage(StartMessage msg) {
        setGroup(msg);
    }

    private void startRequest(Request request){
        int index = getIndexOfFirstNode(request.getKey());

        ActorRef owner = peers.get(index).getActor();
        owner.tell(new RequestAccessMsg(request), getSelf());
    }

    private void onGetValueMsg(GetValueMsg msg) {
        
        int key = msg.key;
        Request newRequest = new Request(key, RequestType.Read, getSender(), getSelf(), null);

        activeRequests.add(newRequest);
        startRequest(newRequest);
    }

    private void onUpdateValueMsg(UpdateValueMsg msg){
        
        int key = msg.key;
        String value = msg.value;
        Request newRequest = new Request(key, RequestType.Update, getSender(), getSelf(), value);

        activeRequests.add(newRequest);
        startRequest(newRequest);

    }

    private void onRequestAccessMsg(RequestAccessMsg msg) {
        Item i = values.get(msg.request.getKey());
        ActorRef coordinator = getSender();
        boolean accessGranted;
        if (msg.request.getType() == RequestType.Read) {
            accessGranted = i.lockRead();
        }
        else {
            accessGranted = i.lockUpdate();
        }

        if (accessGranted) {
            coordinator.tell(new AccessResponseMsg(true, msg.request), getSelf());
        }
        else {
            coordinator.tell(new AccessResponseMsg(false, msg.request), getSelf());
        }
    }



    private void onAccessResponseMsg(AccessResponseMsg msg) {
        // RICHIESTA SODDISFATTA
        if (msg.accessGranted) {
            int key = msg.request.getKey();
            int index = getIndexOfFirstNode(key);
            for (int i = index; i < N + index; i++) {
                int length = peers.size();
                ActorRef actor = peers.get(i % length).getActor();
                actor.tell(new RequestValueMsg(msg.request), getSelf());
            }
        }

        // RICHIESTA MESSA IN CODA
        else {
            activeRequests.remove(msg.request);
            requestQueue.add(msg.request);
        }
    }

    private void onRequestValueMsg(RequestValueMsg msg) {
        Item i = values.get(msg.request.getType());
        ActorRef sender = getSender();
        RequestType requestType = msg.request.getType();
        sender.tell(new ValueResponseMsg(i, msg.request), getSelf());
    }

    private void onValueResponseMsg(ValueResponseMsg msg) {
        if (activeRequests.contains(msg.request)) {
            msg.request.incrementnResponses();
            int nResponses = msg.request.getnResponses();
            Item currBest = msg.request.getCurrBest();
            if (currBest == null) {
                currRequest.setCurrBest(msg.item);
            }
            else {
                if (msg.item.getVersion() > currBest.getVersion()) {
                    currBest = msg.item;
                }
            }

            if(msg.request.getType() == RequestType.Read){    //READ
                if (nResponses >= read_quorum) {
                    msg.request.getClient().tell(new ReturnValueMsg(currBest), getSelf());

                    activeRequests.remove(msg.request);

                    int length = requestQueue.size();
                    for (int i = 0; i < length; i++) {
                        Request r = requestQueue.remove();
                        activeRequests.add(r);
                        startRequest(r);
                    }

                }
            }
            else {                  //WRITE
                if (nResponses >= write_quorum) {

                    msg.request.getClient().tell(new ReturnValueMsg(currBest), getSelf());
                    int index = getIndexOfFirstNode(msg.request.getKey());
                    int newVersion;
                    if (currBest == null) {
                        newVersion = 1;
                    }
                    else {
                        newVersion = currBest.getVersion() + 1;
                    }
                    for (int i = index; i < N + index; i++) {
                        int length = peers.size();
                        ActorRef actor = peers.get(i % length).getActor();
                        actor.tell(new ChangeValueMsg(msg.request, newVersion), getSelf());
                    }

                    activeRequests.remove(msg.request);

                    // TODO controllare che il while non sia un problema e in caso rimettere il for
                    while(!requestQueue.isEmpty()) {
                        Request r = requestQueue.remove();
                        activeRequests.add(r);
                        startRequest(r);
                    }
                } 
            }

        }
    }

    public void onChangeValueMsg(ChangeValueMsg msg) {
        Item newItem = new Item(msg.request.getNewValue(), msg.newVersion);
        this.values.put(msg.request.getKey(), newItem);
    }

    void setTimeout(int time, Request request) {
        getContext().system().scheduler().scheduleOnce(
                Duration.create(time, TimeUnit.MILLISECONDS),
                getSelf(),
                new Timeout(request), // the message to send
                getContext().system().dispatcher(), getSelf()
        );
    }

    public void onTimeout(Timeout msg) {
        /*
        RIMUOVERE RICHIESTA DALLE ACTIVE REQUESTS
        MANDARE MESSAGGIO ERRORE AL CLIENT
         */
        activeRequests.remove(msg.request);
        msg.request.getClient().tell(new ErrorMsg("Your request took too much to be satisfied"), getSelf());

    }


    @SuppressWarnings("unchecked")
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartMessage.class, this::onStartMessage)
                .match(GetValueMsg.class, this::onGetValueMsg)
                //.match(UpdateValueMsg.class, this::onUpdateValueMsg)
                .match(UpdateValueMsg.class, this::onUpdateValueMsg)
                .match(RequestAccessMsg.class, this::onRequestAccessMsg)
                .match(AccessResponseMsg.class, this::onAccessResponseMsg)
                .match(RequestValueMsg.class, this::onRequestValueMsg)
                .match(ValueResponseMsg.class, this::onValueResponseMsg)
                .match(ChangeValueMsg.class, this::onChangeValueMsg)
                .match(Timeout.class, this::onTimeout)
                //.match(ReturnValueMsg.class, this::onReturnValueMsg)  NON CREDO SERVA PERCHE' L'HANDLER DEVE AVERLO IL CLIENT
                .build();
    }
}

