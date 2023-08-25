import akka.actor.*;
import akka.actor.AbstractActor;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;

import java.util.Collections;


public class Peer {
    private ActorRef actor;
        private int id;

        public Peer(ActorRef actor, int id) {
            this.actor = actor;
            this.id = id;
        }
        public ActorRef getActor() {
            return this.actor;
        }

        public int getID() {
            return this.id;
        }
}
