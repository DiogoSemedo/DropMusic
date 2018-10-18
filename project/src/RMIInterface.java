import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface RMIInterface extends Remote{
    public HashMap<String,String> regist() throws RemoteException;
    public HashMap<String,String> login() throws RemoteException;

}
