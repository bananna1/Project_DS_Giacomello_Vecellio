import akka.actor.*;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;

import java.util.Collections;


public class Peer extends Node{
        private ActorRef actor;
        private int id;
        
        public Peer(int id) {
            super(id);
        }

        public ActorRef getActor() {
            return this.actor;
        }

        public int getID() {
            return this.id;
        }

        static public Props props(int id) {
            return Props.create(Peer.class, () -> new Peer(id));
          }
}
