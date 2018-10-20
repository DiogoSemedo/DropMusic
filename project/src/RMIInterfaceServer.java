import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;


public interface RMIInterfaceServer extends Remote{
    public HashMap<String,String> regist(RMIInterfaceClient client) throws RemoteException;
    public HashMap<String,String> login(RMIInterfaceClient client) throws RemoteException;
    public String selectId(RMIInterfaceClient client) throws RemoteException;
    public String select(RMIInterfaceClient client) throws RemoteException;
    public HashMap<String,String> insert(HashMap<String,String> message, RMIInterfaceClient client)throws RemoteException;
    public String rate(RMIInterfaceClient client) throws RemoteException;
    public String review(RMIInterfaceClient client) throws RemoteException;
    public HashMap<String,String> request(HashMap<String,String> message) throws RemoteException;
    public void printMessage(HashMap<String,String> message, RMIInterfaceClient client)  throws RemoteException;
    public String selectKey(RMIInterfaceClient client, String select) throws RemoteException;
    public String selectValue (RMIInterfaceClient client) throws RemoteException;
    public void addRef(String ClientID,RMIInterfaceClient client) throws RemoteException;
    public HashMap<String,String> promote(HashMap<String,String> message) throws RemoteException;
    public void sendNotification(String select, String id) throws RemoteException;

}