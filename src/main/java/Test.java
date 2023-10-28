import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Test {
    // Group of Peer in the ring
    List<Peer> group = new ArrayList<>();

    // Clients
    ActorRef client1;
    ActorRef client2;
    ActorRef client3;

    /**
     * @param system
     * @param group
     */
    public Test (ActorSystem system, List<Peer> group) {
        this.group = group;
        client1 = system.actorOf(Client.props(1));
        client2 = system.actorOf(Client.props(2));
        client3 = system.actorOf(Client.props(3));
    }

    /**
     * Void method to test sequential consistency
     */
    public void testSequentialConsistency(){
        int key_prova_1 = 16;
        int key_prova_2 = 49;

        System.out.println("TEST 1: TWO READS REQUEST TO DIFFERENT COORDINATORS BY DIFFERENT CLIENTS");
        group.get(2).getActor().tell(new Ring.GetValueMsg(key_prova_1), client1);
        group.get(1).getActor().tell(new Ring.GetValueMsg(key_prova_1), client3);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("TEST 1 COMPLETED");

        System.out.println("-----------------------------------------------------");

        System.out.println("TEST 2: TWO READS REQUEST TO THE SAME COORDINATOR BY DIFFERENT CLIENTS");
        group.get(2).getActor().tell(new Ring.GetValueMsg(key_prova_1), client1);
        group.get(2).getActor().tell(new Ring.GetValueMsg(key_prova_2), client3);
         try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("TEST 2 COMPLETED");

        System.out.println("-----------------------------------------------------");

        System.out.println("TEST 3: TWO DIFFERENT WRITES REQUEST TO DIFFERENT COORDINATOR BY DIFFERENT CLIENTS");
        group.get(0).getActor().tell(new Ring.UpdateValueMsg(key_prova_1, "TEST 3"), client3);
        group.get(1).getActor().tell(new Ring.UpdateValueMsg(key_prova_2, "TEST 3"), client1);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("TEST 3 COMPLETED");

        System.out.println("-----------------------------------------------------");

        System.out.println("TEST 4: THE SAME WRITE REQUEST TO DIFFERENT COORDINATOR BY DIFFERENT CLIENTS");
        group.get(0).getActor().tell(new Ring.UpdateValueMsg(key_prova_1, "TEST 4 A"), client3);
        group.get(1).getActor().tell(new Ring.UpdateValueMsg(key_prova_1, "TEST 4 B"), client1);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("TEST 4 COMPLETED");

        System.out.println("-----------------------------------------------------");

        System.out.println("TEST 5: DIFFERENT WRITES REQUEST TO THE SAME COORDINATOR BY DIFFERENT CLIENTS");
        group.get(0).getActor().tell(new Ring.UpdateValueMsg(key_prova_1, "TEST 5"), client3);
        group.get(0).getActor().tell(new Ring.UpdateValueMsg(key_prova_2, "TEST 5"), client1);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("TEST 5 COMPLETED");

        System.out.println("-----------------------------------------------------");

        System.out.println("TEST 6: READ AND WRITE REQUEST WITH DIFFERENT KEYS TO DIFFERENT COORDINATOR BY DIFFERENT CLIENTS");
        group.get(2).getActor().tell(new Ring.GetValueMsg(key_prova_1), client1);
        group.get(0).getActor().tell(new Ring.UpdateValueMsg(key_prova_2, "TEST 6"), client1);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("TEST 6 COMPLETED");

        System.out.println("-----------------------------------------------------");

        System.out.println("TEST 7: READ AND WRITE REQUEST WITH THE SAME KEYS TO DIFFERENT COORDINATOR BY DIFFERENT CLIENTS");
        group.get(2).getActor().tell(new Ring.GetValueMsg(key_prova_1), client1);
        group.get(0).getActor().tell(new Ring.UpdateValueMsg(key_prova_1, "TEST 7"), client1);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("TEST 7 COMPLETED");

        System.out.println("-----------------------------------------------------");

        System.out.println("TEST 8: READ AND WRITE REQUEST WITH THE SAME KEYS (NEW) TO DIFFERENT COORDINATOR BY DIFFERENT CLIENTS");
        group.get(0).getActor().tell(new Ring.UpdateValueMsg(2, "TEST 8 A"), client1);
        group.get(2).getActor().tell(new Ring.GetValueMsg(2), client2);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("TEST 8 COMPLETED");

        System.out.println("-----------------------------------------------------");

        System.out.println("TEST 9: TWO WRITES WITH A NEW SAME KEY TO DIFFERENT COORDINATOR BY DIFFERENT CLIENTS");
        group.get(1).getActor().tell(new Ring.UpdateValueMsg(1, "TEST 9 A"), client2);
        group.get(0).getActor().tell(new Ring.UpdateValueMsg(1, "TEST 9 B"), client1);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("TEST 9 COMPLETED");

        System.out.println("-----------------------------------------------------");

        System.out.println("TEST 10: MULTIPLE READS AND WRITES");
        group.get(2).getActor().tell(new Ring.GetValueMsg(key_prova_1), client1);
        group.get(0).getActor().tell(new Ring.UpdateValueMsg(key_prova_1, "TEST 10 A"), client3);
        group.get(2).getActor().tell(new Ring.GetValueMsg(key_prova_1), client1);
        group.get(3).getActor().tell(new Ring.UpdateValueMsg(key_prova_2, "TEST 10 B"), client1);
        group.get(3).getActor().tell(new Ring.UpdateValueMsg(key_prova_2, "TEST 10 C"), client2);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("TEST 10 COMPLETED");
        
    }
    
    /**
     * Void method to test crash and recovery operations
     */
    public void testCrashRecovery(){
        //Peer p = new Peer(15, system.actorOf(Ring.Node.props(15), "peer" + 15));
        //group.get(2).getActor().tell(new Ring.JoinRequestMsg(p, group.get(2).getActor()), client3);
        //group.get(2).getActor().tell(new Ring.LeaveRequestMsg(), client1);
        /*
        group.get(5).getActor().tell(new Ring.CrashRequestMsg(), client3);
        Thread.sleep(2000);
        group.get(1).getActor().tell(new Ring.RecoveryRequestMsg(group.get(0).getActor()), client3);
        group.get(5).getActor().tell(new Ring.RecoveryRequestMsg(group.get(0).getActor()), client3);
         */
        //group.get(1).getActor().tell(new Ring.UpdateValueMsg(27, "ciao"), client1);
    }
}
