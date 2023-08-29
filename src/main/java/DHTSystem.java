import java.util.ArrayList;
import java.util.List;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.util.concurrent.ThreadLocalRandom;


public class DHTSystem {
    final static int N_PARTICIPANTS = 6;
    final static int N_ITEM = 10;

    public static void main(String[] args) {
        // Create the actor system
        final ActorSystem system = ActorSystem.create("DHT System Initialization");

        // Create the nodes
        List<ActorRef> group = new ArrayList<>();
        for (int i=1; i<=N_PARTICIPANTS; i++) {
            group.add(system.actorOf(Ring.Node.props(i*10), "peer" + i*10));
        }

        // Create the initial key-value storage
        for(int i=1; i<=N_ITEM; i++){
            int key = ThreadLocalRandom.current().nextInt(0, (N_PARTICIPANTS*10+5)  + 1);
            
            for (ActorRef peer: group) {
                
            }
            
            


        }

    }
}
