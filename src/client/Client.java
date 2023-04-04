package client;

import frontend.FrontendInterface;
import models.RequestObject;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;


public class Client  {
    static RequestObject requestObject = new RequestObject();
    public static void main(String [] args) throws MalformedURLException, ParseException {

        startProg();
//        System.out.println("This is a result "+frontendInterface.forwardMessageToSequencer("Hello Message"));
    }
    public static void startProg() throws ParseException, MalformedURLException {
        URL url=new URL("http://localhost:8070/frontend?wsdl");
        QName qName=new QName("http://frontend/","FrontendServiceService");
        Service service= Service.create(url,qName);
        FrontendInterface frontendInterface=service.getPort(FrontendInterface.class);
        System.out.println("Welcome to Movie Ticket Booking System");

        System.out.println("Please enter user id: ");
        Scanner sc = new Scanner(System.in);
        String userID = sc.nextLine();

        boolean isAdmin = checkAdmin(userID);
//        String serverPort = getServerPort(userID);
        requestObject.currentUser = userID;
        String response="";

        if(isAdmin){

            while(true){
                int option = Integer.parseInt(showAdminMenu());
                Scanner sc2 = new Scanner(System.in);
                switch (option){
                    case 1:
                    {
                        String addMovieID = "";

                        /**
                         * Condition for admin adding only
                         * those movie which falls in his
                         * server domain
                         */
                        while (true){
                            System.out.println("Please enter Movie ID");
                            addMovieID = sc2.nextLine();
                            int year=2000+Integer.parseInt(addMovieID.substring(8));
                            String userDate=year+""+addMovieID.substring(6,8)+""+addMovieID.substring(4,6);
                            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMdd");
                            Date date= dateFormat.parse(userDate);
                            int dateDiff= (int) (date.getTime()-new Date().getTime())/(1000*60*60*24);
                            if (dateDiff > 6 || dateDiff<0){
                                System.out.println("Invalid Date. Please try again.");
                                continue;
                            }
                            if(addMovieID.substring(0,3).equals(userID.substring(0,3))){
                                break;
                            }
                            else {
                                System.out.println("Incorrect Server. Please try again.");
                            }
                        }

                        System.out.println("Please enter Movie Name");
                        String addMovieName = sc2.nextLine();
                        System.out.println("Please enter Booking Capacity");
                        int addBookingCapacity = Integer.parseInt(sc2.nextLine());
//                        log="Add Movie Slots";
                        requestObject.movieID = addMovieID;
                        requestObject.movieName = addMovieName;
                        requestObject.bookingCapacity = addBookingCapacity;
                        requestObject.requestType = "addMovieSlots";
                        response=frontendInterface.getRequestFromClient(requestObject);
//                        String res = movieRef.addMovieSlots(addMovieID, addMovieName, addBookingCapacity);
//                        writeToLogFile("addMovieSlots",userID+" "+addMovieID+" "+addMovieName+" "+addBookingCapacity,res);
//                        System.out.println(res);
                        System.out.println(response+" Hi!!!!");
                        break;
                    }
                    case 2:
                    {
                        System.out.println("Please enter Movie Name");
                        String removeMovieName = sc2.nextLine();

                        String removeMovieID = "";

                        while (true){
                            System.out.println("Please enter Movie ID");
                            removeMovieID = sc2.nextLine();
                            if(removeMovieID.substring(0,3).equals(userID.substring(0,3))){
                                break;
                            }
                            else {
                                System.out.println("Incorrect Server. Please try again.");
                            }
                        }
//                        log="Remove Movie Slots";
                        requestObject.movieID = removeMovieID;
                        requestObject.movieName = removeMovieName;
                        requestObject.requestType = "removeMovieSlots";
                        frontendInterface.getRequestFromClient(requestObject);
//                        String res = movieRef.removeMovieSlots(removeMovieID, removeMovieName);
//                        writeToLogFile("removeMovieSlots",userID+" "+removeMovieID+" "+removeMovieName,res);
//                        System.out.println(res);
                        break;
                    }
                    case 3:{
                        System.out.println("Please enter Movie Name");
                        String listMovieName = sc2.nextLine();
//                        log="List Movie Show Availability";
                        requestObject.movieName = listMovieName;
                        requestObject.requestType = "listMovieShowsAvailability";
                        frontendInterface.getRequestFromClient(requestObject);
//                        String res = movieRef.listMovieShowAvailability(listMovieName);
//                        writeToLogFile("listMovieShowsAvailability",userID+" "+listMovieName,res);
//                        System.out.println(res);
                        break;
                    }
                    case 4:{
                        System.out.println("Please enter Movie ID");
                        String bookMovieID = sc2.nextLine();
                        System.out.println("Please enter Movie Name");
                        String bookMovieName = sc2.nextLine();
                        System.out.println("Please enter No of Tickets to Book");
                        int bookNumberOfTickets = Integer.parseInt(sc2.nextLine());
//                        log="Book Movie Tickets";
                        requestObject.movieID = bookMovieID;
                        requestObject.movieName = bookMovieName;
                        requestObject.noOfTickets = bookNumberOfTickets;
                        requestObject.requestType = "bookMovieTickets";
                        frontendInterface.getRequestFromClient(requestObject);
//                        String res = movieRef.bookMovieTickets(userID, bookMovieID, bookMovieName, bookNumberOfTickets);
//                        writeToLogFile("bookMovieTickets",userID+" "+bookMovieID+" "+bookMovieName+" "+bookNumberOfTickets,res);
//                        System.out.println(res.split(";")[res.split(";").length-1]);
                        break;
                    }
                    case 5:{
//                        log="Get Booking Done by user";
                        requestObject.requestType = "getBookingSchedule";
                        frontendInterface.getRequestFromClient(requestObject);
//                        String res = movieRef.getBookingSchedule(userID);
//                        writeToLogFile("getBookingSchedule",userID,res);
//                        System.out.println(res);
                        break;
                    }
                    case 6:{
                        System.out.println("Please enter Movie ID");
                        String cancelBookMovieID = sc2.nextLine();
                        System.out.println("Please enter Movie Name");
                        String cancelBookMovieName = sc2.nextLine();
                        System.out.println("Please enter No of Tickets to Cancel");
                        int cancelBookNumberOfTickets = Integer.parseInt(sc2.nextLine());
//                        log="Tickets Cancelled.";
                        requestObject.movieID = cancelBookMovieID;
                        requestObject.movieName = cancelBookMovieName;
                        requestObject.noOfTickets = cancelBookNumberOfTickets;
                        requestObject.requestType = "cancelMovieTickets";
                        frontendInterface.getRequestFromClient(requestObject);
//                        String res = movieRef.cancelMovieTickets(userID, cancelBookMovieID, cancelBookMovieName, cancelBookNumberOfTickets);
//                        writeToLogFile("cancelMovieTickets",userID+" "+cancelBookMovieID+" "+cancelBookMovieName+" "+cancelBookNumberOfTickets,res);
//                        System.out.println(res);
                        break;
                    }
                    case 7:{
                        startProg();
                        break;
                    }
                    default:
                        break;
                }
            }
        }
        else {
            while(true){
                int option = Integer.parseInt(showCustomerMenu());
                Scanner sc2 = new Scanner(System.in);

                switch (option){
                    case 1:
                    {
                        System.out.println("Please enter Movie ID");
                        String bookMovieID = sc2.nextLine();
                        System.out.println("Please enter Movie Name");
                        String bookMovieName = sc2.nextLine();
                        System.out.println("Please enter No of Tickets to Book");
                        int bookNumberOfTickets = Integer.parseInt(sc2.nextLine());
//                        log="Book Movie Tickets";
                        requestObject.movieID = bookMovieID;
                        requestObject.movieName = bookMovieName;
                        requestObject.noOfTickets = bookNumberOfTickets;
                        requestObject.requestType = "bookMovieTickets";
                        frontendInterface.getRequestFromClient(requestObject);
//                        String res = movieRef.bookMovieTickets(userID, bookMovieID, bookMovieName, bookNumberOfTickets);
//                        writeToLogFile("bookMovieTickets",userID+" "+bookMovieID+" "+bookMovieName+" "+bookNumberOfTickets,res);
//                        System.out.println(res.split(";")[res.split(";").length-1]);
                        break;
                    }
                    case 2:
                    {
//                        log="Get Booking Done by user";
                        requestObject.requestType = "getBookingSchedule";
                        frontendInterface.getRequestFromClient(requestObject);
//                        String res = movieRef.getBookingSchedule(userID);
//                        writeToLogFile("getBookingSchedule",userID,res);
//                        System.out.println(res);
                        break;
                    }
                    case 3:
                    {
                        System.out.println("Please enter Movie ID");
                        String cancelBookMovieID = sc2.nextLine();
                        System.out.println("Please enter Movie Name");
                        String cancelBookMovieName = sc2.nextLine();
                        System.out.println("Please enter No of Tickets to Cancel");
                        int cancelBookNumberOfTickets = Integer.parseInt(sc2.nextLine());
//                        log="Tickets Cancelled.";
                        requestObject.movieID = cancelBookMovieID;
                        requestObject.movieName = cancelBookMovieName;
                        requestObject.noOfTickets = cancelBookNumberOfTickets;
                        requestObject.requestType = "cancelMovieTickets";
                        frontendInterface.getRequestFromClient(requestObject);
//                        String res = movieRef.cancelMovieTickets(userID, cancelBookMovieID, cancelBookMovieName, cancelBookNumberOfTickets);
//                        writeToLogFile("cancelMovieTickets",userID+" "+cancelBookMovieID+" "+cancelBookMovieName+" "+cancelBookNumberOfTickets,res);
//                        System.out.println(res);
                        break;
                    }
                    case 4:
                    {
                        System.out.println("Please enter Movie ID");
                        String bookMovieID = sc2.nextLine();
                        System.out.println("Please enter new Movie ID");
                        String newBookMovieID = sc2.nextLine();
                        System.out.println("Please enter new Movie Name");
                        String newBookMovieName = sc2.nextLine();
                        System.out.println("Please enter new No of Tickets to Book");
                        int newBookNumberOfTickets = Integer.parseInt(sc2.nextLine());
//                        log="Change Movie Tickets";
                        requestObject.movieID = bookMovieID;
                        requestObject.movieName = newBookMovieName;
                        requestObject.noOfTickets = newBookNumberOfTickets;
                        requestObject.requestType = "exchangeMovieTickets";
                        frontendInterface.getRequestFromClient(requestObject);
//                        String res = movieRef.exchangeTickets(userID, bookMovieID, newBookMovieID, newBookMovieName, newBookNumberOfTickets);
//                        writeToLogFile("exchangeMovieTickets",userID+" "+bookMovieID+" "+newBookMovieID+" "+newBookMovieName+" "+newBookNumberOfTickets,res);
//                        System.out.println(res);
                        break;

                    }
                    case 5:{
                        startProg();
                        break;
                    }
                    default:
//                        servant.shutdown();
                        break;
                }
            }


        }
    }

