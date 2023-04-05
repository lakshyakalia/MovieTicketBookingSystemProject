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

    @Override
    public String toString() {
        return "RequestObject{" +
                "requestType='" + requestType + '\'' +
                ", currentUser='" + currentUser + '\'' +
                ", movieID='" + movieID + '\'' +
                ", oldMovieID='" + oldMovieID + '\'' +
                ", movieName='" + movieName + '\'' +
                ", bookingCapacity=" + bookingCapacity +
                ", noOfTickets=" + noOfTickets +
                ", requestCount=" + requestCount +
                '}';
    }
}
