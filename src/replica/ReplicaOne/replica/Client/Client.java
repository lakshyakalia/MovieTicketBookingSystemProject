//package replica.ReplicaOne.replica.Client;
//
//import replica.ReplicaOne.replica.Constants.Constant;
//import replica.ReplicaOne.replica.Interface.AdminInterface;
//import replica.ReplicaOne.replica.Interface.CustomerInterface;
//import replica.ReplicaOne.replica.Interface.MovieInterface;
//import org.omg.CosNaming.NamingContextPackage.CannotProceed;
//import org.omg.CosNaming.NamingContextPackage.InvalidName;
//import org.omg.CosNaming.NamingContextPackage.NotFound;
//
//import javax.xml.namespace.QName;
//import javax.xml.ws.Service;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.net.URL;
//import java.rmi.Naming;
//import java.rmi.NotBoundException;
//import java.rmi.RemoteException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Date;
//import java.util.Scanner;
//
//public class Client extends Constant {
//    public static MovieInterface movieRef;
////    public static CustomerInterface customerRef;
//    public static String currentUser;
//    public static String log;
//    public static Service atwService;
//    public static Service verService;
//    public static Service outService;
//    public static void main(String[] args) throws Exception {
//        URL atwaterURL = new URL("http://localhost:8070/atwater?wsdl");
//        QName atwaterQName = new QName("http://Services/", "MovieTicketServiceService");
//        atwService = Service.create(atwaterURL, atwaterQName);
//
//        URL outremontURL = new URL("http://localhost:8080/outremont?wsdl");
//        QName outremontQName = new QName("http://Services/", "MovieTicketServiceService");
//        outService = Service.create(outremontURL, outremontQName);
//
//        URL verdunURL = new URL("http://localhost:8090/verdun?wsdl");
//        QName verdunQName = new QName("http://Services/", "MovieTicketServiceService");
//        verService = Service.create(verdunURL,verdunQName);
//        startProg();
//    }
//    public static void startProg() throws IOException, NotBoundException, InvalidName, CannotProceed, NotFound, ParseException {
//        System.out.println("Please enter user id: ");
//        Scanner sc = new Scanner(System.in);
//        String userID = sc.nextLine();
//
//        boolean isAdmin = checkAdmin(userID);
//        String serverPort = getServerPort(userID);
//        currentUser = userID;
//
//        if(isAdmin){
//
//            while(true){
//                int option = Integer.parseInt(showAdminMenu());
//                Scanner sc2 = new Scanner(System.in);
//                switch (option){
//                    case 1:
//                    {
//                        String addMovieID = "";
//
//                        /**
//                         * Condition for admin adding only
//                         * those movie which falls in his
//                         * server domain
//                         */
//                        while (true){
//                            System.out.println("Please enter Movie ID");
//                            addMovieID = sc2.nextLine();
//                            int year=2000+Integer.parseInt(addMovieID.substring(8));
//                            String userDate=year+""+addMovieID.substring(6,8)+""+addMovieID.substring(4,6);
//                            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMdd");
//                            Date date= dateFormat.parse(userDate);
//                            int dateDiff= (int) (date.getTime()-new Date().getTime())/(1000*60*60*24);
//                            if (dateDiff > 6 || dateDiff<0){
//                                System.out.println("Invalid Date. Please try again.");
//                                continue;
//                            }
//                            if(addMovieID.substring(0,3).equals(userID.substring(0,3))){
//                                break;
//                            }
//                            else {
//                                System.out.println("Incorrect Server. Please try again.");
//                            }
//                        }
//
//                        System.out.println("Please enter Movie Name");
//                        String addMovieName = sc2.nextLine();
//                        System.out.println("Please enter Booking Capacity");
//                        int addBookingCapacity = Integer.parseInt(sc2.nextLine());
//                        log="Add Movie Slots";
//                        String res = movieRef.addMovieSlots(addMovieID, addMovieName, addBookingCapacity);
//                        writeToLogFile("addMovieSlots",userID+" "+addMovieID+" "+addMovieName+" "+addBookingCapacity,res);
//                        System.out.println(res);
//                        break;
//                    }
//                    case 2:
//                    {
//                        System.out.println("Please enter Movie Name");
//                        String removeMovieName = sc2.nextLine();
//
//                        String removeMovieID = "";
//
//                        while (true){
//                            System.out.println("Please enter Movie ID");
//                            removeMovieID = sc2.nextLine();
//                            if(removeMovieID.substring(0,3).equals(userID.substring(0,3))){
//                                break;
//                            }
//                            else {
//                                System.out.println("Incorrect Server. Please try again.");
//                            }
//                        }
//
//                        log="Remove Movie Slots";
//                        String res = movieRef.removeMovieSlots(removeMovieID, removeMovieName);
//                        writeToLogFile("removeMovieSlots",userID+" "+removeMovieID+" "+removeMovieName,res);
//                        System.out.println(res);
//                        break;
//                    }
//                    case 3:{
//                        System.out.println("Please enter Movie Name");
//                        String listMovieName = sc2.nextLine();
//                        log="List Movie Show Availability";
//                        String res = movieRef.listMovieShowAvailability(listMovieName);
//                        writeToLogFile("listMovieShowsAvailability",userID+" "+listMovieName,res);
//                        System.out.println(res);
//                        break;
//                    }
//                    case 4:{
//                        System.out.println("Please enter Movie ID");
//                        String bookMovieID = sc2.nextLine();
//                        System.out.println("Please enter Movie Name");
//                        String bookMovieName = sc2.nextLine();
//                        System.out.println("Please enter No of Tickets to Book");
//                        int bookNumberOfTickets = Integer.parseInt(sc2.nextLine());
//                        log="Book Movie Tickets";
//                        String res = movieRef.bookMovieTickets(userID, bookMovieID, bookMovieName, bookNumberOfTickets);
//                        writeToLogFile("bookMovieTickets",userID+" "+bookMovieID+" "+bookMovieName+" "+bookNumberOfTickets,res);
//                        System.out.println(res.split(";")[res.split(";").length-1]);
//                        break;
//                    }
//                    case 5:{
//                        log="Get Booking Done by user";
//                        String res = movieRef.getBookingSchedule(userID);
//                        writeToLogFile("getBookingSchedule",userID,res);
//                        System.out.println(res);
//                        break;
//                    }
//                    case 6:{
//                        System.out.println("Please enter Movie ID");
//                        String cancelBookMovieID = sc2.nextLine();
//                        System.out.println("Please enter Movie Name");
//                        String cancelBookMovieName = sc2.nextLine();
//                        System.out.println("Please enter No of Tickets to Cancel");
//                        int cancelBookNumberOfTickets = Integer.parseInt(sc2.nextLine());
//                        log="Tickets Cancelled.";
//                        String res = movieRef.cancelMovieTickets(userID, cancelBookMovieID, cancelBookMovieName, cancelBookNumberOfTickets);
//                        writeToLogFile("cancelMovieTickets",userID+" "+cancelBookMovieID+" "+cancelBookMovieName+" "+cancelBookNumberOfTickets,res);
//                        System.out.println(res);
//                        break;
//                    }
//                    case 7:{
//                        startProg();
//                        break;
//                    }
//                    default:
//                        break;
//                }
//            }
//        }
//        else {
//            while(true){
//                int option = Integer.parseInt(showCustomerMenu());
//                Scanner sc2 = new Scanner(System.in);
//
//                switch (option){
//                    case 1:
//                    {
//                        System.out.println("Please enter Movie ID");
//                        String bookMovieID = sc2.nextLine();
//                        System.out.println("Please enter Movie Name");
//                        String bookMovieName = sc2.nextLine();
//                        System.out.println("Please enter No of Tickets to Book");
//                        int bookNumberOfTickets = Integer.parseInt(sc2.nextLine());
//                        log="Book Movie Tickets";
//                        String res = movieRef.bookMovieTickets(userID, bookMovieID, bookMovieName, bookNumberOfTickets);
//                        writeToLogFile("bookMovieTickets",userID+" "+bookMovieID+" "+bookMovieName+" "+bookNumberOfTickets,res);
//                        System.out.println(res.split(";")[res.split(";").length-1]);
//                        break;
//                    }
//                    case 2:
//                    {
//                        log="Get Booking Done by user";
//                        String res = movieRef.getBookingSchedule(userID);
//                        writeToLogFile("getBookingSchedule",userID,res);
//                        System.out.println(res);
//                        break;
//                    }
//                    case 3:
//                    {
//                        System.out.println("Please enter Movie ID");
//                        String cancelBookMovieID = sc2.nextLine();
//                        System.out.println("Please enter Movie Name");
//                        String cancelBookMovieName = sc2.nextLine();
//                        System.out.println("Please enter No of Tickets to Cancel");
//                        int cancelBookNumberOfTickets = Integer.parseInt(sc2.nextLine());
//                        log="Tickets Cancelled.";
//                        String res = movieRef.cancelMovieTickets(userID, cancelBookMovieID, cancelBookMovieName, cancelBookNumberOfTickets);
//                        writeToLogFile("cancelMovieTickets",userID+" "+cancelBookMovieID+" "+cancelBookMovieName+" "+cancelBookNumberOfTickets,res);
//                        System.out.println(res);
//                        break;
//                    }
//                    case 4:
//                    {
//                        System.out.println("Please enter Movie ID");
//                        String bookMovieID = sc2.nextLine();
//                        System.out.println("Please enter new Movie ID");
//                        String newBookMovieID = sc2.nextLine();
//                        System.out.println("Please enter new Movie Name");
//                        String newBookMovieName = sc2.nextLine();
//                        System.out.println("Please enter new No of Tickets to Book");
//                        int newBookNumberOfTickets = Integer.parseInt(sc2.nextLine());
//                        log="Change Movie Tickets";
//                        String res = movieRef.exchangeTickets(userID, bookMovieID, newBookMovieID, newBookMovieName, newBookNumberOfTickets);
//                        writeToLogFile("exchangeMovieTickets",userID+" "+bookMovieID+" "+newBookMovieID+" "+newBookMovieName+" "+newBookNumberOfTickets,res);
//                        System.out.println(res);
//                        break;
//
//                    }
//                    case 5:{
//                        startProg();
//                        break;
//                    }
//                    default:
////                        servant.shutdown();
//                        break;
//                }
//            }
//
//
//        }
//    }
//
//    public static boolean checkAdmin(String userID){
//        if(userID.charAt(3) == 'A'){
//            return true;
//        }
//        else if(userID.charAt(3) == 'C'){
//            return false;
//        }
//        else {
//            return false;
//        }
//    }
//
//    public static String getServerPort(String userID){
//        String serverSubstring = userID.substring(0,3);
//        String role = userID.substring(3,4);
//            if (serverSubstring.equalsIgnoreCase("ATW")) {
//                movieRef = atwService.getPort(MovieInterface.class);
//                return "atwater";
//            } else if (serverSubstring.equalsIgnoreCase("OUT")) {
//                movieRef = outService.getPort(MovieInterface.class);
//                return "outremont";
//            } else if (serverSubstring.equalsIgnoreCase("VER")) {
//                movieRef = verService.getPort(MovieInterface.class);
//                return "verdun";
//            }
//        return "false";
//    }
//
//    public static String showAdminMenu(){
//        System.out.println("Please select one of the following options: \n" +
//                "1. Add Movie Slots\n" +
//                "2. Remove Movie Slots\n" +
//                "3. List Movie Shows Availability\n" +
//                "4. Book Movie Ticket\n" +
//                "5. Get Booking Schedule\n" +
//                "6. Cancel Movie Tickets\n" +
//                "7. Logout");
//        Scanner sc = new Scanner(System.in);
//        String input = sc.nextLine();
//        return input;
//    }
//    public static String showCustomerMenu(){
//        System.out.println("Please select one of the following options: \n" +
//                "1. Book Movie Ticket\n" +
//                "2. Get Booking Schedule\n" +
//                "3. Cancel Movie Tickets\n" +
//                "4. Exchange Movie Tickets\n" +
//                "5. Logout");
//        Scanner sc = new Scanner(System.in);
//        String input = sc.nextLine();
//        return input;
//    }
//    public static void writeToLogFile(String operation, String params, String response) {
//        try {
//            FileWriter myWriter = new FileWriter("C:\\Users\\laksh\\Intellij-workspace\\MovieTicketBookingSystemProject\\src\\replica\\ReplicaOne\\replica\\Logs\\"+currentUser+".txt",true);
//            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
//            String log = dateFormat.format(LocalDateTime.now()) + " : " + operation + " : " + params + " : "
//                    + " : " + response + "\n";
//            myWriter.write(log);
//            myWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
