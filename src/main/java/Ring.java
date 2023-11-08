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
        public final List<Peer> group;                  // List of peers
        public final List<Integer> keys;                // List of keys
        public final List<String> values;               // List of values

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
        public final int key;                           // Key of the item
        public final String value;                      // Value to update
        
        /**
         * Constructor of UpdateValueMsg
         * @param key Key of the item
         * @param value Value to update
         */ 
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
         * @param key key from which to request the value 
         */
        public GetValueMsg(int key) {
            this.key = key;

            // Set the type of the request to read
            this.type = Ring.Node.RequestType.Read;
        }
    }

    public static class RequestAccessMsg implements Serializable {
        public final Request request;                       // Request

        /**
         * @param request Request
         */
        public RequestAccessMsg(Request request) {
            this.request = request;
        }
    }

    public static class RequestValueMsg implements Serializable {
        public final Request request;                       // Request
        
        /**
         * @param request Request
         */
        public RequestValueMsg(Request request) {
            this.request = request;
        }
    }

    public static class AccessResponseMsg implements Serializable {
        public final boolean accessGranted;                 // If the item is lock or not
        public final boolean itemNotFound;                  // If the item is found or not
        public final Request request;                       // Request
        
        /**
         * @param accessGranted If the item is lock or not
         * @param itemNotFound If the item is found or not
         * @param request Request
         */
        public AccessResponseMsg(boolean accessGranted, boolean itemNotFound, Request request) {
            this.accessGranted = accessGranted;
            this.request = request;

            // If the item is not found for a read request
            if (itemNotFound && request.getType() == Node.RequestType.Read && accessGranted == false) {
                this.itemNotFound = true;
            }

            // If it is a new item for update request
            else {
                this.itemNotFound = false;
            }
        }
    }

    public static class ValueResponseMsg implements Serializable {
        public final Item item;                         // Item
        public final Request request;                   // Request

        /**
             * @param item Item
             * @param request Request
             */
            public ValueResponseMsg(Item item, Request request) {
            this.item = item;
            this.request = request;
        }
    }

    public static class ReturnValueMsg implements Serializable {
        public final Item item;                         // Item
        public final int requestID;                     // ID of the request
        public final Ring.Node.RequestType requestType; // Type of the request

        /**
         * @param item Request
         * @param requestID ID of the request
         * @param requestType Type of the request
         */
        public ReturnValueMsg(Item item, int requestID, Ring.Node.RequestType requestType) {
            this.item = item;
            this.requestID = requestID;
            this.requestType = requestType;
        }
    }

    public static class ChangeValueMsg implements Serializable {
        public final Request request;                   // Request
        public final int newVersion;                    // New version for the item
        
        /**
         * @param request Request
         * @param newVersion New version for the item
         */
        public ChangeValueMsg(Request request, int newVersion) {
            this.request = request;
            this.newVersion = newVersion;
        }
    }

    public static class UnlockMsg implements Serializable {
        public final Request request;                   // Request
        
        /**
         * @param request Request
         */
        public UnlockMsg(Request request) {
            this.request = request;
        }
    }

    public static class OkMsg implements Serializable {
        public final Request request;                   // Request
       
        /**
         * @param request Request
         */
        public OkMsg(Request request) {
            this.request = request;
        }
    }

    public static class ErrorMsg implements Serializable {
        public final String error;                      // Error message

        /**
         * @param error Error message
         */
        public ErrorMsg(String error) {
            this.error = error;
        }
    }

    public static class TimeoutRequest implements Serializable {
        public final int id_request;                    // ID of the request
        
        /**
         * @param id_request ID of the request
         */ 
        public TimeoutRequest(int id_request) {
            this.id_request = id_request;
        }
    }

    public static class TimeoutDequeue implements Serializable {
        public TimeoutDequeue() {        }
    }
    
    public static class TimeoutExternalRequest implements Serializable {
        public final int externalRequestId;                     // ID of the external request
        
        /**
         * @param externalRequestId ID of the external request
         */
        public TimeoutExternalRequest(int externalRequestId) {
            this.externalRequestId = externalRequestId;
        }
    }

    public static class JoinRequestMsg implements Serializable {
        public final Peer joiningPeer;                          // Joining peer
        public final ActorRef bootStrappingPeer;                // Boot-strapping peer

        /**
         * @param joiningPeer Joining peer
         * @param bootStrappingPeer Boot-strapping peer
         */
        public JoinRequestMsg(Peer joiningPeer, ActorRef bootStrappingPeer){
            this.joiningPeer = joiningPeer;
            this.bootStrappingPeer = bootStrappingPeer;
        }
    }

    public static class SendPeerListMsg implements Serializable {
        public final List<Peer> group;                          // List of peers
        public final ExternalRequest request;                   // External request
        
        /**
         * @param group List of peers
         * @param request External request
         */
        public SendPeerListMsg(List<Peer> group, ExternalRequest request){
            this.group = Collections.unmodifiableList(new ArrayList<>(group));
            this.request = request;
        }
    }

    public static class GetNeighborItemsMsg implements Serializable {
        public GetNeighborItemsMsg() {        }
    }

    public static class SendItemsListMsg implements Serializable {
        public final Hashtable<Integer, Item> items;                    // Hashtable of items, with keys and values
        
        /**
         * @param items List of items, with keys and values
         */
        public SendItemsListMsg(Hashtable<Integer, Item> items){
            this.items = items;
        }
    }

    public static class AnnounceJoiningNodeMsg implements Serializable {
        public final int joiningNodeKey;                               // Key of the joining node
        public final List<Item> items;                                 // List of items
        
        /**
         * @param joiningNodeKey Key of the joining node
         * @param items List of items
         */
        public AnnounceJoiningNodeMsg(int joiningNodeKey, List<Item> items) {
            this.joiningNodeKey = joiningNodeKey;
            this.items = items;
        }
    }

    public static class LeaveRequestMsg implements Serializable {
        public LeaveRequestMsg() {        }
    }

    public static class AddItemToStorageMsg implements Serializable {
        public final Item item;                         // Item to add to the storage

        /**
         * @param item Item to add to the storage
         */
        public AddItemToStorageMsg(Item item) {
            this.item = item;
        }
    }

    public static class AnnounceLeavingNodeMsg implements Serializable {
        public final int leavingNodeIndex;                          // Index of the leaving node
        
        /**
         * @param index Index of the leaving node
         */
        public AnnounceLeavingNodeMsg(int index) {
            leavingNodeIndex = index;
        }
    }

    public static class CrashRequestMsg implements Serializable {
        public CrashRequestMsg() {         }
    }

    public static class RecoveryRequestMsg implements Serializable {
        public final ActorRef nodeToContact;                        // Node to contact for the list of peers
        
        /**
         * @param nodeToContact Node to contact for the list of peers
         */
        public RecoveryRequestMsg(ActorRef nodeToContact) {
            this.nodeToContact = nodeToContact;
        }
    }

    public static class GetPeerListMsg implements Serializable {
        public GetPeerListMsg() {        } 
    }

    public static class SendPeerListRecoveryMsg implements Serializable {
        public final List<Peer> group;                              // List of peers

        /**
         * @param group List of peers
         */
        public SendPeerListRecoveryMsg(List<Peer> group) {
            this.group = Collections.unmodifiableList(new ArrayList<>(group));
        }
    }

    public static class GetItemsListMsg implements Serializable {
        public final int recoveryNodeID;                            // ID of the recovery node
        
        /**
         * @param recoveryNodeID ID of the recovery node
         */
        public GetItemsListMsg(int recoveryNodeID) {
            this.recoveryNodeID = recoveryNodeID;
        }
    }

    public static class SendItemsListRecoveryMsg implements Serializable {
        public final Hashtable<Integer, Item> items;                // Hashtable of items, with keys and values
        
        /**
         * @param items Hashtable of items, with keys and values
         */
        public SendItemsListRecoveryMsg(Hashtable<Integer, Item> items){
            this.items = items;
        }
    }

    public static class Node extends AbstractActor{

        private int id;                                                          // ID of the node
        private ActorRef actor;                                                  // Actor of the node
        private Hashtable<Integer, Item> storage = new Hashtable<>();            // List of keys and values
        private List<Peer> peers = new ArrayList<>();                            // List of peers

        private ArrayList<Request> activeRequests = new ArrayList<>();           // List of active requests for the node
        private Queue<Request> requestQueue = new LinkedList<>();                // Request queue for the node
        private ArrayList<Request> pendingRequests = new ArrayList<>();          // List of pending requests for the node

        public final int N = 4;                                                  // Number of peers that have an item

        public final int TIMEOUT_REQUEST = 5000;                                 // Timeout for read/update
        public final int TIMEOUT_DEQUEUE = 2000;                                 // Timeout for dequeue
        public final int TIMEOUT_JOIN = 5000;                                    // Timeout for join
        public final int TIMEOUT_RECOVERY = 10000;                               // Timeout for recovery

        public final int read_quorum = N / 2 + 1;                                // Read quorum
        public final int write_quorum = N / 2 + 1;                               // Update quorum

        private boolean hasCrashed = false;                                      // If the node has crashed or not
        private ExternalRequest currExternalRequest = null;                      // Current extenral request

        public enum RequestType {                                                // Request type
            Read,
            Update,
            ReadJoin,
            ReadRecovery
        }

        public enum ExternalRequestType {                                        // External request type
            Join,
            Leave,
            Recovery
        }

        
        /**
         * @param id ID of the node
         */
        public Node(int id){
            super();
            this.id = id;
            setTimeoutDequeue(TIMEOUT_DEQUEUE);
        }
        
        /**
         * Method to get the ID of the node
         * @return ID of the node
         */
        public int getID() {
            return this.id;
        }

        /**
         * Method to get the actor of the node
         * @return Actor of the node
         */
        public ActorRef getActor() {
            return this.actor;
        }

        /**
         * Method to remove an item based on the key
         * @param key Key of the item to remove
         */
        public void removeValue (int key) {
            storage.remove(key);
        }

        /**
         * Method to add an item to the storage
         * @param key Key of the item to add
         * @param value Value of the item to add
         * @param version Version of the item to add
         */
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

            // If the node has crashed, do nothing
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
            
            // If the node has crashed, do nothing
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

            // If the node has crashed, do nothing
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

            }

            // If access granted
            if (accessGranted) {
                System.out.println("Access granted - id request: " + msg.request.getID() + ", type: " + msg.request.getType() + ", Key: " + msg.request.getKey() + ", client: " + msg.request.getClient());
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

            // If the node has crashed, do nothing
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
            
            // If the node has crashed, do nothing
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

            // If the node has crashed, do nothing
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

                    // Set the newest version to the version of the item
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

        /**
         * Method to insert the item in the storage based on the item in the msg: if it is new, create a new item, 
         * otherwise, change the value and update the version
         * @param msg instance 
         */
        public void onChangeValueMsg(ChangeValueMsg msg) {

            // If the node has crashed, do nothing
            if(this.hasCrashed){ return; }

            // Create a new item
            Item newItem = new Item(msg.request.getKey(), msg.request.getNewValue(), msg.newVersion);
            // Put the new item in the store
            this.storage.put(msg.request.getKey(), newItem);
            //System.out.println("New item - key: " + msg.request.getKey() + ", new value: " + storage.get(msg.request.getKey()).getValue() + ", current version: " + storage.get(msg.request.getKey()).getVersion());
            
            // Print the node with the storare
            printNode();

            // Send an Ok msg to the sender
            getSender().tell(new OkMsg(msg.request), getSelf());
        }

        /**
         * Method to unlock an item
         * @param msg instance of Unlock message
         */
        public void onUnlockMsg(UnlockMsg msg) {

            // If the node has crashed, do nothing
            if(this.hasCrashed){ return; }

            // Get the key of the item to unlock
            int key = msg.request.getKey();

            // If it is a read operation
            if (msg.request.getType() == RequestType.Read || msg.request.getType() == RequestType.ReadJoin || msg.request.getType() == RequestType.ReadRecovery) {
                
                // Unlock the item for read 
                this.storage.get(key).unlockRead();
            }

            // If it is a write operation
            else {

                // Unlock the item for write 
                this.storage.get(key).unlockUpdate();            
            }
        }

        /**
         * Method to handle acknowledgment messages for update requests. It counts the number of acknowledgments received and, 
         * when the write quorum condition is met, it sends an unlock message to the request owner and removes the request 
         * from the pending requests collection, indicating a successful update operation.
         * @param msg instance of Ok message
         */
        public void onOkMsg(OkMsg msg) {

            // If the node has crashed, do nothing
            if(this.hasCrashed){ return; }

            // If the request is in the pending requests
            if (pendingRequests.contains(msg.request)) {

                // Increment the number of responses
                msg.request.incrementOkResponses();

                // If the number of responses is greater than the write quorum
                if (msg.request.getOkResponses() >= write_quorum) {
                    
                    // Unlock the item
                    msg.request.getOwner().tell(new UnlockMsg(msg.request), getSelf());

                    // Remove the request from the pending request
                    this.pendingRequests.remove(msg.request);
                }
            }
        }

        /**
         * Method to schedule a timeout for the actor after a specified duration. When the timeout occurs, it sends a custom message of type TimeoutRequest to the actor
         * @param time Time after the timeout occurs 
         * @param id_request ID of the request to set the timeout for
         */
        private void setTimeout(int time, int id_request) {

            // Schedule a task to be executed once after a specified time
            getContext().system().scheduler().scheduleOnce(
                    Duration.create(time, TimeUnit.MILLISECONDS),                   // Set the duration
                    getSelf(),
                    new TimeoutRequest(id_request),                                 // The message to send
                    getContext().system().dispatcher(), getSelf()
            );
        }

        /**
         * Method to schedule a timeout for the actor after a specified duration. When the timeout occurs, it sends a custom message of type TimeoutRequest to the actor
         * @param time Time after the timeout occurs 
         * @param externalRequestId ID of the external request to set the timeout for
         */
        private void setTimeoutExternalRequest(int time, int externalRequestId) {

            // Schedule a task to be executed once after a specified time
            getContext().system().scheduler().scheduleOnce(
                    Duration.create(time, TimeUnit.MILLISECONDS),                   // Set the duration
                    getSelf(),
                    new TimeoutExternalRequest(externalRequestId),                  // The message to send
                    getContext().system().dispatcher(), getSelf()
            );
        }

        /**
         * Method to schedule a timeout for the actor after a specified duration. When the timeout occurs, it sends a custom message of type TimeoutRequest to the actor
         * @param time Time after the timeout occurs 
         */
        private void setTimeoutDequeue(int time) {

            // Schedule a task to be executed once after a specified times
            getContext().system().scheduler().scheduleOnce(
                    Duration.create(time, TimeUnit.MILLISECONDS),                   // Set the duration
                    getSelf(),
                    new TimeoutDequeue(), 
                    getContext().system().dispatcher(), getSelf()
            );
        }

        /**
         * Method used to handle the timeout event for dequeuing requests. 
         * @param msg instance of TimeoutDequeue message
         */
        public void onTimeoutDequeue(TimeoutDequeue msg) {
            
            // If the requests queue is not empty
            while (!this.requestQueue.isEmpty()) {

                // Remove the request from the queue
                Request r = requestQueue.remove();

                // Add the request to active requests
                activeRequests.add(r);

                // Start the request
                startRequest(r);
            }

            // Set the timeout for dequeue
            setTimeoutDequeue(TIMEOUT_DEQUEUE);
        }


        /**
         * Method to handle timeout events related to join and update requests
         * @param msg instance of TimeoutRequest message
         */
        public void onTimeout(TimeoutRequest msg) {
            
            // Initialize the request
            Request request = null;

            // Check if the request is in the active requests
            for (Request r : this.activeRequests) {
                int i = r.getID();
                if (i == msg.id_request) {
                    request = r;
                    break;
                }
            }

            // If the request is null
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

            // If the request is null
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

            // If the request is not null
            if (request != null) {
                
                // If it is a read request
                if (request.getType() == RequestType.Read) {

                    // If the number of resposes is smaller than the read quorum
                    if (request.getnResponses() < read_quorum) {

                        // Remove the request from the active requests
                        activeRequests.remove(request);
                        // Remove the request from the request queue
                        requestQueue.remove(request);
                        // Remove the request from the pending requests
                        pendingRequests.remove(request);
                        
                        // Send an error to the client
                        request.getClient().tell(new ErrorMsg("Your Read request " + msg.id_request + " took too much to be satisfied"), getSelf());
                    }
                }

                // If it is an update request
                else {

                    // If the number of resposes is smaller than the write quorum
                    if (request.getnResponses() < write_quorum || request.getOkResponses() < write_quorum) {

                        // Remove the request from the active requests
                        activeRequests.remove(request);
                        // Remove the request from the request queue
                        requestQueue.remove(request);
                        // Remove the request from the pending requests
                        pendingRequests.remove(request);
                        
                        // Send an error to the client
                        request.getClient().tell(new ErrorMsg("Your Update request " + msg.id_request +  " took too much to be satisfied"), getSelf());
                    }
                }
            }
        }
        
        /**
         * Method to handle timeout events related to external requests
         * @param msg instance of TimeoutExternlRequest message
         */
        public void onTimeout(TimeoutExternalRequest msg) {
            
            // If it is a join request
            if (currExternalRequest != null && currExternalRequest.getType() == ExternalRequestType.Join && currExternalRequest.getID() == msg.externalRequestId) {

                // Get the client
                ActorRef client = currExternalRequest.getClient();
                currExternalRequest = null;

                // Send an error to the client
                client.tell(new ErrorMsg("Error message for your Join Request - Joining node ID" + this.getID() + "Your join request took too much to be satisfied"), getSelf());
            }

            // If it is a recovery request
            else if (currExternalRequest != null && currExternalRequest.getType() == ExternalRequestType.Recovery && currExternalRequest.getID() == msg.externalRequestId) {
                
                // Get the client
                ActorRef client = currExternalRequest.getClient();
                currExternalRequest = null;

                // Send an error to the client
                client.tell(new ErrorMsg("Error message for your Recovery Request - Joining node ID" + this.getID() + "Your join request took too much to be satisfied"), getSelf());
            }
        }


        /**
         * Method to handle join requests by checking whether the joining node's ID is already taken.
         * @param msg instance of JoinRequest message
         */
        public void onJoinRequestMsg(JoinRequestMsg msg) {

            // If the node has crashed, do nothing
            if(this.hasCrashed){ return; }

            // Set the variable to false
            boolean alreadyTaken = false;

            // For every peer in the ring
            for(Peer peer : peers){
               
                // If the ID of the peer is equal to the ID of the joining node
                if(peer.getID() == msg.joiningPeer.getID()) {

                    // Send an error message because the ID is already taken
                    getSender().tell(new ErrorMsg("Error message for your Join Request - Joining node ID" + msg.joiningPeer.getID() + ". This ID is already taken"), getSelf());
                    // Set the variable to true
                    alreadyTaken = true;
                    break;
                }
            }
            
            // If the ID is not already taken
            if(!alreadyTaken){
                
                // Create an external request with type::Join
                ExternalRequest request = new JoinRequest(getSender());

                System.out.println("BN; Join requested to node " + this.id);

                // Send peer list to the joining node
                msg.joiningPeer.getActor().tell(new SendPeerListMsg(Collections.unmodifiableList(new ArrayList<>(this.peers)), request), getSelf());
            }
        }

        /**
         * Method to update the list of peers, identify the clockwise neighbor, schedule a timeout for the joining process, 
         * and send a message to the neighbor node to retrieve a list of items
         * @param msg instance of SendPeerList message
         */
        public void onSendPeerListMsg(SendPeerListMsg msg) {

            // If the node has crashed, do nothing
            if(this.hasCrashed){ return; } 

            // Set the external request to the message request
            currExternalRequest = msg.request;
            // Add myself to the list of peer
            List<Peer> updatedGroup = addPeer(new Peer(id, getSelf()), msg.group);

            // Update the group
            setGroup(updatedGroup);

            // Find the clockwise neighbor node
            int indexClockwiseNode = getIndexOfFirstNode(this.id) + 1;
            if (indexClockwiseNode == peers.size()) {
                indexClockwiseNode = 0;
            }

            // Set the neighbor to the clockwise node
            Peer neighbor = peers.get(indexClockwiseNode);
            System.out.println("JN; Got SendPeerList message. Found clockwise neighbor: " + neighbor.getID());

            // Set the timeout
            setTimeoutExternalRequest(TIMEOUT_JOIN, currExternalRequest.getID());

            // Send message to the clockwise node in order to retrieve the list of items
            neighbor.getActor().tell(new GetNeighborItemsMsg(), getSelf());

        }

        /**
         * Method used to handle a request from a joining node to retrieve items
         * @param msg instance of GetNeighborItems message
         */
        public void onGetNeighborItemsMsg(GetNeighborItemsMsg msg){

            // If the node has crashed, do nothing
            if(this.hasCrashed){ return; }

            // Send message to the joining node with the items list
            System.out.println("NN; Received request to retrieve items");
            getSender().tell(new SendItemsListMsg(storage), getSelf());
        }
        
        /**
         * Method used to processe a message containing a list of items, add these items to the local storage, 
         * and initiate read requests for each item. 
         * @param msg instance of SendItemsList message
         */
        public void onSendItemsListMsg(SendItemsListMsg msg) {

            // If the node has crashed, do nothing
            if(this.hasCrashed){ return; } 

            // Set the storage of the joining node
            this.storage = msg.items;
            System.out.println("JN; Inserted items in storage, requesting read on each item");

            // If the storage is empty
            if (storage.size() == 0) {

                // Set the external request to null
                currExternalRequest = null;

                // For every peer in the ring
                for (Peer p : peers) {

                    // Send an announce message
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

        /**
         * Method to process messages containing returned values (items) and update the local storage with the most up-to-date versions of items.
         * @param msg instance of ReturnValue message
         */
        public void onReturnValueMsg(ReturnValueMsg msg) {

            // If the node has crashed, do nothing
            if(this.hasCrashed){ return; } 

            // Increment the number of responses
            currExternalRequest.incrementnResponses();

            //System.out.println("Received return message for key " + msg.item.getKey() + ", value: " + msg.item.getValue() + ", version: " + msg.item.getVersion());
            
            // If the storage contains that key
            if(storage.containsKey(msg.item.getKey())){

                // If the version is not updated
                if(msg.item.getVersion() >= storage.get(msg.item.getKey()).getVersion()) {

                    // Change the version
                    storage.put(msg.item.getKey(), msg.item);
                }
            
            // If the storage do not contain that key
            } else {

                // Put the item in the storgae
                storage.put(msg.item.getKey(), msg.item);
            }

            // If the number of responses is equal to the storage size
            if(currExternalRequest.getnResponses() == storage.size()) {

                // If it is a readJoin request
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

                    // Send announceJoiningNodeMsg to all peers
                    for (Peer peer : peers) {
                        peer.getActor().tell(new AnnounceJoiningNodeMsg(this.getID(), items), getSelf());
                    }
                }

                // If it is a readRecovery request
                else if (msg.requestType == RequestType.ReadRecovery) {
                    currExternalRequest = null;
                    printNode();
                }
            }
        }

        /**
         * Method to processe messages announcing the joining of a new node into the distributed system
         * @param msg instance of AnnounceJoiningNode message 
         */
        public void onAnnounceJoiningNodeMsg(AnnounceJoiningNodeMsg msg) {
            
            // If the node has crashed, do nothing
            if(this.hasCrashed){ return; }

            // Update the list of peers
            if (this.id != msg.joiningNodeKey) {
                addPeer(new Peer(msg.joiningNodeKey, getSender()));
            }

            // Create the new storage
            Hashtable<Integer, Item> newStorage = new Hashtable<>();
            Enumeration<Integer> e = storage.keys();

            // For all the keys
            while (e.hasMoreElements()) {

                int key = e.nextElement();
                int index = getIndexOfFirstNode(key);

                // If the node is responsible for that item
                if (isNodeResponsibleForItem(index)) {

                    // Insert the value and the key in the storage
                    newStorage.put(key, storage.get(key));
                }
            }

            this.storage = newStorage;
            printNode();
        }

        
        /**
         * Method used to announce a node leaving, transfer the items it was responsible for to the new appropriate nodes, 
         * set the currExternalRequest to null, and notify all other nodes about its departure. 
         * @param msg instance of LeaveRequest message
         */
        public void onLeaveRequestMsg(LeaveRequestMsg msg) {

            // If the node has crashed, do nothing
            if(this.hasCrashed){ return; }

            Enumeration<Integer> e = storage.keys();

            // Take each item in storage and tell the new node responsible for them to insert it in its storage
            while (e.hasMoreElements()) {

                int key = e.nextElement();
                Item i = storage.get(key);
                int indexOfLastNode = getIndexOfLastNode(key);
                int newIndexOfLastnode = (indexOfLastNode + 1) % peers.size();
                Peer newLastPeer = peers.get(newIndexOfLastnode);

                System.out.println("Node " + peers.get(newIndexOfLastnode).getID() + " is now responsible for key " + i.getKey());

                newLastPeer.getActor().tell(new AddItemToStorageMsg(i), getSelf());
            }

            // Find index of leaving node
            int leavingNodeIndex = 0;

            for(int i = 0; i < peers.size(); i++) {

                Peer p = peers.get(i);

                if (p.getID() == this.id) {

                    leavingNodeIndex = i;
                    break;
                }
            }

            currExternalRequest = null; 

            // For every peer send an announce leaving message
            for (Peer p : peers) {

                p.getActor().tell(new AnnounceLeavingNodeMsg(leavingNodeIndex), getSelf());
            }
        }

        /**
         * Method used to add an item to the store
         * @param msg instance AddItemToStorage message
         */
        public void onAddItemToStorageMsg(AddItemToStorageMsg msg) {

            // If the node has crashed, do nothing
            if(this.hasCrashed){ return; }
            
            // Insert the item in the storage
            storage.put(msg.item.getKey(), msg.item);
        }

        /**
         * Method to handle the case where a node announces its departure from the network, 
         * and it updates the list of peers for the current node by removing the leaving node from the list.
         * @param msg instance of AnnounceLeavingNode message
         */
        public void onAnnounceLeavingNodeMsg(AnnounceLeavingNodeMsg msg) {

            // If the node has crashed, do nothing
            if(this.hasCrashed){ return; }

            //System.out.println("Node " + this.id + " has received announcement of node leaving");

            // Remove the leaving node
            boolean isMe = (peers.get(msg.leavingNodeIndex).getID() == this.id);
            peers.remove(msg.leavingNodeIndex);
            //System.out.println("Node " + this.id + "'s peers: " + this.printPeers());

            // Print the node
            if(!isMe) {
                printNode();
            }
        }

        /**
         * Method to set the node that has crashed
         * @param msg instance of CrashRequest message
         */
        public void onCrashRequestMsg(CrashRequestMsg msg) {
            
            // Set crash to true
            this.hasCrashed = true;

            System.out.println("Node " + this.getID() + " has crashed");
        }

        /**
         * Method to handle recovery requests, either by sending an error response if the node is not in a crashed state 
         * or by transitioning the node from a crashed state to a normal state and initiating the recovery process
         * @param msg instance of RecoveryRequest message
         */
        public void onRecoveryRequestMsg(RecoveryRequestMsg msg) {

            // Check if the node has really crashed
            if(!hasCrashed){ 

                // Send an error to the client
                getSender().tell(new ErrorMsg("Error message for your Recovery Request for node "  + this.id + ". This node is not crashed"), getSelf());
                return;
            }

            // Set crash to false
            hasCrashed = false;

            // Crete a new request for recovery
            ExternalRequest request = new RecoveryRequest(getSender());
            this.currExternalRequest = request;

            // Set the timeout for the request
            setTimeoutExternalRequest(TIMEOUT_RECOVERY, request.getID());

            // Send a message to retrieve the peer list
            msg.nodeToContact.tell(new GetPeerListMsg(), getSelf());
        }

        /**
         * Method used to send the peer list message
         * @param msg instance of GetPeerList message
         */
        public void onGetPeerListMsg(GetPeerListMsg msg) {

            // Send a message to retrieve the peer list
            getSender().tell(new SendPeerListRecoveryMsg(Collections.unmodifiableList(new ArrayList<>(this.peers))), getSelf());
        }

        /**
         * Method to update the node's group, storage, and initiate the retrieval of items it should be responsible for from its anti-clockwise neighbor
         * @param msg instance of SendPeerListRecovery message
         */
        public void onSendPeerListRecoveryMsg(SendPeerListRecoveryMsg msg) {

            // Set the group
            setGroup(msg.group);
            System.out.println("Recovering node updated peers: " + printPeers());

            Enumeration<Integer> en = storage.keys();
            Hashtable<Integer, Item> newStorage = new Hashtable<>();
            while(en.hasMoreElements()) {

                // First index of first node for each item of the node
                int key = en.nextElement();
                int indexOfFirstNode = getIndexOfFirstNode(key);

                // Forgets the items it is no longer responsible for
                if (isNodeResponsibleForItem(indexOfFirstNode)) {
                    newStorage.put(key, storage.get(key));
                }
            }

            this.storage = newStorage;
            printNode();

            // Get the anticlockwise neighbor
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
        
        /**
         * Method used to get the list of items
         * @param msg instance of GetItemsList message
         */
        public void onGetItemsListMsg(GetItemsListMsg msg){

            // Send a message to retrieve the list of items
            getSender().tell(new SendItemsListRecoveryMsg(this.storage), getSelf());

        }

        /**
         * Method responsible for updating the node's storage with items received during the recovery process and sending read requests to update the values of these items
         * @param msg instance of SendItemsListRecovery message
         */
        public void onSendItemsListRecoveryMsg(SendItemsListRecoveryMsg msg){

            // If the storage is empty and there are no items in the message
            if (storage.size() == 0 && msg.items.size() == 0) {
                
                currExternalRequest = null;
            }

            Enumeration<Integer> e1 = msg.items.keys();

            // Insert all the item in the storage
            while (e1.hasMoreElements()) {

                int key = e1.nextElement();
                int indexOfFirstNode = getIndexOfFirstNode(key);

                // If the node is responsible for that item
                if (!this.storage.containsKey(key) && isNodeResponsibleForItem(indexOfFirstNode)) {
                    this.storage.put(key, msg.items.get(key));
                }
            }
            //System.out.print("Final items of recovering node BEFORE THE READ: ");
            printNode();

            Enumeration<Integer> en = storage.keys();
            
            while (en.hasMoreElements()) {
                int key = en.nextElement();
                
                // Send a read request to update the value
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