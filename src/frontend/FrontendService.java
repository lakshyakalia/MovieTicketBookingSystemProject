package frontend;

import constants.Constants;
import models.RequestObject;
import models.ResponseObject;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import java.io.*;
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

    static boolean crashReplicaOne=false;
    static boolean crashReplicaTwo=false;
    static boolean crashReplicaThree=false;
    static boolean crashReplicaFour=false;
    private static int requestCount=0;




    @Override
    public String forwardMessageToSequencer(RequestObject requestObject) {
        return sendMessageToSequencer(requestObject);
    }

    public String sendMessageToSequencer(RequestObject requestObject){
        try{
            //send a message via UDP to a sequencer
            DatagramSocket socket=new DatagramSocket(Constants.frontendPort);
            requestCount++;
//            String messageToSend=message + ";" + requestCount;
            requestObject.requestCount = requestCount;

            // Serialize the object into a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(requestObject);
            byte[] byteMessage = baos.toByteArray();

            InetAddress ia=InetAddress.getLocalHost();
            DatagramPacket packet=new DatagramPacket(byteMessage,byteMessage.length,ia,Constants.sequencerPort);
            socket.send(packet);

            System.out.println("IM HERE");

            //receive a message via UDP from the replicas
            //socket to communicate with replica1
            DatagramSocket socketForReplicaOne=new DatagramSocket(Constants.listenReplicaOnePort);
            System.out.println("IM HERE 1");
            socketForReplicaOne.setSoTimeout(10000);
            //socket to communicate with replica2
            DatagramSocket socketForReplicaTwo=new DatagramSocket(Constants.listenReplicaTwoPort);
            System.out.println("IM HERE 2");
            socketForReplicaTwo.setSoTimeout(10000);
            //socket to communicate with replica3
            DatagramSocket socketForReplicaThree=new DatagramSocket(Constants.listenReplicaThreePort);
            socketForReplicaThree.setSoTimeout(10000);
            //socket to communicate with replica4
            DatagramSocket socketForReplicaFour=new DatagramSocket(Constants.listenReplicaFourPort);
            socketForReplicaFour.setSoTimeout(10000);

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
                crashReplicaOne=true;
            }

            //recieve response from replica 2
            try{
                socketForReplicaTwo.receive(recievePacketTwo);
            } catch (SocketTimeoutException e) {
                crashReplicaTwo=true;
            }

            //recieve response from replica 3
            try{
                socketForReplicaThree.receive(recievePacketThree);
            } catch (SocketTimeoutException e) {
                crashReplicaTwo=true;
            }

            //recieve response from replica 4
            try{
                socketForReplicaFour.receive(recievePacketFour);
            } catch (SocketTimeoutException e) {
                crashReplicaFour=true;
            }


            //Response from four replicas
            // Deserialize the byte array back into the original object
            byte[] dataOne = recievePacketOne.getData();
            ByteArrayInputStream baisOne = new ByteArrayInputStream(dataOne);
            ObjectInputStream oisOne = new ObjectInputStream(baisOne);
            ResponseObject resReplicaOne = (ResponseObject) oisOne.readObject();
            System.out.println(resReplicaOne.responseMessage);

//          String resReplicaOne=new String(recievePacketOne.getData()).trim();
//          Deserialize the byte array back into the original object

            byte[] dataTwo = recievePacketTwo.getData();
            ByteArrayInputStream baisTwo = new ByteArrayInputStream(dataTwo);
            ObjectInputStream oisTwo = new ObjectInputStream(baisTwo);
            ResponseObject resReplicaTwo = (ResponseObject) oisTwo.readObject();

//          String resReplicaTwo=new String(recievePacketTwo.getData()).trim();

            byte[] dataThree = recievePacketThree.getData();
            ByteArrayInputStream baisThree = new ByteArrayInputStream(dataThree);
            ObjectInputStream oisThree = new ObjectInputStream(baisThree);
            ResponseObject resReplicaThree = (ResponseObject) oisThree.readObject();

//            String resReplicaThree=new String(recievePacketThree.getData()).trim();

            byte[] dataFour = recievePacketFour.getData();
            ByteArrayInputStream baisFour = new ByteArrayInputStream(dataFour);
            ObjectInputStream oisFour = new ObjectInputStream(baisFour);
            ResponseObject resReplicaFour = (ResponseObject) oisFour.readObject();

//            String resReplicaFour=new String(recievePacketFour.getData()).trim();

            checkResponseFromReplicas(resReplicaOne.responseMessage,resReplicaTwo.responseMessage,
                    resReplicaThree.responseMessage,resReplicaFour.responseMessage);
            response=majorityResponse(resReplicaOne.responseMessage,resReplicaTwo.responseMessage,
                    resReplicaThree.responseMessage,resReplicaFour.responseMessage);

//            byte [] errorReplicaInfoByteArray=new byte[1024];
            //check for a software failure
            String softwareFailureReplicaInfo="";
            if (faultReplicaOne){
                softwareFailureReplicaInfo="SoftwareFailure";
                sendSoftwareFailureMsgToRM(socketForReplicaOne, recievePacketOne, softwareFailureReplicaInfo.getBytes());
                faultReplicaOne=false;
            }
            else if (faultReplicaTwo){
                softwareFailureReplicaInfo="SoftwareFailure";
                sendSoftwareFailureMsgToRM(socketForReplicaTwo, recievePacketTwo, softwareFailureReplicaInfo.getBytes());
                faultReplicaTwo=false;
            }
            else if(faultReplicaThree){
                softwareFailureReplicaInfo="SoftwareFailure";
                sendSoftwareFailureMsgToRM(socketForReplicaThree, recievePacketThree, softwareFailureReplicaInfo.getBytes());
                faultReplicaThree=false;
            }
            else if (faultReplicaFour){
                softwareFailureReplicaInfo="SoftwareFailure";
                sendSoftwareFailureMsgToRM(socketForReplicaFour, recievePacketFour, softwareFailureReplicaInfo.getBytes());
                faultReplicaFour=false;
            }
            else{
                softwareFailureReplicaInfo="None";
            }

            //check for a crash failure
            String crashFailureReplicaInfo="";
            crashReplicaThree=true;
            if (crashReplicaOne){
                crashFailureReplicaInfo="CrashFailure";
                sendCrashFailureMsgToRM(socketForReplicaOne, recievePacketOne, crashFailureReplicaInfo.getBytes());
                crashReplicaOne=false;
            }
            else if (crashReplicaTwo){
                crashFailureReplicaInfo="CrashFailure";
                sendCrashFailureMsgToRM(socketForReplicaTwo, recievePacketTwo, crashFailureReplicaInfo.getBytes());
                crashReplicaTwo=false;
            }
            else if(crashReplicaThree){
                crashFailureReplicaInfo="CrashFailure";
                sendCrashFailureMsgToRM(socketForReplicaThree, recievePacketThree, crashFailureReplicaInfo.getBytes());
                crashReplicaThree=false;
            }
            else if (crashReplicaFour){
                crashFailureReplicaInfo="CrashFailure";
                sendCrashFailureMsgToRM(socketForReplicaFour, recievePacketFour, crashFailureReplicaInfo.getBytes());
                crashReplicaFour=false;
            }
            else{
                crashFailureReplicaInfo="None";
            }

////            errorReplicaInfoByteArray=failureReplicaInfo.getBytes();
//
//            //send possible error reply to replica One
//            InetAddress addressReplicaOne=recievePacketOne.getAddress();
//            int portReplicaOne=recievePacketOne.getPort();
//            DatagramPacket packetForOne=new DatagramPacket(errorReplicaInfoByteArray,errorReplicaInfoByteArray.length,addressReplicaOne,portReplicaOne);
//            socketForReplicaOne.send(packetForOne);
//
//            //send possible error reply to replica Two
//            InetAddress addressReplicaTwo=recievePacketTwo.getAddress();
//            int portReplicaTwo=recievePacketTwo.getPort();
//            DatagramPacket packetForTwo=new DatagramPacket(errorReplicaInfoByteArray,errorReplicaInfoByteArray.length,addressReplicaTwo,portReplicaTwo);
//            socketForReplicaTwo.send(packetForTwo);
//
//            //send possible error reply to replica Three
//            InetAddress addressReplicaThree=recievePacketThree.getAddress();
//            int portReplicaThree=recievePacketThree.getPort();
//            DatagramPacket packetForThree=new DatagramPacket(errorReplicaInfoByteArray,errorReplicaInfoByteArray.length,addressReplicaThree,portReplicaThree);
//            socketForReplicaThree.send(packetForThree);
//
//            //send possible error reply to replica Four
//            InetAddress addressReplicaFour=recievePacketFour.getAddress();
//            int portReplicaFour=recievePacketFour.getPort();
//            DatagramPacket packetForFour=new DatagramPacket(errorReplicaInfoByteArray,errorReplicaInfoByteArray.length,addressReplicaFour,portReplicaFour);
//            socketForReplicaFour.send(packetForFour);

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
        } catch (ClassNotFoundException e) {
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
        return forwardMessageToSequencer(requestObject);
    }
    public String sendSoftwareFailureMsgToRM(DatagramSocket socket, DatagramPacket packet, byte[] softwareFailureByteArray) throws IOException {
        InetAddress addressReplicaOne=packet.getAddress();
        int portReplicaOne=packet.getPort();
        DatagramPacket packetForOne=new DatagramPacket(softwareFailureByteArray,softwareFailureByteArray.length,addressReplicaOne,portReplicaOne);
        socket.send(packetForOne);
        return "This will send a software failure message to a replica";
    }
    public String sendCrashFailureMsgToRM(DatagramSocket socket, DatagramPacket packet, byte[] crashFailureByteArray) throws IOException {
        InetAddress addressReplicaOne=packet.getAddress();
        int portReplicaOne=packet.getPort();
        DatagramPacket packetForOne=new DatagramPacket(crashFailureByteArray,crashFailureByteArray.length,addressReplicaOne,portReplicaOne);
        socket.send(packetForOne);
        return "This will send a crash failure message to a replica";
    }
    public String sendReplicaReplaceRequest(int replicaNumber){
        return "This will replace a server replica";
//        return forwardMessageToSequencer("Hello");
    }
}