    public static boolean checkAdmin(String userID){
        if(userID.charAt(3) == 'A'){
            return true;
        }
        else if(userID.charAt(3) == 'C'){
            return false;
        }
        else {
            return false;
        }
    }

//    public static String getServerPort(String userID){
//        String serverSubstring = userID.substring(0,3);
//        String role = userID.substring(3,4);
//        if (serverSubstring.equalsIgnoreCase("ATW")) {
//            movieRef = atwService.getPort(MovieInterface.class);
//            return "atwater";
//        } else if (serverSubstring.equalsIgnoreCase("OUT")) {
//            movieRef = outService.getPort(MovieInterface.class);
//            return "outremont";
//        } else if (serverSubstring.equalsIgnoreCase("VER")) {
//            movieRef = verService.getPort(MovieInterface.class);
//            return "verdun";
//        }
//        return "false";
//    }

    public static String showAdminMenu(){
        System.out.println("Please select one of the following options: \n" +
                "1. Add Movie Slots\n" +
                "2. Remove Movie Slots\n" +
                "3. List Movie Shows Availability\n" +
                "4. Book Movie Ticket\n" +
                "5. Get Booking Schedule\n" +
                "6. Cancel Movie Tickets\n" +
                "7. Logout");
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        return input;
    }
    public static String showCustomerMenu(){
        System.out.println("Please select one of the following options: \n" +
                "1. Book Movie Ticket\n" +
                "2. Get Booking Schedule\n" +
                "3. Cancel Movie Tickets\n" +
                "4. Exchange Movie Tickets\n" +
                "5. Logout");
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        return input;
    }
}
