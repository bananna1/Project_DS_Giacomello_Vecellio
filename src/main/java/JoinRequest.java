import akka.actor.ActorRef;

public class JoinRequest extends ExternalRequest {
    private boolean joinConcluded = false;
    private boolean timeoutJoin = false;

    public JoinRequest(ActorRef client) {
        super(Ring.Node.ExternalRequestType.Join, client);
    }
    public void concludeJoin() {
        joinConcluded = true;
    }
    public boolean isJoinConcluded() {
        return joinConcluded;
    }
    public void setTimeoutJoin() {
        timeoutJoin = true;
    }
    public boolean isTimeoutJoin() {
        return timeoutJoin;
    }
}
