package replicas.ReplicaTwo.replica.Interface;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;


@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface AdminInterface{
    @WebMethod
    String addMovieSlots(String movieID, String movieName, int bookingCapacity) throws ParseException;
    @WebMethod
    String removeMovieSlots (String movieID,String movieName) ;
    @WebMethod
    String listMovieShowsAvailability (String movieName) throws ExecutionException, InterruptedException;
    @WebMethod
    String bookMovieTickets (String customerID,String movieID,String movieName,int numberOfTickets) ;
    @WebMethod
    String getBookingSchedule (String customerID) ;
    @WebMethod
    String cancelMovieTickets (String customerID,String movieID, String movieName,int numberOfTickets);
    @WebMethod
    String exchangeTickets(String customerID, String old_movieName, String old_movieID, String new_movieID, String new_movieName, int numberOfTickets) throws RemoteException;

   // String listMovieShowAvailability(String movieName);
}
