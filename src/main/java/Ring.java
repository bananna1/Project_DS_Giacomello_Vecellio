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

import java.util.*;
import java.util.Enumeration;

public class Ring {
    // Start message that sends the list of participants to everyone
    public static class StartMessage implements Serializable {
        public final List<Peer> group;
        public final List<Integer> keys;
        public final List<String> values;

        public StartMessage(List<Peer> group, List<Integer> keys, List<String> values) {
              this.group = Collections.unmodifiableList(new ArrayList<>(group));
              this.keys = Collections.unmodifiableList(new ArrayList<>(keys));
              this.values = Collections.unmodifiableList(new ArrayList<>(values));
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
        public final int requestID;
        public final Ring.Node.RequestType requestType;
        public ReturnValueMsg(Item item, int requestID, Ring.Node.RequestType requestType) {
            this.item = item;
            this.requestID = requestID;
            this.requestType = requestType;
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

    public static class Timeout implements Serializable {
        public final int id_request;
        public Timeout(int id_request) {
            this.id_request = id_request;
        }
    }


    public static class Node extends AbstractActor{

        private int id;                                                         // Node ID
        private ActorRef actor;
        private Hashtable<Integer, Item> storage = new Hashtable<>();            // list of keys and values
        private List<Peer> peers = new ArrayList<>();                       // list of peer banks

        //private Request currRequest;

        private ArrayList<Request> activeRequests = new ArrayList<>();

        private Queue<Request> requestQueue = new LinkedList<>();

        public final int N = 4;

        public final int TIMEOUT_REQUEST = 5000;
        public final int read_quorum = N / 2 + 1;
        public final int write_quorum = N / 2 + 1;

        public enum RequestType {
            Read,
            Update
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
            storage.remove(key);
        }

        public void addValue (int key, String value, int version) {
            storage.put(key, new Item(value, version));
        }

        void setGroup(StartMessage sm) {
            peers = new ArrayList<>();
            for (Peer b: sm.group) {
                this.peers.add(b);
            }
            //print("starting with " + sm.group.size() + " peer(s)");
        }

        private int getIndexOfFirstNode (int key) {
            int index = 0;

            for (int i = 0; i < peers.size(); i++) {
                if (peers.get(i).getID() >= key) {
                    index = i;
                    break;
                    // If we're not able to find a node whose ID is greater than the key,
                    // then the first node to store the value is necessarily the node with the lowest ID (aka index = 0)
                }
            }
            return index;
        }

        

        private void setInitialStorage(List<Integer> keys, List<String> values){
            int my_index = 0;

            for (int j = 0; j < peers.size(); j++) {
                if (this.id == peers.get(j).getID()) {
                    my_index = j;
                    break;
                }
            }
            for(int i = 0; i < keys.size(); i++) {
                int index = getIndexOfFirstNode(keys.get(i));
                //System.out.println(keys.get(i) + " " + index);

                if ((my_index >= index && my_index < index + N) || (my_index <= index && my_index < ((index + N) % peers.size()))) {
                    this.storage.put(keys.get(i), new Item(values.get(i), 1));
                }
            }
            printNode();
        }

        private void printNode(){
            //System.out.print(id + ": {");
            String printString = id + ": {";

            // Creating  Enumeration interface and get keys() from Hashtable
            Enumeration<Integer> e = storage.keys();

            // Checking for next element in Hashtable object with the help of hasMoreElements() method
            while (e.hasMoreElements()) {

                // Getting the key of a particular entry
                int key = e.nextElement();
    
                // Print and display the key and item
                Item i = storage.get(key);
                printString += "["+ key + ": " + i.getValue() + ", " + i.getVersion() + "]";
            }

            printString += "}";

            System.out.println(printString);
        }

        public void onStartMessage(StartMessage msg) {
            setGroup(msg);
            setInitialStorage(msg.keys, msg.values);

        }

        private void startRequest(Request request){
            int index = getIndexOfFirstNode(request.getKey());

            ActorRef owner = peers.get(index).getActor();
            request.setOwner(owner);
            owner.tell(new RequestAccessMsg(request), getSelf());
        }

        private void onGetValueMsg(GetValueMsg msg) {
            
            int key = msg.key;
            Request newRequest = new Request(key, RequestType.Read, getSender(), null);

            activeRequests.add(newRequest);
            setTimeout(TIMEOUT_REQUEST, newRequest.getID());
            startRequest(newRequest);
        }

        private void onUpdateValueMsg(UpdateValueMsg msg){
            
            int key = msg.key;
            String value = msg.value;
            Request newRequest = new Request(key, RequestType.Update, getSender(), value);

            activeRequests.add(newRequest);
            setTimeout(TIMEOUT_REQUEST, newRequest.getID());
            startRequest(newRequest);

        }

        private void onRequestAccessMsg(RequestAccessMsg msg) {
            Item i = storage.get(msg.request.getKey());
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
            Item i = storage.get(msg.request.getKey());

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
                    msg.request.setCurrBest(msg.item);
                    //System.out.println(msg.item.getValue() + " " + msg.item.getVersion());
                    //System.out.println("Ho impostato currBest a: " + msg.request.getCurrBest().getValue() + " " + msg.request.getCurrBest().getVersion());
                }
                else {
                    if (msg.item.getVersion() > currBest.getVersion()) {
                        msg.request.setCurrBest(msg.item);
                    }
                }

                if(msg.request.getType() == RequestType.Read){    //READ
                    if (nResponses >= read_quorum) {
                        if (msg.request.getCurrBest() == null) {
                            msg.request.getClient().tell(new ErrorMsg("Value does not exist in the ring"), getSelf());
                        }
                        else {
                            msg.request.getClient().tell(new ReturnValueMsg(currBest, msg.request.getID(), msg.request.getType()), getSelf());
                        }


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
                        // TODO ritornare solo messaggio di ok
                        msg.request.getClient().tell(new ReturnValueMsg(currBest, msg.request.getID(), msg.request.getType()), getSelf());
                        int index = getIndexOfFirstNode(msg.request.getKey());
                        int newVersion;
                        if (currBest == null) {
                            newVersion = 1;
                        }
                        else {
                            newVersion = currBest.getVersion() + 1;
                            System.out.println("STO AGGIORNANDO LA VERSIONE " + newVersion);
                        }
                        msg.request.resetnResponses();
                        for (int i = index; i < N + index; i++) {
                            int length = peers.size();
                            ActorRef actor = peers.get(i % length).getActor();
                            actor.tell(new ChangeValueMsg(msg.request, newVersion), getSelf());
                        }

                        //activeRequests.remove(msg.request);
                        // AGGIUNGO UN PASSAGGIO IN CUI

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
            this.storage.put(msg.request.getKey(), newItem);
        }

        void setTimeout(int time, int id_request) {
            getContext().system().scheduler().scheduleOnce(
                    Duration.create(time, TimeUnit.MILLISECONDS),
                    getSelf(),
                    new Timeout(id_request), // the message to send
                    getContext().system().dispatcher(), getSelf()
            );
        }

        public void onTimeout(Timeout msg) {
            Request request = null;

            // check if the request is in the active requests
            for (Request r : this.activeRequests) {
                int i = r.getID();
                if (i == msg.id_request) {
                    request = r;
                    break;
                }
            }
            if (request == null) {
                for (Request r : this.requestQueue) {
                    int i = r.getID();
                    if (i == msg.id_request) {
                        request = r;
                        break;
                    }
                }
            }
            else {
                if (request.getType() == RequestType.Read) {
                    if (request.getnResponses() < read_quorum) {
                        activeRequests.remove(request);
                        request.getClient().tell(new ErrorMsg("Your Read request " + msg.id_request + " took too much to be satisfied"), getSelf());
                    }
                }
                else {
                    if (request.getnResponses() < write_quorum) {
                        activeRequests.remove(request);
                        request.getClient().tell(new ErrorMsg("Your Update request " + msg.id_request +  " took too much to be satisfied"), getSelf());
                    }
                }
            }
        }

        static public Props props(int id) {
            return Props.create(Node.class, () -> new Node(id));
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


}
