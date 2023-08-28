import java.util.ArrayList;
import java.util.List;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class DHTSystem {
    final static int N_PARTICIPANTS = 6;

    public static void main(String[] args) {
        // Create the actor system
        final ActorSystem system = ActorSystem.create("DHT System Initialization");

        // Create participants
        List<ActorRef> group = new ArrayList<>();
        for (int i=0; i<N_PARTICIPANTS; i++) {
            group.add(system.actorOf(Peer.props(i), "peer" + i));
        }

    }
}
