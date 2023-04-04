package replica.ReplicaOne;

import constants.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;

public class ReplicaManagerOne {
    HashMap<Integer,String> requestSequenceMap=new HashMap<>();
    static int expectedSequence=0;
    private static int softwareFailureCount=0;
    private static int requestCount=1;
    private static ArrayList<String> requestList=new ArrayList<>();
    public static void main(String[] args) throws IOException {
        while(true){
            System.out.println("Replica Manager One Started");
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
                System.out.println("Repica One recieved... "+recieved);

                if("end".equals(recieved)) {
                    break;
                }
                System.out.println(recieved);

                boolean flag=false;
                int index = 0;
                if(requestList.size()>0){
                    for (String s:requestList) {
                        if(Integer.parseInt(s.split(";")[s.split(";").length-1])== requestCount){
                            flag = true;
                            index = requestList.indexOf(s);
                            break;
                        }
                    }

                }
                if(flag){
                    requestCount++;
                    //TODO: do required operations in the replicas
                    requestList.remove(index);
                }
                else if (Integer.parseInt(recieved.split(";")[recieved.split(";").length-1])== requestCount ) {
                    requestCount++;
                    //TODO: do required operations in the replicas
                }
                else{
                    requestList.add(recieved);
                }



                //Send Response to the frontend
                String toFrontEnd="Ticket Booked Successfully";
                DatagramSocket toFrontEndSocket=new DatagramSocket();
                byte[] byteMessage=toFrontEnd.getBytes();
                InetAddress ia=InetAddress.getLocalHost();
                DatagramPacket packetToFrontend=new DatagramPacket(byteMessage,byteMessage.length,ia,Constants.listenReplicaOnePort);
                toFrontEndSocket.send(packetToFrontend);

                //Recieve the update from the frontend about the response
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
