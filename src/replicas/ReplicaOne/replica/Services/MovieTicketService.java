package replicas.ReplicaOne.replica.Services;

import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

//import Interface.AdminInterface;
//import Interface.CustomerInterface;

//import static java.net.HttpURLConnection.HTTP_OK;
//import org.omg.CORBA.ORB;
import org.omg.CORBA.ORB;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(endpointInterface = "replicas.ReplicaOne.replica.Interface.MovieInterface")

@SOAPBinding(style = SOAPBinding.Style.RPC)
public class MovieTicketService {
    //    MovieName > MovieID : BookingCapacity
    public HashMap<String, HashMap<String, Integer>> movieMap = new HashMap<>();
    //    UserID > MovieName > MovieID : noOfTickets
    public HashMap<String, HashMap<String, HashMap<String, Integer>>> userMap = new HashMap<>();
    DatagramSocket dss;
    String serverID = "";
    String serverName = "";
    int atwPort = 4556;
    int outPort = 4557;
    int verPort = 4558;
    private ORB orb;

    public static HashMap<String,String> file = new HashMap<>();

    public String log;
    public String Status;
    public int bookingLimit = 3;

    static {
        file.put("ATW","ATWserver.txt");
        file.put("VER", "VERserver.txt");
        file.put("OUT", "OUTserver.txt");
    }
    public MovieTicketService(String serverID,String serverName) throws Exception{
        super();
        this.serverID = serverID;
        this.serverName = serverName;
    }

    public MovieTicketService(DatagramSocket ds) throws Exception{
        super();
        this.dss = ds;
    }

    public MovieTicketService() throws RemoteException {
        super();

    }

    public String addMovieSlots(String movieID, String movieName, int bookingCapacity){
        log = "Slots not added.";
        Status = "Failed";

        if(!movieMap.isEmpty() && movieMap.containsKey(movieName)){
            HashMap<String,Integer> temp = new HashMap<>();
            for (Map.Entry<String, Integer> x : movieMap.get(movieName).entrySet()) {
                temp.put(x.getKey(), x.getValue());
            }
            temp.put(movieID,bookingCapacity);
            movieMap.put(movieName,temp);
        }
        else {
            HashMap<String,Integer> temp = new HashMap<>();
            temp.put(movieID,bookingCapacity);
            movieMap.put(movieName,temp);
        }
        log = "Slots added.";
        Status = "Passed";
        writeToLogFile("addMovieSlots","Movie- " + movieName+" Show at- "+movieID+" "+bookingCapacity,Status,bookingCapacity + " slots for movie " + movieName + " for movie ID " + movieID + " have been added.");
        return "Movie Slot Added.";
    }
    public String removeMovieSlots(String movieID, String movieName){
        log = "Slots not deleted.";
        Status = "Failed";
        String responseString = "";
        if(!movieMap.isEmpty() && movieMap.containsKey(movieName) && movieMap.get(movieName).containsKey(movieID)){

//            ArrayList dateList = new ArrayList();
//            for (var x : movieMap.get(movieName).entrySet()) {
//                dateList.add(x.getKey().substring(3));
//            }
//            ArrayList sortedDateList = sortDates(dateList);
//            int index = sortedDateList.indexOf(movieID.substring(3));
//            String updatedMovieID = (String) sortedDateList.get(index);
//            bookMovieTickets();

            movieMap.get(movieName).remove(movieID);
            log="Slots removed";
            Status="Passed";
            writeToLogFile("addMovieSlots","Movie- " + movieName+" Show at- "+movieID+" ",Status,"The slots for movie " + movieName + " for movie ID " + movieID + " have been deleted successfully.");
            responseString = "Movie slot deleted.";
            /**
             * TODO: Shift customer with existing movie slot
             * TODO: to the next available movie show
             */
        }
        else {
            writeToLogFile("addMovieSlots","Movie- " + movieName+" Show at- "+movieID+" ",Status,"The slots for movie " + movieName + " for movie ID " + movieID + " does not exist.");
            responseString = "Movie slot does not exists.";
        }
        return responseString;
    }
    public String listMovieShowAvailability(String movieName) {
        log="No Shows Available";
        Status="Failed";
        String serverOneResponse = "";
        String serverTwoResponse = "";

        String responseString = "";

        if(!movieMap.isEmpty()){
            if(movieMap.containsKey(movieName)) {
                for (Map.Entry<String, Integer> x : movieMap.get(movieName).entrySet()) {
                    responseString = responseString + "Movie Show: " + x.getKey() + " Capacity: " + x.getValue() + "\n";
                }
            }
        }

        if (this.serverID.equals("atw")) {
            serverOneResponse = sendMsgToServer("listMovieShowAvailability", null, movieName, null, 0, outPort,null);
            serverTwoResponse = sendMsgToServer("listMovieShowAvailability", null, movieName, null, 0, verPort,null);
        } else if (this.serverID.equals("out")) {
            serverOneResponse = sendMsgToServer("listMovieShowAvailability", null, movieName, null, 0, atwPort,null);
            serverTwoResponse = sendMsgToServer("listMovieShowAvailability", null, movieName, null, 0, verPort,null);
        } else if (this.serverID.equals("ver")) {
            serverOneResponse = sendMsgToServer("listMovieShowAvailability", null, movieName, null, 0, atwPort,null);
            serverTwoResponse = sendMsgToServer("listMovieShowAvailability", null, movieName, null, 0, outPort,null);
        }

        responseString = responseString + "\n" + serverOneResponse + "\n" + serverTwoResponse;
        if(responseString.equals("\n"+"\n"))
        {
            writeToLogFile("listMovieShowsAvailability",movieName,Status,"No Shows Available");
        }
        else{
            log="Show Available";
            Status="Passed";
            writeToLogFile("listMovieShowsAvailability",movieName,Status,responseString);
        }
        return responseString;
    }

