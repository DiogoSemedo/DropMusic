import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;


public interface RMIInterfaceServer extends Remote{
    public HashMap<String,String> regist(RMIInterfaceClient client) throws RemoteException;
    public HashMap<String,String> login(RMIInterfaceClient client) throws RemoteException;
    public String selectId() throws RemoteException;
    public String select() throws RemoteException;
    public HashMap<String,String> insert(HashMap<String,String> message)throws RemoteException;
    public String rate() throws RemoteException;
    public String review() throws RemoteException;
    public HashMap<String,String> request(HashMap<String,String> message) throws RemoteException;
    public void printMessage(HashMap<String,String> message, RMIInterfaceClient client)  throws RemoteException;
}