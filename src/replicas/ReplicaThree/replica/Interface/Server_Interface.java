package replicas.ReplicaThree.replica.Interface;



import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)

public interface Server_Interface{
    @WebMethod
    public String  bookMovieTickets (String customerID,String movieID,String movieName,int  numberOfTickets) throws Exception;
    @WebMethod
    public String  getBookingSchedule (String customerID) throws Exception ;
    @WebMethod
    public String  cancelMovieTickets (String customerID, String movieID,String  movieName, int numberOfTickets) throws Exception;
    @WebMethod
    public String exchangeTickets (String customerID, String old_movieName,String movieID, String new_movieID,String  new_movieName, int numberOfTickets) throws Exception;


    @WebMethod
    public String addMovieSlots (String movieID, String movieName, int bookingCapacity) throws Exception;
    @WebMethod
    public String removeMovieSlots(String movieID,String movieName) throws Exception;
    @WebMethod
    public String listMovieShowAvailability(String movieName) throws Exception;
    @WebMethod
    public String  adminBookMovieTickets (String customerID,String movieID,String movieName,int  numberOfTickets) throws Exception;
    @WebMethod
    public String   adminGetBookingSchedule (String customerID) throws Exception ;
    @WebMethod
    public String  adminCancelMovieTickets (String customerID, String movieID,String  movieName, int numberOfTickets) throws Exception;
    // public String ServerExchangeTickets (String customerID, String movieID, String new_movieID,String  new_movieName, int numberOfTickets);
}