    public String bookMovieTickets(String userID, String movieID, String movieName, int noOfTickets) {
        log="Booking Failed";
        Status="Failed";

        String targetServer = movieID.substring(0,3).toLowerCase();
        String serverResponse = "";

        String userTargetServer = userID.substring(0,3).toLowerCase();
        String userBookings = getBookingSchedule(userID);
        String[] userBookingsArray = userBookings.split("\n");

        if(!targetServer.equals(userTargetServer)){
            for (String x : userBookingsArray) {
                if (x.contains(movieID.substring(3))) {
                    serverResponse = "You already have a booking for a movie on this date.";
                    writeToLogFile("bookMovieTickets",userID+" "+movieID+" "+movieName+" "+noOfTickets,Status,"You already have a booking for a movie on this date.");
                    return serverResponse;
                }
            }
        }

        if(!targetServer.equals(userTargetServer) && this.bookingLimit <= 0){
            serverResponse = "Cannot make more than 3 bookings in other server";
            writeToLogFile("bookMovieTickets",userID+" "+movieID+" "+movieName+" "+noOfTickets,Status,"Cannot make more than 3 bookings in other server.");
            return serverResponse;
        }

//        boolean flag = false;
//        if(!userTargetServer.equals(targetServer) && noOfTickets > 3){
//            flag = true;
//        }
//        if(flag){
////            Improve condition
//            writeToLogFile("bookMovieTickets",userID+" "+movieID+" "+movieName+" "+noOfTickets,Status,"You cannot book more than 3 ticket in other servers.");
//            serverResponse = "Cannot book more than 3 ticket in other server";
//        }
        if(this.serverID.equals(targetServer)){
            if(movieMap.containsKey(movieName)){
                if(movieMap.get(movieName).containsKey(movieID)){
                    int capacity = movieMap.get(movieName).get(movieID);
                    if(capacity >= noOfTickets){
                        if(userMap.containsKey(userID)){
                            if(userMap.get(userID).containsKey(movieName)){
                                if(userMap.get(userID).get(movieName).containsKey(movieID)){
                                    int oldNoOfTickets = userMap.get(userID).get(movieName).get(movieID);
                                    userMap.get(userID).get(movieName).put(movieID,oldNoOfTickets + noOfTickets);
                                    movieMap.get(movieName).put(movieID,capacity - noOfTickets);
                                    log="Movie Tickets Updated";
                                    Status="Success";
                                    writeToLogFile("bookMovieTickets",userID+" "+movieID+" "+movieName+" "+noOfTickets,Status,"Tickets Booking Updated");
                                    serverResponse = "Tickets Booked.";
                                    bookingLimit--;
                                }
                                else {
                                    HashMap<String,Integer> temp = new HashMap<>();
                                    for (Map.Entry<String, Integer> x : userMap.get(userID).get(movieName).entrySet()) {
                                        temp.put(x.getKey(), x.getValue());
                                    }
                                    temp.put(movieID,noOfTickets);
                                    userMap.get(userID).put(movieName,temp);
                                    movieMap.get(movieName).put(movieID,capacity - noOfTickets);
                                    log="Movie Tickets Booked";
                                    Status="Success";
                                    writeToLogFile("bookMovieTickets",userID+" "+movieID+" "+movieName+" "+noOfTickets,Status,"Tickets Booking Booked");
                                    serverResponse = "Tickets Booked.";
                                    bookingLimit--;
                                }
                            }
                            else {
                                HashMap<String,Integer> subTemp = new HashMap<>();
                                subTemp.put(movieID,noOfTickets);
                                HashMap<String, HashMap<String, Integer>> temp = new HashMap<>();
                                temp.put(movieName,subTemp);
                                userMap.put(userID,temp);

//                            userMap.get(userID).put(movieID, new HashMap<String, Integer>());
//                            userMap.get(userID).get(movieID).put(movieName,noOfTickets);

                                movieMap.get(movieName).put(movieID,capacity - noOfTickets);
                                log="Movie Tickets Booked";
                                Status="Success";
                                writeToLogFile("bookMovieTickets",userID+" "+movieID+" "+movieName+" "+noOfTickets,Status,"Tickets Booking Booked");
                                serverResponse = "Tickets Booked.";
                                bookingLimit--;
                            }
                        }
                        else{
                            HashMap<String,Integer> subTemp = new HashMap<>();
                            subTemp.put(movieID,noOfTickets);
                            HashMap<String, HashMap<String, Integer>> temp = new HashMap<>();
                            temp.put(movieName,subTemp);
                            userMap.put(userID,temp);

//                            userMap.get(userID).put(movieID, new HashMap<String, Integer>());
//                            userMap.get(userID).get(movieID).put(movieName,noOfTickets);

                            movieMap.get(movieName).put(movieID,capacity - noOfTickets);
                            log="Movie Tickets Booked";
                            Status="Success";
                            writeToLogFile("bookMovieTickets",userID+" "+movieID+" "+movieName+" "+noOfTickets,Status,"Tickets Booking Booked");
                            serverResponse = "Tickets Booked.";
                            bookingLimit--;
                        }
                    }
                    else{
                        writeToLogFile("bookMovieTickets",userID+" "+movieID+" "+movieName+" "+noOfTickets,Status,"Entered value for tickets is more than booking capacity.");
                        serverResponse = "Entered value for tickets is more than booking capacity.";
                    }
                }
                else{
                    writeToLogFile("bookMovieTickets",userID+" "+movieID+" "+movieName+" "+noOfTickets,Status,"Incorrect MovieID. No Movie show exists.");
                    serverResponse = "Incorrect MovieID. No Movie show exists.";
                }
            }
            else {
                writeToLogFile("bookMovieTickets",userID+" "+movieID+" "+movieName+" "+noOfTickets,Status,"Incorrect Movie Name. Movie does not exists.");
                serverResponse = "Incorrect Movie Name. Movie does not exists.";
            }

        } else if (targetServer.equals("atw")) {
            serverResponse = sendMsgToServer("bookMovieTickets",userID,movieName,movieID,noOfTickets,atwPort,null);
        } else if (targetServer.equals("out")) {
            serverResponse = sendMsgToServer("bookMovieTickets",userID,movieName,movieID,noOfTickets,outPort,null);
        } else if (targetServer.equals("ver")) {
            serverResponse = sendMsgToServer("bookMovieTickets",userID,movieName,movieID,noOfTickets,verPort,null);
        }
        return serverResponse;
    }
    public String getBookingSchedule(String userID) {
        log="Schedule fetched successfully";
        Status="Passed";

        String serverOneResponse = "";
        String serverTwoResponse = "";
        String responseString = "";

        if(!userMap.isEmpty()) {
            if (userMap.containsKey(userID)) {
                for (Map.Entry<String, HashMap<String, Integer>> x : userMap.get(userID).entrySet()) {
                    responseString = responseString + "Tickets booked for movie: " + x.getKey() + " at: " + x.getValue() + "\n";
                }
            }
        }

        if(this.serverID.equals("atw")){
            serverOneResponse = sendMsgToServer("getBookingSchedule",userID,null,null,0,outPort,null);
            serverTwoResponse = sendMsgToServer("getBookingSchedule",userID,null,null,0,verPort,null);
        } else if(this.serverID.equals("out")){
            serverOneResponse = sendMsgToServer("getBookingSchedule",userID,null,null,0,atwPort,null);
            serverTwoResponse = sendMsgToServer("getBookingSchedule",userID,null,null,0,verPort,null);
        } else if (this.serverID.equals("ver")) {
            serverOneResponse = sendMsgToServer("getBookingSchedule",userID,null,null,0,atwPort,null);
            serverTwoResponse = sendMsgToServer("getBookingSchedule",userID,null,null,0,outPort,null);
        }
        responseString = responseString + "\n" + serverOneResponse + "\n" + serverTwoResponse;
        writeToLogFile("getBookingSchedule",userID,Status,responseString);
        return responseString;
    }
    public String cancelMovieTickets(String userID, String movieID, String movieName, int noOfTicketsToCancel) {
        log="Movie tickets cancelled successfully";
        Status="Passed";

        String targetServer = movieID.substring(0,3).toLowerCase();
        String serverResponse = "";

        if(this.serverID.equals(targetServer)){
            if(movieMap.containsKey(movieName)){
                if(movieMap.get(movieName).containsKey(movieID)){
                    if(userMap.containsKey(userID) &&  userMap.get(userID).containsKey(movieName) && userMap.get(userID).get(movieName).containsKey(movieID)){
                        int ticketsBooked = userMap.get(userID).get(movieName).get(movieID);
                        if(noOfTicketsToCancel <= ticketsBooked){
                            if(noOfTicketsToCancel < ticketsBooked){
                                userMap.get(userID).get(movieName).put(movieID,ticketsBooked - noOfTicketsToCancel);
                                int capacity = movieMap.get(movieName).get(movieID);
                                movieMap.get(movieName).put(movieID,capacity + noOfTicketsToCancel);
                                writeToLogFile("cancelMovieTickets",userID+" "+movieID+" "+movieName+" "+noOfTicketsToCancel,Status,"Tickets successfully cancelled!");
                                serverResponse = "Tickets successfully cancelled!";
                            }
                            else {
                                userMap.get(userID).get(movieName).remove(movieID);
                                int capacity = movieMap.get(movieName).get(movieID);
                                movieMap.get(movieName).put(movieID,capacity + noOfTicketsToCancel);
                                writeToLogFile("cancelMovieTickets",userID+" "+movieID+" "+movieName+" "+noOfTicketsToCancel,Status,"Tickets successfully cancelled!");
                                serverResponse = "Tickets successfully cancelled!";
                            }
                        }
                        else {
                            log="No. of tickets to cancel is more than booked tickets";
                            Status="Failed";
                            writeToLogFile("cancelMovieTickets",userID+" "+movieID+" "+movieName+" "+noOfTicketsToCancel,Status,"No. of tickets to cancel is more than booked tickets");
                            serverResponse = "No. of tickets to cancel is more than booked tickets";
                        }
                    }
                    else {
                        log="No booking found for user.";
                        Status="Failed";
                        writeToLogFile("cancelMovieTickets",userID+" "+movieID+" "+movieName+" "+noOfTicketsToCancel,Status,"No booking found for user.");
                        serverResponse = "No booking found for user.";
                    }
                }
                else {
                    log="No movie show found.";
                    Status="Failed";
                    writeToLogFile("cancelMovieTickets",userID+" "+movieID+" "+movieName+" "+noOfTicketsToCancel,Status,"No movie show found.");
                    serverResponse = "No movie show found.";
                }
            }
            else {
                log="No movie found.";
                Status="Failed";
                writeToLogFile("cancelMovieTickets",userID+" "+movieID+" "+movieName+" "+noOfTicketsToCancel,Status,"No movie found.");
                serverResponse = "No movie found.";
            }
        } else if (targetServer.equals("atw")) {
            serverResponse = sendMsgToServer("cancelMovieTickets",userID,movieName,movieID,noOfTicketsToCancel,atwPort,null);
        } else if (targetServer.equals("out")) {
            serverResponse = sendMsgToServer("cancelMovieTickets",userID,movieName,movieID,noOfTicketsToCancel,outPort,null);
        } else if (targetServer.equals("ver")) {
            serverResponse = sendMsgToServer("cancelMovieTickets",userID,movieName,movieID,noOfTicketsToCancel,verPort,null);
        }
        return serverResponse;
    }

