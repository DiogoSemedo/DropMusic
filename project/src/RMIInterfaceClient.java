import java.rmi.*;

public interface RMIInterfaceClient extends Remote {
    public void print_on_client(String s) throws RemoteException;
    public String getInput() throws RemoteException;
}
