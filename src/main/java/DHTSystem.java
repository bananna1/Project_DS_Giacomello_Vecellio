import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class DHTSystem {
    final static int N_PARTICIPANTS = 6;            // Number of initial peers of the ring
    final static int N_ITEM = 4;                    // Number of initial items of the ring

    /**
     * Method to generate random characters
     * @return random character
     */
    private static char randomChar() {
        Random r = new Random();
        return (char)(r.nextInt(26) + 'A');
    }

    /**
     * @param args
     * @throws InterruptedException
     * @throws NumberFormatException
     * @throws IOException
     */
    public static void main(String[] args) throws InterruptedException, NumberFormatException, IOException {
        // Create the actor system
        final ActorSystem system = ActorSystem.create("DHT_System");

        // Create the group of peers with ID: 10, 20, 30, 40, 50, 60
        ArrayList<Peer> group = new ArrayList<>();
        for (int i=1; i<=N_PARTICIPANTS; i++) {
            Peer p = new Peer(i*10, system.actorOf(Ring.Node.props(i*10), "peer" + i*10));
            group.add(p);
        }
        
        // List of initial keys of the ring
        List<Integer> keys = new ArrayList<>();
        // List of the initial values of the ring
        List<String> values = new ArrayList<>();

        // createStorage(keys, values);                 // Method to create random storage
        
        // Add keys
        keys.add(16);
        keys.add(49);
        keys.add(48);
        keys.add(64);

        // Add values
        values.add("A");
        values.add("B");
        values.add("C");
        values.add("D");

        // Send start messages to the participants to inform them of the group and to create the initial storage
        Ring.StartMessage start = new Ring.StartMessage(group, keys, values);
        
        // Send a start message to every Peer in the ring
        for (Peer peer: group) {
            peer.getActor().tell(start, null);
        }

        // Create the graphic interface
        createInterface(system, group);
    }

    /**
     * Method to create the graphic interface
     * @param system actor system
     * @param group  list of peer
     * @throws InterruptedException
     */
    private static void createInterface(ActorSystem system, ArrayList<Peer> group) throws InterruptedException {

        // Create three clients
        ActorRef client1 = system.actorOf(Client.props(1));
        ActorRef client2 = system.actorOf(Client.props(2));
        ActorRef client3 = system.actorOf(Client.props(3));

        // Information about commands
        System.out.println(">>> Press ENTER to send the command <<<");
        System.out.println(">>> Press 1 to read <<<");
        System.out.println(">>> Press 2 to update <<<");
        System.out.println(">>> Press 3 to join <<<");
        System.out.println(">>> Press 4 to leave <<<");
        System.out.println(">>> Press 5 to crash <<<");
        System.out.println(">>> Press 6 to recover <<<");
        System.out.println(">>> Press 7 to test sequential consistency <<<");
        System.out.println(">>> Press 8 to test join and leave <<<");
        System.out.println(">>> Press 9 to test crash and recovery <<<");
        System.out.println(">>> Press 10 to test all functionalities together <<<");
        System.out.println(">>> Press ctrl+C to exit <<<");
        
        
        // Reading data using readLine
        while(true){

            // Create the scanner in which insert the commands
            Scanner in = new Scanner(System.in);
            int action = in.nextInt();

            // Peer coordinator for some operations
            int coordinator;
            
            switch (action) {
                case 1:         // Read  

                    // Key of the value to read
                    System.out.println("Insert the key of the value to read");
                    int keyRead = in.nextInt();

                    // Peer to ask the request                                         
                    coordinator = requestCoordinator(group);

                    // Read request
                    group.get(coordinator).getActor().tell(new Ring.GetValueMsg(keyRead), client1);
                    break;

                case 2:         // Update

                    // Key of the value to update or create
                    System.out.println("Insert the key of the value to update or create");
                    int keyUpdate = in.nextInt();

                    // Value to modify for the key
                    System.out.println("Insert the value");
                    String valueUpdate = in.nextLine();

                    // Peer to ask the request 
                    coordinator = requestCoordinator(group);    

                    // Update request                       
                    group.get(coordinator).getActor().tell(new Ring.UpdateValueMsg(keyUpdate, valueUpdate), client3);
                    break;

                case 3:         // Join

                    // ID of the joining node
                    System.out.println("Insert the ID of the joining node");
                    int id = in.nextInt();

                    // Create the new peer with ID
                    Peer p = new Peer(id, system.actorOf(Ring.Node.props(id), "peer" + id));

                    // Bootstrapping node
                    System.out.println("Enter the ID of the bootstrapping node");
                    int idBoot = in.nextInt();

                    int index = getMyIndex(id, group);

                    // Send joining request
                    group.get(idBoot).getActor().tell(new Ring.JoinRequestMsg(p, group.get(idBoot).getActor()), client3);

                    // Add the new peer to the group
                    addPeer(p, group);
                    break;

                case 4:         // Leave

                    // Index of the leaving node
                    System.out.println("Insert the ID of the leaving node");
                    int idLeave = in.nextInt();
                    int indexLeave = getMyIndex(idLeave, group);

                    // Request leaving
                    group.get(indexLeave).getActor().tell(new Ring.LeaveRequestMsg(), client1);
                    break;

                case 5:         // Crash

                    // ID of the crashing node
                    System.out.println("Enter the ID of crashing node");
                    int idCrash = in.nextInt();
                    int indexCrash = getMyIndex(idCrash, group);

                    // Request crash
                    group.get(indexCrash).getActor().tell(new Ring.CrashRequestMsg(), client3);
                    break;

                case 6:         // Recovery

                    // ID of the recovery node
                    System.out.println("Enter the ID of recovering node");
                    int idRecover = in.nextInt();
                    int indexRecover = getMyIndex(idRecover, group);

                    // Request recovery
                    group.get(indexRecover).getActor().tell(new Ring.RecoveryRequestMsg(group.get(requestCoordinator(group)).getActor()), client3);
                    break;

                case 7:        // Test sequential consistency

                    // Initialize class test
                    Test test = new Test(system, group);
                    // Test sequential consistency
                    test.testSequentialConsistency();
                    break;

                case 8:         // Test join and leave

                    // Initialize class test
                    Test test1 = new Test(system, group);
                    // Test sequential join/leave
                    test1.testJoinLeave(system);
                    break;

                case 9:         // Test crash and recovery

                    // Initialize class test
                    Test test2 = new Test(system, group);
                    // Test sequential crash and recovery
                    test2.testCrashRecovery(system);
                    break;

                case 10:
                    Test test_final = new Test(system, group);
                    test_final.completeTest(system);
                    break;
                default:
                    System.out.println("Insert a value from 1 to 7");
                    break;
            }
        }
    }

    /**
     * Method to insert the ID of the node to forward a request
     * @param group list of peers
     * @return index of the node to forward the request
     */
    private static int requestCoordinator(List<Peer> group){

        // Scanner in which insert values
        Scanner in = new Scanner(System.in);
        
        // ID of the node
        System.out.println("Enter the ID of the node to forward the request to");
        int id = in.nextInt();

        // Get the idex based on the ID
        int index = getMyIndex(id, group);

        return index;
    }

    /**
     * Method to retrieve the position of the node from the ID
     * @param id ID of the node
     * @param group list of peers
     * @return position of the node
     */
    private static int getMyIndex(int id, List<Peer> group) {

        int my_index = -1;

        // Cycle in order to find the position of the node with ID
        for (int j = 0; j < group.size(); j++) {
            if (id == group.get(j).getID()) {
                my_index = j;
                break;
            }
        }
        return my_index;
    }

    /**
     * Method to add a new peer to the group in the right position
     * @param newPeer peer to add to the group
     * @param group list of peers
     */
    private static void addPeer(Peer newPeer, List<Peer> group) {


        int newPeerPosition = 0;

        // Cycle for find the position in which insert the new node
        for (int i = 0; i < group.size(); i++) {
            if (newPeer.getID() < group.get(i).getID()) {
                newPeerPosition = i;
                break;
            }
        }
        group.add(newPeerPosition, newPeer);
    }
    
    /**
     * Method to create a random list of keys and values
     * @param keys list of keys
     * @param values list of values
     */
    private static void createStorage(List<Integer> keys, List<String> values){

        // Cycle to create N_ITEM random keys and values
        for (int i=1; i<=N_ITEM; i++) {

            // Create random keys
            int key = ThreadLocalRandom.current().nextInt(0, (N_PARTICIPANTS*10+5)  + 1);
            keys.add(key);
            
            // Create random values
            char value = randomChar();
            values.add(value + "");
        }
    }
}