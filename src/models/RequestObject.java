package models;

public class RequestObject implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public RequestObject(){

    }
    public String requestType;
    public String currentUser;
    public String movieID;
    public String oldMovieID;
    public String movieName;
    public int bookingCapacity;
    public int noOfTickets;
    public int requestCount;
}
