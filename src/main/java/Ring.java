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
        public final boolean isJoin;
        public GetValueMsg(int key, boolean isJoin) {
            this.key = key;
            this.isJoin = isJoin;
        }
        public GetValueMsg(int key) {
            this.key = key;
            this.isJoin = false;
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
        public final boolean itemNotFound;
        public final Request request;
        public AccessResponseMsg(boolean accessGranted, boolean itemNotFound, Request request) {
            this.accessGranted = accessGranted;
            this.request = request;
            if (itemNotFound && request.getType() == Node.RequestType.Read && accessGranted == false) {
                this.itemNotFound = true;
            }
            else {
                this.itemNotFound = false;
            }
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

    public static class UnlockMsg implements Serializable {
        public final Request request;
        public UnlockMsg(Request request) {
            this.request = request;
        }
    }

    public static class OkMsg implements Serializable {
        public final Request request;
        public OkMsg(Request request) {
            this.request = request;
        }
    }

    public static class ErrorMsg implements Serializable {
        public final String error;
        public ErrorMsg(String error) {
            this.error = error;
        }
    }

    public static class TimeoutRequest implements Serializable {
        public final int id_request;
        public TimeoutRequest(int id_request) {
            this.id_request = id_request;
        }
    }

    public static class TimeoutDequeue implements Serializable {
        public TimeoutDequeue() {
        }
    }
    public static class TimeoutExternalRequest implements Serializable {
        public final int externalRequestId;
        public TimeoutExternalRequest(int externalRequestId) {
            this.externalRequestId = externalRequestId;
        }
    }

    public static class JoinRequestMsg implements Serializable {
        public final Peer joiningPeer;
        public final ActorRef bootStrappingPeer;
        public JoinRequestMsg(Peer joiningPeer, ActorRef bootStrappingPeer){
            this.joiningPeer = joiningPeer;
            this.bootStrappingPeer = bootStrappingPeer;
        }
    }

    public static class SendPeerListMsg implements Serializable {
        public final List<Peer> group;
        public final ExternalRequest request;
        public SendPeerListMsg(List<Peer> group, ExternalRequest request){
            this.group = Collections.unmodifiableList(new ArrayList<>(group));
            this.request = request;
        }
    }

    public static class GetNeighborItemsMsg implements Serializable {
        public GetNeighborItemsMsg() {

        }
    }

    public static class SendItemsListMsg implements Serializable {
        public final Hashtable<Integer, Item> items;
        public SendItemsListMsg(Hashtable<Integer, Item> items){
            this.items = items;
        }
    }

    public static class AnnounceJoiningNodeMsg implements Serializable {
        public final int joiningNodeKey;
        public final List<Item> items;
        public AnnounceJoiningNodeMsg(int joiningNodeKey, List<Item> items) {
            this.joiningNodeKey = joiningNodeKey;
            this.items = items;
        }
    }

    public static class LeaveRequestMsg implements Serializable {
        public LeaveRequestMsg() {
        }
    }
    public static class AddItemToStorageMsg implements Serializable {
        public final Item item;
        public AddItemToStorageMsg(Item item) {
            this.item = item;
        }
    }

    public static class AnnounceLeavingNodeMsg implements Serializable {
        public final int leavingNodeIndex;
        public AnnounceLeavingNodeMsg(int index) {
            leavingNodeIndex = index;
        }
    }

    public static class CrashRequestMsg implements Serializable {
        public CrashRequestMsg() {

        }
    }

    public static class RecoveryRequestMsg implements Serializable {
        public final ActorRef nodeToContact;
        public RecoveryRequestMsg(ActorRef nodeToContact) {
            this.nodeToContact = nodeToContact;
        }
    }

    public static class GetPeerListMsg implements Serializable {
        public GetPeerListMsg() {

        } 
    }

    public static class SendPeerListRecoveryMsg implements Serializable {
        public final List<Peer> group;
        public SendPeerListRecoveryMsg(List<Peer> group) {
            this.group = Collections.unmodifiableList(new ArrayList<>(group));
        }
    }

    public static class GetItemsListMsg implements Serializable {
        public final int recoveryNodeID;
        public GetItemsListMsg(int recoveryNodeID) {
            this.recoveryNodeID = recoveryNodeID;
        }
    }

    public static class SendItemsListRecoveryMsg implements Serializable {
        public final Hashtable<Integer, Item> items;
        public SendItemsListRecoveryMsg(Hashtable<Integer, Item> items){
            this.items = items;
        }
    }

    public static class Node extends AbstractActor{

        private int id;                                                         // Node ID
        private ActorRef actor;
        private Hashtable<Integer, Item> storage = new Hashtable<>();            // list of keys and values
        private List<Peer> peers = new ArrayList<>();                       // list of peer banks

        private ArrayList<Request> activeRequests = new ArrayList<>();

        private Queue<Request> requestQueue = new LinkedList<>();
        private ArrayList<Request> pendingRequests = new ArrayList<>();
        public final int N = 4;

        public final int TIMEOUT_REQUEST = 5000;
        public final int TIMEOUT_DEQUEUE = 2000;
        public final int TIMEOUT_JOIN = 5000;
        public final int TIMEOUT_RECOVERY = 5000;

        public final int read_quorum = N / 2 + 1;
        public final int write_quorum = N / 2 + 1;

        private boolean hasCrashed = false;
        private ExternalRequest currExternalRequest = null;

        public enum RequestType {
            Read,
            Update,
            ReadJoin
        }

        public enum ExternalRequestType {
            Join,
            Leave,
            Recovery
        }

        /*-- Node constructor --------------------------------------------------- */
        public Node(int id /*boolean isCoordinator, Node next, Node previous*/){
            super();
            this.id = id;
            setTimeoutDequeue(TIMEOUT_DEQUEUE);
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
            storage.put(key, new Item(key, value, version));
        }

        void setGroup(List<Peer> group) {
            peers = new ArrayList<>();
            for (Peer b: group) {
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

        private int getIndexOfLastNode(int key) {
            int indexOfFirstNode = getIndexOfFirstNode(key);
            int indexOfLastNode = (indexOfFirstNode + N - 1) % peers.size();
            return indexOfLastNode;
        }
        private void addPeer(Peer newPeer) {
            int newPeerPosition = 0;
            for (int i = 0; i < peers.size(); i++) {
                if (newPeer.getID() < peers.get(i).getID()) {
                    newPeerPosition = i;
                    break;
                }
            }
            peers.add(newPeerPosition, newPeer);
        }
        private List<Peer> addPeer(Peer newPeer, List<Peer> list) {
            int newPeerPosition = 0;
            List<Peer> newList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                if (newPeer.getID() < list.get(i).getID()) {
                    newPeerPosition = i;
                    break;
                }
            }
            int listIndex = 0;
            for (int j = 0; j < list.size() + 1; j++) {
                if (j == newPeerPosition) {
                    newList.add(newPeer);
                }
                else {
                    newList.add(list.get(listIndex));
                    listIndex++;
                }
            }
            return newList;
        }

        private boolean isNodeResponsibleForItem(int indexOfFirstNode) {
            int myIndex = getMyIndex();
            if ((myIndex >= indexOfFirstNode && myIndex < indexOfFirstNode + N) || (myIndex <= indexOfFirstNode && myIndex < ((indexOfFirstNode + N) % peers.size()) && ((indexOfFirstNode + N) % peers.size()) < indexOfFirstNode)) {
                return true;
            }
            return false;
        }

        private int getMyIndex() {
            int my_index = 0;

            for (int j = 0; j < peers.size(); j++) {
                if (this.id == peers.get(j).getID()) {
                    my_index = j;
                    break;
                }
            }
            return my_index;
        }

        static public Props props(int id) {
            return Props.create(Node.class, () -> new Node(id));
        }

        private void setInitialStorage(List<Integer> keys, List<String> values){
            for(int i = 0; i < keys.size(); i++) {
                int indexOfFirstNode = getIndexOfFirstNode(keys.get(i));
                if (isNodeResponsibleForItem(indexOfFirstNode)) {
                    this.storage.put(keys.get(i), new Item(keys.get(i), values.get(i), 1));
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

        public String printPeers() {
            String s = "[";
            for (Peer p : peers) {
                s += p.getID() + " ";
            }
            s += "]";
            return s;
        }

        public void onStartMessage(StartMessage msg) {
            setGroup(msg.group);
            setInitialStorage(msg.keys, msg.values);

        }

        private void startRequest(Request request){
            int index = getIndexOfFirstNode(request.getKey());

            ActorRef owner = peers.get(index).getActor();
            request.setOwner(owner);
            System.out.println("Request started - id request: " + request.getID() + ", type: " + request.getType() + ", Key: " + request.getKey() + ", client: " + request.getClient());
            owner.tell(new RequestAccessMsg(request), getSelf());
        }

        private void onGetValueMsg(GetValueMsg msg) {

            if(this.hasCrashed){ return; }
                
            int key = msg.key;
            Request newRequest;

            if(msg.isJoin == true)
                newRequest = new Request(key, RequestType.ReadJoin, getSender(), null);
            else
                newRequest = new Request(key, RequestType.Read, getSender(), null);

            activeRequests.add(newRequest);
            setTimeout(TIMEOUT_REQUEST, newRequest.getID());
            startRequest(newRequest);
        }

        private void onUpdateValueMsg(UpdateValueMsg msg){
            
            if(this.hasCrashed){ return; }

            int key = msg.key;
            String value = msg.value;
            Request newRequest = new Request(key, RequestType.Update, getSender(), value);

            activeRequests.add(newRequest);
            setTimeout(TIMEOUT_REQUEST, newRequest.getID());
            startRequest(newRequest);

        }

        private void onRequestAccessMsg(RequestAccessMsg msg) {

            if(this.hasCrashed){ return; }

            //System.out.println("Access requested");
            Item i = storage.get(msg.request.getKey());
            ActorRef coordinator = getSender();
            boolean accessGranted;
            if (msg.request.getType() == RequestType.Read) { //READ
                //SE NON CI SONO UPDATE, ACCCESS GRANTED
                if (i == null) {
                    accessGranted = false;
                    coordinator.tell(new AccessResponseMsg(accessGranted, true,  msg.request), getSelf());
                    return;
                }
                else {
                    accessGranted = i.lockRead();
                }

            }
            else { //UPDATE
                // SE NON CI SONO READ DELL'ITEM O UPDATE DELL'ITEM, ACCESS GRANTED
                if (i == null) {
                    i = new Item(msg.request.getKey(), "", 0); // version 0 is useful to identify that the item is new
                }
                accessGranted = i.lockUpdate();
                System.out.println("Access granted for update operation - id request: " + msg.request.getID() + ", type: " + msg.request.getType() + ", Key: " + msg.request.getKey() + ", client: " + msg.request.getClient());

            }

            if (accessGranted) {
                coordinator.tell(new AccessResponseMsg(true, false, msg.request), getSelf());
            }
            else {
                System.out.println("Access denied - id request: " + msg.request.getID() + ", type: " + msg.request.getType() + ", Key: " + msg.request.getKey() + ", client: " + msg.request.getClient());
                coordinator.tell(new AccessResponseMsg(false, false,  msg.request), getSelf());
            }
        }

        private void onAccessResponseMsg(AccessResponseMsg msg) {

            if(this.hasCrashed){ return; }               

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
                if (msg.itemNotFound && msg.request.getType() == RequestType.Read) { // il controllo sul tipo della richiesta è ridondante
                    activeRequests.remove(msg.request);
                    msg.request.getClient().tell(new ErrorMsg("Error message for Read Request - request id: " + msg.request.getID() + ", key: " + msg.request.getKey() + ". Item not found"), getSelf());
                    return;
                }
                activeRequests.remove(msg.request);
                requestQueue.add(msg.request);
                System.out.println("Request added to the queue - id request: " + msg.request.getID() + ", type: " + msg.request.getType() + ", Key: " + msg.request.getKey() + ", client: " + msg.request.getClient());
            }
        }

        private void onRequestValueMsg(RequestValueMsg msg) {

            if(this.hasCrashed){ return; }

            Item i = storage.get(msg.request.getKey());
            if (i == null) {
                i = new Item(msg.request.getKey(), "", 0);
            }
            ActorRef sender = getSender();
            RequestType requestType = msg.request.getType();
            //System.out.println("value requested - id request: " + msg.request.getID() + ", type: " + msg.request.getType() + ", Key: " + msg.request.getKey() + ", client: " + msg.request.getClient());
            sender.tell(new ValueResponseMsg(i, msg.request), getSelf());
        }

        private void onValueResponseMsg(ValueResponseMsg msg) {

            if(this.hasCrashed){ return; }

            if (activeRequests.contains(msg.request)) {
                msg.request.incrementnResponses();
                //System.out.println("Response message n. " + msg.request.getnResponses() + " - id request: " + msg.request.getID() + ", type: " + msg.request.getType() + ", Key: " + msg.request.getKey() + ", client: " + msg.request.getClient());
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
                            msg.request.getClient().tell(new ErrorMsg("Error message for Read Request - request id: " + msg.request.getID() + ", key: " + msg.request.getKey() + ". Value does not exist in the ring"), getSelf());
                        }
                        else {
                            msg.request.getClient().tell(new ReturnValueMsg(currBest, msg.request.getID(), msg.request.getType()), getSelf());
                        }

                        activeRequests.remove(msg.request);
                        msg.request.getOwner().tell(new UnlockMsg(msg.request), getSelf());

                    }
                }
                else {                  //WRITE
                    if (nResponses >= write_quorum) {
                        // TODO ritornare solo messaggio di ok
                        //System.out.println("HO RAGGIUNTO IL WRITE QUORUM");
                        msg.request.getClient().tell(new ReturnValueMsg(currBest, msg.request.getID(), msg.request.getType()), getSelf());
                        int index = getIndexOfFirstNode(msg.request.getKey());
                        int newVersion = currBest.getVersion();
                        /*
                        if (currBest.getVersion() == 0) {
                            newVersion = 1;
                        }
                        else {
                            newVersion = currBest.getVersion() + 1;
                            //System.out.println("STO AGGIORNANDO LA VERSIONE " + newVersion);
                        }
                         */

                        for (int i = index; i < N + index; i++) {
                            int length = peers.size();
                            ActorRef actor = peers.get(i % length).getActor();
                            actor.tell(new ChangeValueMsg(msg.request, newVersion), getSelf());
                        }

                        activeRequests.remove(msg.request);
                        pendingRequests.add(msg.request);
                        //mettiamo la richiesta in un altro array in attesa dei messaggi di ok
                        // AGGIUNGO UN PASSAGGIO IN CUI ATTENDO CHE I NODI MI RISPONDANO OK PER L'UPDATE IN MODO DA POTER FARE L'UNLOCK DELL'ITEM NELL'OWNER

                    } 
                }

            }
        }

        public void onChangeValueMsg(ChangeValueMsg msg) {

            if(this.hasCrashed){ return; }

            Item newItem = new Item(msg.request.getKey(), msg.request.getNewValue(), msg.newVersion);
            this.storage.put(msg.request.getKey(), newItem);
            //System.out.println("New item - key: " + msg.request.getKey() + ", new value: " + storage.get(msg.request.getKey()).getValue() + ", current version: " + storage.get(msg.request.getKey()).getVersion());
            printNode();
            getSender().tell(new OkMsg(msg.request), getSelf());
        }

        public void onUnlockMsg(UnlockMsg msg) {

            if(this.hasCrashed){ return; }

            int key = msg.request.getKey();
            if (msg.request.getType() == RequestType.Read) {
                this.storage.get(key).unlockRead();
            }
            else {
                this.storage.get(key).unlockUpdate();
                for(Peer p: peers){
                    //printNode();
                }
                
            }
        }

        public void onOkMsg(OkMsg msg) {

            if(this.hasCrashed){ return; }

            // Nota: OkMsg riguarda solo le update requests, quindi non serve differenziare i casi in base al tipo di richiesta
            if (pendingRequests.contains(msg.request)) {
                msg.request.incrementOkResponses();
                if (msg.request.getOkResponses() >= write_quorum) {
                    for (Peer p : peers) {
                    }
                    msg.request.getOwner().tell(new UnlockMsg(msg.request), getSelf());
                    this.pendingRequests.remove(msg.request);
                }
            }
        }

        private void setTimeout(int time, int id_request) {
            getContext().system().scheduler().scheduleOnce(
                    Duration.create(time, TimeUnit.MILLISECONDS),
                    getSelf(),
                    new TimeoutRequest(id_request), // the message to send
                    getContext().system().dispatcher(), getSelf()
            );
        }
        private void setTimeoutExternalRequest(int time, int externalRequestId) {
            getContext().system().scheduler().scheduleOnce(
                    Duration.create(time, TimeUnit.MILLISECONDS),
                    getSelf(),
                    new TimeoutExternalRequest(externalRequestId), // the message to send
                    getContext().system().dispatcher(), getSelf()
            );
        }

        private void setTimeoutDequeue(int time) {
            getContext().system().scheduler().scheduleOnce(
                    Duration.create(time, TimeUnit.MILLISECONDS),
                    getSelf(),
                    new TimeoutDequeue(), // the message to send
                    getContext().system().dispatcher(), getSelf()
            );
        }

        public void onTimeoutDequeue(TimeoutDequeue msg) {
            //System.out.println("SONO NEL TIMEOUT DEQUEUEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
            while (!this.requestQueue.isEmpty()) {
                System.out.println("Queue size: " + requestQueue.size()); // Debugging output
                System.out.println("STO FACENDO IL DEQUEUE");
                Request r = requestQueue.remove();
                System.out.println("Removed request: " + r); // Debugging output
                activeRequests.add(r);
                startRequest(r);
                System.out.println("Request processed: " + r); // Debugging output
            }
            setTimeoutDequeue(TIMEOUT_DEQUEUE);
        }

        public void onTimeout(TimeoutRequest msg) {
            //System.out.println("TIMEOUT SCATTATO!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            Request request = null;

            // check if the request is in the active requests
            for (Request r : this.activeRequests) {
                int i = r.getID();
                if (i == msg.id_request) {
                    request = r;
                    System.out.println("Ho trovato la richiesta per la chiave " + request.getKey());
                    break;
                }
            }
            if (request == null) {
                // check if the request is in the request queue
                for (Request r : this.requestQueue) {
                    int i = r.getID();
                    if (i == msg.id_request) {
                        request = r;
                        break;
                    }
                }
            }
            if (request == null) {
                // check if the request is in the request queue
                for (Request r : this.pendingRequests) {
                    int i = r.getID();
                    if (i == msg.id_request) {
                        request = r;
                        break;
                    }
                }
            }
            if (request != null) {
                System.out.println("Ho trovato la richiesta che ha fatto scattare il timeout");
                if (request.getType() == RequestType.Read) {
                    if (request.getnResponses() < read_quorum) {
                        activeRequests.remove(request);
                        System.out.println("Sono nella funzione di timeout, sto togliendo l'elemento, queue size: " + requestQueue.size());
                        requestQueue.remove(request);
                        pendingRequests.remove(request);
                        System.out.println("Sono nella funzione di timeout, ho tolto l'elemento, queue size: " + requestQueue.size());
                        request.getClient().tell(new ErrorMsg("Your Read request " + msg.id_request + " took too much to be satisfied"), getSelf());
                    }
                }
                else {
                    if (request.getnResponses() < write_quorum || request.getOkResponses() < write_quorum) {
                        activeRequests.remove(request);
                        System.out.println("Sono nella funzione di timeout, sto togliendo l'elemento, queue size: " + requestQueue.size());
                        requestQueue.remove(request);
                        pendingRequests.remove(request);
                        System.out.println("Sono nella funzione di timeout, ho tolto l'elemento, queue size: " + requestQueue.size());
                        request.getClient().tell(new ErrorMsg("Your Update request " + msg.id_request +  " took too much to be satisfied"), getSelf());
                    }
                }
            }
        }
        public void onTimeout(TimeoutExternalRequest msg) {
            /*
            if (currJoiningNodeRequest != null && currJoiningNodeRequest.getJoiningPeer() == msg.request.getJoiningPeer() && currJoiningNodeRequest.getID() == msg.request.getID()) { // TODO VEDI TUTTI I CASI
                System.out.println("");
                currJoiningNodeRequest = null;
                msg.request.getClient().tell(new ErrorMsg("Your Join request " + msg.request.getID() +  " took too much to be satisfied"), getSelf());
            }
             */
            if (currExternalRequest != null && currExternalRequest.getType() == ExternalRequestType.Join && currExternalRequest.getID() == msg.externalRequestId) {
                ActorRef client = currExternalRequest.getClient();
                currExternalRequest = null;
                client.tell(new ErrorMsg("Error message for your Join Request - Joining node ID" + this.getID() + "Your join request took too much to be satisfied"), getSelf());
            }
            else if (currExternalRequest != null && currExternalRequest.getType() == ExternalRequestType.Recovery && currExternalRequest.getID() == msg.externalRequestId) {
                ActorRef client = currExternalRequest.getClient();
                currExternalRequest = null;
                client.tell(new ErrorMsg("Error message for your Recovery Request - Joining node ID" + this.getID() + "Your join request took too much to be satisfied"), getSelf());
            }
        }

        public void onJoinRequestMsg(JoinRequestMsg msg) {

            if(this.hasCrashed){ return; }

            // Error message if the key already exists
            boolean alreadyTaken = false;
            for(Peer peer : peers){
                //System.out.println("Id peer: " + peer.getID());
                if(peer.getID() == msg.joiningPeer.getID()) {
                    getSender().tell(new ErrorMsg("Error message for your Join Request - Joining node ID" + msg.joiningPeer.getID() + ". This ID is already taken"), getSelf());
                    alreadyTaken = true;
                    break;
                }
            }
            System.out.println("Valore di alreadyTaken: " + alreadyTaken);
            if(!alreadyTaken){
                //System.out.println("SONO QUA X2");
                // Create an external request with type::Join
                ExternalRequest request = new JoinRequest(getSender());
                System.out.println("BN; Join requested to node " + this.id);

                // Send peer list to the joining node
                msg.joiningPeer.getActor().tell(new SendPeerListMsg(Collections.unmodifiableList(new ArrayList<>(this.peers)), request), getSelf());
            }
            

        }

        public void onSendPeerListMsg(SendPeerListMsg msg) {

            if(this.hasCrashed){ return; } //TODO VEDERE SE TOGLIERE CONTROLLO VISTO CHE IL NODO NON PUO' ESSERE CRASHATO
            currExternalRequest = msg.request;
            // Add myself to the list of peer
            List<Peer> updatedGroup = addPeer(new Peer(id, getSelf()), msg.group);

            setGroup(updatedGroup);

            // Find the clockwise neighbor node
            int indexClockwiseNode = getIndexOfFirstNode(this.id) + 1; // ALTRIMENTI TROVA SE STESSO; IN QUESTO MODO TROVA IL NODO IMMEDIATAMENTE SUCCESSIVO A SE STESSO
            if (indexClockwiseNode == peers.size()) {
                indexClockwiseNode = 0; // se il nuovo nodo è l'ultimo allora il clockwise neighbor è il primo della lista
            }
            // Send message to the clockwise node in order to retrieve the list of items
            Peer neighbor = peers.get(indexClockwiseNode);
            System.out.println("JN; Got SendPeerList message. Found clockwise neighbor: " + neighbor.getID());
            setTimeoutExternalRequest(TIMEOUT_JOIN, currExternalRequest.getID());
            neighbor.getActor().tell(new GetNeighborItemsMsg(), getSelf());

        }

        public void onGetNeighborItemsMsg(GetNeighborItemsMsg msg){

            if(this.hasCrashed){ return; }

            // Send message to the joining node with the items list
            System.out.println("NN; Received request to retrieve items");
            getSender().tell(new SendItemsListMsg(storage), getSelf());
        }
        
        public void onSendItemsListMsg(SendItemsListMsg msg) {

            if(this.hasCrashed){ return; } // TODO VEDERE SE TOGLIERLO PERCHE' QUESTO METODO VIENE ESEGUITO SOLO DA UN JOINING NODE E QUINDI QUA IL NODO NON PUO' ESSERE CRASHATO
            // Set the storage of the joining node
            this.storage = msg.items;
            System.out.println("JN; Inserted items in storage, requesting read on each item");
            System.out.println("STAMPO LO STORAGE DEL NUOVO NODO");

            if (storage.size() == 0) {
                currExternalRequest = null;
                for (Peer p : peers) {
                    p.getActor().tell(new AnnounceJoiningNodeMsg(this.id, new ArrayList<>()), getSelf());
                }
            }
            // Creating  Enumeration interface and get keys() from Hashtable
            Enumeration<Integer> e = storage.keys();

            // Checking for next element in Hashtable object with the help of hasMoreElements() method
            while (e.hasMoreElements()) {

                // Getting the key of a particular entry
                int key = e.nextElement();
                System.out.println(key);

                // Send a read request for every items
                getSender().tell(new GetValueMsg(key, true), getSelf());
            }



        }
        public void onReturnValueMsg(ReturnValueMsg msg) {

            if(this.hasCrashed){ return; }

            currExternalRequest.incrementnResponses();
            System.out.println("Received return message for key " + msg.item.getKey() + ", value: " + msg.item.getValue() + ", version: " + msg.item.getVersion());
            // Change version if it is not updated
            if(storage.containsKey(msg.item.getKey())){
                if(msg.item.getVersion() > storage.get(msg.item.getKey()).getVersion()) {
                    storage.put(msg.item.getKey(), msg.item);
                    System.out.println("Changed item version");
                }
            } else {
                storage.put(msg.item.getKey(), msg.item);
            }

            if(currExternalRequest.getnResponses() == storage.size()) {
                if(msg.requestType == RequestType.ReadJoin){
                    List<Item> items = new ArrayList<>();

                    // Creating  Enumeration interface and get keys() from Hashtable
                    Enumeration<Integer> e = storage.keys();

                    // Checking for next element in Hashtable object with the help of hasMoreElements() method
                    while (e.hasMoreElements()) {

                        // Getting the key of a particular entry
                        int key = e.nextElement();

                        Item i = storage.get(key);

                        items.add(i);

                    }

                    currExternalRequest = null;

                    // Send announceJoiningNodeMsg
                    for (Peer peer : peers) {
                        peer.getActor().tell(new AnnounceJoiningNodeMsg(this.getID(), items), getSelf());
                    }
                }
            }

            printNode();

        }

        public void onAnnounceJoiningNodeMsg(AnnounceJoiningNodeMsg msg) {

            if(this.hasCrashed){ return; }

            //System.out.println("Node " + this.id + " received joining announcement");
            // Update the list of peers
            if (this.id != msg.joiningNodeKey) {
                addPeer(new Peer(msg.joiningNodeKey, getSender()));
            }
            // Find common items
            List<Item> commonItems = new ArrayList<>();

            for(Item i : msg.items) {
                if(this.storage.get(i.getKey()) != null) {
                    commonItems.add(i);
                }
            }

            // First index of first node for each common item

            for (Item i : commonItems) {
                int indexOfFirstNode = getIndexOfFirstNode(i.getKey());

                // Check if you are among the N nodes that can hold the item
                if (isNodeResponsibleForItem(indexOfFirstNode)) {
                    Hashtable<Integer, Item> newStorage = new Hashtable<>();
                    Enumeration<Integer> e = storage.keys();

                    while (e.hasMoreElements()) {
                        int key = e.nextElement();
                        if (i.getKey() != key) {
                            newStorage.put(key, storage.get(key));
                        }
                    }
                    this.storage = newStorage;
                    //Item removed_item = this.storage.remove(i.getKey());
                    //System.out.println("NODE: " + this.id + ", KEY REMOVED: " + removed_item.getKey() + ", VALUE REMOVED: " + removed_item.getValue());
                    //System.out.println("Node " + this.id + " removed item with key " + i.getKey());
                }
                else {
                    //System.out.println("Node " + this.id + " keeps item with key " + i.getKey());
                }
            }
            printNode();

        }

        public void onLeaveRequestMsg(LeaveRequestMsg msg) {

            if(this.hasCrashed){ return; }

            Enumeration<Integer> e = storage.keys();

            // take each item in storage and tell the new node responsible for them to insert it in its storage
            while (e.hasMoreElements()) {
                int key = e.nextElement();
                Item i = storage.get(key);
                int indexOfLastNode = getIndexOfLastNode(key);
                int newIndexOfLastnode = (indexOfLastNode + 1) % peers.size();
                Peer newLastPeer = peers.get(newIndexOfLastnode);
                System.out.println("Node " + peers.get(newIndexOfLastnode).getID() + " is now responsible for key " + i.getKey());
                newLastPeer.getActor().tell(new AddItemToStorageMsg(i), getSelf());
            }

            // find index of leaving node
            int leavingNodeIndex = 0;
            for(int i = 0; i < peers.size(); i++) {
                Peer p = peers.get(i);
                if (p.getID() == this.id) {
                    leavingNodeIndex = i;
                    break;
                }
            }
            currExternalRequest = null; // serve nel caso lo stesso nodo venga riutilizzato per un successivo join (non so se si possa fare ma in caso siamo coperti)
            for (Peer p : peers) {
                p.getActor().tell(new AnnounceLeavingNodeMsg(leavingNodeIndex), getSelf());
            }
        }

        public void onAddItemToStorageMsg(AddItemToStorageMsg msg) {

            if(this.hasCrashed){ return; }

            storage.put(msg.item.getKey(), msg.item);
        }

        public void onAnnounceLeavingNodeMsg(AnnounceLeavingNodeMsg msg) {

            if(this.hasCrashed){ return; }

            System.out.println("Node " + this.id + " has received announcement of node leaving");
            boolean isMe = (peers.get(msg.leavingNodeIndex).getID() == this.id);
            peers.remove(msg.leavingNodeIndex);
            System.out.println("Node " + this.id + "'s peers: " + this.printPeers());
            if(!isMe) {
                printNode();
            }
        }

        public void onCrashRequestMsg(CrashRequestMsg msg) {
            // TODO decidere se fare i controlli anche su leave e join
            this.hasCrashed = true;

            System.out.println("Node " + this.getID() + " has crashed");
        }

        public void onRecoveryRequestMsg(RecoveryRequestMsg msg) {

            // TODO decidere se controllare se vengono fatti read e update

            // Check if the node has really crashed
            if(!hasCrashed){ 
                //VariabileProva = 0;
                getSender().tell(new ErrorMsg("Error message for your Recovery Request for node "  + this.id + ". This node is not crashed"), getSelf());
                return;
            }
            hasCrashed = false;
            ExternalRequest request = new RecoveryRequest(getSender());
            this.currExternalRequest = request;
            setTimeoutExternalRequest(TIMEOUT_RECOVERY, request.getID());
            msg.nodeToContact.tell(new GetPeerListMsg(), getSelf());

        }

        public void onGetPeerListMsg(GetPeerListMsg msg) {
            getSender().tell(new SendPeerListRecoveryMsg(Collections.unmodifiableList(new ArrayList<>(this.peers))), getSelf());
        }

        public void onSendPeerListRecoveryMsg(SendPeerListRecoveryMsg msg) {
            this.peers = msg.group;

            // Forgets the items it is no longer responsible for
            // First index of first node for each item of the node

            Enumeration<Integer> en = storage.keys();
            while (en.hasMoreElements()) {
                int key = en.nextElement();
                
                int indexOfFirstNode = getIndexOfFirstNode(key);

                // Check if you are among the N nodes that can hold the item
                if (isNodeResponsibleForItem(indexOfFirstNode)) {
                    Hashtable<Integer, Item> newStorage = new Hashtable<>();
                    Enumeration<Integer> e = storage.keys();

                    while (e.hasMoreElements()) {
                        int key1 = e.nextElement();
                        if (key != key1) {
                            newStorage.put(key1, storage.get(key1));
                            System.out.println("HO TENUTO: " + key1);
                        }
                    }

                    this.storage = newStorage;
                }
            }

            
            int my_index = getMyIndex();
            int antiClockwiseNeighbor = my_index - 1;
            if(antiClockwiseNeighbor == -1) {
                antiClockwiseNeighbor = peers.size() - 1;
            }

            // Requests the items it became responsible for to the anti clockwise node. We only ask the ant-clockwise neighbor because the only items we could possibly be
            // missing in doing so are the ones for which the recovery node is the  first holder, i.e. the owner. Nevertheless, since during the alleged creation
            // of those items said node was down, the request wouldn't have been authorized.
            peers.get(antiClockwiseNeighbor).getActor().tell(new GetItemsListMsg(this.getID()), getSelf());
            
        }
        
        public void onGetItemsListMsg(GetItemsListMsg msg){

            getSender().tell(new SendItemsListRecoveryMsg(this.storage), getSelf());

        }

        public void onSendItemsListRecoveryMsg(SendItemsListRecoveryMsg msg){

            Enumeration<Integer> e = msg.items.keys();

            while (e.hasMoreElements()) {
                int key = e.nextElement();

                int indexOfFirstNode = getIndexOfFirstNode(key);
                if (isNodeResponsibleForItem(indexOfFirstNode)) {
                    
                    this.storage.put(key, msg.items.get(key));
                    System.out.println("Ho inserito la chiave " + key + " nello storage");
                }
            }

            Enumeration<Integer> en = storage.keys();
            
            while (en.hasMoreElements()) {
                int key = en.nextElement();
                getSender().tell(new GetValueMsg(key, false), getSelf());
            }

            //printNode();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(StartMessage.class, this::onStartMessage)
                    .match(GetValueMsg.class, this::onGetValueMsg)
                    .match(UpdateValueMsg.class, this::onUpdateValueMsg)
                    .match(RequestAccessMsg.class, this::onRequestAccessMsg)
                    .match(AccessResponseMsg.class, this::onAccessResponseMsg)
                    .match(RequestValueMsg.class, this::onRequestValueMsg)
                    .match(ValueResponseMsg.class, this::onValueResponseMsg)
                    .match(ChangeValueMsg.class, this::onChangeValueMsg)
                    .match(TimeoutRequest.class, this::onTimeout)
                    .match(TimeoutExternalRequest.class, this::onTimeout)
                    .match(TimeoutDequeue.class, this::onTimeoutDequeue)
                    .match(UnlockMsg.class, this::onUnlockMsg)
                    .match(OkMsg.class, this::onOkMsg)
                    .match(JoinRequestMsg.class, this::onJoinRequestMsg)
                    .match(SendPeerListMsg.class, this::onSendPeerListMsg)
                    .match(GetNeighborItemsMsg.class, this::onGetNeighborItemsMsg)
                    .match(SendItemsListMsg.class, this::onSendItemsListMsg)
                    .match(AnnounceJoiningNodeMsg.class, this::onAnnounceJoiningNodeMsg)
                    .match(ReturnValueMsg.class, this::onReturnValueMsg)
                    .match(LeaveRequestMsg.class, this::onLeaveRequestMsg)
                    .match(AddItemToStorageMsg.class, this::onAddItemToStorageMsg)
                    .match(AnnounceLeavingNodeMsg.class, this::onAnnounceLeavingNodeMsg)
                    .match(CrashRequestMsg.class, this::onCrashRequestMsg)
                    .match(RecoveryRequestMsg.class, this::onRecoveryRequestMsg)
                    .match(GetPeerListMsg.class, this::onGetPeerListMsg)
                    .match(SendPeerListRecoveryMsg.class, this::onSendPeerListRecoveryMsg)
                    .match(GetItemsListMsg.class, this::onGetItemsListMsg)
                    .match(SendItemsListRecoveryMsg.class, this::onSendItemsListRecoveryMsg)
                    .build();
        }
    }


}
