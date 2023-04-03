package frontend;

import constants.Constants;
import models.RequestObject;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import java.io.IOException;
import java.net.*;

@WebService(endpointInterface="frontend.FrontendInterface")
@SOAPBinding(style = Style.RPC)
public class FrontendService implements FrontendInterface{
    static int bugCountRmOne=0;
    static int bugCountRmTwo=0;
    static int bugCountRmThree=0;
    static int bugCountRmFour=0;

    static boolean faultReplicaOne=false;
    static boolean faultReplicaTwo=false;
    static boolean faultReplicaThree=false;
    static boolean faultReplicaFour=false;



    @Override
    public String forwardMessageToSequencer(String message) {
        return sendMessageToSequencer(message);
    }

    public String sendMessageToSequencer(String message){
        try{
            //send a message via UDP to a sequencer
            DatagramSocket socket=new DatagramSocket(Constants.frontendPort);
            String messageToSend=message;
            byte[] byteMessage=messageToSend.getBytes();
            InetAddress ia=InetAddress.getLocalHost();
            DatagramPacket packet=new DatagramPacket(byteMessage,byteMessage.length,ia,Constants.sequencerPort);
            socket.send(packet);

            //recieve a message via UDP from the replicas
            //socket to communicate with replica1
            DatagramSocket socketForReplicaOne=new DatagramSocket(Constants.listenReplicaOnePort);
            socketForReplicaOne.setSoTimeout(10000);
            //socket to communicate with replica2
            DatagramSocket socketForReplicaTwo=new DatagramSocket(Constants.listenReplicaTwoPort);
            //socket to communicate with replica3
            DatagramSocket socketForReplicaThree=new DatagramSocket(Constants.listenReplicaThreePort);
            //socket to communicate with replica4
            DatagramSocket socketForReplicaFour=new DatagramSocket(Constants.listenReplicaFourPort);

            String response="";
            byte [] recieveByteOne=new byte[1024];
            byte [] recieveByteTwo=new byte[1024];
            byte [] recieveByteThree=new byte[1024];
            byte [] recieveByteFour=new byte[1024];
            DatagramPacket recievePacketOne=new DatagramPacket(recieveByteOne,recieveByteOne.length);
            DatagramPacket recievePacketTwo=new DatagramPacket(recieveByteTwo,recieveByteTwo.length);
            DatagramPacket recievePacketThree=new DatagramPacket(recieveByteThree,recieveByteThree.length);
            DatagramPacket recievePacketFour=new DatagramPacket(recieveByteFour,recieveByteFour.length);

            //recieve response from replica 1
            try{
                socketForReplicaOne.receive(recievePacketOne);
            } catch (SocketTimeoutException e) {
                //informCrash();
            }

            //recieve response from replica 2
            try{
                socketForReplicaTwo.receive(recievePacketTwo);
            } catch (SocketTimeoutException e) {
                //informCrash();
            }

            //recieve response from replica 3
            try{
                socketForReplicaThree.receive(recievePacketThree);
            } catch (SocketTimeoutException e) {
                //informCrash();
            }

            //recieve response from replica 4
            try{
                socketForReplicaFour.receive(recievePacketFour);
            } catch (SocketTimeoutException e) {
                //informCrash();
            }


            //Response from four replicas
            String resReplicaOne=new String(recievePacketOne.getData()).trim();
            String resReplicaTwo=new String(recievePacketTwo.getData()).trim();
            String resReplicaThree=new String(recievePacketThree.getData()).trim();
            String resReplicaFour=new String(recievePacketFour.getData()).trim();

            checkResponseFromReplicas(resReplicaOne,resReplicaTwo,resReplicaThree,resReplicaFour);
            response=majorityResponse(resReplicaOne,resReplicaTwo,resReplicaThree,resReplicaFour);

            //check for a software failure
            String errorReplicaInfo="";
            byte [] errorReplicaInfoByteArray=new byte[1024];
            if (faultReplicaOne){
                errorReplicaInfo="ReplicaOne";
            }
            else if (faultReplicaTwo){
                errorReplicaInfo="ReplicaTwo";
            }
            else if(faultReplicaThree){
                errorReplicaInfo="ReplicaThree";
            }
            else if (faultReplicaFour){
                errorReplicaInfo="ReplicaFour";
            }
            else{
                errorReplicaInfo="None";
            }

            errorReplicaInfoByteArray=errorReplicaInfo.getBytes();

            //send possible error reply to replica One
            InetAddress addressReplicaOne=recievePacketOne.getAddress();
            int portReplicaOne=recievePacketOne.getPort();
            DatagramPacket packetForOne=new DatagramPacket(errorReplicaInfoByteArray,errorReplicaInfoByteArray.length,addressReplicaOne,portReplicaOne);
            socketForReplicaOne.send(packetForOne);

            //send possible error reply to replica Two
            InetAddress addressReplicaTwo=recievePacketTwo.getAddress();
            int portReplicaTwo=recievePacketTwo.getPort();
            DatagramPacket packetForTwo=new DatagramPacket(errorReplicaInfoByteArray,errorReplicaInfoByteArray.length,addressReplicaTwo,portReplicaTwo);
            socketForReplicaTwo.send(packetForTwo);

            //send possible error reply to replica Three
            InetAddress addressReplicaThree=recievePacketThree.getAddress();
            int portReplicaThree=recievePacketThree.getPort();
            DatagramPacket packetForThree=new DatagramPacket(errorReplicaInfoByteArray,errorReplicaInfoByteArray.length,addressReplicaThree,portReplicaThree);
            socketForReplicaThree.send(packetForThree);

            //send possible error reply to replica Four
            InetAddress addressReplicaFour=recievePacketFour.getAddress();
            int portReplicaFour=recievePacketFour.getPort();
            DatagramPacket packetForFour=new DatagramPacket(errorReplicaInfoByteArray,errorReplicaInfoByteArray.length,addressReplicaFour,portReplicaFour);
            socketForReplicaFour.send(packetForFour);


            socketForReplicaOne.close();
            socketForReplicaTwo.close();
            socketForReplicaThree.close();
            socketForReplicaFour.close();
            socket.close();
            return response;
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void checkResponseFromReplicas(String resReplicaOne,String resReplicaTwo,String resReplicaThree,String resReplicaFour) {
        String [] responses=new String[4];
        responses[0]=resReplicaOne;
        responses[1]=resReplicaTwo;
        responses[2]=resReplicaThree;
        responses[3]=resReplicaFour;

        //count not matching response of each replica
        for (int i=0;i<4;i++) {
            for(int j=0;j<4;j++) {
                if (i!=j) {
                    if(!responses[i].equals(responses[j])) {
                        bugCount(i);
                    }
                }
            }
        }

    }
    public void bugCount(int rmNumber) {
        switch(rmNumber) {
            case 0:{
                bugCountRmOne++;
                if (bugCountRmOne==3) {
                    faultReplicaOne=true;
                }
                break;
            }
            case 1:{
                bugCountRmTwo++;
                if (bugCountRmTwo==3) {
                    faultReplicaTwo=true;
                }
                break;
            }
            case 2:{
                bugCountRmThree++;
                if (bugCountRmThree==3) {
                    faultReplicaThree=true;
                }
                break;
            }

            case 3:{
                bugCountRmFour++;
                if (bugCountRmFour==3) {
                    faultReplicaFour=true;
                }
                break;
            }
        }
    }

    public String majorityResponse(String resReplicaOne, String resReplicaTwo, String resReplicaThree,String resReplicaFour){
        //least bug count will be returned as a response to the client.
        String resultResponse="";
        int [] bugCount=new int[4];
        bugCount[0]=bugCountRmOne;
        bugCount[1]=bugCountRmTwo;
        bugCount[2]=bugCountRmThree;
        bugCount[3]=bugCountRmFour;

        int minBug=Integer.MAX_VALUE;
        int result=0;
        for (int i=0;i<4;i++) {
            if (bugCount[i]<minBug) {
                minBug=bugCount[i];
                result=i;
            }
        }
        bugCountRmOne=0;
        bugCountRmTwo=0;
        bugCountRmThree=0;
        bugCountRmFour=0;
        switch(result) {
            case 0: {
                resultResponse = resReplicaOne;
                break;
            }
            case 1: {
                resultResponse = resReplicaTwo;
                break;
            }
            case 2: {
                resultResponse = resReplicaThree;
                break;
            }
            case 3: {
                resultResponse = resReplicaFour;
                break;
            }
        }
        return resultResponse;
    }

    public String getRequestFromClient(RequestObject requestObject) {
        return forwardMessageToSequencer("Hello");
    }
    public String sendReplicaReplaceRequest(int replicaNumber){
        return "This will replace a server replica";
//        return forwardMessageToSequencer("Hello");
    }
}
