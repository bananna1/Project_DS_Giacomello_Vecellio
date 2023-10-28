import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import java.util.concurrent.TimeUnit;

public class Test {
    List<Peer> group = new ArrayList<>();
    ActorRef client1;
    ActorRef client2;
    ActorRef client3;

    public Test (ActorSystem system, List<Peer> group) {
        this.group = group;
        client1 = system.actorOf(Client.props(1));
        client2 = system.actorOf(Client.props(2));
        client3 = system.actorOf(Client.props(3));
    }

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
        group.get(0).getActor().tell(new Ring.UpdateValueMsg(key_prova_1, "change1"), client3);
        group.get(1).getActor().tell(new Ring.UpdateValueMsg(key_prova_2, "change1"), client1);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("TEST 3 COMPLETED");

        System.out.println("-----------------------------------------------------");

        System.out.println("TEST 4: THE SAME WRITE REQUEST TO DIFFERENT COORDINATOR BY DIFFERENT CLIENTS");
        group.get(0).getActor().tell(new Ring.UpdateValueMsg(key_prova_1, "secondA"), client3);
        group.get(1).getActor().tell(new Ring.UpdateValueMsg(key_prova_1, "secondB"), client1);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("TEST 4 COMPLETED");

        System.out.println("-----------------------------------------------------");

        System.out.println("TEST 4: DIFFERENT WRITES REQUEST TO THE SAME COORDINATOR BY DIFFERENT CLIENTS");
        group.get(0).getActor().tell(new Ring.UpdateValueMsg(key_prova_1, "change1"), client3);
        group.get(0).getActor().tell(new Ring.UpdateValueMsg(key_prova_2, "change1"), client1);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("TEST 4 COMPLETED");

        System.out.println("-----------------------------------------------------");

        System.out.println("TEST 5: READ AND WRITE REQUEST WITH DIFFERENT KEYS TO DIFFERENT COORDINATOR BY DIFFERENT CLIENTS");
        group.get(2).getActor().tell(new Ring.GetValueMsg(key_prova_1), client1);
        group.get(0).getActor().tell(new Ring.UpdateValueMsg(key_prova_2, "change1"), client1);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("TEST 5 COMPLETED");

        System.out.println("-----------------------------------------------------");

        System.out.println("TEST 6: READ AND WRITE REQUEST WITH THE SAME KEYS TO DIFFERENT COORDINATOR BY DIFFERENT CLIENTS");
        group.get(2).getActor().tell(new Ring.GetValueMsg(key_prova_1), client1);
        group.get(0).getActor().tell(new Ring.UpdateValueMsg(key_prova_1, "change61"), client1);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("TEST 6 COMPLETED");

        System.out.println("-----------------------------------------------------");

        System.out.println("TEST 7: TWO WRITES WITH A NEW SAME KEY TO DIFFERENT COORDINATOR BY DIFFERENT CLIENTS");
        group.get(1).getActor().tell(new Ring.UpdateValueMsg(1, "A"), client2);
        group.get(0).getActor().tell(new Ring.UpdateValueMsg(1, "B"), client1);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("TEST 7 COMPLETED");

        System.out.println("-----------------------------------------------------");

        System.out.println("TEST 8: MULTIPLE READS AND WRITES");
        group.get(2).getActor().tell(new Ring.GetValueMsg(key_prova_1), client1);
        group.get(0).getActor().tell(new Ring.UpdateValueMsg(key_prova_1, "change5"), client3);
        group.get(2).getActor().tell(new Ring.GetValueMsg(key_prova_1), client1);
        group.get(3).getActor().tell(new Ring.UpdateValueMsg(key_prova_2, "change5A"), client1);
        group.get(3).getActor().tell(new Ring.UpdateValueMsg(key_prova_2, "change5B"), client2);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("TEST 8 COMPLETED");
        
    }

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
