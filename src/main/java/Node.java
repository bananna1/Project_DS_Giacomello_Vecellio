import akka.actor.*;
import akka.actor.AbstractActor;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;

import java.util.Collections;


public abstract class Node extends AbstractActor{

    // Start message that sends the list of participants to everyone
    public static class StartMessage implements Serializable {
        public final List<Peer> group;
        public StartMessage(List<Peer> group) {
              this.group = Collections.unmodifiableList(new ArrayList<>(group));
        }
    }

    private class Item {
        private String value;
        private int version;

        private boolean lockedUpdate = false;
        private int nLockedRead = 0;

        public Item (String value, int version) {
            this.value = value;
            this.version = version;
        }
        public void updateItem (String newValue) {
            this.value = newValue;
            this.version ++;
        }
        public boolean lockUpdate() {
            if (this.lockedUpdate || this.nLockedRead > 0) {
                return false;
            }
            this.lockedUpdate = true;
            return true;
        }
        public void unlockUpdate() {
            this.lockedUpdate = false;
        }
        public boolean isLockedUpdate() {
            return this.lockedUpdate;
        }
        public boolean lockRead() {
            if (this.lockedUpdate) {
                return false;
            }
            nLockedRead ++;
            return true;
        }
        public void unlockRead() {
            nLockedRead --;
            if (nLockedRead < 0) { // per sicurezza
                nLockedRead = 0;
            }
        }
    }

    private class Peer {
        private ActorRef actor;
        private int id;

        public Peer(ActorRef actor, int id) {
            this.actor = actor;
            this.id = id;
        }
        public ActorRef getActor() {
            return this.actor;
        }

        public int getID() {
            return this.id;
        }
    }

    public enum RequestType {
        Read,
        Update
    }

    private class Request {
        private int id;
        private int key;
        private RequestType type;
        private ActorRef client;

        public Request(int id, int key, RequestType type, ActorRef client) {
            this.id = id;
            this.key = key;
            this.type = type;
            this.client = client;
        }
        public void setClient(ActorRef client) { // FORSE NON SERVE
            this.client = client;
        }
        public ActorRef getClient() {
            return this.client;
        }
    }

    public static class GetValueMsg implements Serializable {
        public final int key;
        public GetValueMsg(int key) {
            this.key = key;
        }
    }

    /*public static class UpdateValueMsg implements Serializable {
        public final int key;
        public UpdateValueMsg(int key) {
            this.key = key;
        }
    }*/

    public static class CreateValueMsg implements Serializable {
        public final int key;
        public final String value;
        public CreateValueMsg(int key, String value) {
            this.key = key;
            this.value = value;
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

    private int id;                                                         // Node ID        
    private Hashtable<Integer, Item> values = new Hashtable<>();            // list of keys and values
    private List<Peer> peers = new ArrayList<>();                           // list of peer banks

    private boolean isCoordinator = false;                                  // the node is the coordinator

    private Node next;
    private Node previous;

    private int nResponses = 0;
    private Item currBest = null;

    private Request currRequest;

    private ArrayList<Request> requestQueue = new ArrayList<>();





    public final int N = 4;

    public final int read_quorum = N / 2 + 1;
    public final int write_quorum = N / 2 + 1;

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

    public void removeValue (int key) {
        values.remove(key);
    }

    public void addValue (int key, String value, int version) {
        values.put(key, new Item(value, version));
    }

    void setGroup(StartMessage sm) {
        peers = new ArrayList<>();
        for (Peer b: sm.group) {
            this.peers.add(b);
        }
        //print("starting with " + sm.group.size() + " peer(s)");
    }
    public void updatePrevious(Node newPrev) {
        this.previous = newPrev;
    }

    public void updateNext(Node newNext) {
        this.previous = newNext;
    }

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

    private void onGetValueMsg(GetValueMsg msg) {
        System.out.println(msg.key);
        int key = msg.key;
        Request newRequest = new Request(0, key, RequestType.Read, getSender()); // TODO scegliere come assegnare id richieste

        int index = getIndexOfFirstNode(key);

        ActorRef owner = peers.get(index).getActor();
        owner.tell(new RequestAccessMsg(newRequest), getSelf());
    }

    private void onCreateValueMsg(CreateValueMsg msg){
        System.out.println(msg.key + " : " + msg.value);
        int key = msg.key;
        Request newRequest = new Request(0, key, RequestType.Update, getSender());

        int index = getIndexOfFirstNode(key);


        for (int i = index; i < N + index; i++) {
            int length = peers.size();
            ActorRef actor = peers.get(i % length).getActor();
            actor.tell(new RequestValueMsg(newRequest), getSelf());
        }

    }

    private void onRequestAccessMsg(RequestAccessMsg msg) {
        Item i = values.get(msg.request.key);
        ActorRef coordinator = getSender();
        boolean accessGranted;
        if (msg.request.type == RequestType.Read) {
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
        if (msg.accessGranted) {
            int key = msg.request.key;
            int index = getIndexOfFirstNode(key);
            for (int i = index; i < N + index; i++) {
                int length = peers.size();
                ActorRef actor = peers.get(i % length).getActor();
                actor.tell(new RequestValueMsg(msg.request), getSelf());
            }
        }
        else {
            // TODO implementare meccanismo per coda
        }
    }

    private void onRequestValueMsg(RequestValueMsg msg) {
        Item i = values.get(msg.request.key);
        ActorRef sender = getSender();
        RequestType requestType = msg.request.type;
        sender.tell(new ValueResponseMsg(i, msg.request), getSelf());
    }

    private void onValueResponseMsg(ValueResponseMsg msg) {
        nResponses ++;
        if (currBest == null) {
            currBest = msg.item;
        }
        else {
            if (msg.item.version > currBest.version) {
                currBest = msg.item;
            }
        }

        if(msg.request.type == RequestType.Read){    //READ
            if (nResponses >= read_quorum) {
                msg.request.getClient().tell(new ReturnValueMsg(currBest), getSelf());
                // TODO bloccare successive risposte per questa richiesta
                // TODO mandare a tutti i nodi interessati l'ordine di s
            }
        }
        else {                  //WRITE
            if (nResponses >= write_quorum) {
                //FARE L'UPDATE DI TUTTO
                //this.values.updateItem(msg.item.value);
                msg.request.getClient().tell(new ReturnValueMsg(currBest), getSelf());
                // TODO bloccare successive risposte per questa richiesta
            }
        }
        
    }




    @SuppressWarnings("unchecked")
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartMessage.class, this::onStartMessage)
                .match(GetValueMsg.class, this::onGetValueMsg)
                //.match(UpdateValueMsg.class, this::onUpdateValueMsg)
                .match(CreateValueMsg.class, this::onCreateValueMsg)
                .match(RequestAccessMsg.class, this::onRequestAccessMsg)
                .match(AccessResponseMsg.class, this::onAccessResponseMsg)
                .match(RequestValueMsg.class, this::onRequestValueMsg)
                .match(ValueResponseMsg.class, this::onValueResponseMsg)
                //.match(ReturnValueMsg.class, this::onReturnValueMsg)  NON CREDO SERVA PERCHE' L'HANDLER DEVE AVERLO IL CLIENT
                .build();
    }
}

