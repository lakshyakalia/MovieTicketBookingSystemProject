package replicas.ReplicaTwo.replica.Interface;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface CustomerInterface {

    /**
     * Book Movie Tickets
     * @param customerID
     * @param movieID
     * @param movieName
     * @param noOfTickets
     * @return
     */
    public String bookMovieTickets(String customerID, String movieID, String movieName, int noOfTickets);

    /**
     * Get list of all bookings
     * by the user
     * @param customerID
     * @return
     */
    public String getBookingSchedule(String customerID);

    /**
     * Cancel any tickets booked
     * by the user
     * @param customerID
     * @param movieID
     * @param movieName
     * @param noOfTickets
     * @return
     */
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int noOfTickets) ;

    /**
     * Exchange tickets
     * @param customerID
     * @param movieID
     * @param new_movieID
     * @param new_movieName
     * @param numberOfTickets
     * @return
     * @
     */
    public String exchangeTickets(String customerID, String movieID, String new_movieID, String new_movieName, int numberOfTickets) ;
}
