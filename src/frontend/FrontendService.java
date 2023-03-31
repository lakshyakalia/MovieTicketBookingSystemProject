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
    public String forwardMessageToSequencer(String message) {
        return sendMessageToSequencer(message);
    }

    public String sendMessageToSequencer(String message){
        try{
            //send a message via UDP
            DatagramSocket socket=new DatagramSocket(4455);
            String messageToSend=message;
            byte[] byteMessage=messageToSend.getBytes();
            InetAddress ia=InetAddress.getLocalHost();
            DatagramPacket packet=new DatagramPacket(byteMessage,byteMessage.length,ia,4822);
            socket.send(packet);

            //recieve a message via UDP
            DatagramSocket socketForReplicaOne=new DatagramSocket(5122);
            DatagramSocket socketForReplicaTwo=new DatagramSocket(5123);
            String response="";
            byte [] recieveByteOne=new byte[1024];
            byte [] recieveByteTwo=new byte[1024];
            DatagramPacket recievePacketOne=new DatagramPacket(recieveByteOne,recieveByteOne.length);
            DatagramPacket recievePacketTwo=new DatagramPacket(recieveByteTwo,recieveByteTwo.length);
            socketForReplicaOne.receive(recievePacketOne);
            socketForReplicaTwo.receive(recievePacketTwo);
            response=new String(recievePacketOne.getData()).trim() + " "+ new String(recievePacketTwo.getData()).trim();
            socketForReplicaOne.close();
            socketForReplicaTwo.close();
            socket.close();
            return response;
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
