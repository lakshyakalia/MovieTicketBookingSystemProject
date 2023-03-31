package ReplicaManagerTwo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ReplicaManagerTwo {
    public static void main(String[] args) throws IOException {
        System.out.println("Replica Manager Two Started");
        while(true){
            recieveMulticstMessage();
        }
    }
    public static void recieveMulticstMessage() throws IOException {
        try{
            String recieved="";
            MulticastSocket multicastSocket=new MulticastSocket(4400);
            byte[] recieveMessage=new byte[1024];
            InetAddress group=InetAddress.getByName("230.0.0.0");
            multicastSocket.joinGroup(group);
            while(true){
                DatagramPacket packet=new DatagramPacket(recieveMessage,recieveMessage.length);
                multicastSocket.receive(packet);
                recieved=new String(packet.getData(),0,packet.getLength()).trim().toString();
                System.out.println("Repica Two recieved... "+recieved);
                if("end".equals(recieved)) {
                    break;
                }
                System.out.println(recieved);
                String toFrontEnd=recieved+" Message From the Replica Manager 2. ";
                DatagramSocket toFrontEndSocket=new DatagramSocket();
                byte[] byteMessage=toFrontEnd.getBytes();
                InetAddress ia=InetAddress.getLocalHost();
                DatagramPacket packetToFrontend=new DatagramPacket(byteMessage,byteMessage.length,ia,5123);
                toFrontEndSocket.send(packetToFrontend);
            }
            multicastSocket.leaveGroup(group);
            multicastSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
