import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface RMIInterface extends Remote{


    public HashMap<String,String> request(HashMap<String,String> message) throws RemoteException;

}
