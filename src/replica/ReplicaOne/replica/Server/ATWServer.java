package replica.ReplicaOne.replica.Server;

import replica.ReplicaOne.replica.Services.MovieTicketService;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.Naming;

public class ATWServer {

    public ATWServer() throws Exception {
        super();
    }
    private static final String serverEndPoint = "http://localhost:8070/atwater";

    public static void main(String[] args) throws Exception {
        try{

            MovieTicketService atwMovieService = new MovieTicketService("atw","ATW");
            Endpoint endpoint = Endpoint.publish(serverEndPoint, atwMovieService);



            System.out.println("ATW Server ready and waiting ...");

            requestListener(atwMovieService);
//            while (true) {
//                orb.run();
//            }


        }
        catch (Exception e){
            e.printStackTrace();
        }



//        MovieTicketService atwMovieService = new MovieTicketService("atw","ATW");
//        Naming.bind("rmi://localhost/atw", atwMovieService);
//        System.out.println("ATW Server started...");

//        Runnable task = () -> {
//            requestListener(atwMovieService,ds);
//        };

    }
    public static void requestListener(MovieTicketService atwMovieService) {
        try{
            String callbackResponse = "";
            DatagramSocket ds = new DatagramSocket(4556);
            byte[] byteRequest = new byte[1024];
            while (true){

                DatagramPacket dp = new DatagramPacket(byteRequest,byteRequest.length);
                ds.receive(dp);
                String requestString = new String(dp.getData(), 0, dp.getLength()).trim();
                String[] requestStringArr = requestString.split(";");
                String func = requestStringArr[0];
                String userID = requestStringArr[1];
                String movieName = requestStringArr[2];
                String movieID = requestStringArr[3];
                int noOfTickets = Integer.parseInt(requestStringArr[4]);

                switch (func) {
                    case "listMovieShowAvailability": {
                        String res = atwMovieService.listMovieShowAvailabilityUDP(movieName);
                        callbackResponse = res;
                        break;
                    }
                    case "bookMovieTickets": {
                        String res = atwMovieService.bookMovieTickets(userID,movieID,movieName,noOfTickets);
                        callbackResponse = res;
                        break;
                    }
                    case "getBookingSchedule": {
                        String res = atwMovieService.getBookingScheduleUDP(userID);
                        callbackResponse = res;
                        break;
                    }
                    case "cancelMovieTickets": {
                        String res = atwMovieService.cancelMovieTickets(userID,movieID,movieName,noOfTickets);
                        callbackResponse = res;
                        break;
                    }
                    case "exchangeTickets":{
                        String new_movieID = requestStringArr[5];
                        String res = atwMovieService.exchangeTickets(userID,movieID,new_movieID,movieName,noOfTickets);
                        callbackResponse = res;
                        break;
                    }
                    case "exchangeTicketsCapacityUDP": {
                        String res = atwMovieService.exchangeTicketsCapacityUDP(userID,movieID,movieName,noOfTickets);
                        callbackResponse = res;
                        break;
                    }
                }
//                byte[] byteToSend = callbackResponse.trim().getBytes();
                byte[] byteToSend = callbackResponse.getBytes();
                InetAddress ia = InetAddress.getLocalHost();
                DatagramPacket response = new DatagramPacket(byteToSend, byteToSend.length, ia,
                        dp.getPort());
                ds.send(response);
//                ds.close();
            }

        }
        catch (SocketException e){
            System.out.println("Socket exception: " + e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
