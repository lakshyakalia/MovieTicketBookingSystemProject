package frontend;


import javax.xml.ws.Endpoint;
import java.io.IOException;

public class Frontend {
    public static void main(String [] args) throws IOException, ClassNotFoundException {
        Endpoint endpoint=Endpoint.publish("http://localhost:8070/frontend", new FrontendService());
        System.out.println("WebServer Started");
        Runnable task=()->{
            while(true)
            {
                try {
                    FrontendService.ListenFrontEnd();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        };
       Thread t=new Thread(task);
       t.start();
    }
}