    public String listMovieShowAvailabilityUDP(String movieName) {
        String responseString = "";
        if(movieMap.containsKey(movieName)){
            for(Map.Entry<String, Integer> x: movieMap.get(movieName).entrySet()){
                responseString = responseString + "Movie Show: " + x.getKey() + " Capacity: " + x.getValue() + "\n";
            }
        }
        log="Getting Movie List Availability using UDP";
        Status="Passed";
        writeToLogFile("listMovieShowAvailabilityUDP",movieName,Status,"List Movie Shows Availability using UDP from " +this.serverName +" server.");
        return responseString;
    }
    public String exchangeTickets(String userID, String movieID, String new_movieID, String new_movieName, int numberOfTickets){
        log="Movie tickets exchanged successfully";
        Status="Passed";
        String targetServer = movieID.substring(0,3).toLowerCase();
        String newTargetServer = new_movieID.substring(0,3).toLowerCase();
        String responseString = "";

        String oldMovieName = "";
        if(this.serverID.equals(targetServer)){
            if(!userMap.isEmpty()) {
                if (userMap.containsKey(userID)) {
                    boolean oldMovieIDExists = false;
                    String newMovieCapacityExists = "";
                    for (Map.Entry<String, HashMap<String, Integer>> x : userMap.get(userID).entrySet()) {
                        oldMovieName = String.valueOf(x.getKey());
                        for(Map.Entry<String, Integer> y: x.getValue().entrySet()){
                            if(y.getKey().equals(movieID)){
                                oldMovieIDExists = true;
                                break;
                            }
                        }
                    }
                    if(oldMovieIDExists){
                        if(this.serverID.equals(newTargetServer)){
                            if(movieMap.containsKey(new_movieName)){
                                if(movieMap.get(new_movieName).containsKey(new_movieID)){
                                    if(movieMap.get(new_movieName).get(new_movieID) >= numberOfTickets){
                                        newMovieCapacityExists = "true";
                                    }
                                }
                            }

                        } else if (newTargetServer.equals("atw")) {
                            newMovieCapacityExists = sendMsgToServer("exchangeTicketsCapacityUDP",userID,new_movieName,new_movieID,numberOfTickets,atwPort,null);
                        } else if (newTargetServer.equals("out")) {
                            newMovieCapacityExists = sendMsgToServer("exchangeTicketsCapacityUDP",userID,new_movieName,new_movieID,numberOfTickets,outPort,null);
                        } else if (newTargetServer.equals("ver")) {
                            newMovieCapacityExists = sendMsgToServer("exchangeTicketsCapacityUDP",userID,new_movieName,new_movieID,numberOfTickets,verPort,null);
                        }
                        if(newMovieCapacityExists.equals("true")){
                            String res2 = this.cancelMovieTickets(userID,movieID,oldMovieName,numberOfTickets);

                            if(res2.equals("Tickets successfully cancelled!")){
                                String res1 = this.bookMovieTickets(userID,new_movieID,new_movieName,numberOfTickets);
                                if(!res1.equals("Tickets Booked.")){
                                    responseString = "Movie Tickets Exchange Failed!";
                                    writeToLogFile("exchangeTickets",new_movieName,Status,"Movie Tickets Exchange Failed!");
                                    String res3 = this.bookMovieTickets(userID,movieID,oldMovieName,numberOfTickets);
                                }
                                else{
                                    responseString = "Movie Tickets Exchanged!";
                                    writeToLogFile("exchangeTickets",new_movieName,Status,"Tickets exchanged on " +this.serverName +" server.");
                                }
                            }

//                            responseString = "Movie Tickets Exchanged!";
                            writeToLogFile("exchangeTickets",new_movieName,Status,"Tickets exchanged on " +this.serverName +" server.");
                        }
                    }
                    else {
                        log="No booking found for user.";
                        Status="Failed";
                        writeToLogFile("exchangeTickets",userID+" "+movieID+" "+new_movieID+" "+new_movieName+" "+numberOfTickets,Status,"No booking found for user.");
                        responseString = "No booking found for user.";
                    }

                }
                else {
                    log="No booking found for user.";
                    Status="Failed";
                    writeToLogFile("exchangeTickets",userID+" "+movieID+" "+new_movieID+" "+new_movieName+" "+numberOfTickets,Status,"No booking found for user.");
                    responseString = "No booking found for user.";
                }
            }
        } else if (targetServer.equals("atw")) {
            responseString = sendMsgToServer("exchangeTickets",userID,new_movieName,movieID,numberOfTickets,atwPort,new_movieID);
        } else if (targetServer.equals("out")) {
            responseString = sendMsgToServer("exchangeTickets",userID,new_movieName,movieID,numberOfTickets,outPort,new_movieID);
        } else if (targetServer.equals("ver")) {
            responseString = sendMsgToServer("exchangeTickets",userID,new_movieName,movieID,numberOfTickets,verPort,new_movieID);
        }

        return responseString;
    }
    public String getBookingScheduleUDP(String userID){
        String responseString = "";
        if(!userMap.isEmpty()){
            if(userMap.containsKey(userID)){
                for(Map.Entry<String, HashMap<String, Integer>> x : userMap.get(userID).entrySet()){
                    responseString = responseString + "Tickets booked for movie: " + x.getKey() + " at: " + x.getValue() + "\n";
                }
            }
        }
        log="Getting Booking Schedule using UDP";
        Status="Passed";
        writeToLogFile("getBookingScheduleUDP",userID,Status,"Getting Booking Schedule using UDP from " +this.serverName +" server.");
        return responseString;
    }
    public String exchangeTicketsCapacityUDP(String userID, String new_movieID, String new_movieName, int numberOfTickets) {
        String newMovieCapacityExists = "";
        if(movieMap.containsKey(new_movieName)){
            if(movieMap.get(new_movieName).containsKey(new_movieID)){
                if(movieMap.get(new_movieName).get(new_movieID) >= numberOfTickets){
                    newMovieCapacityExists = "true";
                }
            }
        }
        return newMovieCapacityExists;
    }
    public String sendMsgToServer(String func, String userID,String movieName, String movieID,int noOfTickets, int port,String newMovieID) {
        try{
            /**
             * UDP request to
             * remote server
             */
            DatagramSocket ds = new DatagramSocket();
            String requestString = func + ";" + userID + ";" + movieName + ";" + movieID + ";" + noOfTickets + ";" + newMovieID;
            byte[] request = requestString.getBytes();
            InetAddress ia = InetAddress.getLocalHost();
            DatagramPacket dp = new DatagramPacket(request,request.length,ia,port);
            ds.send(dp);

            /**
             * UDP response from
             * remote server
             */
            byte[] byteReceive = new byte[2048];
            DatagramPacket dpReceived = new DatagramPacket(byteReceive,byteReceive.length);
            ds.receive(dpReceived);
            System.out.println(dpReceived.getData());
            return new String(dpReceived.getData()).trim();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public void writeToLogFile(String operation, String params, String status, String response) {
        try {
            FileWriter myLogWriter = new FileWriter("C:\\Users\\laksh\\Intellij-workspace\\MovieTicketBookingSystemProject\\src\\replicas\\ReplicaOne\\replica\\Logs\\"+file.get(this.serverName),true);
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String log = dateFormat.format(LocalDateTime.now()) + " : " + operation + " : " + params + " : " + status
                    + " : " + response + "\n";
            myLogWriter.write(log);
            myLogWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
