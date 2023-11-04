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

//import Ring.Node.RequestType;

import java.util.Enumeration;

public class Ring {
    // Start message that sends the list of participants to everyone
    public static class StartMessage implements Serializable {
        public final List<Peer> group;
        public final List<Integer> keys;
        public final List<String> values;

        /**
         * Constructor for StartMessage
         * @param group group of peers
         * @param keys set of initial keys
         * @param values set of initial values
         */
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

    // Create instances of messages to request values associated with specific keys, 
    // and it allows for specifying the request type explicitly or using a default value if not provided.
    public static class GetValueMsg implements Serializable {
        public final int key;
        public final Ring.Node.RequestType type;
        
        /**
         * Create instances of messages to request values associated with specific keys with a specified request type
         * @param key key from which to request the value 
         * @param type request type
         */
        public GetValueMsg(int key, Ring.Node.RequestType type) {
            this.key = key;
            this.type = type;
        }
        /**
         * @param key
         */
        public GetValueMsg(int key) {
            this.key = key;
            this.type = Ring.Node.RequestType.Read;
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
        public final int TIMEOUT_RECOVERY = 10000;

        public final int read_quorum = N / 2 + 1;
        public final int write_quorum = N / 2 + 1;

        private boolean hasCrashed = false;
        private ExternalRequest currExternalRequest = null;

        public enum RequestType {
            Read,
            Update,
            ReadJoin,
            ReadRecovery
        }

        public enum ExternalRequestType {
            Join,
            Leave,
            Recovery
        }

        /*-- Node constructor --------------------------------------------------- */
        public Node(int id){
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


        /**
         * Method to set the initial group (list of peers)
         * @param group list of initial peers
         */
        void setGroup(List<Peer> group) {

            // Create a new array list
            this.peers = new ArrayList<>();

            // Add every peer of the group to the list of peers
            for (Peer b: group) {
                this.peers.add(b);
            }
        }


        /**
         * Method to find the first node in a list of peers whose ID is greater than or equal to the given key
         * @param key Given key 
         * @return The index of the first node in a list of peers whose ID is greater than or equal to the given key
         */
        private int getIndexOfFirstNode (int key) {

            // Set the index to 0 (if it is the first node)
            int index = 0;

            // Find the first node in a list of peers whose ID is greater than or equal to the given key
            for (int i = 0; i < peers.size(); i++) {

                // If the key is greater than or equal to the ID of the peer
                if (peers.get(i).getID() >= key) {
                    // Set index to the index of the peer
                    index = i;
                    break;
                    // If we're not able to find a node whose ID is greater than the key,
                    // then the first node to store the value is necessarily the node with the lowest ID (aka index = 0)
                }
            }
            return index;
        }


        /** 
         * Method to find the last node in a list of peers whose ID is smaller than or equal to the given key
         * @param key Given key 
         * @return The index of the last node in a list of peers whose ID is smaller than or equal to the given key
         */
        private int getIndexOfLastNode(int key) {

            // Find the first node in a list of peers whose ID is greater than or equal to the given key
            int indexOfFirstNode = getIndexOfFirstNode(key);

            // Usa a formula to calculate the last node from the first
            int indexOfLastNode = (indexOfFirstNode + N - 1) % peers.size();

            return indexOfLastNode;
        }


        /** 
         * Method to add a new peer to a list called peers while maintaining a sorted order based on the ID of the peers. 
         * @param newPeer new peer to add to the list of peers
         */
        private void addPeer(Peer newPeer) {

            // Set the initial position to 0
            int newPeerPosition = 0;

            // Find the right position based on the ID of the peers for the new peer
            for (int i = 0; i < peers.size(); i++) {

                // If the ID of the new peer is smaller than the ID of the peer
                if (newPeer.getID() < peers.get(i).getID()) {

                    // Set the index of the new peer equal to the idex of the peer
                    newPeerPosition = i;
                    break;
                }
            }

            // Add the new peer in the right position based on the ID of the peers
            peers.add(newPeerPosition, newPeer);
        }


        /**
         * Method to add the new peer to a new list while maintaining a sorted order based on the ID of the peers.
         * @param newPeer new peer to add to the new list of peers
         * @param list list of peers
         * @return new list of peers with the new peer in the right position
         */
        private List<Peer> addPeer(Peer newPeer, List<Peer> list) {

            // Set the initial position to 0
            int newPeerPosition = 0;
            // Create the new list of peers
            List<Peer> newList = new ArrayList<>();

            // Find the right position based on the ID of the peers for the new peer
            for (int i = 0; i < list.size(); i++) {

                // If the ID of the new peer is smaller than the ID of the peer
                if (newPeer.getID() < list.get(i).getID()) {

                    // Set the index of the new peer equal to the idex of the peer
                    newPeerPosition = i;
                    break;
                }
            }

            int listIndex = 0;

            // Create the new list of peers
            for (int j = 0; j < list.size() + 1; j++) {

                // Add the new peers
                if (j == newPeerPosition) {
                    newList.add(newPeer);
                }

                // Add the older peers
                else {
                    newList.add(list.get(listIndex));
                    listIndex++;
                }
            }
            return newList;
        }

        /**
         * Find if the node is responsible for a given item
         * @param indexOfFirstNode Index of the first node responsible for a given item
         * @return If the node is resposible for a given item or not
         */
        private boolean isNodeResponsibleForItem(int indexOfFirstNode) {

            // Get the index of the node
            int myIndex = getMyIndex();

            // If the node is responsible for that item
            if ((myIndex >= indexOfFirstNode && myIndex < indexOfFirstNode + N) || (myIndex <= indexOfFirstNode && myIndex < ((indexOfFirstNode + N) % peers.size()) && ((indexOfFirstNode + N) % peers.size()) < indexOfFirstNode)) {
                return true;
            }

            return false;
        }


        /**
         * Method to find the index of the node
         * @return the idnex of the node
         */
        private int getMyIndex() {

            // Set the index to 0
            int my_index = 0;

            // Find the index of the node
            for (int j = 0; j < peers.size(); j++) {

                // If the id is equal to the ID of the peer
                if (this.id == peers.get(j).getID()) {

                    // Set the index eqaul to the index of the peer
                    my_index = j;
                    break;
                }
            }

            return my_index;
        }


        /**
         *  Method used to create a configuration (Props) for creating a new actor of the Node class with the specified id
         * @param id id of the new actor
         * @return An instance of the Props class with the new id
         */
        static public Props props(int id) {
            return Props.create(Node.class, () -> new Node(id));
        }


        /**
         * Set the initial storage of the ring based on a list of keys and values
         * @param keys list of initial keys
         * @param values list of initial values
         */
        private void setInitialStorage(List<Integer> keys, List<String> values){

            // For every key in the list of keys
            for(int i = 0; i < keys.size(); i++) {

                // Find the first node responsible for the key
                int indexOfFirstNode = getIndexOfFirstNode(keys.get(i));

                // If the node is responsible for the first key
                if (isNodeResponsibleForItem(indexOfFirstNode)) {

                    // Put the key and the value into the storage of the node with version 1
                    this.storage.put(keys.get(i), new Item(keys.get(i), values.get(i), 1));
                }

            }

            // Print the node with the storage
            printNode();
        }


        /**
         * Method to print a node with its storage
         */
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


        /**
         * Method to construct the list of peers in order to print it
         * @return String with the list of peers
         */
        public String printPeers() {
            String s = "[";

            // For every peer
            for (Peer p : this.peers) {
                
                // Add to the string the ID of the peer
                s += p.getID() + " ";
            }

            s += "]";
            return s;
        }

        /**
         * Method to set the initial group and the initial storage
         * @param msg instance of StartMessage
         */
        public void onStartMessage(StartMessage msg) {
            // Set the initial group
            setGroup(msg.group);

            // Set the initial storage
            setInitialStorage(msg.keys, msg.values);
        }

        /**
         * Method for initiating a request by determining the appropriate owner actor based on the request's key,
         * setting the owner for the request, printing a log message, and then sending a message to the owner actor to handle the request. 
         * @param request request to send
         */
        private void startRequest(Request request){

            // Index of the first node responsible for the item specified in the request
            int index = getIndexOfFirstNode(request.getKey());
            
            // Owner of the item
            ActorRef owner = peers.get(index).getActor();
            // Set the owner for the request
            request.setOwner(owner);

            // Print the log message with the request
            System.out.println("Request started - id request: " + request.getID() + ", type: " + request.getType() + ", Key: " + request.getKey() + ", client: " + request.getClient());
            
            // Send a message to the owner actor to handle the request
            owner.tell(new RequestAccessMsg(request), getSelf());
        }

        /**
         * Method used to start a read request
         * @param msg instance of GetValue message
         */
        private void onGetValueMsg(GetValueMsg msg) {

            // If the node has creashed, do nothing
            if(this.hasCrashed){ return; }
            
            // Set the key to the key of the message
            int key = msg.key;
            // Create a new request
            Request newRequest;

            // If the read request is for join
            if(msg.type == RequestType.ReadJoin) {

                // Create a new read request for join 
                newRequest = new Request(key, RequestType.ReadJoin, getSender(), null);
            }

            // If the read request is for recovery
            else if (msg.type == RequestType.ReadRecovery) {

                // Create a new read request for recovery 
                newRequest = new Request(key, RequestType.ReadRecovery, getSender(), null);
            }  

            // If it is a normal read
            else {

                // Create a new read request
                newRequest = new Request(key, RequestType.Read, getSender(), null);
            }

            // Add the new request to the active requests
            activeRequests.add(newRequest);

            // Set a new timeout for this reques
            setTimeout(TIMEOUT_REQUEST, newRequest.getID());

            // Start the new request
            startRequest(newRequest);
        }


        /**
         * Method to initialize an update/write request
         * @param msg instance of UpdateValue message
         */
        private void onUpdateValueMsg(UpdateValueMsg msg){
            
            // If the node has creashed, do nothing
            if(this.hasCrashed){ return; }

            // Set the key to the key specified in the message
            int key = msg.key;
            // Set the value to the value specified in the message
            String value = msg.value;

            // Create a new update request
            Request newRequest = new Request(key, RequestType.Update, getSender(), value);

            // Add the new request to the active requests
            activeRequests.add(newRequest);
            
            // Set a new timeout for this reques
            setTimeout(TIMEOUT_REQUEST, newRequest.getID());

            // Start the new request
            startRequest(newRequest);
        }

        /**
         * Method to handle access requests, determines whether access is granted or denied based on the type of request and the availability of the requested item, 
         * and to communicate the access status to the coordinator
         * @param msg instance of RequestAccess message
         */
        private void onRequestAccessMsg(RequestAccessMsg msg) {

            // If the node has creashed, do nothing
            if(this.hasCrashed){ return; }

            // Create a new Item for the specified key
            Item i = storage.get(msg.request.getKey());
            // Initialize the coordinator
            ActorRef coordinator = getSender();
            // Boolean for accessing the item
            boolean accessGranted;

            // If it is a read request
            if (msg.request.getType() == RequestType.Read || msg.request.getType() == RequestType.ReadJoin || msg.request.getType() == RequestType.ReadRecovery) {
                
                // If the item with that key does not exist
                if (i == null) {

                    // Access not granted
                    accessGranted = false;

                    // Send a message to the coordinator that the item was not found
                    coordinator.tell(new AccessResponseMsg(accessGranted, true,  msg.request), getSelf());
                    return;
                }
                else {

                    // Access granted and lock the item for read
                    accessGranted = i.lockRead();
                }

            }

            // If it is an update request
            else { 

                // If there are no reads for the item and no updates
                if (i == null) {

                    // Create a new item with that key and value, with version 0
                    i = new Item(msg.request.getKey(), "", 0); // version 0 is useful to identify that the item is new

                    // Put the item in the storage
                    storage.put(msg.request.getKey(), i);
                }

                // Set access granted to the lock of the item
                accessGranted = i.lockUpdate();

                // If access granted
                if (accessGranted) {

                    // Print the request
                    System.out.println("Access GRANTED for update operation - id request: " + msg.request.getID() + ", type: " + msg.request.getType() + ", Key: " + msg.request.getKey() + ", client: " + msg.request.getClient());
                }

                // If access not granted
                else{

                    // Print access denied for that request
                    System.out.println("Access DENIED for update operation - id request: " + msg.request.getID() + ", type: " + msg.request.getType() + ", Key: " + msg.request.getKey() + ", client: " + msg.request.getClient());
                }

            }

            // If access granted
            if (accessGranted) {

                // Send a new AccessResponde message
                coordinator.tell(new AccessResponseMsg(true, false, msg.request), getSelf());
            }
            else {

                // Print access denied for that request
                System.out.println("Access denied - id request: " + msg.request.getID() + ", type: " + msg.request.getType() + ", Key: " + msg.request.getKey() + ", client: " + msg.request.getClient());
                
                // Send a new AccessResponde message
                coordinator.tell(new AccessResponseMsg(false, false,  msg.request), getSelf());
            }
        }

        /**
         * Method to sends the request to nodes that has the item if access granted, if not send an error message is the item does not exist
         * or add the request to the queue
         * @param msg instance of AccessResponse message
         */
        private void onAccessResponseMsg(AccessResponseMsg msg) {

            // If the node has creashed, do nothing
            if(this.hasCrashed){ return; }               

            // If access granted
            if (msg.accessGranted) {

                // Set the key to the key specified in the message
                int key = msg.request.getKey();

                // Get the index of the first node in the ring that has the key in the storage
                int index = getIndexOfFirstNode(key);

                // For every peer that has this item
                for (int i = index; i < N + index; i++) {

                    int length = peers.size();
                    // Find the actor of the peer
                    ActorRef actor = peers.get(i % length).getActor();

                    // Send a request value message to that peer
                    actor.tell(new RequestValueMsg(msg.request), getSelf());
                }
            }

            // If access not granted
            else {

                // If the request is for read and the item is not found
                if (msg.itemNotFound && msg.request.getType() == RequestType.Read) {

                    // Remove the request from the active request
                    activeRequests.remove(msg.request);

                    // Send an error message to the client
                    msg.request.getClient().tell(new ErrorMsg("Error message for Read Request - request id: " + msg.request.getID() + ", key: " + msg.request.getKey() + ". Item not found"), getSelf());
                    return;
                }

                // Remove the request from the active request
                activeRequests.remove(msg.request);

                // Add the request to the request queue
                requestQueue.add(msg.request);

                // Print a message to say that the request is added to the queue
                System.out.println("Request added to the queue - id request: " + msg.request.getID() + ", type: " + msg.request.getType() + ", Key: " + msg.request.getKey() + ", client: " + msg.request.getClient());
            }
        }

        /**
         * Method to create a new Item if the requested key is new, and to send a response containing the requested value to the sender. 
         * @param msg instance of RequestValue message
         */
        private void onRequestValueMsg(RequestValueMsg msg) {
            
            // If the node has creashed, do nothing
            if(this.hasCrashed){ return; }

            // Create a new item with that key and value, with version 0
            Item i = storage.get(msg.request.getKey());

            // If the key is new
            if (i == null) {

                // Create the new item
                i = new Item(msg.request.getKey(), "", 0);
            }

            // Get the sender actor
            ActorRef sender = getSender();
            //RequestType requestType = msg.request.getType();
            //System.out.println("value requested - id request: " + msg.request.getID() + ", type: " + msg.request.getType() + ", Key: " + msg.request.getKey() + ", client: " + msg.request.getClient());
            
            // Send the request to the sender
            sender.tell(new ValueResponseMsg(i, msg.request), getSelf());
        }

        /**
         * Method to handle value response messages, to keep track of the number of responses received, update the current best value, 
         * and make decisions based on the type of request and the number of responses received
         * @param msg instance of ValueResponse message
         */
        private void onValueResponseMsg(ValueResponseMsg msg) {

            // If the node has creashed, do nothing
            if(this.hasCrashed){ return; }

            // If the request is in the active request
            if (activeRequests.contains(msg.request)) {

                // Increments a counter in the request object to keep track of the number of responses received for this request
                msg.request.incrementnResponses();
                //System.out.println("Response message n. " + msg.request.getnResponses() + " - id request: " + msg.request.getID() + ", type: " + msg.request.getType() + ", Key: " + msg.request.getKey() + ", client: " + msg.request.getClient());
                
                // Number of responses
                int nResponses = msg.request.getnResponses();
                // Item with the current newest version
                Item currBest = msg.request.getCurrBest();

                // If the current newest version is null
                if (currBest == null) {

                    // Set che newest version to the version of the item
                    msg.request.setCurrBest(msg.item);
                }

                // If the current newest version is not null
                else {

                    // If the version of the item is greater than the current newest version
                    if (msg.item.getVersion() > currBest.getVersion()) {

                        // Set che newest version to the version of the item
                        msg.request.setCurrBest(msg.item);
                    }
                }

                // If it is a read request
                if(msg.request.getType() == RequestType.Read || msg.request.getType() == RequestType.ReadJoin || msg.request.getType() == RequestType.ReadRecovery){

                    // If the number of responses is greater or equal to the read quorum
                    if (nResponses >= read_quorum) {
                        
                        // Remove the request from the active requests
                        activeRequests.remove(msg.request);

                        // Unlock the item
                        msg.request.getOwner().tell(new UnlockMsg(msg.request), getSelf());

                        // If the current newest version is null
                        if (msg.request.getCurrBest() == null) {

                            // Send an error to the client
                            msg.request.getClient().tell(new ErrorMsg("Error message for Read Request - request id: " + msg.request.getID() + ", key: " + msg.request.getKey() + ". Value does not exist in the ring"), getSelf());
                        }
                        else {

                            // Return a message to the client
                            msg.request.getClient().tell(new ReturnValueMsg(currBest, msg.request.getID(), msg.request.getType()), getSelf());
                        }
                    }
                }

                // If it is an update request
                else {                  

                    // If the number of responses is greater or equal to the write quorum
                    if (nResponses >= write_quorum) {

                        // Return a message to the client
                        msg.request.getClient().tell(new ReturnValueMsg(currBest, msg.request.getID(), msg.request.getType()), getSelf());

                        // Get the idex of the owner of the item
                        int index = getIndexOfFirstNode(msg.request.getKey());
                        // Increment the version
                        int newVersion = currBest.getVersion() + 1;
                        
                        // For every node that has the item
                        for (int i = index; i < N + index; i++) {

                            int length = peers.size();
                            // Find the actor for that peer
                            ActorRef actor = peers.get(i % length).getActor();

                            // Send a message to the actor in order to change the value
                            actor.tell(new ChangeValueMsg(msg.request, newVersion), getSelf());
                        }

                        // Remove the request from the active requests
                        activeRequests.remove(msg.request);
                        // Add the request to the pending requests
                        pendingRequests.add(msg.request);
                    } 
                }
            }
        }

        public void onChangeValueMsg(ChangeValueMsg msg) {

            if(this.hasCrashed){ return; }

            Item newItem = new Item(msg.request.getKey(), msg.request.getNewValue(), msg.newVersion);
            this.storage.put(msg.request.getKey(), newItem);
            //System.out.println("New item - key: " + msg.request.getKey() + ", new value: " + storage.get(msg.request.getKey()).getValue() + ", current version: " + storage.get(msg.request.getKey()).getVersion());
            //System.out.print("On change value msg");
            printNode();
            getSender().tell(new OkMsg(msg.request), getSelf());
        }

        public void onUnlockMsg(UnlockMsg msg) {

            if(this.hasCrashed){ return; }

            int key = msg.request.getKey();
            if (msg.request.getType() == RequestType.Read || msg.request.getType() == RequestType.ReadJoin || msg.request.getType() == RequestType.ReadRecovery) {
                this.storage.get(key).unlockRead();
            }
            else {
                this.storage.get(key).unlockUpdate();            
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
                //System.out.println("Queue size: " + requestQueue.size()); // Debugging output
                //System.out.println("STO FACENDO IL DEQUEUE");
                Request r = requestQueue.remove();
                //System.out.println("Removed request: " + r); // Debugging output
                activeRequests.add(r);
                startRequest(r);
                //System.out.println("Request processed: " + r); // Debugging output
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
                        //System.out.println("Sono nella funzione di timeout, sto togliendo l'elemento, queue size: " + requestQueue.size());
                        requestQueue.remove(request);
                        pendingRequests.remove(request);
                        //System.out.println("Sono nella funzione di timeout, ho tolto l'elemento, queue size: " + requestQueue.size());
                        request.getClient().tell(new ErrorMsg("Your Read request " + msg.id_request + " took too much to be satisfied"), getSelf());
                    }
                }
                else {
                    if (request.getnResponses() < write_quorum || request.getOkResponses() < write_quorum) {
                        activeRequests.remove(request);
                        //System.out.println("Sono nella funzione di timeout, sto togliendo l'elemento, queue size: " + requestQueue.size());
                        requestQueue.remove(request);
                        pendingRequests.remove(request);
                        //System.out.println("Sono nella funzione di timeout, ho tolto l'elemento, queue size: " + requestQueue.size());
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
                indexClockwiseNode = 0; // System.out.println("Ho finito di fare il read join");se il nuovo nodo è l'ultimo allora il clockwise neighbor è il primo della lista
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
                System.out.println("Sto facendo il read per: " + key);

                // Send a read request for every items
                getSender().tell(new GetValueMsg(key, RequestType.ReadJoin), getSelf());
            }



        }
        public void onReturnValueMsg(ReturnValueMsg msg) {

            if(this.hasCrashed){ return; } // NON HA SENSO PERCHE' QUESTO METODO VIENE ESEGUITO SOLO IN CASO DI RECOVERY E JOIN

            currExternalRequest.incrementnResponses();
            //System.out.println("Received return message for key " + msg.item.getKey() + ", value: " + msg.item.getValue() + ", version: " + msg.item.getVersion());
            // Change version if it is not updated
            if(storage.containsKey(msg.item.getKey())){
                if(msg.item.getVersion() >= storage.get(msg.item.getKey()).getVersion()) {
                    storage.put(msg.item.getKey(), msg.item);
                    //System.out.println("Changed item version");
                }
                //System.out.println("IF; Item in storage: " + storage.get(msg.item.getKey()).getKey() + " " + storage.get(msg.item.getKey()).getValue() + " " + storage.get(msg.item.getKey()).getVersion());
            } else {
                storage.put(msg.item.getKey(), msg.item);
                //System.out.println("ELSE; Item in storage: " + storage.get(msg.item.getKey()).getKey() + " " + storage.get(msg.item.getKey()).getValue() + " " + storage.get(msg.item.getKey()).getVersion());
                
            }

            if(currExternalRequest.getnResponses() == storage.size()) {
                if(msg.requestType == RequestType.ReadJoin){
                    //System.out.println("Ho finito di fare il read join");
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
                else if (msg.requestType == RequestType.ReadRecovery) {
                    //System.out.println("Ho finito di fare il read recovery");
                    currExternalRequest = null;
                    printNode();
                }
            }
            //System.out.print("On return value msg");
            //printNode();

        }

        public void onAnnounceJoiningNodeMsg(AnnounceJoiningNodeMsg msg) {

            if(this.hasCrashed){ return; }

            //System.out.println("Node " + this.id + " received joining announcement");
            // Update the list of peers
            if (this.id != msg.joiningNodeKey) {
                addPeer(new Peer(msg.joiningNodeKey, getSender()));
            }
            System.out.println("Node " + this.id + "'s peers: " + printPeers());
            Hashtable<Integer, Item> newStorage = new Hashtable<>();
            Enumeration<Integer> e = storage.keys();
            while (e.hasMoreElements()) {
                int key = e.nextElement();
                int index = getIndexOfFirstNode(key);
                if (isNodeResponsibleForItem(index)) {
                    newStorage.put(key, storage.get(key));
                }
            }
            this.storage = newStorage;
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
            setGroup(msg.group);
            System.out.println("Recovering node updated peers: " + printPeers());

            // Forgets the items it is no longer responsible for
            // First index of first node for each item of the node
            Enumeration<Integer> en = storage.keys();
            Hashtable<Integer, Item> newStorage = new Hashtable<>();
            while(en.hasMoreElements()) {
                int key = en.nextElement();
                int indexOfFirstNode = getIndexOfFirstNode(key);
                if (isNodeResponsibleForItem(indexOfFirstNode)) {
                    newStorage.put(key, storage.get(key));
                }
            }


            this.storage = newStorage;
            //System.out.print("Items of recovering node BEFORE ASKING ANTI-CLOCKWISE NEIGHBOUR: ");
            printNode();

            int my_index = getMyIndex();
            int antiClockwiseNeighbor = my_index - 1;
            if(antiClockwiseNeighbor == -1) {
                antiClockwiseNeighbor = peers.size() - 1;
            }

            // Requests the items it became responsible for to the anti-clockwise node. We only ask the anti-clockwise neighbor because the only items we could possibly be
            // missing in doing so are the ones for which the recovery node is the  first holder, i.e. the owner. Nevertheless, since during the alleged creation
            // of those items said node was down, the request wouldn't have been authorized.
            peers.get(antiClockwiseNeighbor).getActor().tell(new GetItemsListMsg(this.getID()), getSelf());
            
        }
        
        public void onGetItemsListMsg(GetItemsListMsg msg){

            getSender().tell(new SendItemsListRecoveryMsg(this.storage), getSelf());

        }

        public void onSendItemsListRecoveryMsg(SendItemsListRecoveryMsg msg){
            if (storage.size() == 0 && msg.items.size() == 0) {
                // RECOVERY TERMINATO PERCHE' NON SERVE FARE IL READ DEGLI ITEM VISTO CHE LO STORAGE E' VUOTO
                currExternalRequest = null;
            }
            Enumeration<Integer> e1 = msg.items.keys();
            while (e1.hasMoreElements()) {
                int key = e1.nextElement();
                int indexOfFirstNode = getIndexOfFirstNode(key);
                if (!this.storage.containsKey(key) && isNodeResponsibleForItem(indexOfFirstNode)) {
                    this.storage.put(key, msg.items.get(key));
                }
            }
            //System.out.print("Final items of recovering node BEFORE THE READ: ");
            printNode();

            Enumeration<Integer> en = storage.keys();
            
            while (en.hasMoreElements()) {
                int key = en.nextElement();
                
                getSender().tell(new GetValueMsg(key, RequestType.ReadRecovery), getSelf());
            }

        
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
