package replicas.ReplicaFour.replica.Interface;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import java.io.IOException;


@WebService
@SOAPBinding(style=Style.RPC)
public interface MovieTicketInterface {

	/**
	 * 
	 * @param movieID
	 * @param movieName
	 * @param bookingCapacity
	 * @return
	 * @throwsRemoteException
	 */
	public String addMovieSlots(String movieID, String movieName, int bookingCapacity);
	
	/**
	 * 
	 * @param movieID
	 * @param movieName
	 * @return
	 */
	public String removeMovieSlots (String movieID, String movieName);
	
	/**
	 * 
	 * @param movieName
	 */
	public String listMovieShowAvailability(String movieName);
	
	/**
	 * 
	 * @param customerID
	 * @param movieID
	 * @param movieName
	 * @param numberOfTickets
	 * @return
	 * @throws IOException
	 */
	public String bookMovieTickets(String customerID, String movieID,String movieName, int numberOfTickets);

	/**
	 * 
	 * @param customerID
	 * @return
	 * @throws IOException
	 */
	public String getBookingSchedule(String customerID);
	
	/**
	 * 
	 * @param customerID
	 * @param movieID
	 * @param movieName
	 * @param numberOfTicekts
	 * @return
	 * @throws IOException
	 */
	public String cancelMovieTickets(String customerID,String movieID, String movieName,int numberOfTicekts);
	
	/**
	 * 
	 * @param customerID
	 * @param movieID
	 * @param newMovieID
	 * @param newMovieName
	 * @paramnumberOfTickets
	 * @return
	 * @throws IOException
	 */
	public String exchangeTickets(String customerID,String movieName, String movieID,String newMovieID,String newMovieName,int numberOfTicketsToCancel);
}


