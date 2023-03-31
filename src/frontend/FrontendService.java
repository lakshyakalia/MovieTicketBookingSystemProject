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
    int replicaOneFaultCount=0;
    int replicaTwoFaultCount=0;
    int replicaThreeFaultCount=0;
    int replicaFourFaultCount=0;


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
            String[] arr=checkSoftwareFailure.split(";");
            response=arr[0];
            String error=arr[1];
            if(replicaOneFaultCount==3){
                replicaOneFaultCount=0;
                sendReplicaReplaceRequest(1);
            }
            else if(replicaTwoFaultCount==3){
                replicaTwoFaultCount=0;
                sendReplicaReplaceRequest(2);
            }
            else if(replicaThreeFaultCount==3){
                replicaThreeFaultCount=0;
                sendReplicaReplaceRequest(3);
            }
            else if(replicaFourFaultCount==3){
                replicaFourFaultCount=0;
                sendReplicaReplaceRequest(4);
            }


//            response="This is a final response to the client";
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
        String result="";
        String error="";
        String[] arr=new String[4];
        arr[0]=resReplicaOne;
        arr[1]=resReplicaTwo;
        arr[2]=resReplicaThree;
        arr[3]=resReplicaFour;
        int flag = 0;
        for(int i=0;i<4;i++){
            for(int j=i+1;j<4;j++){
                if(!arr[i].equals(arr[j])){
                    flag++;
                }
                if(flag==2){
                    error= arr[i];
                    break;
                }
            }
            if(flag==0){
                result = arr[i];
            }
        }
        if(error.equals(resReplicaOne)){
            replicaOneFaultCount++;
            error="ReplicaOne";
        }
        else if(error.equals(resReplicaTwo)){
            replicaTwoFaultCount++;
            error="ReplicaTwo";
        }
        else if(error.equals(resReplicaThree)){
            replicaThreeFaultCount++;
            error="ReplicaThree";
        }
        else if(error.equals(resReplicaFour)){
            replicaFourFaultCount++;
            error="ReplicaFour";
        }
        String res = result+";"+error;
        return res;
    }
    public String getRequestFromClient(RequestObject requestObject){
        return null;
    }
    public String sendReplicaReplaceRequest(int replicaNumber){
        return null;
    }
}
