package replicas.ReplicaFour;
import constants.Constants;
import models.RequestObject;
import models.ResponseObject;
import replicas.ReplicaFour.replica.Implementation.MovieTicketImplementation;
import replicas.ReplicaFour.replica.Interface.MovieTicketInterface;
import replicas.ReplicaOne.replica.Interface.MovieInterface;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
public class ReplicaManagerFour {
    HashMap<Integer,String> requestSequenceMap=new HashMap<>();
    static int expectedSequence=0;
    private static int softwareFailureCount=0;
    private static int requestCount=1;
    public static Service atwService;
    public static Service verService;
    public static Service outService;
    public static MovieTicketInterface movieRef;
    private static ArrayList<RequestObject> requestList=new ArrayList<>();
    public static void main(String[] args) throws IOException {
        while(true){
            System.out.println("Replica Manager Four Started");
            initializeServices();
            receiveMulticastMessage();
        }
    }
    public static void receiveMulticastMessage() throws IOException {
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

                // Deserialize the byte array back into the original object
                byte[] data = packet.getData();
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bais);
                RequestObject received = (RequestObject) ois.readObject();

//              recieved=new String(packet.getData(),0,packet.getLength()).trim().toString();
                System.out.println("Repica Four recieved... "+received);

                if("end".equals(received)) {
                    break;
                }
                System.out.println(received);
                String serverPort = getServerPort(received.currentUser);

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
                ResponseObject response = new ResponseObject();
                if(flag){
                    requestCount++;
                    //TODO: do required operations in the replicas
                    response = sendRequestToReplica(requestList.get(index));
                    requestList.remove(index);
                }
                else if (received.requestCount == requestCount ) {
                    requestCount++;
                    //TODO: do required operations in the replicas
                    response = sendRequestToReplica(received);
                }
                else{
                    requestList.add(received);
                }
                System.out.println(response);


                //Send Response to the frontend
                String toFrontEnd="Ticket Booked Successfully";
                DatagramSocket toFrontEndSocket=new DatagramSocket();
//              byte[] byteMessage=toFrontEnd.getBytes();

                // Serialize the object into a byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(response);
                byte[] byteMessage = baos.toByteArray();

                InetAddress ia=InetAddress.getLocalHost();
//              InetAddress ia=InetAddress.getByName("192.168.0.169");
//              System.out.println(ia);
                DatagramPacket packetToFrontend=new DatagramPacket(byteMessage,byteMessage.length,ia,Constants.listenReplicaFourPort);
                toFrontEndSocket.send(packetToFrontend);

                //Receive the update from the frontend about the response in case of Error
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
        } catch (ClassNotFoundException e) {
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
        URL atwaterURL = new URL("http://localhost:8090/atwater?wsdl");
        QName atwaterQName = new QName("http://Implementation.replica.ReplicaFour.replicas/", "MovieTicketImplementationService");
        atwService = Service.create(atwaterURL, atwaterQName);

        URL outremontURL = new URL("http://localhost:8091/outremont?wsdl");
        QName outremontQName = new QName("http://Implementation.replica.ReplicaFour.replicas/", "MovieTicketImplementationService");
        outService = Service.create(outremontURL, outremontQName);

        URL verdunURL = new URL("http://localhost:8092/verdun?wsdl");
        QName verdunQName = new QName("http://Implementation.replica.ReplicaFour.replicas/", "MovieTicketImplementationService");
        verService = Service.create(verdunURL,verdunQName);
    }
    public static ResponseObject sendRequestToReplica(RequestObject request){
        ResponseObject res = new ResponseObject();
        switch (request.requestType){
            case "addMovieSlots":{
                res.responseMessage = movieRef.addMovieSlots(request.movieID, request.movieName, request.bookingCapacity);
                break;
            }
            case "removeMovieSlots":{
                res.responseMessage = movieRef.removeMovieSlots(request.movieID, request.movieName);
                break;
            }
            case "listMovieAvailability":{
                res.responseMessage = movieRef.listMovieShowAvailability(request.movieName);
                break;
            }
            case "bookMovieTicket":{
                res.responseMessage = movieRef.bookMovieTickets(request.currentUser, request.movieID, request.movieName, request.noOfTickets);
                break;
            }
            case "getBookingSchedule":{
                res.responseMessage = movieRef.getBookingSchedule(request.currentUser);
                break;
            }
            case "cancelMovieTicket":{
                res.responseMessage = movieRef.cancelMovieTickets(request.currentUser, request.movieID, request.movieName, request.noOfTickets);
                break;
            }
            case "exchangeMovieTickets":{
                res.responseMessage = movieRef.exchangeTickets(request.currentUser, request.oldMovieID, request.movieID, request.movieName, request.noOfTickets);
                break;
            }
            default:{
                System.out.println("Invalid Request");
                res.responseCode = HTTP_BAD_REQUEST;
                break;
            }
        }
        return res;
    }
    public static String getServerPort(String userID){
        String serverSubstring = userID.substring(0,3);
        String role = userID.substring(3,4);
        if (serverSubstring.equalsIgnoreCase("ATW")) {
            movieRef = atwService.getPort(MovieTicketInterface.class);
            return "atwater";
        } else if (serverSubstring.equalsIgnoreCase("OUT")) {
            movieRef = outService.getPort(MovieTicketInterface.class);
            return "outremont";
        } else if (serverSubstring.equalsIgnoreCase("VER")) {
            movieRef = verService.getPort(MovieTicketInterface.class);
            return "verdun";
        }
        return "false";
    }
}
