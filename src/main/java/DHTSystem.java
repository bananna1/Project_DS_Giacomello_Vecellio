import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import java.util.Random;

import java.util.concurrent.ThreadLocalRandom;


public class DHTSystem {
    final static int N_PARTICIPANTS = 6;
    final static int N_ITEM = 4;

    private static char randomChar() {
        Random r = new Random();
        return (char)(r.nextInt(26) + 'A');
    }

    public static void main(String[] args) {
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
        try {
            System.out.println(">>> Press ENTER to exit <<<");
            System.in.read();
        } 
        catch (IOException ignored) {}
        system.terminate();

    }
}
