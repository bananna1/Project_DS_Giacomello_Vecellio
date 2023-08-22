import akka.actor.*;
import akka.actor.AbstractActor;
import scala.concurrent.duration.Duration;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.lang.Thread;
import java.lang.InterruptedException;

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

        public Item (String value, int version) {
            this.value = value;
            this.version = version;
        }
        public void updateItem (String newValue) {
            this.value = newValue;
            this.version ++;
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
        Write,
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
    }

    public static class GetValueMsg implements Serializable {
        public final int key;
        public GetValueMsg(int key) {
            this.key = key;
        }
    }

    public static class RequestValueMsg implements Serializable {
        public final int key;
        public RequestValueMsg(int key) {
            this.key = key;
        }
    }

    public static class ValueResponseMsg implements Serializable {
        public final Item item;
        public ValueResponseMsg(Item item) {
            this.item = item;
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
    private List<Peer> peers = new ArrayList<>();                       // list of peer banks
    private Node next;
    private Node previous;
    private int nResponses = 0;
    private Item currBest = null;
    private ActorRef currClient = null;
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
          if (!b.equals(getSelf())) {
  
            // copying all participant refs except for self
            this.peers.add(b);
          }
        }
        //print("starting with " + sm.group.size() + " peer(s)");
    }
    public void updatePrevious(Node newPrev) {
        this.previous = newPrev;
    }

    public void updateNext(Node newNext) {
        this.previous = newNext;
    }

    private void onGetValueMsg(GetValueMsg msg) {
        System.out.println(msg.key);
        currClient = getSender();
        int key = msg.key;
        int index = 0;
        for (int i = 0; i < peers.size(); i++) {
            if (peers.get(i).getID() > key) {
                index = i;
                break;
                // If we're not able to find a node whose ID is greater than the key,
                // then the first node to store the value is necessarily the node with the lowest ID (aka index = 0)
            }
        }
        for (int i = index; i < N + index; i++) {
            int length = peers.size();
            ActorRef actor = peers.get(i % length).getActor();
            actor.tell(new RequestValueMsg(key), getSelf());
        }
    }

    private void onRequestValueMsg(RequestValueMsg msg) {
        Item i = values.get(msg.key);
        ActorRef sender = getSender();
        sender.tell(new ValueResponseMsg(i), getSelf());

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
        if (nResponses >= read_quorum) {
            currClient.tell(new ReturnValueMsg(currBest), getSelf());
            // TODO bloccare successive risposte per questa richiesta
        }
    }




    @SuppressWarnings("unchecked")
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GetValueMsg.class, this::onGetValueMsg)
                //.match(UpdateValueMsg.class, this::onUpdateValueMsg)
                .match(RequestValueMsg.class, this::onRequestValueMsg)
                .match(ValueResponseMsg.class, this::onValueResponseMsg)
                //.match(ReturnValueMsg.class, this::onReturnValueMsg)  NON CREDO SERVA PERCHE' L'HANDLER DEVE AVERLO IL CLIENT
                .build();
    }
}

