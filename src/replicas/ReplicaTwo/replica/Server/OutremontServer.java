package replicas.ReplicaTwo.replica.Server;


import replicas.ReplicaTwo.replica.Implementation.Implementation;

import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class OutremontServer {
    public static void main(String args[]) {
        try{
            Implementation obj;
            obj = new Implementation();

            obj.serverName="OUT";
            obj.portsToPing= new int[]{5099, 5098};

            SimpleDateFormat formatter = new SimpleDateFormat("ddMMyy");
            Date d = new Date();
            String date=formatter.format(d);

            obj.movieData.put("AVATAR", new HashMap<String, Integer>());
            obj.movieData.put("TITANIC", new HashMap<String, Integer>());
            obj.movieData.put("AVENGERS", new HashMap<String, Integer>());
            obj.movieData.get("AVATAR").put("OUTM"+date, 400);
            obj.movieData.get("AVATAR").put("OUTA"+date, 400);
            obj.movieData.get("AVATAR").put("OUTE"+date, 400);
            obj.showSort.put("AVATAR",new ArrayList<>(Arrays.asList("M"+date, "A"+date, "E"+date)));
            obj.movieData.get("AVENGERS").put("OUTM"+date, 400);
            obj.movieData.get("AVENGERS").put("OUTA"+date, 400);
            obj.movieData.get("AVENGERS").put("OUTE"+date, 400);
            obj.showSort.put("AVENGERS",new ArrayList<>(Arrays.asList("M"+date, "A"+date, "E"+date)));

            obj.movieData.get("TITANIC").put("OUTM"+date, 400);
            obj.movieData.get("TITANIC").put("OUTA"+date, 400);
            obj.movieData.get("TITANIC").put("OUTE"+date, 400);
            obj.showSort.put("TITANIC",new ArrayList<>(Arrays.asList("M"+date, "A"+date, "E"+date)));



            Endpoint ep = Endpoint.publish("http://localhost:" + 5097 + "/DMTBS", obj);
            System.out.println(ep.isPublished());
            System.out.println("Outremont Server is ready and waiting ...");

            //UDP
            Runnable task = () -> {
                DatagramSocket socket = null;
                try {
                    socket = new DatagramSocket(5097);
                } catch (SocketException e) {
                    throw new RuntimeException(e);
                }
                while(true)
                {
                    System.out.println("hit inside server");
                    byte[] responseData = new byte[1024];
                    DatagramPacket response = new DatagramPacket(responseData, responseData.length);
                    try {
                        socket.receive(response);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    String[] responseString =new String(response.getData()).trim().split(";");
                    int port=Integer.parseInt(responseString[0]);
                    String userID=responseString[1];
                    String movieName;
                    String movieID;
                    String new_movieName="";
                    String new_movieID="";
                    if(port==4){
                        movieName=responseString[2].split("_")[0];
                        movieID=responseString[3].split("_")[0];
                        new_movieName=responseString[2].split("_")[1];
                        new_movieID=responseString[3].split("_")[1];
                    }else{
                        movieName=responseString[2];
                        movieID=responseString[3];
                    }
                    int numberOfTickets=Integer.parseInt(responseString[4]);
                    String stringToSend="";
                    switch (port){
                        case 1:
                            stringToSend=obj.receiveFromServerListShows(movieName);
                            break;
                        case 2:
                            stringToSend=obj.receiveFromServerBookTickets(userID, movieID, movieName,numberOfTickets);
                            break;
                        case 3:
                            stringToSend=obj.receiveFromServerCancelTickets(userID, movieID, movieName,numberOfTickets);
                            break;
                        case 4:
                            stringToSend=obj.receiveExchangeTickets( userID,movieName,movieID, new_movieID, new_movieName, numberOfTickets);
                            break;
                    }

                    System.out.println(stringToSend);
                    byte[] b2= stringToSend.getBytes();
                    InetAddress ia= null;
                    try {
                        ia = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                    DatagramPacket dp1=new DatagramPacket(b2,b2.length,ia,response.getPort());
                    try {
                        socket.send(dp1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            };
            Thread t = new Thread(task);
            t.start();





        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}

