package replicas.ReplicaFour.replica.Implementation;

import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import replicas.ReplicaFour.replica.Interface.MovieTicketInterface;
@WebService(endpointInterface = "replicas.ReplicaFour.replica.Interface.MovieTicketInterface")
@SOAPBinding(style=Style.RPC)
public class MovieTicketImplementation {
    //Nested Hashmap for movie. <movieName,<movieSlotID,Capacity>>
    protected HashMap<String,HashMap<String,Integer>> movieDataMap=new HashMap<>();
    //Nested Hashmap for user and movies. <userID<movieName<movieSlotID,numberOfTickets>>>
    protected HashMap<String,HashMap<String,HashMap<String,Integer>>> customerDataMap=new HashMap<>();

    int numberOfSlotsBookedInOtherServer=0;
    public String  serverName="";
    public String serverID="";

    protected int aPort=3800;
    protected int vPort=3801;
    protected int oPort=3802;
    protected final String bookingtoOtherOpen="Ticket Canceled now you can book ticket for one more slot in other server";
    protected final String bookingSuccess="Ticket booked Successfully";
    protected final String cancelSuccess="Ticket Canceled";
    protected final String bookedInOther="Yes";
    protected final String notBookedInOther="No";
    public MovieTicketImplementation(String serverID) throws Exception {
        super();
        this.serverID=serverID;
    }
    /**
     * to handle exception by UnicastRemoteObject
     * @throws Exception
     */
    public MovieTicketImplementation() throws Exception{
        super();
    }

    //Hash Map for log files
    public static HashMap<String,String> file = new HashMap<>();

    public String log;
    public String Status;

    static {
        file.put("Atwater","Atwater.txt");
        file.put("Verdun", "Verdun.txt");
        file.put("Outremont", "Outremont.txt");
    }

