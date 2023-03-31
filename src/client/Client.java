package client;

import frontend.FrontendInterface;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;


public class Client  {
    public static void main(String [] args) throws MalformedURLException {
        URL url=new URL("http://localhost:8070/frontend?wsdl");
        QName qName=new QName("http://frontend/","FrontendServiceService");
        Service service= Service.create(url,qName);
        FrontendInterface frontendInterface=service.getPort(FrontendInterface.class);
        System.out.println("This is a result "+frontendInterface.forwardMessageToSequencer("Hello Message"));
    }
}
