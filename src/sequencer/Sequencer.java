package sequencer;

import java.io.IOException;
import java.net.*;

public class Sequencer {
    public static void main(String[] args) throws IOException {
        DatagramSocket socket=new DatagramSocket(4822);
        System.out.println("Sequencer Started");
        while(true){
            requestHandler(socket);
        }

    }
    public static void requestHandler(DatagramSocket socket) throws IOException {
        byte[] byteMessage=new byte[1024];
        DatagramPacket dataPacket=new DatagramPacket(byteMessage,byteMessage.length);
        socket.receive(dataPacket);
        String request=new String(dataPacket.getData()).trim();
        String reply="Message recieved by sequencer "+request;
        multicast(request);
//        int port= dataPacket.getPort();
//        InetAddress ia=dataPacket.getAddress();
//        byte[] responseByte=reply.getBytes();
//        DatagramPacket sendPacket=new DatagramPacket(responseByte,responseByte.length,ia,port);
//        socket.send(sendPacket);
    }
    public static void multicast(String request) throws IOException {
        DatagramSocket multicastSocket=new DatagramSocket();
        InetAddress group=InetAddress.getByName("230.0.0.0");
        byte[] messageToSend=new byte[1024];
        messageToSend=request.getBytes();
        DatagramPacket packet=new DatagramPacket(messageToSend,messageToSend.length,group,4400);
        multicastSocket.send(packet);
        multicastSocket.close();
    }
}
