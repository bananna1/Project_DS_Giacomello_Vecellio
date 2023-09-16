import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import java.util.Random;
import java.util.Scanner;


public class DHTSystem {
    final static int N_PARTICIPANTS = 6;
    final static int N_ITEM = 4;

    private static char randomChar() {
        Random r = new Random();
        return (char)(r.nextInt(26) + 'A');
    }

    public static void main(String[] args) throws InterruptedException, NumberFormatException, IOException {
        // Create the actor system
        final ActorSystem system = ActorSystem.create("DHT_System");

        // Create the nodes
        ArrayList<Peer> group = new ArrayList<>();
        for (int i=1; i<=N_PARTICIPANTS; i++) {
            Peer p = new Peer(i*10, system.actorOf(Ring.Node.props(i*10), "peer" + i*10));
            group.add(p);
        }

        List<Integer> keys = new ArrayList<>();
        List<String> values = new ArrayList<>();

        // Create the initial key-value storage
        int key_prova_1 = 0;
        int key_prova_2 = 0;

        /*
        for (int i=1; i<=N_ITEM; i++) {

            int key = ThreadLocalRandom.current().nextInt(0, (N_PARTICIPANTS*10+5)  + 1);
            keys.add(key);
            if (i == 1) {
                key_prova_1 = key;
            }
            if (i == 2) {
                key_prova_2 = key;
            }
            char value = randomChar();
            values.add(value + "");
        }
         */
        keys.add(16);
        keys.add(49);
        keys.add(48);
        keys.add(64);


        values.add("A");
        values.add("B");
        values.add("C");
        values.add("D");

        // Send start messages to the participants to inform them of the group and to create the initial storage
        Ring.StartMessage start = new Ring.StartMessage(group, keys, values);

        for (Peer peer: group) {
            peer.getActor().tell(start, null);
        }

        ActorRef client1 = system.actorOf(Client.props(1));
        ActorRef client2 = system.actorOf(Client.props(2));
        ActorRef client3 = system.actorOf(Client.props(3));

        //System.out.println("Client 3 richiede chiave " + key_prova_1);
        //System.out.println("Client 2 richiede chiave " + key_prova_1);
        //System.out.println("Client 3 richiede chiave " + key_prova_2);
        //System.out.println("Client 3 richiede update chiave " + key_prova_1 + " con il valore CACCA");
        //System.out.println("Client 3 richiede chiave " + key_prova_1);

        //group.get(2).getActor().tell(new Ring.GetValueMsg(key_prova_1), client1);
        //group.get(1).getActor().tell(new Ring.GetValueMsg(key_prova_1), client3);
        //group.get(0).getActor().tell(new Ring.UpdateValueMsg(key_prova_1, "CACCA 1"), client3);
        //group.get(2).getActor().tell(new Ring.UpdateValueMsg(key_prova_2, "CACCA 2"), client1);
        //group.get(4).getActor().tell(new Ring.GetValueMsg(key_prova_1), client3);

        //Peer p = new Peer(15, system.actorOf(Ring.Node.props(15), "peer" + 15));
        //group.get(2).getActor().tell(new Ring.JoinRequestMsg(p, group.get(2).getActor()), client3);
        //group.get(2).getActor().tell(new Ring.LeaveRequestMsg(), client1);
        /*
        group.get(5).getActor().tell(new Ring.CrashRequestMsg(), client3);
        Thread.sleep(2000);
        group.get(1).getActor().tell(new Ring.RecoveryRequestMsg(group.get(0).getActor()), client3);
        group.get(5).getActor().tell(new Ring.RecoveryRequestMsg(group.get(0).getActor()), client3);
         */
        //group.get(1).getActor().tell(new Ring.UpdateValueMsg(27, "ciao"), client1);
        
        System.out.println(">>> Press ENTER send the command <<<");
        System.out.println(">>> Press 1 to read <<<");
        System.out.println(">>> Press 2 to update <<<");
        System.out.println(">>> Press 3 to join <<<");
        System.out.println(">>> Press 4 to leave <<<");
        System.out.println(">>> Press 5 to crash <<<");
        System.out.println(">>> Press 6 to recover <<<");
        System.out.println(">>> Press 7 to exit <<<");
        
        
        //BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // Reading data using readLine
        while(true){
            Scanner in = new Scanner(System.in);
            int action = in.nextInt();
            System.out.println(action);
            int key;
            String value;
            
            switch (action) {
                case 1:
                    group.get(2).getActor().tell(new Ring.GetValueMsg(requestKey()), client1);
                    break;
                case 2:                                
                    group.get(0).getActor().tell(new Ring.UpdateValueMsg(requestKey(), requestValue()), client3);
                    break;
                case 3:
                    System.out.println("Insert the ID of the joining node");
                    int id = in.nextInt();
                    Peer p = new Peer(id, system.actorOf(Ring.Node.props(id), "peer" + id));
                    group.get(2).getActor().tell(new Ring.JoinRequestMsg(p, group.get(2).getActor()), client3);
                    break;
                case 4:
                    group.get(requestID()).getActor().tell(new Ring.LeaveRequestMsg(), client1);
                    break;
                case 5:
                    group.get(requestID()).getActor().tell(new Ring.CrashRequestMsg(), client3);
                    break;
                case 6: 
                    group.get(1).getActor().tell(new Ring.RecoveryRequestMsg(group.get(requestID()).getActor()), client3);
                    break;
                case 7:
                    system.terminate();
                    break;
                default:
                    System.out.println("Insert a value from 1 to 7");
                    break;
           

            }

        }
            

    }

    private static int requestKey() {
        Scanner in = new Scanner(System.in);
        
        System.out.println("Insert the key of the value to read");
        return in.nextInt();
    }

    private static String requestValue() {
        Scanner in = new Scanner(System.in);
        
        System.out.println("Insert the value");
        return in.nextLine();
    }
    
    private static int requestID(){
        Scanner in = new Scanner(System.in);
        
        System.out.println("Insert the ID of the leaving node");
        int id = in.nextInt();

        while (id < 0 || id >= N_PARTICIPANTS){
            System.out.println("Insert the ID of the leaving node");
            id = in.nextInt();
        }

        return id;
    }
}
