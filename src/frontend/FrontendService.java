package frontend;

import models.RequestObject;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import java.io.IOException;
import java.net.*;

@WebService(endpointInterface="frontend.FrontendInterface")
@SOAPBinding(style = Style.RPC)
public class FrontendService implements FrontendInterface{

    @Override
    public String forwardMessageToSequencer(String message) {
        return sendMessageToSequencer(message);
    }

    public String sendMessageToSequencer(String message){
        try{
            //send a message via UDP
            DatagramSocket socket=new DatagramSocket(4455);
            String messageToSend=message;
            byte[] byteMessage=messageToSend.getBytes();
            InetAddress ia=InetAddress.getLocalHost();
            DatagramPacket packet=new DatagramPacket(byteMessage,byteMessage.length,ia,4822);
            socket.send(packet);

            //recieve a message via UDP
            DatagramSocket socketForReplicaOne=new DatagramSocket(5122);
            DatagramSocket socketForReplicaTwo=new DatagramSocket(5123);
            String response="";
            byte [] recieveByteOne=new byte[1024];
            byte [] recieveByteTwo=new byte[1024];
            DatagramPacket recievePacketOne=new DatagramPacket(recieveByteOne,recieveByteOne.length);
            DatagramPacket recievePacketTwo=new DatagramPacket(recieveByteTwo,recieveByteTwo.length);
            socketForReplicaOne.receive(recievePacketOne);
            socketForReplicaTwo.receive(recievePacketTwo);

            //Response from four replicas
            String resReplicaOne=new String(recievePacketOne.getData()).trim();
            String resReplicaTwo=new String(recievePacketTwo.getData()).trim();
            String resReplicaThree=resReplicaOne;
            String resReplicaFour=resReplicaTwo;

            String checkSoftwareFailure=checkResponsesFromReplicas(resReplicaOne,resReplicaTwo,resReplicaThree,resReplicaFour);

            response="This is a final response to the client";
            socketForReplicaOne.close();
            socketForReplicaTwo.close();
            socket.close();
            return response;
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String checkResponsesFromReplicas(String resReplicaOne,String resReplicaTwo, String resReplicaThree,String resReplicaFour){
        String noFailure="No failure";
        int countEqualResponseOne=0;
        if(resReplicaOne.equals(resReplicaTwo)){
            countEqualResponseOne++;
        }
        if(resReplicaOne.equals(resReplicaThree)){
            countEqualResponseOne++;
        }
        if(resReplicaOne.equals(resReplicaFour)){
            countEqualResponseOne++;
        }
        return "";

    }
    public String getRequestFromClient(RequestObject requestObject){
        return null;
    }
}
