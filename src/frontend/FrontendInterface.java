package frontend;

import models.RequestObject;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService
@SOAPBinding(style = Style.RPC)
public interface FrontendInterface {
    public String forwardMessageToSequencer(RequestObject requestObject);
    public String getRequestFromClient(RequestObject requestObject);

//    public String recieveFromReplicaManager();
}
