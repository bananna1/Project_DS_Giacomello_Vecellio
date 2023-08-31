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
        List<Peer> group = new ArrayList<>();
        for (int i=1; i<=N_PARTICIPANTS; i++) {
            Peer p = new Peer(i*10, system.actorOf(Ring.Node.props(i*10), "peer" + i*10));
            group.add(p);
        }

        List<Integer> keys = new ArrayList<>();
        List<String> values = new ArrayList<>();

        // Create the initial key-value storage
        for(int i=1; i<=N_ITEM; i++){

            int key = ThreadLocalRandom.current().nextInt(0, (N_PARTICIPANTS*10+5)  + 1);
            keys.add(key);

            char value = randomChar();
            values.add(value + "");
        }

        // Send start messages to the participants to inform them of the group and to create the initial storage
        Ring.StartMessage start = new Ring.StartMessage(group, keys, values);
        for (Peer peer: group) {
            
            peer.getActor().tell(start, null);
        }
        System.out.println("CIAOOOOOOOOOO");



        try {
            System.out.println(">>> Press ENTER to exit <<<");
            System.in.read();
        } 
        catch (IOException ignored) {}
        system.terminate();

    }
}
