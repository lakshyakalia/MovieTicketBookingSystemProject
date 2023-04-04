package sequencer;

import constants.Constants;
import models.RequestObject;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Sequencer {
    private static int requestCount=1;
    private static ArrayList<RequestObject> requestList=new ArrayList<>();
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        DatagramSocket socket=new DatagramSocket(Constants.sequencerPort);
        System.out.println("Sequencer Started");
        while(true){
            requestHandler(socket);
        }

    }
    public static void requestHandler(DatagramSocket socket) throws IOException, ClassNotFoundException {
        //receive a request from the frontend
        byte[] byteMessage=new byte[1024];
        DatagramPacket dataPacket=new DatagramPacket(byteMessage,byteMessage.length);
        socket.receive(dataPacket);

        // Deserialize the byte array back into the original object
        byte[] data = dataPacket.getData();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        RequestObject request = (RequestObject) ois.readObject();

//        String request=new String(dataPacket.getData()).trim();
        boolean flag=false;
        int index = 0;
        if(requestList.size()>0){
            for (RequestObject s:requestList) {
                if(s.requestCount == requestCount){
                    flag = true;
                    index = requestList.indexOf(s);
                    break;
                }
            }

        }
//        TODO: May need to improve this code
        if(flag){
            requestCount++;
            String reply="Message received by sequencer "+requestList.get(index);
            multicast(requestList.get(index));
            requestList.remove(index);
        }
        else if (request.requestCount == requestCount ) {
            requestCount++;
            String reply="Message received by sequencer "+request;
            multicast(request);
        }
        else{
            requestList.add(request);
        }

    }
    public static void multicast(RequestObject request) throws IOException {
        DatagramSocket multicastSocket=new DatagramSocket();
        InetAddress group=InetAddress.getByName("230.0.0.0");

        // Serialize the object into a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(request);
        byte[] messageToSend = baos.toByteArray();

//        byte[] messageToSend=new byte[1024];
//        messageToSend=request.getBytes();

        DatagramPacket packet=new DatagramPacket(messageToSend,messageToSend.length,group,Constants.multicastPort);
        multicastSocket.send(packet);
        multicastSocket.close();
    }
}
