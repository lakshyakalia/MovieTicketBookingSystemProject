package replicas.ReplicaThree;

import constants.Constants;
import models.RequestObject;
import models.ResponseObject;
import replicas.ReplicaThree.replica.Interface.Server_Interface;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

public class ReplicaManagerThree {
    HashMap<Integer,String> requestSequenceMap=new HashMap<>();
    static int expectedSequence=0;
    private static int softwareFailureCount=0;
    private static int requestCount=1;
    public static Service atwService;
    public static Service verService;
    public static Service outService;
    public static Server_Interface movieRef;
    private static ArrayList<RequestObject> requestList=new ArrayList<>();
    public static void main(String[] args) throws IOException {
git            System.out.println("Replica Manager Three Started");
            initializeServices();
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    // code to be executed in this thread
                    try {
                        receiveMulticastMessage();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread.start();
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
                System.out.println("Repica One recieved... "+received);

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

                InetAddress ia=InetAddress.getByName("172.20.10.7");
//                System.out.println(ia);
                DatagramPacket packetToFrontend=new DatagramPacket(byteMessage,byteMessage.length,ia,Constants.listenReplicaTwoPort);
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
        } catch (Exception e) {
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
        URL atwaterURL = new URL("http://localhost:9090/DMTBS?wsdl");
        QName atwaterQName = new QName("http://Services.replica.ReplicaThree.replicas/", "implementationService");
        atwService = Service.create(atwaterURL, atwaterQName);

        URL outremontURL = new URL("http://localhost:9091/DMTBS?wsdl");
        QName outremontQName = new QName("http://Services.replica.ReplicaThree.replicas/", "implementationService");
        outService = Service.create(outremontURL, outremontQName);

        URL verdunURL = new URL("http://localhost:9092/DMTBS?wsdl");
        QName verdunQName = new QName("http://Services.replica.ReplicaThree.replicas/", "implementationService");
        verService = Service.create(verdunURL,verdunQName);
    }
    public static ResponseObject sendRequestToReplica(RequestObject request) throws Exception {
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
                //String customerID, String old_movieName,String movieID, String new_movieID,String  new_movieName, int numberOfTickets
                res.responseMessage = movieRef.exchangeTickets(request.currentUser,request.oldMovieName, request.oldMovieID, request.movieID, request.movieName, request.noOfTickets);
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
            movieRef = atwService.getPort(Server_Interface.class);
            return "atwater";
        } else if (serverSubstring.equalsIgnoreCase("OUT")) {
            movieRef = outService.getPort(Server_Interface.class);
            return "outremont";
        } else if (serverSubstring.equalsIgnoreCase("VER")) {
            movieRef = verService.getPort(Server_Interface.class);
            return "verdun";
        }
        return "false";
    }
}
