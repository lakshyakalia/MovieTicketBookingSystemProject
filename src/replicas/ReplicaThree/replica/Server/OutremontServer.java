package replicas.ReplicaThree.replica.Server;




import replicas.ReplicaThree.replica.Services.implementation;

import javax.xml.ws.Endpoint;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class OutremontServer {
    public static void main(String[] args) throws RemoteException {
        implementation i;
        i = new implementation();
        i.setName("OUT");
        i.arr[0]=6890;
        i.arr[1]=6887;
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyy");
        Date d = new Date();
        String date=formatter.format(d);
        i.hashMap.put("AVATAR", new HashMap<String, Integer>());
        i.hashMap.put("TITANIC", new HashMap<String, Integer>());
        i.hashMap.put("AVENGERS", new HashMap<String, Integer>());
        i.hashMap.get("AVATAR").put("OUTM"+date, 400);
        i.hashMap.get("AVATAR").put("OUTA"+date, 400);
        i.hashMap.get("AVATAR").put("OUTE"+date, 400);

        i.sortShows.put("AVATAR",new ArrayList<>(Arrays.asList("M"+date, "A"+date, "E"+date)));

        i.hashMap.get("AVENGERS").put("OUTM"+date, 400);
        i.hashMap.get("AVENGERS").put("OUTA"+date, 400);
        i.hashMap.get("AVENGERS").put("OUTE"+date, 400);
        i.sortShows.put("AVENGERS",new ArrayList<>(Arrays.asList("M"+date, "A"+date, "E"+date)));


        i.hashMap.get("TITANIC").put("OUTM"+date, 400);
        i.hashMap.get("TITANIC").put("OUTA"+date, 400);
        i.hashMap.get("TITANIC").put("OUTE"+date, 400);
        i.sortShows.put("TITANIC",new ArrayList<>(Arrays.asList("M"+date, "A"+date, "E"+date)));

        try {
            //LocateRegistry.createRegistry(4555);

            Endpoint ep = Endpoint.publish("http://localhost:"+9091+ "/DMTBS",i);
            System.out.println("Outremont server ready");
        } catch (Exception e) {
            System.out.println(e);
        }
        while (true) {
            try (DatagramSocket aSocket = new DatagramSocket(6889)) {
                String result = "";
                byte[] buffer = new byte[1000];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String data = new String(request.getData()).trim();
                String[] data1=data.split(":");
                switch (data1[0])
                {
                    case "getBookingSchedule":
                        result=i.getServerBookingSchedule(data1[1]);
                        break;
                    case "bookMovieTickets":
                        String[] para=data1[1].split(" ");
                        result=i.serverbookMovieTickets(para[0],para[2],para[1],Integer.parseInt(para[3]));
                        break;
                    case "cancelMovieTickets":
                        String[] para1=data1[1].split(" ");
                        result=i.serverCancelMovieTickets(para1[0],para1[1],para1[2],Integer.parseInt(para1[3]));
                        break;
                    case "listMovieShowAvailability":
                        result=i.serverListMovieShowAvailability(data1[1]);
                        break;
                    case "checkMovieTicket":
                        String[] para2=data1[1].split(" ");
                        result=i.ServerexchangeTicketsCheck(para2[0],para2[1],para2[2],para2[3],para2[4],Integer.parseInt(para2[5]));
                        break;
                    case "checkMovieTicket1":
                        String[] para3=data1[1].split(" ");
                        result=i.ServerexchangeTicketsCheck2(para3[0],para3[1],para3[2],para3[3],para3[4],Integer.parseInt(para3[5]));
                        break;
                    case "checkCustomer":
                        String[] para4=data1[1].split(" ");
                        result=i.isCustomerBooked(para4[0],para4[1],para4[2]);
                        break;

                }


                DatagramPacket reply = new DatagramPacket(result.getBytes(), result.length(), request.getAddress(),
                        request.getPort());
                aSocket.send(reply);

            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }
}
