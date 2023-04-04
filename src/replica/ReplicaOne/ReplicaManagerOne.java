package replica.ReplicaOne;

import constants.Constants;
import replica.ReplicaOne.replica.Interface.MovieInterface;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ReplicaManagerOne {
    HashMap<Integer,String> requestSequenceMap=new HashMap<>();
    static int expectedSequence=0;
    private static int softwareFailureCount=0;
    private static int requestCount=1;
    public static Service atwService;
    public static Service verService;
    public static Service outService;
    public static MovieInterface movieRef;
    private static ArrayList<String> requestList=new ArrayList<>();
    public static void main(String[] args) throws IOException {
        while(true){
            System.out.println("Replica Manager One Started");
            initializeServices();
            recieveMulticstMessage();
        }
    }
    public static void recieveMulticstMessage() throws IOException {
        try{
            String recieved="";
            MulticastSocket multicastSocket=new MulticastSocket(Constants.multicastPort);
            byte[] recieveMessage=new byte[1024];
            InetAddress group=InetAddress.getByName("230.0.0.0");
            multicastSocket.joinGroup(group);
            while(true){
                //Recieve the request from the sequencer
                DatagramPacket packet=new DatagramPacket(recieveMessage,recieveMessage.length);
                multicastSocket.receive(packet);
                recieved=new String(packet.getData(),0,packet.getLength()).trim().toString();
                System.out.println("Repica One recieved... "+recieved);

                if("end".equals(recieved)) {
                    break;
                }
                System.out.println(recieved);

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
                    sendRequestToReplica(requestList.get(index));
                    //TODO: do required operations in the replicas
                    requestList.remove(index);
                }
                else if (Integer.parseInt(recieved.split(";")[recieved.split(";").length-1])== requestCount ) {
                    requestCount++;
                    sendRequestToReplica(recieved);
                    //TODO: do required operations in the replicas
                }
                else{
                    requestList.add(recieved);
                }



                //Send Response to the frontend
                String toFrontEnd="Ticket Booked Successfully";
                DatagramSocket toFrontEndSocket=new DatagramSocket();
                byte[] byteMessage=toFrontEnd.getBytes();
                InetAddress ia=InetAddress.getLocalHost();
                DatagramPacket packetToFrontend=new DatagramPacket(byteMessage,byteMessage.length,ia,Constants.listenReplicaOnePort);
                toFrontEndSocket.send(packetToFrontend);

                //Recieve the update from the frontend about the response
                byte[] byteFromFrontend=new byte[1024];
                DatagramPacket packetFromFrontend=new DatagramPacket(byteFromFrontend,byteFromFrontend.length);
                toFrontEndSocket.receive(packetFromFrontend);
                String checkString=new String(packetFromFrontend.getData()).trim();
                checkErrorResponseFromFrontend(checkString);
//              System.out.println("Final "+checkString);
            }
            multicastSocket.leaveGroup(group);
            multicastSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void checkErrorResponseFromFrontend(String checkString){
        if(checkString.equals("SoftwareFailure")){
            softwareFailureCount++;
            if(softwareFailureCount==3){
                System.out.println("Software Failure");
//                TODO: Replace instance of replica
            }
//            System.out.println("Error in the response from the frontend");
        }
        else if(checkString.equals("CrashFailure")){
                System.out.println("Crash Failure");
//            TODO: Replace instance of replica
        }
    }

    public static void initializeServices() throws MalformedURLException {
        URL atwaterURL = new URL("http://localhost:8070/atwater?wsdl");
        QName atwaterQName = new QName("http://Services/", "MovieTicketServiceService");
        atwService = Service.create(atwaterURL, atwaterQName);

        URL outremontURL = new URL("http://localhost:8080/outremont?wsdl");
        QName outremontQName = new QName("http://Services/", "MovieTicketServiceService");
        outService = Service.create(outremontURL, outremontQName);

        URL verdunURL = new URL("http://localhost:8090/verdun?wsdl");
        QName verdunQName = new QName("http://Services/", "MovieTicketServiceService");
        verService = Service.create(verdunURL,verdunQName);
    }
    public static String sendRequestToReplica(String request){
        String res = "";
        switch (request.split(";")[request.split(";").length - 2]){
            case "addMovieSlots":{
                res = movieRef.addMovieSlots(addMovieID, addMovieName, addBookingCapacity);
                break;
            }
            case "removeMovieSlots":{
                res = movieRef.removeMovieSlots(removeMovieID, removeMovieName);
                break;
            }
            case "listMovieAvailability":{
                res = movieRef.listMovieShowAvailability(listMovieName);
                break;
            }
            case "bookMovieTicket":{
                res = movieRef.bookMovieTickets(userID, bookMovieID, bookMovieName, bookNumberOfTickets);
                break;
            }
            case "getBookingSchedule":{
                res = movieRef.getBookingSchedule(userID);
                break;
            }
            case "cancelMovieTicket":{
                res = movieRef.cancelMovieTickets(userID, cancelBookMovieID, cancelBookMovieName, cancelBookNumberOfTickets);
                break;
            }
            case "exchangeMovieTickets":{
                res = movieRef.exchangeTickets(userID, bookMovieID, newBookMovieID, newBookMovieName, newBookNumberOfTickets);
                break;
            }
            default:{
                System.out.println("Invalid Request");
                break;
            }
        }
        return res;
    }
}
