//package replicas.ReplicaFour.replica.Client;
//
//import java.util.Date;
//import java.util.Scanner;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//
//import javax.xml.namespace.QName;
//import javax.xml.ws.Service;
//
//import Interface.MovieTicketInterface;
//
//public class Client {
//	static Scanner userInput=new Scanner(System.in);
//	public static void main(String args[]) throws MalformedURLException {
//
//	    System.out.println("Client ready and waiting...");
//
//
//	      // call the add method on the Addition object
//	      boolean programFlag=true;						//flag to stop the program.
//
//			while(programFlag) {
//				System.out.println("Enter userID");
//				String userID=userInput.nextLine().trim();
//
//
//				int check=checkUser(userID);					//checking the type of user.
//				String serverUrl=getServerAPI(userID);
//				URL url=new URL(serverUrl+"/?wsdl");
//				QName qName= new QName("http://Implementation/","MovieTicketImplementationService");
//				Service service=Service.create(url,qName);
//				MovieTicketInterface movieTicketInterface=service.getPort(MovieTicketInterface.class);
//				boolean userFlag=true;
//				String movieSlotID="";
//
//				if(check==1) {									//for admin user
//					System.out.println("Admin Working"+serverUrl);
////					AdminInterface adminObj=(AdminInterface)Naming.lookup(serverAdddress);
//					while(userFlag) {
//						int adminChoice=adminFunctions();				//Menu Choice
//						switch(adminChoice) {
//							//adding moive slots
//							case 1:{
//								while(true) {
//									System.out.println("Enter Movie Slot ID: \n");
//									movieSlotID=userInput.nextLine().trim();
//									int year=2000+Integer.parseInt(movieSlotID.substring(8));
//									String userDate=year+""+movieSlotID.substring(6,8)+""+movieSlotID.substring(4,6);
//									SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMdd");
//									Date date = null;
//									try {
//										date = dateFormat.parse(userDate);
//									} catch (ParseException e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									}
//									int dateDiff= (int) (date.getTime()-new Date().getTime())/(1000*60*60*24);
//									if (dateDiff > 6 || dateDiff<0) {
//										System.out.println("You can only book with in a week from now.");
//										continue;
//									}
//									if(movieSlotID.substring(0,3).equals(userID.substring(0,3))) {
//										break;
//									}
//									else {
//										System.out.println("You cannot add slots to other servers. Try Again \n");
//									}
//								}
//
//								boolean movieChoiceFlag=true;
//								String movieName="";
//								while(movieChoiceFlag) {
//									System.out.println("Choose a Movie : \n 1.Avatar \n 2.Avenger \n 3.Titanic");
//									int movieChoose=Integer.parseInt(userInput.nextLine());
//									if (movieChoose==1) {
//										movieName="Avatar";
//										movieChoiceFlag=false;
//									}
//									else if(movieChoose==2) {
//										movieName="Avenger";
//										movieChoiceFlag=false;
//									}
//									else if(movieChoose==3){
//										movieName="Titanic";
//										movieChoiceFlag=false;
//									}
//									else {
//										System.out.println("Choose right movie");
//										movieChoiceFlag=true;
//									}
//								}
//
//								System.out.println("Enter the capacity");
//								int capacity=Integer.parseInt(userInput.nextLine());
//								String message=movieTicketInterface.addMovieSlots(movieSlotID,movieName,capacity);
//								System.out.println(message);
//								break;
//							}
//							//removing movie slots
//							case 2:{
//								while(true) {
//									System.out.println("Enter Movie Slot ID you want to remove: \n");
//									movieSlotID=userInput.nextLine().trim();
//									if(movieSlotID.substring(0,3).equals(userID.substring(0,3))) {
//										break;
//									}
//									else {
//										System.out.println("You cannot delete slots from other servers. Try Again \n");
//									}
//								}
//
//								System.out.println("Enter Movie Name: \n");
//								String movieName=userInput.nextLine().trim();
//
//								String message=movieTicketInterface.removeMovieSlots(movieSlotID,movieName);
//								System.out.println(message);
//								break;
//							}
//							//listing all movie shows availability.
//							case 3:{
//								System.out.println("Enter a movie name for slot availability");
//								String movieName=userInput.nextLine().trim();
//								String result=movieTicketInterface.listMovieShowsAvailability(movieName);
//								System.out.println(movieName+":"+result);
//								break;
//							}
//							//booking a movie tickets
//							case 4:{
//								System.out.println("Enter Movie slot ID");
//								String bookmovieSlotID=userInput.nextLine();
//								System.out.println("Enter Movie Name");
//								String bookMovieName=userInput.nextLine();
//								System.out.println("Enter Number of tickets");
//								int numberOfTicketsToBook=Integer.parseInt(userInput.nextLine().trim());
//								String result=movieTicketInterface.bookMovieTicket(userID, bookmovieSlotID, bookMovieName, numberOfTicketsToBook);
//								System.out.println(result);
//								break;
//							}
//							//Listing movie booked schedule
//							case 5:{
//								String result=movieTicketInterface.getBookingSchedule(userID);
//								System.out.println("Booking schedule "+result);
//								break;
//							}
//							case 6:{
//								System.out.println("Enter Movie slot ID");
//								String cancelmovieSlotID=userInput.nextLine();
//								System.out.println("Enter Movie Name");
//								String cancelMovieName=userInput.nextLine();
//								System.out.println("Enter Number of tickets to cancel");
//								int numberOfTicketsToCancel=Integer.parseInt(userInput.nextLine().trim());
//								String result=movieTicketInterface.cancelMovieTickets(userID,cancelmovieSlotID, cancelMovieName, numberOfTicketsToCancel);
//								System.out.println(result);
//								break;
//							}
//							case 7:{
//								System.out.println("To exchange tickets:\n Enter movie name of existing ticket");
//								String movieName=userInput.nextLine().trim();
//								System.out.println("Enter movieID of existing ticket");
//								movieSlotID=userInput.nextLine().trim();
//								System.out.println("Enter new movie Name");
//								String newMovieName=userInput.nextLine().trim();
//								System.out.println("Enter new movieID");
//								String newMovieSlotID=userInput.nextLine().trim();
//								System.out.println("Enter Number of Tickets you want to exchange");
//								int numberOfTickets=Integer.parseInt(userInput.nextLine().trim());
//								String result=movieTicketInterface.exchangeTickets(userID,movieName,movieSlotID, newMovieSlotID, newMovieName, numberOfTickets);
//								System.out.println(result);
//								break;
//							}
//							case 8:{
//								userFlag=false;
//								break;
//							}
//						}
//					}
//
//				}
//				else if(check==2) {										//for customer user
//					System.out.println("Customer Working");
////					UserInterface movieTicketInterface = UserInterfaceHelper.narrow(ncRef.resolve_str(serverAdddress));
//					while(userFlag) {
//						int customerChoice=customerFunctions();
//						switch(customerChoice){
//						case 1:{
//							System.out.println("Enter Movie slot ID");
//							String bookmovieSlotID=userInput.nextLine().trim();
//							System.out.println("Enter Movie Name");
//							String bookMovieName=userInput.nextLine();
//							System.out.println("Enter Number of tickets");
//							int numberOfTicketsToBook=Integer.parseInt(userInput.nextLine().trim());
//							System.out.println("userID is: "+userID);
//							String result=movieTicketInterface.bookMovieTicket(userID, bookmovieSlotID, bookMovieName, numberOfTicketsToBook);
//							System.out.println(result);
//							break;
//						}
//						//Listing movie booked schedule
//						case 2:{
//							String result=movieTicketInterface.getBookingSchedule(userID);
//							System.out.println("Booking schedule "+result);
//							break;
//						}
//						case 3:{
//							System.out.println("Enter Movie slot ID");
//							String cancelmovieSlotID=userInput.nextLine();
//							System.out.println("Enter Movie Name");
//							String cancelMovieName=userInput.nextLine();
//							System.out.println("Enter Number of tickets to cancel");
//							int numberOfTicketsToCancel=Integer.parseInt(userInput.nextLine().trim());
//							String result=movieTicketInterface.cancelMovieTickets(userID,cancelmovieSlotID, cancelMovieName, numberOfTicketsToCancel);
//							System.out.println(result);
//							break;
//						}
//						case 4:{
//							System.out.println("To exchange tickets:\n Enter movie name of existing ticket");
//							String movieName=userInput.nextLine().trim();
//							System.out.println("Enter movieID of existing ticket");
//							movieSlotID=userInput.nextLine().trim();
//							System.out.println("Enter new movie Name");
//							String newMovieName=userInput.nextLine().trim();
//							System.out.println("Enter new movieID");
//							String newMovieSlotID=userInput.nextLine().trim();
//							System.out.println("Enter Number of Tickets you want to exchange");
//							int numberOfTickets=Integer.parseInt(userInput.nextLine().trim());
//							String result=movieTicketInterface.exchangeTickets(userID,movieName,movieSlotID, newMovieSlotID, newMovieName, numberOfTickets);
//							System.out.println(result);
//							break;
//						}
//						case 5:{
//							userFlag=false;
//							break;
//						}
//						}
//					}
//				}
//				else {
//					System.out.println("Invalid Username");
//				}
//
//			}
//}
//
//	public static int checkUser(String userID) {
//		if(userID.charAt(3)=='A') {
//			int type=1;
//			return type;
//		}
//		else if (userID.charAt(3)=='C') {
//			int type=2;
//			return type;
//		}
//		else {
//			int type=3;
//			return type;
//		}
//
//	}
//
//	public static String getServerAPI(String userID) {
//		String serverSubstring=userID.substring(0,3).toUpperCase();
//		System.out.println(serverSubstring);
//		if(serverSubstring.equals("ATW")) {
//			return "http://localhost:7000/atwater";
//		}
//		else if(serverSubstring.equals("OUT")) {
//			return "http://localhost:7001/outremont";
//		}
//		else if(serverSubstring.equals("VER")) {
//			return "http://localhost:7002/verdun";
//		}
//		else {
//			return "Enter Servername Correctly";
//		}
//
//	}
//
//	static int adminFunctions() {
//		System.out.println("Choose:\n 1.Add Movie Slots \n 2.Remove Movie Slots \n 3. List Movie Slot Availability \n 4. Book Movie Tickets \n 5. Get Movie Booking Schedule  \n 6.Cancel Movie Ticket \n 7. Exchange Movie Ticket \n 8.Logout");
//		int adminChoice=Integer.parseInt(userInput.nextLine());
//		return adminChoice;
//	}
//
//	static int customerFunctions() {
//		System.out.println("Choose:\n 1. Book Movie Tickets \n 2. Get Movie Booking Schedule  \n 3.Cancel Movie Ticket \n 4.Exchange Movie Tickets \n 5.Logout");
//		int customerChoice=Integer.parseInt(userInput.nextLine());
//		return customerChoice;
//	}
//}
//
