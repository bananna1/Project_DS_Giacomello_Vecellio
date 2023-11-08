import akka.actor.*;

public class Peer {
        private ActorRef actor;                 // Actor for the peer
        private int id;                         // ID of the peer
        
        /**
         * Constructor for the Peer
         * @param id ID of the peer
         * @param actor Actor for the peer
         */
        public Peer(int id, ActorRef actor) {
            this.id = id;
            this.actor = actor;
        }

        /**
         * Method to get the actor for a peer
         * @return Actor for the peer
         */
        public ActorRef getActor() {
            return this.actor;
        }

        /**
         * Method to get the ID of the peer
         * @return ID of the peer
         */
        public int getID() {
            return this.id;
        }
}