    /**
     * Log Writter method. This method is invoked to write in log file.
     * @param operation
     * @param params
     * @param status
     * @param responceDetails
     */
    public void logWriter(String operation, String params, String status, String responceDetails) {
        try {
            FileWriter myWriter = new FileWriter("C:\\Users\\manish\\eclipse-workspace\\DistributedMovieTicketSystem\\src\\Logs\\"+file.get(this.serverName),true);
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String log = dateFormat.format(LocalDateTime.now()) + " : " + operation + " : " + params + " : " + status
                    + " : " + responceDetails + "\n";
            myWriter.write(log);
            myWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * This method implements the sends request to the server using UDP
     * @param userID
     * @param movieName
     * @param movieSlotID
     * @param numberOfTickets
     * @param port
     * @param funcationality
     * @return
     * @throws IOException
     */
    public  String sendRequestToServer(String userID,String movieName,String movieSlotID,int numberOfTickets,int port, String funcationality,String newMovieName, String newMovieID) throws IOException {
        try {
            //Send request to server
            DatagramSocket ds=new DatagramSocket();
            ds.setSoTimeout(10000);
            String stringToSend=userID+"#"+movieName+"#"+movieSlotID+"#"+numberOfTickets+"#"+port+"#"+funcationality+"#"+newMovieName+"#"+newMovieID;		//message to send through udp
            byte[] bArray=stringToSend.getBytes();
            InetAddress ia=InetAddress.getLocalHost();
            DatagramPacket dp1=new DatagramPacket(bArray, bArray.length,ia,port);
            ds.send(dp1);

            //recieve response from the server
            byte [] b2=new byte[1024];
            DatagramPacket dp2=new DatagramPacket(b2, b2.length);
            try {
                ds.receive(dp2);
            }
            catch (SocketTimeoutException e) {
                e.printStackTrace();
            }

            String response=new String(dp2.getData()).trim();
            ds.close();
            return response;
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    /**
     * This method adds movie slots.
     */
    public String addMovieSlots(String movieID, String movieName, int bookingCapacity){
        //Not empty and contains the movie case
        if(!movieDataMap.isEmpty() && movieDataMap.containsKey(movieName)) {
            //This map will store the content of the movieDataMap on a temporary basis.
            HashMap <String,Integer> map=new HashMap();
            for (Entry<String, Integer> data : movieDataMap.get(movieName).entrySet()) {
                map.put(data.getKey(),data.getValue());
            }
            if (map.containsKey(movieID)) {
                int updatedCapacity=map.get(movieID)+bookingCapacity;
                map.put(movieID,updatedCapacity);
            }
            else {
                map.put(movieID, bookingCapacity);
            }
            movieDataMap.put(movieName, map);
        }
        //Doesnot contain the movie case
        else {
            HashMap<String,Integer> map = new HashMap<>();
            map.put(movieID,bookingCapacity);
            movieDataMap.put(movieName, map);
        }
        log = "Slots added Successfully";
        Status = "Passed";
        logWriter("Add movie slots",movieID+" "+movieName+" "+bookingCapacity,Status,  movieName + " Slots " + movieID+" "+bookingCapacity + " available");
        return "Movie Slot Added Successfuly ";
    }
    /**
     * This method removes movie slots.
     */
    public String removeMovieSlots (String movieID, String movieName){
        String result="";
        boolean removed=false;
        if (!movieDataMap.isEmpty() && movieDataMap.containsKey(movieName) && movieDataMap.get(movieName).containsKey(movieID)) {
            movieDataMap.get(movieName).remove(movieID);
            result="Movie slot removed succefully";
            removed=true;
        }
        else {
            result="Movie Slots doesnot exists";
            removed=false;
        }
        if(removed) {
            log = "Slot removed Successfully";
            Status = "Passed";
            logWriter("Remove Movie Slot ",movieID+" "+movieName+" ",Status,  movieName + " Slots " + movieID+" removed successfully.");
        }
        else {
            log = "Slots doesnot exits";
            Status = "Failed";
            logWriter("Remove Movie Slot ",movieID, Status ," Failed to remove the slot.");
        }
        return result;
    }

    /**
     * This method shows the availability of the movie.
     */
    public String listMovieShowAvailability(String movieName){
        String fromMyServer="";								//response from this server
        String responseOtherServerOne="";					//response from other server
        String responseOtherServerTwo="";					//response from other server
        String function="listMovieShowsAvailability";
        String	allResponse="";								//final response
        if(!movieDataMap.isEmpty()) {
            if(movieDataMap.containsKey(movieName)) {
                for (Entry<String, Integer> data:movieDataMap.get(movieName).entrySet()) {
                    fromMyServer= fromMyServer+" Movie Slot:"+ data.getKey() + "Availability:"+data.getValue();
                }
            }
        }

        if (this.serverID.equals("ATW")) {
            try {
                responseOtherServerOne=sendRequestToServer("",movieName,"",0,oPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                responseOtherServerTwo=sendRequestToServer("",movieName,"",0,vPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if (this.serverID.equals("OUT")) {
            try {
                responseOtherServerOne=sendRequestToServer("",movieName,"",0,aPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                responseOtherServerTwo=sendRequestToServer("",movieName,"",0,vPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        else if (this.serverID.equals("VER")) {
            try {
                responseOtherServerOne=sendRequestToServer("",movieName,"",0,aPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                responseOtherServerTwo=sendRequestToServer("",movieName,"",0,oPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        allResponse=fromMyServer+" "+responseOtherServerOne+" "+responseOtherServerTwo;
        log = "Show Availability";
        Status = "Success";
        logWriter("Show availability for ",movieName+" ", Status ," Show Availibility Displayed");
        return allResponse;
    }

    /**
     *Method for booking tickets. This will be invoked by both admin and customers.
     */
    public String bookMovieTickets(String customerID, String movieID,String movieName, int numberOfTickets){
        String toServer=movieID.substring(0,3).toUpperCase().trim();		//Movie slot ID will give the respective server where the message is targeted to.
        String customerFromServer=customerID.substring(0,3).toUpperCase().trim();	//To check where the current logged in customer is from.
        String result="";
        String function="bookMovieTicket";


        //check for the otherserver and number of tickets greater than 3 conditions.
//			if (!customerFromServer.equals(toServer) && numberOfTickets>3 ){
//				log = "Book Movie Ticket";
//	            Status = "Failed";
//	            logWriter("Ticket booking for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status ," user cannot book for than 3 tickets in other server.");
//				result="You cannot book more than 3 tickets in other server";
//			}

        //if the server is same server.
        if (this.serverID.equals(toServer)) {
            if(movieDataMap.containsKey(movieName)) {
                if (movieDataMap.get(movieName).containsKey(movieID)) {
                    boolean sameSlotBooked=checkSlotCollision(customerID,movieID,movieName);
                    if (!sameSlotBooked) {
                        int slotsAvailable=movieDataMap.get(movieName).get(movieID); //number of available slots
                        if (slotsAvailable>0) {
                            //whether slot is less than the number of tickets to be booked.
                            if(slotsAvailable>=numberOfTickets) {
                                if (customerDataMap.containsKey(customerID)) {
                                    if (customerDataMap.get(customerID).containsKey(movieName)) {
                                        if (customerDataMap.get(customerID).get(movieName).containsKey(movieID)) {
                                            int previousNumberOfTicket=customerDataMap.get(customerID).get(movieName).get(movieID); //previously booked ticket number
                                            int updatedNumberOfTickets=previousNumberOfTicket+numberOfTickets;						//number of ticket of a particular user.
                                            customerDataMap.get(customerID).get(movieName).put(movieID, updatedNumberOfTickets);
                                            movieDataMap.get(movieName).put(movieID, slotsAvailable-numberOfTickets);
                                            log = "Book Movie Ticket";
                                            Status = "Success";
                                            logWriter("Ticket booking for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" " ,numberOfTickets+ " Movie Tickets booked successfully.");
                                            result=bookingSuccess;
                                        }
                                        else {
                                            HashMap<String,Integer> map=new HashMap<>();
                                            for(Entry<String, Integer> data : customerDataMap.get(customerID).get(movieName).entrySet()) {
                                                map.put(data.getKey(), data.getValue());
                                            }
                                            map.put(movieID, numberOfTickets);
                                            customerDataMap.get(customerID).put(movieName, map);
                                            movieDataMap.get(movieName).put(movieID, slotsAvailable-numberOfTickets);	//slots deduction after booking
                                            log = "Book Movie Ticket";
                                            Status = "Success";
                                            logWriter("Ticket booking for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", numberOfTickets+ " Movie Tickets booked successfully.");
                                            result=bookingSuccess;
                                        }
                                    }
                                    else {
                                        HashMap<String,Integer> innerMap=new HashMap<>();
                                        innerMap.put(movieID, numberOfTickets);
                                        HashMap <String,HashMap<String, Integer>> map=new HashMap<>();
                                        for(Entry<String, HashMap<String, Integer>> data: customerDataMap.get(customerID).entrySet()) {
                                            map.put(data.getKey(),data.getValue());
                                        }
                                        map.put(movieName, innerMap);
                                        customerDataMap.put(customerID, map);
                                        movieDataMap.get(movieName).put(movieID, slotsAvailable-numberOfTickets);
                                        log = "Book Movie Ticket";
                                        Status = "Success";
                                        logWriter("Ticket booking for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", numberOfTickets+ " Movie Tickets booked successfully.");
                                        result=bookingSuccess;
                                    }
                                }
                                else {
                                    HashMap<String,Integer> innerMap=new HashMap<>();
                                    innerMap.put(movieID, numberOfTickets);
                                    HashMap<String,HashMap<String,Integer>> outerMap=new HashMap<>();
                                    outerMap.put(movieName, innerMap);
                                    customerDataMap.put(customerID, outerMap);
                                    movieDataMap.get(movieName).put(movieID,slotsAvailable-numberOfTickets);
                                    log = "Book Movie Ticket";
                                    Status = "Success";
                                    logWriter("Ticket booking for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", numberOfTickets+ " Movie Tickets booked successfully.");
                                    result=bookingSuccess;
                                }
                            }
                            else {
                                log = "Book Movie Ticket";
                                Status = "Failed";
                                logWriter("Ticket booking for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " Booking failed because user tried to book more than available ");
                                result="You cannot book more than available. Only "+slotsAvailable+" seats available";
                            }
                        }
                        else {
                            log = "Book Movie Ticket";
                            Status = "Failed";
                            logWriter("Ticket booking for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " Show Houseful ");
                            result="No Ticket available for this slot";
                        }

                    }
                    else {
                        result="Cannot booked because same movie same slot is already booked.";
                    }
                }
                else {
                    log = "Book Movie Ticket";
                    Status = "Failed";
                    logWriter("Ticket booking for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " Movie Slot Doesnot exists.");
                    result="Movie Slot doesnot exists";
                }
            }
            else {
                log = "Book Movie Ticket";
                Status = "Failed";
                logWriter("Ticket booking for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " Movie Doesnot exists.");
                result="Movie doesnot exists";
            }

        }
        //If servers is other.
        else if (toServer.equals("ATW")) {
            try {
                result=sendRequestToServer(customerID, movieName, movieID, numberOfTickets, aPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if(toServer.equals("OUT")){
            try {
                result=sendRequestToServer(customerID, movieName, movieID, numberOfTickets, oPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if(toServer.equals("VER")) {
            try {
                result=sendRequestToServer(customerID, movieName, movieID, numberOfTickets, vPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * This method displays the booking schedule of the current user.
     * @throws
     */
    public String getBookingSchedule(String customerID){
        String function="getBookingSchedule";
        String firstServerReply="";
        String secondServerReply="";
        String finalResult="";
        if(!customerDataMap.isEmpty() && customerDataMap.containsKey(customerID)){
            for (Entry<String, HashMap<String, Integer>> data:customerDataMap.get(customerID).entrySet()) {
                finalResult=finalResult+" Movie Name: "+data.getKey()+"for slots: "+data.getValue()+"\n";
            }
        }

        if (this.serverID.equals("ATW")) {
            try {
                firstServerReply=sendRequestToServer(customerID,"","",0,oPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                secondServerReply=sendRequestToServer(customerID, "", "", 0, vPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if (this.serverID.equals("OUT")) {
            try {
                firstServerReply=sendRequestToServer(customerID,"","",0,aPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                secondServerReply=sendRequestToServer(customerID, "", "", 0, vPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if (this.serverID.equals("VER")) {
            try {
                firstServerReply=sendRequestToServer(customerID,"","",0,aPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                secondServerReply=sendRequestToServer(customerID, "", "", 0, oPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        finalResult=finalResult+" "+firstServerReply+" "+ secondServerReply;
        log = "Booking Schedule";
        Status = "Success";
        logWriter("Ticket Schedule for ","Username: "+customerID +" ", Status+" ", "Booking Schedule successfully displayed");
        return finalResult;
    }

    /**
     * This is a method to cancel booked movie tickets.
     */
    public String cancelMovieTickets(String customerID,String movieID, String movieName,int numberOfTickets){
        String customerFrom=customerID.substring(0,3);
        String toServer=movieID.substring(0,3).toUpperCase().trim();
        String result="";
        String function="cancelMovieTickets";
        if (this.serverID.equals(toServer)) {
            if (movieDataMap.containsKey(movieName)) {
                if(movieDataMap.get(movieName).containsKey(movieID)) {
                    if(customerDataMap.containsKey(customerID)) {
                        if(customerDataMap.get(customerID).containsKey(movieName)) {
                            if(customerDataMap.get(customerID).get(movieName).containsKey(movieID)) {
                                int previousBooking=customerDataMap.get(customerID).get(movieName).get(movieID);
                                int availability=movieDataMap.get(movieName).get(movieID);
                                if(numberOfTickets<=previousBooking) {
                                    int newNumberOfTicket=previousBooking-numberOfTickets;
                                    if(newNumberOfTicket>0) {
                                        customerDataMap.get(customerID).get(movieName).put(movieID, newNumberOfTicket);
                                        movieDataMap.get(movieName).put(movieID, availability+numberOfTickets);

                                        log = "Cancel Movie Ticket";
                                        Status = "Success";
                                        logWriter("Ticket Canceled for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " Movie Ticket cancelled successfully ");
                                        result=cancelSuccess;
                                    }
                                    else {
                                        customerDataMap.get(customerID).get(movieName).remove(movieID);
                                        movieDataMap.get(movieName).put(movieID, availability+numberOfTickets);
                                        log = "Cancel Movie Ticket";
                                        Status = "Success";
                                        logWriter("Ticket Canceled for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " Movie Ticket cancelled successfully ");
                                        if(!customerFrom.equals(this.serverID)) {
                                            result=bookingtoOtherOpen;
                                        }
                                        else {
                                            result=cancelSuccess;
                                        }

                                    }
                                }
                                else {
                                    log = "Cancel Movie Ticket";
                                    Status = "Failed";
                                    logWriter("Ticket Canceled failed for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " You cannot cancelled for than you booked ");
                                    result="You cannot canceled more than you booked. You booked "+ previousBooking +" previously";
                                }
                            }
                            else {
                                log = "Cancel Movie Ticket";
                                Status = "Failed";
                                logWriter("Ticket Canceled failed for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " No booking slot available ");
                                result="There is no booking for this slot";
                            }
                        }
                        else {
                            log = "Cancel Movie Ticket";
                            Status = "Failed";
                            logWriter("Ticket Canceled failed for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " No booking for this movie available ");
                            result="There is no booking for this movie";
                        }
                    }
                    else {
                        log = "Cancel Movie Ticket";
                        Status = "Failed";
                        logWriter("Ticket Canceled failed for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " Nothing booked for the customer ");
                        result="There is no booking for the customer";
                    }
                }
                else {
                    log = "Cancel Movie Ticket";
                    Status = "Failed";
                    logWriter("Ticket Canceled failed for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " Slot does not exists ");
                    result="Movie slot not found";
                }
            }
            else {
                log = "Cancel Movie Ticket";
                Status = "Failed";
                logWriter("Ticket Canceled failed for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " Movie doesnot exists ");
                result="Movie doesnot exists";
            }

        }
        else if (toServer.equals("ATW")) {
            try {
                result=sendRequestToServer(customerID, movieName, movieID, numberOfTickets, aPort,function,"","");
                if(result.equals(bookingtoOtherOpen)) {
                    numberOfSlotsBookedInOtherServer--;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if(toServer.equals("OUT")){
            try {
                result=sendRequestToServer(customerID, movieName, movieID, numberOfTickets, oPort,function,"","");
                if(result.equals(bookingtoOtherOpen)) {
                    numberOfSlotsBookedInOtherServer--;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if(toServer.equals("VER")) {
            try {
                result=sendRequestToServer(customerID, movieName, movieID, numberOfTickets, vPort,function,"","");
                if(result.equals(bookingtoOtherOpen)) {
                    numberOfSlotsBookedInOtherServer--;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * This methods list all the availabilty of particular moive from other servers.
     * @param movieName
     * @return
     * @throws IOException
     */
    public String listMovieShowsAvailabilityFromOther(String movieName){
        String	allResponse="";
        if(!movieDataMap.isEmpty()) {
            if(movieDataMap.containsKey(movieName)) {
                for (Entry<String, Integer> data:movieDataMap.get(movieName).entrySet()) {
                    allResponse=allResponse+" Movie Slot:"+ data.getKey() + "Availability:"+data.getValue();
                }
            }
        }
        return allResponse;
    }


    /**
     * This method returns booking schedule from other servers.
     * @param customerID
     * @return
     */
    public String getBookingScheduleFromOther(String customerID) {
        String result="";
        if(!customerDataMap.isEmpty() && customerDataMap.containsKey(customerID)){
            for (Entry<String, HashMap<String, Integer>> data:customerDataMap.get(customerID).entrySet()) {
                result=result+" Movie Name: "+data.getKey()+" for slots: "+data.getValue()+"\n";
            }
        }
        return result;
    }

    public synchronized String exchangeTickets(String customerID,String movieName, String movieID,String newMovieID,String newMovieName,int numberOfTicketsToCancel){
        String funcationality="exchangeTickets";
        String result="";
        String customerFromServer=customerID.substring(0,3).toUpperCase().trim();
        String movieBookedFromServer=movieID.substring(0,3).toUpperCase().trim();
        String newMovieToBookServer=newMovieID.substring(0,3).toUpperCase().trim();

        if(movieBookedFromServer.equals(this.serverID)) {
            if(!customerDataMap.isEmpty() && customerDataMap.containsKey(customerID)) {
                if(customerDataMap.get(customerID).containsKey(movieName)){
                    if(customerDataMap.get(customerID).get(movieName).containsKey(movieID)) {
                        int numberOfTicketsBooked=customerDataMap.get(customerID).get(movieName).get(movieID);
                        if(numberOfTicketsBooked>=numberOfTicketsToCancel) {
                            if(newMovieToBookServer.equals(this.serverID)) {
                                if(movieDataMap.get(newMovieName).get(newMovieID)>=numberOfTicketsToCancel) {
                                    //Update movieMap
                                    //bookingSuccess
                                    String bookMovierequest="";
//										bookMovierequest=bookMovieTicket(customerID, newMovieID, newMovieName, numberOfTicketsToCancel);
//										if(bookMovierequest.equals(bookingSuccess)) {
//											cancelMovieTickets(customerID, movieID, movieName, numberOfTicketsToCancel);
//											result="Ticket Exchanged Successfully";
//										}
//										else{
//											result=bookMovierequest;
//										}
                                    //This is cancel first
                                    String cancelTicketRequest="";
                                    if(customerFromServer.equals(this.serverID) && numberOfSlotsBookedInOtherServer>=3) {
                                        result="Cannot exchange because this will excede allowed number of ticket to booked in other server.";
                                    }
                                    else {
                                        cancelTicketRequest=cancelMovieTickets(customerID, movieID, movieName, numberOfTicketsToCancel);
                                        if(cancelTicketRequest.equals(cancelSuccess)|| cancelTicketRequest.equals(bookingtoOtherOpen)) {
                                            bookMovierequest=bookMovieTickets(customerID, newMovieID, newMovieName, numberOfTicketsToCancel);
                                            result="Ticket Exchanged Successfully";
                                        }
                                        else{
                                            result=cancelTicketRequest;
                                        }
                                    }
                                }
                                else {
                                    result="Not Sufficient Seat Available";
                                }
                            }
                            else if(newMovieToBookServer.equals("ATW")){
                                funcationality="bookingInExchange";
                                String bookMovierequest="";
                                String cancelTicketRequest="";
                                if(customerFromServer.equals(this.serverID) && numberOfSlotsBookedInOtherServer>=3) {
                                    result="Cannot exchange because this will excede allowed number of ticket to booked in other server.";
                                }
                                else {
//										try {
//											bookMovierequest = sendRequestToServer(customerID, movieName, movieID, numberOfTicketsToCancel,aPort,funcationality,newMovieName,newMovieID).trim();
//										} catch (IOException e) {
//											// TODO Auto-generated catch block
//											e.printStackTrace();
//										}
//										if(bookMovierequest.equals(bookingSuccess)) {
//											//Cancelling after successful booking
//											cancelMovieTickets(customerID, movieID, movieName, numberOfTicketsToCancel);
//											result= "Ticket Exchanged Successfully";
//										}
//										else {
//											result= bookMovierequest;
//										}
                                    //Cancel first
                                    cancelTicketRequest = cancelMovieTickets(customerID, movieID, movieName, numberOfTicketsToCancel);
                                    if (cancelTicketRequest.equals(cancelSuccess) || cancelTicketRequest.equals(bookingtoOtherOpen) ) {
                                        try {
                                            sendRequestToServer(customerID, movieName, movieID, numberOfTicketsToCancel,aPort,funcationality,newMovieName,newMovieID).trim();
                                            result="Ticket Exchanged Successfully";
                                            numberOfSlotsBookedInOtherServer++;
                                        }
                                        catch(IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else {
                                        result= cancelTicketRequest;
                                    }
                                }

                            }
                            else if(newMovieToBookServer.equals("VER")){
//									funcationality="bookingInExchange";
//									String bookMovierequest="";
//									try {
//										bookMovierequest = sendRequestToServer(customerID, movieName, movieID, numberOfTicketsToCancel,vPort,funcationality,newMovieName,newMovieID).trim();
//									} catch (IOException e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									}
//									if(bookMovierequest.equals(bookingSuccess)) {
//										//Cancelling after successful booking
//										cancelMovieTickets(customerID, movieID, movieName, numberOfTicketsToCancel);
//										result= "Ticket exchanged Successfully";
//									}
//									else {
//										result= bookMovierequest;
//									}
//
                                funcationality="bookingInExchange";
                                String bookMovierequest="";
                                String cancelTicketRequest="";
                                if(customerFromServer.equals(this.serverID) && numberOfSlotsBookedInOtherServer>=3) {
                                    result="Cannot exchange because this will excede allowed number of ticket to booked in other server.";
                                }
                                else {
//										try {
//											bookMovierequest = sendRequestToServer(customerID, movieName, movieID, numberOfTicketsToCancel,aPort,funcationality,newMovieName,newMovieID).trim();
//										} catch (IOException e) {
//											// TODO Auto-generated catch block
//											e.printStackTrace();
//										}
//										if(bookMovierequest.equals(bookingSuccess)) {
//											//Cancelling after successful booking
//											cancelMovieTickets(customerID, movieID, movieName, numberOfTicketsToCancel);
//											result= "Ticket Exchanged Successfully";
//										}
//										else {
//											result= bookMovierequest;
//										}
                                    //Cancel first
                                    cancelTicketRequest = cancelMovieTickets(customerID, movieID, movieName, numberOfTicketsToCancel);
                                    if (cancelTicketRequest.equals(cancelSuccess) || cancelTicketRequest.equals(bookingtoOtherOpen) ) {
                                        try {
                                            sendRequestToServer(customerID, movieName, movieID, numberOfTicketsToCancel,vPort,funcationality,newMovieName,newMovieID).trim();
                                            result="Ticket Exchanged Successfully";
                                            numberOfSlotsBookedInOtherServer++;
                                        }
                                        catch(IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else {
                                        result= cancelTicketRequest;
                                    }
                                }

                            }
                            else if(newMovieToBookServer.equals("OUT")){
//									funcationality="bookingInExchange";
//									String bookMovierequest="";
//									try {
//										bookMovierequest = sendRequestToServer(customerID, movieName, movieID, numberOfTicketsToCancel,oPort,funcationality,newMovieName,newMovieID).trim();
//									} catch (IOException e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									}
//									if(bookMovierequest.equals(bookingSuccess)) {
//
//										//Cancelling after successful booking
//										cancelMovieTickets(customerID, movieID, movieName, numberOfTicketsToCancel);
//										result= "Ticket exchanged Successfully";
//									}
//									else {
//										result= bookMovierequest;
//									}

                                funcationality="bookingInExchange";
                                String bookMovierequest="";
                                String cancelTicketRequest="";
                                if(customerFromServer.equals(this.serverID) && numberOfSlotsBookedInOtherServer>=3) {
                                    result="Cannot exchange because this will excede allowed number of ticket to booked in other server.";
                                }
                                else {
//										try {
//											bookMovierequest = sendRequestToServer(customerID, movieName, movieID, numberOfTicketsToCancel,aPort,funcationality,newMovieName,newMovieID).trim();
//										} catch (IOException e) {
//											// TODO Auto-generated catch block
//											e.printStackTrace();
//										}
//										if(bookMovierequest.equals(bookingSuccess)) {
//											//Cancelling after successful booking
//											cancelMovieTickets(customerID, movieID, movieName, numberOfTicketsToCancel);
//											result= "Ticket Exchanged Successfully";
//										}
//										else {
//											result= bookMovierequest;
//										}
                                    //Cancel first
                                    cancelTicketRequest = cancelMovieTickets(customerID, movieID, movieName, numberOfTicketsToCancel);
                                    if (cancelTicketRequest.equals(cancelSuccess) || cancelTicketRequest.equals(bookingtoOtherOpen) ) {
                                        try {
                                            sendRequestToServer(customerID, movieName, movieID, numberOfTicketsToCancel,oPort,funcationality,newMovieName,newMovieID).trim();
                                            result="Ticket Exchanged Successfully";
                                            if(customerFromServer.equals(this.serverID)) {
                                                numberOfSlotsBookedInOtherServer++;
                                            }
                                        }
                                        catch(IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else {
                                        result= cancelTicketRequest;
                                    }
                                }
                            }
                        }
                        else {
                            result=" You cannot cancel more than booked";
                        }
                    }
                    else {
                        result=" Customer have not booked ticket for this slot. ";
                    }
                }
                else {
                    result=" Customer doesnot have booking for this movie";
                }
            }
            else {
                result=" Customer Doesnot exists";
            }
        }

        else if(movieBookedFromServer.equals("ATW")) {
            try {
                result=sendRequestToServer(customerID, movieName, movieID, numberOfTicketsToCancel,aPort,funcationality,newMovieName,newMovieID);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
//				(String userID,String movieName,String movieSlotID,int numberOfTickets,int port, String funcationality)
//				String customerID,String movieName, String movieID,String newMovieID,String newMovieName,int numberOfTicketsToCancel
        }

        else if(movieBookedFromServer.equals("OUT")) {
            try {
                result=sendRequestToServer(customerID, movieName, movieID, numberOfTicketsToCancel,oPort,funcationality,newMovieName,newMovieID);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        else if(movieBookedFromServer.equals("VER")) {
            try {
                result=sendRequestToServer(customerID, movieName, movieID, numberOfTicketsToCancel,vPort,funcationality,newMovieName,newMovieID);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return result;
    }

    public boolean checkSlotCollision(String customerID, String movieID,String movieName) {
        String checkOne="";
        String checkTwo="";
        String function="checkBookingForSameSlotForSameMovie";
        boolean collision=true;
        if (this.serverID.equals("ATW")) {
            try {
                checkOne=sendRequestToServer(customerID, movieName, movieID, 0, oPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                checkTwo=sendRequestToServer(customerID, movieName, movieID, 0, vPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (checkOne.trim().equals("No") && checkOne.trim().equals("No")) {
                collision=false;
            }
            else {
                collision=true;
            }
        }
        else if (this.serverID.equals("OUT")) {
            try {
                checkOne=sendRequestToServer(customerID, movieName, movieID, 0, aPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                checkTwo=sendRequestToServer(customerID, movieName, movieID, 0, vPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (checkOne.trim().equals("No") && checkOne.trim().equals("No")) {
                collision=false;
            }
            else {
                collision=true;
            }
        }
        else if (this.serverID.equals("VER")) {
            try {
                checkOne=sendRequestToServer(customerID, movieName, movieID, 0, aPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                checkTwo=sendRequestToServer(customerID, movieName, movieID, 0, oPort,function,null,null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (checkOne.trim().equals("No") && checkOne.trim().equals("No")) {
                collision=false;
            }
            else {
                collision=true;
            }
        }
        return collision;
    }

    public String checkBookingForSameSlotForSameMovie(String customerID, String movieID,String movieName) {
        if (this.customerDataMap.containsKey(customerID)) {
            if (this.customerDataMap.get(customerID).containsKey(movieName)) {
                int count=0;
                for (String data:customerDataMap.get(customerID).get(movieName).keySet()) {
                    if (data.substring(3).equals(movieID.substring(3))) {
                        count++;
                    }
                }
                if (count==0) {
                    return "No";
                }
                else {
                    return "Yes";
                }
            }
            else {
                return "No";
            }
        }
        else {
            return "No";
        }
    }

}

