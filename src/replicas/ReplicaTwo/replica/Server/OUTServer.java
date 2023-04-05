package replicas.ReplicaTwo.replica.Server;

import replicas.ReplicaTwo.replica.Services.MovieTicketService;

import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class OUTServer {

    public OUTServer() throws Exception {
    }
    private static final String serverEndPoint = "http://localhost:8111/outremont";

    public static void main(String[] args) {
        try{

            MovieTicketService outMovieService = new MovieTicketService("out","OUT");
            Endpoint endpoint = Endpoint.publish(serverEndPoint, outMovieService);


            System.out.println("OUT Server ready and waiting ...");

            requestListener(outMovieService);
//            while (true) {
//                orb.run();
//            }


        }
        catch (Exception e){
            e.printStackTrace();
        }
//        DatagramSocket ds = new DatagramSocket(4557);

//        MovieTicketService outMovieService = new MovieTicketService("out","OUT");
//        Naming.bind("rmi://localhost/out", outMovieService);
//        System.out.println("OUT Server started...");

//        byte[] byteReceiveFromClient = new byte[1024];
//
//        DatagramPacket dp = new DatagramPacket(byteReceiveFromClient,byteReceiveFromClient.length);
//        ds.receive(dp);
//        String str = new String(dp.getData());
//        String[] arr = str.split(" ");
//        switch (arr[0]){
//            case "listMovieShowAvailability":
//                outMovieService.listMovieShowAvailability(arr[1]);
//                break;
//        }

//        System.out.println(str);
//        Runnable task = () -> {
//            requestListener(outMovieService);
//        };

    }
    public static void requestListener(MovieTicketService outMovieService) {
        try{
            String callbackResponse = "";
            DatagramSocket ds = new DatagramSocket(5557);
            byte[] byteRequest = new byte[1024];
            while (true){
                DatagramPacket dp = new DatagramPacket(byteRequest,byteRequest.length);
                ds.receive(dp);
                String requestString = new String(dp.getData(), 0, dp.getLength());
                String[] requestStringArr = requestString.split(";");
                String func = requestStringArr[0];
                String userID = requestStringArr[1];
                String movieName = requestStringArr[2];
                String movieID = requestStringArr[3];
                int noOfTickets = Integer.parseInt(requestStringArr[4]);

                switch (func) {
                    case "listMovieShowAvailability": {
                        String res = outMovieService.listMovieShowAvailabilityUDP(movieName);
                        callbackResponse = res;
                        break;
                    }
                    case "bookMovieTickets": {
                        String res = outMovieService.bookMovieTickets(userID,movieID,movieName,noOfTickets);
                        callbackResponse = res;
                        break;
                    }
                    case "getBookingSchedule": {
                        String res = outMovieService.getBookingScheduleUDP(userID);
                        callbackResponse = res;
                        break;
                    }
                    case "cancelMovieTickets": {
                        String res = outMovieService.cancelMovieTickets(userID,movieID,movieName,noOfTickets);
                        callbackResponse = res;
                        break;
                    }
                    case "exchangeTickets":{
                        String new_movieID = requestStringArr[5];
                        String res = outMovieService.exchangeTickets(userID,movieID,new_movieID,movieName,noOfTickets);
                        callbackResponse = res;
                        break;
                    }
                    case "exchangeTicketsCapacityUDP": {
                        String res = outMovieService.exchangeTicketsCapacityUDP(userID,movieID,movieName,noOfTickets);
                        callbackResponse = res;
                        break;
                    }
                }
                byte[] byteToSend = callbackResponse.getBytes();
                DatagramPacket response = new DatagramPacket(byteToSend, byteToSend.length, dp.getAddress(),
                        dp.getPort());
                ds.send(response);

            }

        }
        catch (SocketException e){
            System.out.println("Socket exception: " + e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
