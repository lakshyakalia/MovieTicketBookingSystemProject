package replicas.ReplicaOne.replica.Interface;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface MovieInterface {
    /**
     * Adding Movie Slots
     * @param movieID
     * @param movieName
     * @param bookingCapacity
     * @return
     */
    public String addMovieSlots(String movieID, String movieName, int bookingCapacity) ;

    /**
     * Removing Movie Slots
     * @param movieID
     * @param movieName
     * @return
     */
    public String removeMovieSlots(String movieID, String movieName) ;

    /**
     * List all the no of tickets
     * Available for a particular
     * movie in all locations
     * @param movieName
     * @return
     */
    public String listMovieShowAvailability(String movieName) ;

    /**
     * Book Movie Tickets
     * @param customerID
     * @param movieID
     * @param movieName
     * @param noOfTickets
     * @return
     */
    public String bookMovieTickets(String customerID, String movieID, String movieName, int noOfTickets) ;

    /**
     * Get list of all bookings
     * by the user
     * @param customerID
     * @return
     */
    public String getBookingSchedule(String customerID) ;

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
    public String exchangeTickets(String customerID, String movieID, String new_movieID, String new_movieName, int numberOfTickets) ;
}
