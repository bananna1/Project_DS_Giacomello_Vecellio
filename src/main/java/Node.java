import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.AbstractActor;
import akka.actor.Cancellable;
import akka.actor.Props;
import scala.concurrent.duration.Duration;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.lang.Thread;
import java.lang.InterruptedException;
import java.util.Collections;

public class Node extends AbstractActor{

    // Start message that sends the list of participants to everyone
    public static class StartMessage implements Serializable {
        public final List<ActorRef> group;
        public StartMessage(List<ActorRef> group) {
              this.group = Collections.unmodifiableList(new ArrayList<>(group));
        }
  }

    private int id;                                                         // Node ID        
    private Hashtable<Integer, String> values = new Hashtable<>();          // list of keys and values
    private List<ActorRef> peers = new ArrayList<>();                       // list of peer banks

    private boolean isCoordinator = false;                                  // the node is the coordinator

    /*-- Actor constructors --------------------------------------------------- */
    public Node(int id, boolean isCoordinator){
        this.id = id; 
        this.isCoordinator = isCoordinator;
    }

    public int getID() {
        return this.id;
    }

    public void removeValue (int key) {
        values.remove(key);
    }

    public void addValue (int key, String value) {
        values.put(key, value);
    }

    void setGroup(StartMessage sm) {
        peers = new ArrayList<>();
        for (ActorRef b: sm.group) {
          if (!b.equals(getSelf())) {
  
            // copying all participant refs except for self
            this.peers.add(b);
          }
        }
        //print("starting with " + sm.group.size() + " peer(s)");
      }

    @SuppressWarnings("unchecked")
    @Override
    public Receive createReceive() {
        return null;
    
    }
}

