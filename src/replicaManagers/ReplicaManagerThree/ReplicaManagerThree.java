package replicaManagers.ReplicaManagerThree;

import constants.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;

public class ReplicaManagerThree {

    private static int softwareFailureCount=0;
    HashMap<Integer,String> requestSequenceMap=new HashMap<>();
    static int expectedSequence=0;
    public static void main(String[] args) throws IOException {
        while(true){
            System.out.println("Replica Manager Three Started");
            recieveMulticstMessage();
        }
    }
    public static void recieveMulticstMessage() throws IOException {
        try{
            String recieved="";
            MulticastSocket multicastSocket=new MulticastSocket(Constants.multicastPort);
            byte[] recieveMessage=new byte[1024];
            InetAddress group=InetAddress.getByName("230.0.0.0");
            multicastSocket.joinGroup(group);
            while(true){
                //Recieve the request from the sequencer
                DatagramPacket packet=new DatagramPacket(recieveMessage,recieveMessage.length);
                multicastSocket.receive(packet);
                recieved=new String(packet.getData(),0,packet.getLength()).trim().toString();
                System.out.println("Repica Three recieved... "+recieved);
                if("end".equals(recieved)) {
                    break;
                }
                System.out.println(recieved);

                //do required operations in the replicas

                //Send Response to the frontend
                String toFrontEnd="Ticket Booked Successfully";
                DatagramSocket toFrontEndSocket=new DatagramSocket();
                byte[] byteMessage=toFrontEnd.getBytes();
                InetAddress ia=InetAddress.getLocalHost();
                DatagramPacket packetToFrontend=new DatagramPacket(byteMessage,byteMessage.length,ia,Constants.listenReplicaThreePort);
                toFrontEndSocket.send(packetToFrontend);

                //Recieve the update from the Frontend
                byte[] byteFromFrontend=new byte[1024];
                DatagramPacket packetFromFrontend=new DatagramPacket(byteFromFrontend,byteFromFrontend.length);
                toFrontEndSocket.receive(packetFromFrontend);
                String checkString=new String(packetFromFrontend.getData()).trim();
                checkErrorResponseFromFrontend(checkString);
//              System.out.println("Final "+checkString);
            }
            multicastSocket.leaveGroup(group);
            multicastSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void checkErrorResponseFromFrontend(String checkString){
        if(checkString.equals("SoftwareFailure")){
            softwareFailureCount++;
            if(softwareFailureCount==3){
                System.out.println("Software Failure");
//                TODO: Replace instance of replica
            }
//            System.out.println("Error in the response from the frontend");
        }
        else if(checkString.equals("CrashFailure")){
            System.out.println("Crash Failure");
//            TODO: Replace instance of replica
        }
    }
}
