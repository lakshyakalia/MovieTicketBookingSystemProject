package frontend;


import constants.Constants;
import models.ResponseObject;

import javax.xml.ws.Endpoint;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Frontend {

    static boolean faultReplicaOne=false;
    static boolean faultReplicaTwo=false;
    static boolean faultReplicaThree=false;
    static boolean faultReplicaFour=false;

    static boolean crashReplicaOne=false;
    static boolean crashReplicaTwo=false;
    static boolean crashReplicaThree=false;
    static boolean crashReplicaFour=false;

    public static void main(String [] args) throws IOException, ClassNotFoundException {
        Endpoint endpoint=Endpoint.publish("http://localhost:8071/frontend", new FrontendService());
        System.out.println("WebServer Started");
        DatagramSocket socketForReplicaOne=new DatagramSocket(Constants.listenReplicaOnePort);
        System.out.println("IM HERE 1");
        // socketForReplicaOne.setSoTimeout(10000);
        //socket to communicate with replica2
        DatagramSocket socketForReplicaTwo=new DatagramSocket(Constants.listenReplicaTwoPort);
        System.out.println("IM HERE 2");
        //socketForReplicaTwo.setSoTimeout(10000);
        //socket to communicate with replica3
        DatagramSocket socketForReplicaThree=new DatagramSocket(Constants.listenReplicaThreePort);
        //socketForReplicaThree.setSoTimeout(10000);
        //socket to communicate with replica4
        DatagramSocket socketForReplicaFour=new DatagramSocket(Constants.listenReplicaFourPort);


        while(true)
            {
                System.out.println("IM HERE");

                //receive a message via UDP from the replicas
                //socket to communicate with replica1
                //socketForReplicaFour.setSoTimeout(10000);

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
                    System.out.println("From 1: "+recievePacketOne.getData());
                } catch (IOException e) {
                    crashReplicaOne=true;
                }


                //recieve response from replica 2
                try{
                    socketForReplicaTwo.receive(recievePacketTwo);
                    System.out.println("From 2: "+recievePacketTwo.getData());
                } catch (IOException e) {
                    crashReplicaTwo=true;
                }

                //recieve response from replica 3
                try{
                    socketForReplicaThree.receive(recievePacketThree);
                    System.out.println("From 3: "+recievePacketThree.getData());
                } catch (IOException e) {
                    crashReplicaTwo=true;
                }

                //recieve response from replica 4
                try{
                    socketForReplicaFour.receive(recievePacketFour);
                    System.out.println("From 4: "+recievePacketFour.getData());
                } catch (IOException e) {
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
//
//                byte[] dataTwo = recievePacketTwo.getData();
//                ByteArrayInputStream baisTwo = new ByteArrayInputStream(dataTwo);
//                ObjectInputStream oisTwo = new ObjectInputStream(baisTwo);
//                ResponseObject resReplicaTwo = (ResponseObject) oisTwo.readObject();
//                System.out.println(resReplicaTwo.responseMessage);
//
////          String resReplicaTwo=new String(recievePacketTwo.getData()).trim();
//
//                byte[] dataThree = recievePacketThree.getData();
//                ByteArrayInputStream baisThree = new ByteArrayInputStream(dataThree);
//                ObjectInputStream oisThree = new ObjectInputStream(baisThree);
//                ResponseObject resReplicaThree = (ResponseObject) oisThree.readObject();
//                System.out.println(resReplicaThree.responseMessage);
////            String resReplicaThree=new String(recievePacketThree.getData()).trim();
//
//                byte[] dataFour = recievePacketFour.getData();
//                ByteArrayInputStream baisFour = new ByteArrayInputStream(dataFour);
//                ObjectInputStream oisFour = new ObjectInputStream(baisFour);
//                ResponseObject resReplicaFour = (ResponseObject) oisFour.readObject();
//                System.out.println(resReplicaFour.responseMessage);
//                socketForReplicaOne.close();
//                socketForReplicaTwo.close();
//                socketForReplicaThree.close();
//                socketForReplicaFour.close();

//          String resReplicaFour=new String(recievePacketFour.getData()).trim();
//
//                checkResponseFromReplicas(resReplicaOne.responseMessage,resReplicaTwo.responseMessage,
//                        resReplicaThree.responseMessage,resReplicaFour.responseMessage);
//                response=majorityResponse(resReplicaOne.responseMessage,resReplicaTwo.responseMessage,
//                        resReplicaThree.responseMessage,resReplicaFour.responseMessage);
//
////           byte [] errorReplicaInfoByteArray=new byte[1024];
//                //check for a software failure
//                String softwareFailureReplicaInfo="";
//                if (faultReplicaOne){
//                    softwareFailureReplicaInfo="SoftwareFailure";
//                    sendSoftwareFailureMsgToRM(socketForReplicaOne, recievePacketOne, softwareFailureReplicaInfo.getBytes());
//                    faultReplicaOne=false;
//                }
//                else if (faultReplicaTwo){
//                    softwareFailureReplicaInfo="SoftwareFailure";
//                    sendSoftwareFailureMsgToRM(socketForReplicaTwo, recievePacketTwo, softwareFailureReplicaInfo.getBytes());
//                    faultReplicaTwo=false;
//                }
//                else if(faultReplicaThree){
//                    softwareFailureReplicaInfo="SoftwareFailure";
//                    sendSoftwareFailureMsgToRM(socketForReplicaThree, recievePacketThree, softwareFailureReplicaInfo.getBytes());
//                    faultReplicaThree=false;
//                }
//                else if (faultReplicaFour){
//                    softwareFailureReplicaInfo="SoftwareFailure";
//                    sendSoftwareFailureMsgToRM(socketForReplicaFour, recievePacketFour, softwareFailureReplicaInfo.getBytes());
//                    faultReplicaFour=false;
//                }
//                else{
//                    softwareFailureReplicaInfo="None";
//                }
//
//                //check for a crash failure
//                String crashFailureReplicaInfo="";
//                crashReplicaThree=true;
//                if (crashReplicaOne){
//                    crashFailureReplicaInfo="CrashFailure";
//                    sendCrashFailureMsgToRM(socketForReplicaOne, recievePacketOne, crashFailureReplicaInfo.getBytes());
//                    crashReplicaOne=false;
//                }
//                else if (crashReplicaTwo){
//                    crashFailureReplicaInfo="CrashFailure";
//                    sendCrashFailureMsgToRM(socketForReplicaTwo, recievePacketTwo, crashFailureReplicaInfo.getBytes());
//                    crashReplicaTwo=false;
//                }
//                else if(crashReplicaThree){
//                    crashFailureReplicaInfo="CrashFailure";
//                    sendCrashFailureMsgToRM(socketForReplicaThree, recievePacketThree, crashFailureReplicaInfo.getBytes());
//                    crashReplicaThree=false;
//                }
//                else if (crashReplicaFour){
//                    crashFailureReplicaInfo="CrashFailure";
//                    sendCrashFailureMsgToRM(socketForReplicaFour, recievePacketFour, crashFailureReplicaInfo.getBytes());
//                    crashReplicaFour=false;
//                }
//                else{
//                    crashFailureReplicaInfo="None";
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



            }


}