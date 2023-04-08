package replicas.ReplicaFour.replica.Server;

import replicas.ReplicaFour.replica.Implementation.MovieTicketImplementation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import javax.xml.ws.Endpoint;


public class OutremontServer {
	public static void main(String[] args) throws Exception {
		MovieTicketImplementation outMovieTicketImplementation=new MovieTicketImplementation("OUT");
		Endpoint endpoint= Endpoint.publish("http://localhost:8091/outremont",outMovieTicketImplementation);
		System.out.println("Outremont Web Service is published Successfully "+endpoint.isPublished());
		while(true) {
			requestHandler(outMovieTicketImplementation);
		}
		
	}		
	public static void requestHandler(MovieTicketImplementation outMovieTicketImplementation) throws IOException{
			//Send request to get data.
			try {
				DatagramSocket socket = new DatagramSocket(3802);
				String responseReturn="";
				byte[] b=new byte[1024];
				while(true){
					DatagramPacket dp1=new DatagramPacket(b, b.length);
					socket.receive(dp1);

					//Now to send response
					String request=new String(dp1.getData()).trim();
					String [] requestMessageArray=request.split("#");

					String userID=requestMessageArray[0];
					String movieName=requestMessageArray[1];
					String movieSlotID=requestMessageArray[2];
					int numberOfTickets=Integer.parseInt(requestMessageArray[3]);
					int port=Integer.parseInt(requestMessageArray[4]);
					String function=requestMessageArray[5];
					String newMovieName=requestMessageArray[6];
					String newMovieID=requestMessageArray[7];
					switch(function) {
						case "listMovieShowsAvailability":{
							String result= outMovieTicketImplementation.listMovieShowsAvailabilityFromOther(movieName);
							responseReturn=result;
							break;
						}
						case "bookMovieTicket":{
							String result=outMovieTicketImplementation.bookMovieTickets(userID, movieSlotID, movieName, numberOfTickets);
							responseReturn=result;
							break;
						}
						case "getBookingSchedule":{
							String result= outMovieTicketImplementation.getBookingScheduleFromOther(userID);
							responseReturn=result;
							break;
						}
						case "cancelMovieTickets":{
							String result=outMovieTicketImplementation.cancelMovieTickets(userID, movieSlotID, movieName, numberOfTickets);
							responseReturn=result;
							break;
						}
						case "exchangeTickets":{
							String result=outMovieTicketImplementation.exchangeTickets(userID, movieName, movieSlotID, newMovieID, newMovieName, numberOfTickets);
							responseReturn=result;
							break;
						}
						case "bookingInExchange":{
							String result=outMovieTicketImplementation.bookMovieTickets(userID, newMovieID, newMovieName, numberOfTickets);
							responseReturn=result;
							break;
						}
						case "checkBookingForSameSlotForSameMovie":{
							String result=outMovieTicketImplementation.checkBookingForSameSlotForSameMovie(userID, movieSlotID, movieName);
							responseReturn=result;
							break;
						}
						default:{
							break;
						}
					}
					String message=responseReturn;
					byte[] b2=message.getBytes();
					DatagramPacket dp2=new DatagramPacket(b2, b2.length,dp1.getAddress(),dp1.getPort());
					socket.send(dp2);
				}

			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return;
		}

}
