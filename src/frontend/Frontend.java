package frontend;

import javax.xml.ws.Endpoint;

public class Frontend {
    public static void main(String [] args){
        Endpoint endpoint=Endpoint.publish("http://localhost:8070/frontend", new FrontendService());
        System.out.println("WebServer Started");
    }
}
