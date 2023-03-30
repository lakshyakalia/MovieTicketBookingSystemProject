package ReplicaManagerOne;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ReplicaManagerOne {
    public static void main(String[] args) throws IOException {
        while(true){
            System.out.println("Replica Manager One Started");
            recieveMulticstMessage();
        }
    }
    public static void recieveMulticstMessage() throws IOException {
        try{
            MulticastSocket multicastSocket=new MulticastSocket(4400);
            byte[] recieveMessage=new byte[1024];
            InetAddress group=InetAddress.getByName("230.0.0.0");
            multicastSocket.joinGroup(group);
            while(true){
                DatagramPacket packet=new DatagramPacket(recieveMessage,recieveMessage.length);
                multicastSocket.receive(packet);
                String recieved=new String(packet.getData(),0,packet.getLength()).trim();
                System.out.println("Repica One recieved... "+recieved.toString());
                if("end".equals(recieved)) {
                    break;
                }
            }
            multicastSocket.leaveGroup(group);
            multicastSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
