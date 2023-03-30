package frontend;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import java.io.IOException;
import java.net.*;

@WebService(endpointInterface="frontend.FrontendInterface")
@SOAPBinding(style = Style.RPC)
public class FrontendService implements FrontendInterface{

    @Override
    public void forwardMessageToSequencer(String message) {
        sendMessageToSequencer(message);
        return;
    }

    public void sendMessageToSequencer(String message){
        try{
            //send a message via UDP
            DatagramSocket socket=new DatagramSocket();
            String messageToSend=message;
            byte[] byteMessage=messageToSend.getBytes();
            InetAddress ia=InetAddress.getLocalHost();
            DatagramPacket packet=new DatagramPacket(byteMessage,byteMessage.length,ia,4822);
            socket.send(packet);
            String response="";

            //recieve a message via UDP
            byte [] recieveByte=new byte[1024];
            DatagramPacket recievePacket=new DatagramPacket(recieveByte,recieveByte.length);
            socket.receive(recievePacket);
            response=new String(recievePacket.getData()).trim();
            socket.close();
            return;
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
