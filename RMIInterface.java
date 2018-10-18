import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface RMIInterface extends Remote{
    public HashMap<String,String> regist() throws RemoteException;
    public HashMap<String,String> login() throws RemoteException;
    public String selectId() throws RemoteException;
    public String select() throws RemoteException;
    public HashMap<String,String> insert(HashMap<String,String> message)throws RemoteException;
    public String rate() throws RemoteException;
    public String review() throws RemoteException;

}
