package replicas.ReplicaThree.replica.Services;



//import Interfacee.Client_Interface;
//import Interfacee.Interface;
import replicas.ReplicaThree.replica.Interface.Server_Interface;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.*;
import java.rmi.server.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;



@WebService(endpointInterface = "Interface.Server_Interface")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class implementation extends UnicastRemoteObject implements Server_Interface {

    public static HashMap<String, Integer> port = new HashMap<>();

    static {
        port.put("ATW", 6890);
        port.put("VER", 6887);
        port.put("OUT", 6889);
    }
    public static HashMap<String,String> file = new HashMap<>();

    static {
        file.put("ATW","ATWserver.txt");
        file.put("VER", "VERserver.txt");
        file.put("OUT", "OUTserver.txt");
    }


    String acronym;
    TimeUnit time = TimeUnit.SECONDS;

    int ans;
    int ans1;
    String temp1=" ";
    String temp2=" ";
    String sum="";
    String sum1="";
    String servername;
    String Status="";
    String response="";
    String log="";
    boolean check1=false;
    boolean check2=false;


    private String message;
    public HashMap<String, HashMap<String, Integer>> hashMap = new HashMap<>();
    public HashMap<String, HashMap<String, HashMap<String, Integer>>> hashMapCustomer = new HashMap<>();
    public HashMap<String, HashMap<String,List<String>> > reschedule = new HashMap<>();

    public HashMap<String,ArrayList> sortShows=new HashMap<>();
    public ArrayList<String> movieArray;
    public int[] arr=new int[2];
    SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd");



    public implementation() throws RemoteException {
        super();
    }
    public void setName(String serverName)
    {
        this.servername=serverName;
    }

    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) throws Exception {
        log = "Tickets not Booked.";
        Status = "Failed";
        response = "Appointment canceled!!!";
        String ret = "";
        if (customerID.substring(0, 3).equals(movieID.substring(0, 3))) {
            if (hashMap == null) {
                return "No Data Found";
            }
            boolean check1 = hashMap.containsKey(movieName);
            if (hashMap.containsKey(movieName)) {
                if (hashMap.get(movieName).containsKey(movieID)) {
                    int capacity = hashMap.get(movieName).get(movieID);
                    if (capacity >= numberOfTickets) {
                        hashMap.get(movieName).put(movieID, capacity - numberOfTickets);
                        if (!hashMapCustomer.containsKey(customerID)) {
                            HashMap<String, HashMap<String, Integer>> addressMap = new HashMap<>();
                            HashMap<String, Integer> addressMap1 = new HashMap<>();
                            addressMap1.put(movieID, numberOfTickets);
                            addressMap.put(movieName, addressMap1);
                            hashMapCustomer.put(customerID, addressMap);
                            List<String> l1=new ArrayList<>();
                            if(!reschedule.containsKey(movieName))
                            {
                                l1.add(customerID);
                                HashMap<String,List<String>> reschedule2 = new HashMap<>();
                                reschedule2.put(movieID,l1);
                                reschedule.put(movieName,reschedule2);
                            }
                            else {
                                if(!reschedule.get(movieName).containsKey(movieID))
                                {
                                    HashMap<String, List<String>> reschedule1 = reschedule.get(movieName);
                                    l1.add(customerID);
                                    reschedule1.put(movieID,l1);
                                    reschedule.put(movieName,reschedule1);
                                }
                                else {
                                    List<String> l2=reschedule.get(movieName).get(movieID);
                                    l2.add(customerID);
                                    reschedule.get(movieName).put(movieID,l2);
                                }
                            }
                            log="Tickets Booked Successfully";
                            Status="SUCSSES";
                            ret="DONE SUCSSESFULLY";
                        } else {
                            HashMap<String, HashMap<String, Integer>> addressMap = hashMapCustomer.get(customerID);
                            if (!addressMap.containsKey(movieName)) {
                                HashMap<String, Integer> addressMap1 = new HashMap<>();
                                if(addressMap1.containsKey(movieID))
                                {
                                    int count1=hashMapCustomer.get(customerID).get(movieName).get(movieID);
                                    addressMap1.put(movieID, numberOfTickets+count1);
                                    addressMap.put(movieName, addressMap1);
                                    hashMapCustomer.put(customerID, addressMap);

                                }
                                else {
                                    addressMap1.put(movieID, numberOfTickets);
                                    addressMap.put(movieName, addressMap1);
                                    hashMapCustomer.put(customerID, addressMap);

                                }


                            } else {
                                HashMap<String, Integer> addmap = addressMap.get(movieName);
                                addmap.put(movieID, numberOfTickets);
                                addressMap.put(movieName, addmap);
                                hashMapCustomer.put(customerID, addressMap);

                            }
                            List<String> l1=new ArrayList<>();
                            if(!reschedule.containsKey(movieName))
                            {

                                l1.add(customerID);
                                HashMap<String,List<String>> reschedule2 = new HashMap<>();
                                reschedule2.put(movieID,l1);
                                reschedule.put(movieName,reschedule2);

                            }
                            else {
                                if(!reschedule.get(movieName).containsKey(movieID))
                                {
                                    HashMap<String, List<String>> reschedule1 = reschedule.get(movieName);
                                    l1.add(customerID);
                                    reschedule1.put(movieID,l1);
                                    reschedule.put(movieName,reschedule1);
                                }
                                else {
                                    List<String> l2=reschedule.get(movieName).get(movieID);
                                    if(!l2.contains(customerID))
                                    {
                                        l2.add(customerID);
                                        reschedule.get(movieName).put(movieID,l2);
                                    }


                                }
                            }

                            System.out.println(reschedule.get(movieName).get(movieID));
                            log="Tickets Booked Successfully";
                            Status="SUCSSES";
                            ret="DONE SUCSSESFULLY";
                        }


                    } else {
                        log="Tickets Not Booked";
                        Status="Failed";
                        ret = "Only" + capacity + "no of tickets available";
                    }

                } else {
                    log="Tickets Not Booked";
                    Status="Failed";
                    ret = "No Shows Available for this MovieId";
                }


            } else {
                System.out.println("hellooo 123");

                log="Tickets Not Booked";
                Status="Failed";
                ret = "No Bookings for this MovieName Available";
            }
            writeToLog("BOOK TICKETS",customerID,Status,log);

            return ret;

        } else {
            if(hashMapCustomer.containsKey(customerID))
            {
                if (hashMapCustomer.get(customerID).containsKey(movieName))
                {
                    if(hashMapCustomer.get(customerID).get(movieName).containsKey(servername+movieID.substring(3)))
                    {
                        return "you are not allowed to do same time booking on different server";
                    }
                }
            }
            System.out.println(hashMapCustomer.entrySet());
            new Thread() {
                public void run() {

                    temp1 = udpThread("checkCustomer:" + customerID + " " + movieID + " " + movieName , arr[0]);

                }
            }.start();
            new Thread() {
                public void run() {

                    temp2 = udpThread("checkCustomer:" + customerID + " " + movieID + " " + movieName , arr[1]);

                }
            }.start();
            try {
                time.sleep(2L);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            if(temp1.equals("true") || temp2.equals("true"))
            {
                return "you are not allowed to do same time booking on different server";
            }

//            if(hashMapCustomer.get(customerID).get(movieName).containsKey(this.servername+movieID.substring(3,movieID.length())))
//            {
//                log="Tickets Not Booked";
//                Status="Failed";
//                writeToLog("BOOK TICKETS",customerID,Status,log);
//                return "you are not allowed to do same time booking on different server";
//            }
            int total=callCounter(customerID);
            System.out.println(total);
            if(total==3)
            {
                return "CUSTOMER ALREADY BOOKED 3 TICKETS OUTSIDE OWN SERVER";
            }
            else {
                new Thread(() -> {
                    temp1 = udpThread("bookMovieTickets:" + customerID + " " + movieName + " " + movieID + " " + numberOfTickets, port.get(movieID.substring(0, 3)));
                    if(temp1.equals("DONE SUCCESSFULLY"))
                    {
                        Status="Sucess";
                        log="Tickets Booked";
                        writeToLog("BOOK TICKETS TRANSFERED TO"+" "+movieID.substring(0,3)+"Server",customerID,Status,log);

                    }
                    else {
                        log="Tickets Not Booked";
                        Status="Failed";
                        writeToLog("BOOK TICKETS TRANSFERED TO"+" "+movieID.substring(0,3)+"Server",customerID,Status,log);

                    }

                }).start();
                try {
                    time.sleep(2L);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }

        }
        return temp1;
    }


    public String getBookingSchedule(String customerID) throws Exception {

        Status="Success";
        log="Booking Schedule found";
        String ret = "";
        if (hashMapCustomer.containsKey(customerID)) {
            HashMap<String, HashMap<String, Integer>> addMap = hashMapCustomer.get(customerID);
            for (Map.Entry<String, HashMap<String, Integer>> letterEntry : addMap.entrySet()) {
                String letter = letterEntry.getKey();
                // ...
                for (Map.Entry<String, Integer> nameEntry : letterEntry.getValue().entrySet()) {
                    ret = ret + nameEntry.getValue() + " " + "Tickets have been Booked for Movie:" + letter + " " + "MovieID:" + nameEntry.getKey() + "\n";
                    // ...
                }
            }
        }
        if(this.servername.equals("ATW")){
            new Thread() {
                public void run() {
                    temp1 = udpThread("getBookingSchedule:" + customerID, port.get("VER"));

                }
            }.start();

            new Thread() {
                public void run() {
                    temp2 = udpThread("getBookingSchedule:" + customerID, port.get("OUT"));
                }
            }.start();


        }
        else if (this.servername.equals("VER")){
            new Thread() {
                public void run() {
                    temp1 = udpThread("getBookingSchedule:" + customerID, port.get("ATW"));
                }
            }.start();

            new Thread() {
                public void run() {
                    temp2 = udpThread("getBookingSchedule:" + customerID,  port.get("OUT"));
                }
            }.start();

        }
        else {
            new Thread() {
                public void run() {
                    temp1 = udpThread("getBookingSchedule:" + customerID,  port.get("ATW"));
                }
            }.start();

            new Thread() {
                public void run() {
                    temp2 = udpThread("getBookingSchedule:" + customerID,  port.get("VER"));
                }
            }.start();

        }


        try {
            time.sleep(2L);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        if((ret+temp1+temp2).trim().equals(""))
        {
            Status="failed";
            log="No bookings found";
        }
        writeToLog("getBookingSchedule",customerID,Status,log);
        return ret + temp1 + temp2;

    }


    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) throws Exception {
        int tickets;
        log="Tickets Not Cancelled";
        Status="Failed";

        String ret=" ";
        if(customerID.substring(0,3).equals(movieID.substring(0,3)))
        {
            if(hashMapCustomer.containsKey(customerID))
            {
                if(hashMapCustomer.get(customerID).containsKey(movieName))
                {
                    if (hashMapCustomer.get(customerID).get(movieName).containsKey(movieID))
                    {
                        tickets= hashMap.get(movieName).get(movieID);
                        if(hashMapCustomer.get(customerID).get(movieName).get(movieID)==numberOfTickets)
                        {
                            hashMap.get(movieName).put(movieID,tickets+numberOfTickets);
                            Integer rem=hashMapCustomer.get(customerID).get(movieName).remove(movieID);
                            System.out.println(hashMapCustomer.get(customerID).get(movieName));
                            log="Tickets Cancelled";
                            Status="Success";
                            ret="DONE SUCCESSFULLY";
                        }
                        else {
                            ret="YOU HAVE ONLY BOOKED"+" "+hashMapCustomer.get(customerID).get(movieName).get(movieID)+" "+"NO OF TICKETS";
                        }


                    }
                    else {
                        ret="WRONG MOVIE ID";
                    }
                }
                else {
                    ret="NO MOVIE BOOKINGS FOUND";
                }
            }
            else{
                ret="NO BOOKINGS FOUND";

            }
        }
        else {
            new Thread() {
                public void run() {

                    temp1 = udpThread("cancelMovieTickets:" + customerID + " " + movieID + " " + movieName + " " + numberOfTickets, port.get(movieID.substring(0, 3)));
                    if(temp1.equals("DONE SUCCESSFULLY"))
                    {
                        log="Tickets Cancelled";
                        Status="Success";
                    }

                }
            }.start();
            try {
                time.sleep(2L);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }


        }
        writeToLog("cancelMovieTickets:",movieID+"-"+customerID,Status,log);
        return (ret+temp1).trim();



    }



    public String addMovieSlots(String movieID, String movieName, int bookingCapacity) throws ParseException {
        Status="falied";
        log="Movie slots added";
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyy");
        Date cd = new Date();
        String cd_1 = formatter.format(cd);
        Date currentDate = new SimpleDateFormat("ddMMyy").parse(cd_1);
        Date userDate = new SimpleDateFormat("ddMMyy").parse(movieID.substring(4, movieID.length()));
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.DATE, 7);


        if(userDate.before(currentDate)==true){
            writeToLog("addMovieSlots",movieID,Status,log);
            return "CANT ADD MOVIE SLOT FOR PREVIOUS DATE";
        }


        if (c.getTime().compareTo(userDate) < 0) {
            writeToLog("addMovieSlots",movieID,Status,log);
            return "YOU CAN ADD SLOTS UPTO 7 DAYS ONLY";

        }

        int capacity;
        if (hashMap.containsKey(movieName)) {
            HashMap<String, Integer> addslot = hashMap.get(movieName);
            addslot.put(movieID, bookingCapacity);
            hashMap.put(movieName, addslot);
            movieArray=new ArrayList<>();
            sortShows.get(movieName).add(movieID.substring(3));
            sortShows.put(movieName,sortDates(sortShows.get(movieName)));
        } else {

            HashMap<String, Integer> addslot = new HashMap<>();
            addslot.put(movieID, bookingCapacity);
            hashMap.put(movieName, addslot);
        }
        capacity = hashMap.get(movieName).get(movieID);
        System.out.println(sortShows.entrySet());
        Status="Success";
        log="Movie slots added Successfully";
        writeToLog("addMovieSlots",movieID,Status,log);
        return "movie slots added successfully:" + capacity;
    }


    public String removeMovieSlots(String movieID, String movieName) {
        HashMap<String, Integer> stringIntegerHashMap = hashMap.get(movieName);
        Set<String> strings = stringIntegerHashMap.keySet();
        String nextMovie="";
        int initialval=0;
        if(reschedule.containsKey(movieName)){
            if(reschedule.get(movieName).containsKey(movieID)){
                List temp=reschedule.get(movieName).get(movieID);
                for(int i=0;i<sortShows.get(movieName).size();i++){
                    if(sortShows.get(movieName).get(i).equals(movieID.substring(3))){
                        System.out.println((String) sortShows.get(movieName).get(i+1));
                        nextMovie =movieID.substring (0,3)+(String) sortShows.get(movieName).get(i+1);
                        System.out.println(nextMovie);
                        reschedule.get(movieName).remove(movieID);
                        reschedule.get(movieName).put(nextMovie,temp);
                        initialval=hashMap.get(movieName).get(nextMovie);
                        hashMap.get(movieName).remove(movieID);


                        break;
                    }
                }
                int sum=0;
                for(int i=0;i<temp.size();i++)
                {
                    if(hashMapCustomer.containsKey(temp.get(i))){
                        if(hashMapCustomer.get(temp.get(i)).containsKey(movieName)){
                            if(hashMapCustomer.get(temp.get(i)).get(movieName).containsKey(movieID)){
                                sum+=hashMapCustomer.get(temp.get(i)).get(movieName).get(movieID);
                                int temp1=hashMapCustomer.get(temp.get(i)).get(movieName).get(movieID);
                                hashMapCustomer.get(temp.get(i)).get(movieName).remove(movieID);
                                hashMapCustomer.get(temp.get(i)).get(movieName).put(nextMovie,temp1);
                            }
                        }
                    }
                }

                hashMap.get(movieName).put(nextMovie,initialval-sum);
                return "slots removed successfully";
            }else{
                return "This movie ID doesn't exist";
            }
        }else{
            return "This movie name doesn't exist";
        }
    }
    public String listMovieShowAvailability(String movieName) {
        Status="Failed";
        log="No movie show available";
        String ret = "";
        if (hashMap.containsKey(movieName)) {

            for (Map.Entry<String, Integer> letter : hashMap.get(movieName).entrySet()) {
                ret = ret + " " + letter.getKey() + " " + letter.getValue() + "\n";
            }
        }
        if(this.servername.equals("ATW"))
        {
            new Thread() {
                public void run() {
                    temp1 = udpThread("listMovieShowAvailability:"+movieName, port.get("VER"));
                }
            }.start();
            new Thread() {
                public void run() {
                    temp2 = udpThread("listMovieShowAvailability:" +movieName, port.get("OUT"));
                }
            }.start();

        } else if (this.servername.equals("VER")) {
            new Thread() {
                public void run() {
                    temp1 = udpThread("listMovieShowAvailability:" +movieName, port.get("ATW"));
                }
            }.start();
            new Thread() {
                public void run() {
                    temp2 = udpThread("listMovieShowAvailability:"+movieName, port.get("OUT"));
                }
            }.start();
        }
        else  {
            new Thread() {
                public void run() {
                    temp1 = udpThread("listMovieShowAvailability:" +movieName, port.get("ATW"));
                }
            }.start();
            new Thread() {
                public void run() {
                    temp2 = udpThread("listMovieShowAvailability:"+movieName, port.get("VER"));
                }
            }.start();

        }
        try {
            time.sleep(2L);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        if((ret+temp1+temp2).trim().equals(""))
        {
            writeToLog("listMovieShowAvailability",movieName,Status,log);
            return "NO MOVIE SHOWS AVAILABLE";
        }
        else {
            Status="Success";
            log="Movie shows listed Successfully";
            writeToLog("listMovieShowAvailability",movieName,Status,log);
            return ret+temp1+temp2;
        }

    }


    public String adminBookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) throws Exception {
        log = "Tickets not Booked.";
        Status = "Failed";
        response = "Appointment canceled!!!";
        String ret = "";
        if (customerID.substring(0, 3).equals(movieID.substring(0, 3))) {
            if (hashMap == null) {
                return "No Data Found";
            }
            boolean check1 = hashMap.containsKey(movieName);
            if (hashMap.containsKey(movieName)) {
                if (hashMap.get(movieName).containsKey(movieID)) {
                    int capacity = hashMap.get(movieName).get(movieID);
                    if (capacity >= numberOfTickets) {
                        hashMap.get(movieName).put(movieID, capacity - numberOfTickets);
                        if (!hashMapCustomer.containsKey(customerID)) {
                            HashMap<String, HashMap<String, Integer>> addressMap = new HashMap<>();
                            HashMap<String, Integer> addressMap1 = new HashMap<>();
                            addressMap1.put(movieID, numberOfTickets);
                            addressMap.put(movieName, addressMap1);
                            hashMapCustomer.put(customerID, addressMap);
                            List<String> l1=new ArrayList<>();
                            if(!reschedule.containsKey(movieName))
                            {

                                l1.add(customerID);
                                HashMap<String,List<String>> reschedule2 = new HashMap<>();
                                reschedule2.put(movieID,l1);
                                reschedule.put(movieName,reschedule2);

                            }
                            else {
                                if(!reschedule.get(movieName).containsKey(movieID))
                                {
                                    HashMap<String, List<String>> reschedule1 = reschedule.get(movieName);
                                    l1.add(customerID);
                                    reschedule1.put(movieID,l1);
                                    reschedule.put(movieName,reschedule1);
                                }
                                else {
                                    List<String> l2=reschedule.get(movieName).get(movieID);
                                    l2.add(customerID);
                                    reschedule.get(movieName).put(movieID,l2);
                                }
                            }
                            System.out.println(reschedule.get(movieName).get(movieID));
                            log="Tickets Booked Successfully";
                            Status="SUCSSES";
                            ret="DONE SUCSSESFULLY";
                        } else {
                            HashMap<String, HashMap<String, Integer>> addressMap = hashMapCustomer.get(customerID);
                            if (!addressMap.containsKey(movieName)) {
                                HashMap<String, Integer> addressMap1 = new HashMap<>();
                                if(addressMap1.containsKey(movieID))
                                {
                                    int count1=hashMapCustomer.get(customerID).get(movieName).get(movieID);
                                    addressMap1.put(movieID, numberOfTickets+count1);
                                    addressMap.put(movieName, addressMap1);
                                    hashMapCustomer.put(customerID, addressMap);

                                }
                                else {
                                    addressMap1.put(movieID, numberOfTickets);
                                    addressMap.put(movieName, addressMap1);
                                    hashMapCustomer.put(customerID, addressMap);

                                }


                            } else {
                                HashMap<String, Integer> addmap = addressMap.get(movieName);
                                addmap.put(movieID, numberOfTickets);
                                addressMap.put(movieName, addmap);
                                hashMapCustomer.put(customerID, addressMap);

                            }
                            List<String> l1=new ArrayList<>();
                            if(!reschedule.containsKey(movieName))
                            {

                                l1.add(customerID);
                                HashMap<String,List<String>> reschedule2 = new HashMap<>();
                                reschedule2.put(movieID,l1);
                                reschedule.put(movieName,reschedule2);

                            }
                            else {
                                if(!reschedule.get(movieName).containsKey(movieID))
                                {
                                    HashMap<String, List<String>> reschedule1 = reschedule.get(movieName);
                                    l1.add(customerID);
                                    reschedule1.put(movieID,l1);
                                    reschedule.put(movieName,reschedule1);
                                }
                                else {
                                    List<String> l2=reschedule.get(movieName).get(movieID);
                                    if(!l2.contains(customerID))
                                    {
                                        l2.add(customerID);
                                        reschedule.get(movieName).put(movieID,l2);
                                    }


                                }
                            }

                            System.out.println(reschedule.get(movieName).get(movieID));
                            System.out.println("done");
                            log="Tickets Booked Successfully";
                            Status="SUCSSES";
                            ret="DONE SUCSSESFULLY";


                        }


                    } else {
                        log="Tickets Not Booked";
                        Status="Failed";
                        ret = "Only" + capacity + "no of tickets available";
                    }

                } else {
                    log="Tickets Not Booked";
                    Status="Failed";
                    ret = "No Shows Available for this MovieId";
                }


            } else {
                System.out.println("heyy");
                log="Tickets Not Booked";
                Status="Failed";
                ret = "No Bookings for this MovieName Available";
            }
            writeToLog("BOOK TICKETS BY ADMIN",customerID,Status,log);

            return ret;

        } else {
            int total=callCounter(customerID);
            System.out.println(total);
            if(total==3)
            {
                return "CUSTOMER ALREADY BOOKED 3 TICKETS OUTSIDE OWN SERVER";
            }
            else {
                new Thread() {
                    public void run() {
                        temp1 = udpThread("bookMovieTickets:" + customerID + " " + movieName + " " + movieID + " " + numberOfTickets, port.get(movieID.substring(0, 3)));
                        if(temp1.equals("DONE SUCCESSFULLY"))
                        {
                            Status="Sucess";
                            log="Tickets Booked";
                            writeToLog("BOOK TICKETS TRANSFERED TO"+" "+movieID.substring(0,3)+"Server",customerID,Status,log);

                        }
                        else {
                            log="Tickets Not Booked";
                            Status="Failed";
                            writeToLog("BOOK TICKETS TRANSFERED TO"+" "+movieID.substring(0,3)+"Server",customerID,Status,log);

                        }

                    }
                }.start();
                try {
                    time.sleep(2L);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }

        }
        return temp1;
    }


    public String adminGetBookingSchedule(String customerID) throws Exception {

        Status="Success";
        log="Booking Schedule found";
        String ret = "";
        if (hashMapCustomer.containsKey(customerID)) {
            HashMap<String, HashMap<String, Integer>> addMap = hashMapCustomer.get(customerID);
            for (Map.Entry<String, HashMap<String, Integer>> letterEntry : addMap.entrySet()) {
                String letter = letterEntry.getKey();
                // ...
                for (Map.Entry<String, Integer> nameEntry : letterEntry.getValue().entrySet()) {
                    ret = ret + nameEntry.getValue() + " " + "Tickets have been Booked for Movie:" + letter + " " + "MovieID:" + nameEntry.getKey() + "\n";
                    // ...
                }
            }
        }
        if(this.servername.equals("ATW")){
            new Thread() {
                public void run() {
                    temp1 = udpThread("getBookingSchedule:" + customerID, port.get("VER"));

                }
            }.start();

            new Thread() {
                public void run() {
                    temp2 = udpThread("getBookingSchedule:" + customerID, port.get("OUT"));
                }
            }.start();


        }
        else if (this.servername.equals("VER")){
            new Thread() {
                public void run() {
                    temp1 = udpThread("getBookingSchedule:" + customerID, port.get("ATW"));
                }
            }.start();

            new Thread() {
                public void run() {
                    temp2 = udpThread("getBookingSchedule:" + customerID,  port.get("OUT"));
                }
            }.start();

        }
        else {
            new Thread() {
                public void run() {
                    temp1 = udpThread("getBookingSchedule:" + customerID,  port.get("ATW"));
                }
            }.start();

            new Thread() {
                public void run() {
                    temp2 = udpThread("getBookingSchedule:" + customerID,  port.get("VER"));
                }
            }.start();

        }


        try {
            time.sleep(2L);
        } catch (InterruptedException e1) {// TODO Auto-generated catch block
            e1.printStackTrace();
        }
        if((ret+temp1+temp2).trim().equals(""))
        {
            Status="failed";
            log="No bookings found";
        }
        writeToLog("getBookingSchedule",customerID,Status,log);
        return ret + temp1 + temp2;

    }





    public String adminCancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) throws Exception {
        int tickets;
        log="Tickets Not Cancelled";
        Status="Failed";

        String ret=" ";
        if(customerID.substring(0,3).equals(movieID.substring(0,3)))
        {
            if(hashMapCustomer.containsKey(customerID))
            {
                if(hashMapCustomer.get(customerID).containsKey(movieName))
                {
                    if (hashMapCustomer.get(customerID).get(movieName).containsKey(movieID))
                    {
                        tickets= hashMap.get(movieName).get(movieID);
                        if(hashMapCustomer.get(customerID).get(movieName).get(movieID)>=numberOfTickets)
                        {
                            hashMap.get(movieName).put(movieID,tickets+numberOfTickets);
                            Integer rem=hashMapCustomer.get(customerID).get(movieName).remove(movieID);
                            System.out.println(hashMapCustomer.get(customerID).get(movieName));
                            log="Tickets Cancelled";
                            Status="Success";
                            ret="DONE SUCCESSFULLY";
                        }
                        else {
                            ret="YOU HAVE ONLY BOOKED"+" "+hashMapCustomer.get(customerID).get(movieName).get(movieID)+" "+"NO OF TICKETS";
                        }


                    }
                    else {
                        ret="WRONG MOVIE ID";
                    }
                }
                else {
                    ret="NO MOVIE BOOKINGS FOUND";
                }
            }
            else{
                ret="NO BOOKINGS FOUND";

            }
        }
        else {
            new Thread() {
                public void run() {
                    temp1 = udpThread("cancelMovieTickets:" + customerID + " " + movieID + " " + movieName + " " + numberOfTickets, port.get(movieID.substring(0, 3)));
                    if(temp1.equals("DONE SUCCESSFULLY"))
                    {
                        log="Tickets Cancelled";
                        Status="Success";
                    }

                }
            }.start();
            try {
                time.sleep(2L);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }


        }
        writeToLog("cancelMovieTickets:",movieID+"-"+customerID,Status,log);
        return (ret+temp1).trim();

    }

//    @Override
//    public String ServerExchangeTickets(String customerID, String movieID, String new_movieID, String new_movieName, int numberOfTickets) {
//        return null;
//    }


    public String getServerBookingSchedule(String customerID) throws Exception {
        Status="Success";
        log="Booking Schedule found";

        String ret = "";
        if (hashMapCustomer.containsKey(customerID)) {
            HashMap<String, HashMap<String, Integer>> addMap = hashMapCustomer.get(customerID);
            for (Map.Entry<String, HashMap<String, Integer>> letterEntry : addMap.entrySet()) {
                String letter = letterEntry.getKey();
                // ...
                for (Map.Entry<String, Integer> nameEntry : letterEntry.getValue().entrySet()) {
                    ret = ret + nameEntry.getValue() + " " + "Tickets have been Booked for Movie:" + letter + " " + "MovieID:" + nameEntry.getKey() + "\n";
                    // ...
                }
            }
        }
        if((ret+temp1+temp2).trim().equals(""))
        {
            Status="failed";
            log="No bookings found";
        }
        writeToLog("getBookingSchedule",customerID,Status,log);

        return ret;
    }
    public String serverbookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) throws Exception {
        log = "Tickets not Booked.";
        Status = "Failed";

        String ret = "";

        if (hashMap == null) {
            return "No Data Found";
        }

        boolean check1 = hashMap.containsKey(movieName);
        System.out.println(check1);
        System.out.println(hashMap);

        if (hashMap.containsKey(movieName)) {
            if (hashMap.get(movieName).containsKey(movieID)) {
                int capacity = hashMap.get(movieName).get(movieID);
                if (capacity >= numberOfTickets) {
                    hashMap.get(movieName).put(movieID, capacity - numberOfTickets);
                    if (!hashMapCustomer.containsKey(customerID)) {
                        HashMap<String, HashMap<String, Integer>> addressMap = new HashMap<>();
                        HashMap<String, Integer> addressMap1 = new HashMap<>();
                        addressMap1.put(movieID, numberOfTickets);
                        addressMap.put(movieName, addressMap1);
                        hashMapCustomer.put(customerID, addressMap);
                        System.out.println(hashMapCustomer);

                    } else {
                        HashMap<String, HashMap<String, Integer>> addressMap = hashMapCustomer.get(customerID);
                        if (!addressMap.containsKey(movieName)) {
                            HashMap<String, Integer> addressMap1 = new HashMap<>();
                            addressMap1.put(movieID, numberOfTickets);
                            addressMap.put(movieName, addressMap1);
                            hashMapCustomer.put(customerID, addressMap);


                        } else {
                            HashMap<String, Integer> addmap = addressMap.get(movieName);
                            addmap.put(movieID, numberOfTickets);
                            addressMap.put(movieName, addmap);
                            hashMapCustomer.put(customerID, addressMap);


                        }
                        log = "Tickets Booked.";
                        Status = "Success";

                        ret="DONE SUCCESSFULLY";


                    }
                    log = "Tickets Booked.";
                    Status = "Success";

                    ret="DONE SUCCESSFULLY";


                } else {
                    log = "Tickets Not Booked.";
                    Status = "Failed";

                    ret = "Only" + capacity + "no of tickets available";
                }

            } else {
                log = "Tickets Not Booked.";
                Status = "Failed";

                ret = "No Shows Available for this MovieId";
            }


        } else {
            System.out.println("hii hello");
            log = "Tickets Not Booked.";
            Status = "Failed";

            ret = "No Bookings for this MovieName Available";
        }
        writeToLog("ServerBookMovieTickets",customerID,Status,log);
        return ret;

    }

    public String serverCancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) throws Exception {
        log = "Tickets Not Cancelled.";
        Status = "Failed";

        int tickets;
        String ret=null;


        if(hashMapCustomer.containsKey(customerID))
        {
            if(hashMapCustomer.get(customerID).containsKey(movieName))
            {
                if (hashMapCustomer.get(customerID).get(movieName).containsKey(movieID))
                {
                    tickets= hashMap.get(movieName).get(movieID);
                    hashMap.get(movieName).put(movieID,tickets+numberOfTickets);
                    Status="Success";
                    log="tickets cancelled successfull";
                    ret="DONE SUCCESSFULLY";
                    hashMapCustomer.get(customerID).get(movieName).remove(movieID);
                    //Implemented to check                     hashMapCustomer.get(customerID).get(movieName).remove(movieID);
                }
                else {
                    ret="DATABASE DOES NOT CONTAIN THIS MOVIE ID";
                }
            }
            else {
                ret="DATABASE DOES NOT CONTAIN THIS MOVIE";
            }
        }
        else{
            ret="INVALID CLIENT ID";

        }
        writeToLog("Server cancel movie ticket",customerID,Status,log);
        return  ret;
    }
    public String serverListMovieShowAvailability(String movieName) {
        String ret = "";
        if (hashMap.containsKey(movieName)) {
            for (Map.Entry<String, Integer> letter : hashMap.get(movieName).entrySet()) {
                ret = ret + " " + letter.getKey() + " " + letter.getValue() + "\n";
            }
        }
        return ret;
    }
    public  int  callCounter(String id)
    {

        new Thread() {
            public void run() {
                sum =udpThread("callCounter:"+id,arr[0]);
                try {
                    ans=Integer.parseInt(sum);
                }
                catch (Exception e)
                {
                    System.out.println(e);
                }

            }
        }.start();
        new Thread() {
            public void run() {
                sum1 =udpThread("callCounter:"+id,arr[1]);
                try {
                    ans1=Integer.parseInt(sum1);
                }
                catch (Exception e)
                {
                    System.out.println(e);
                }
            }
        }.start();
        try {
            time.sleep(2L);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return ans1+ans;
    }
    public String countCustomer(String s) {
        int count=0;
        if(hashMapCustomer.containsKey(s))
        {
            for (Map.Entry<String, HashMap<String, Integer>> v1: hashMapCustomer.get(s).entrySet()) {
                for (Map.Entry<String,Integer> v2:v1.getValue().entrySet())
                {
                    count++;
                }


            }
        }
        String exp=new String(String.valueOf(count));
        return exp;

    }
    public String udpThread(String data, int port) {
        // Status="TRANSFER SUCCESS";
        // log="TRANSFER TO PORT"+" "+port;
        String result = "";
        try (DatagramSocket aSocket = new DatagramSocket()) {
            DatagramPacket request = new DatagramPacket(data.getBytes(), data.getBytes().length,
                    InetAddress.getByName("localhost"), port);
            //   writeToLog("UDP DATA TRANSFER","TRANSFER FROM"+" "+this.servername,Status,log);
            aSocket.send(request);
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);
            aSocket.close();
            result = new String(reply.getData()).trim();

        } catch (Exception e) {
            e.printStackTrace();
        }
        // log="reply";
        // writeToLog("UDP DATA TRANSFER","TRANSFER FROM"+this.servername,Status,log);
        return result;
    }
    public void writeToLog(String operation, String params, String status, String responceDetails) {
        try {
            FileWriter myWriter = new FileWriter("C:\\\\Users\\\\yashb\\\\IdeaProjects\\\\DSD_demo\\\\src\\\\Logs\\\\"+file.get(this.servername),true);
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

    public static ArrayList sortDates(ArrayList arr){
        Collections.sort(arr, new Comparator<String>() {
            SimpleDateFormat format = new SimpleDateFormat("yyMMdd");

            @Override
            public int compare(String date1, String date2) {
                try {
                    String datePart1 = date1.substring(1);
                    String datePart2 = date2.substring(1);

                    Date dateOne = format.parse(datePart1);
                    Date dateTwo = format.parse(datePart2);

                    int dateCompare = dateOne.compareTo(dateTwo);
                    if (dateCompare != 0) {
                        return dateCompare;
                    } else {
                        char time1 = date1.charAt(0);
                        char time2 = date2.charAt(0);
                        if (time1 == 'M') {
                            if (time2 == 'M') {
                                return 0;
                            } else {
                                return -1;
                            }
                        } else if (time1 == 'A') {
                            if (time2 == 'M') {
                                return 1;
                            } else if (time2 == 'A') {
                                return 0;
                            } else {
                                return -1;
                            }
                        } else {
                            if (time2 == 'M' || time2 == 'A') {
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });

        return arr;
    }
    @Override
    public String exchangeTickets(String customerID,String old_movieName, String movieID, String new_movieID, String new_movieName, int numberOfTickets) throws Exception {
        if (movieID.substring(0, 3).equals(this.servername))
        {
            if(hashMapCustomer.containsKey(customerID))
            {
                if (hashMapCustomer.get(customerID).containsKey(old_movieName))
                {
                    if (hashMapCustomer.get(customerID).get(old_movieName).containsKey(movieID))
                    {
                        if(hashMapCustomer.get(customerID).get(old_movieName).get(movieID)==numberOfTickets)

                        {
                            System.out.println("hello");
                            check1=true;

                        }
                    }
                }
            }
        }
        else {
            new Thread() {
                public void run() {
                    temp1 = udpThread("checkMovieTicket:" + customerID + " " + old_movieName + " " + movieID + " " + new_movieID +" "+new_movieName+" "+ numberOfTickets, port.get(movieID.substring(0, 3)));
                    if(temp1.equals("done"))
                    {
                        System.out.println(temp1);
                        check1=true;
                    }

                }
            }.start();
            try {
                time.sleep(2L);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        }
        //noww it will check for new movie slot availability
        if(new_movieID.substring(0, 3).equals(this.servername)) {
            if (hashMap.containsKey(new_movieName)) {
                if (hashMap.get(new_movieName).containsKey(new_movieID)) {
                    if (hashMap.get(new_movieName).get(new_movieID) >= numberOfTickets) {
                        check2 = true;
                    }
                }
            }
        }
        else {
            new Thread() {
                public void run() {
                    temp1 = udpThread("checkMovieTicket1:" + customerID + " " + old_movieName + " " + movieID + " " + new_movieID +" "+new_movieName+" "+ numberOfTickets, port.get(new_movieID.substring(0, 3)));
                    if(temp1.equals("done"))
                    {
                        System.out.println(temp1);
                        check2=true;
                    }

                }
            }.start();
            try {
                time.sleep(2L);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }


        }
        if(check1==true && check2==true)
        {
            System.out.println("hello hii i am here in the condition block");
            String s1= cancelMovieTickets(customerID,movieID,old_movieName,numberOfTickets);
            String s2=  bookMovieTickets(customerID,new_movieID,new_movieName,numberOfTickets);
            return s2;

        }
        else {
            return "Tickets Exchanged failed";
        }
    }
    public String ServerexchangeTicketsCheck(String customerID,String old_movieName, String movieID, String new_movieID, String new_movieName, int numberOfTickets) {

        if (hashMapCustomer.containsKey(customerID)) {
            if (hashMapCustomer.get(customerID).containsKey(old_movieName)) {
                if (hashMapCustomer.get(customerID).get(old_movieName).containsKey(movieID)) {
                    if (hashMapCustomer.get(customerID).get(old_movieName).get(movieID) == numberOfTickets) {
                        return "done";

                    }
                }
            }
        }
        return "not done";
    }
    public String ServerexchangeTicketsCheck2(String customerID,String old_movieName, String movieID, String new_movieID, String new_movieName, int numberOfTickets) {
        if (hashMap.containsKey(new_movieName)) {
            if (hashMap.get(new_movieName).containsKey(new_movieID)) {
                if (hashMap.get(new_movieName).get(new_movieID) >= numberOfTickets) {
                    return "done";
                }
            }
        }

        return "not done";
    }
    public String isCustomerBooked(String CustomerId , String movieId,String MovieName)
    {
        if(hashMapCustomer.containsKey(CustomerId))
        {
            Set<String> Str=hashMapCustomer.get(CustomerId).keySet();
            for(String i:Str)
            {
                if (hashMapCustomer.get(CustomerId).containsKey(i))
                {
                    if(hashMapCustomer.get(CustomerId).get(i).containsKey(this.servername+movieId.substring(3)))
                    {
                        return "true";
                    }
                }
            }

        }
        return "false";
    }





}