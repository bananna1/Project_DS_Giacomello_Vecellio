import java.util.ArrayList;

public abstract class Test {
    List<Peer> group = new ArrayList<>();
    ActorRef client1 = system.actorOf(Client.props(1));
    ActorRef client2 = system.actorOf(Client.props(2));
    ActorRef client3 = system.actorOf(Client.props(3));

    public Test (List<Peer> group) {
        this.group = group;
    }

    public testSequentialConsistency(){
        System.out.println("TEST 1: TWO READS REQUEST TO DIFFERENT COORDINATORS BY DIFFERENT CLIENTS");
        group.get(2).getActor().tell(new Ring.GetValueMsg(key_prova_1), client1);
        group.get(1).getActor().tell(new Ring.GetValueMsg(key_prova_1), client3);
        System.out.println("TEST 1 COMPLETED");

        System.out.println("TEST 2: TWO READS REQUEST TO DIFFERENT COORDINATORS BY DIFFERENT CLIENTS");
        group.get(2).getActor().tell(new Ring.GetValueMsg(key_prova_1), client1);
        group.get(1).getActor().tell(new Ring.GetValueMsg(key_prova_1), client3);
        System.out.println("TEST 2 COMPLETED");
    }

    public testCrashRecovery(){

    }
}
