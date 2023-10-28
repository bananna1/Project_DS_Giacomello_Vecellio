import akka.actor.*;

public class Peer {
        private ActorRef actor;
        private int id;
        
        public Peer(int id, ActorRef actor) {
            this.id = id;
            this.actor = actor;
        }

        public ActorRef getActor() {
            return this.actor;
        }

        public int getID() {
            return this.id;
        }
}
