package sequencer;

import constants.Constants;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Sequencer {
    private static int requestCount=1;
    private static ArrayList<String> requestList=new ArrayList<>();
    public static void main(String[] args) throws IOException {
        DatagramSocket socket=new DatagramSocket(Constants.sequencerPort);
        System.out.println("Sequencer Started");
        while(true){
            requestHandler(socket);
        }

    }
    public static void requestHandler(DatagramSocket socket) throws IOException {
        //revieve a request from the frontend
        byte[] byteMessage=new byte[1024];
        DatagramPacket dataPacket=new DatagramPacket(byteMessage,byteMessage.length);
        socket.receive(dataPacket);
        String request=new String(dataPacket.getData()).trim();
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
            String reply="Message recieved by sequencer "+requestList.get(index);
            multicast(requestList.get(index));
            requestList.remove(index);
        }
        else if (Integer.parseInt(request.split(";")[request.split(";").length-1])== requestCount ) {
            requestCount++;
            String reply="Message recieved by sequencer "+request;
            multicast(request);
        }
        else{
            requestList.add(request);
        }

    }
    public static void multicast(String request) throws IOException {
        DatagramSocket multicastSocket=new DatagramSocket();
        InetAddress group=InetAddress.getByName("230.0.0.0");
        byte[] messageToSend=new byte[1024];
        messageToSend=request.getBytes();
        DatagramPacket packet=new DatagramPacket(messageToSend,messageToSend.length,group,Constants.multicastPort);
        multicastSocket.send(packet);
        multicastSocket.close();
    }
}
