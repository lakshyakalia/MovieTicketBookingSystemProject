package models;

import java.net.HttpURLConnection;

public class ResponseObject implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public ResponseObject(){

    }
    public String response;
    public String currentUser;
    public String movieID;
    public String movieName;
    public int bookingCapacity;
    public int noOfTickets;
    public int requestCount;
    public int responseCode;
    public String responseMessage;
}
