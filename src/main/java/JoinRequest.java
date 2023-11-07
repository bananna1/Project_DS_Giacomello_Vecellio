import akka.actor.ActorRef;

public class JoinRequest extends ExternalRequest {

    private boolean joinConcluded = false;                          // If the join operation is concluded
    private boolean timeoutJoin = false;                            // If the timout is triggered

    /**
     * Constructor of the join request
     * @param client Client that request the join
     */
    public JoinRequest(ActorRef client) {
        super(Ring.Node.ExternalRequestType.Join, client);
    }

    /**
     * Method to set that the join is concluded
     */
    public void concludeJoin() {
        joinConcluded = true;
    }

    /**
     * Method used to see if the join is concluded or not
     * @return If the join operation is concluded
     */
    public boolean isJoinConcluded() {
        return joinConcluded;
    }

    /**
     * Method to see if the timeout is triggered
     */
    public void setTimeoutJoin() {
        timeoutJoin = true;
    }

    /**
     * Method to see if the timeout is triggered or not
     * @return If the timeout is triggered
     */
    public boolean isTimeoutJoin() {
        return timeoutJoin;
    }
}
