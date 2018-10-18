import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Scanner;

public class RMIServer extends UnicastRemoteObject implements RMIInterface{

    public RMIServer() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws RemoteException{
        RMIInterface rmi = new RMIServer();
        LocateRegistry.createRegistry(1099).rebind("dropmusic",rmi);
        System.out.println("Server ready...");
    }

    //methods
    public HashMap<String,String> regist(){
        Scanner read = new Scanner(System.in);
        //exemplo --> type|regist;username|name;password|pass
        HashMap<String,String> message = new HashMap<>();
        message.put("type","regist");
        System.out.println("Username:");
        message.put("username",read.nextLine());
        System.out.println("Password:");
        message.put("password",read.nextLine());
        //enviar pela socket
        //receber resposta
        return message;
    }

    public HashMap<String,String> login(){
        Scanner read = new Scanner(System.in);
        HashMap<String,String> message = new HashMap<>();
        message.put("type","login");
        System.out.println("Username:");
        message.put("username",read.nextLine());
        System.out.println("Password:");
        message.put("password",read.nextLine());
        //enviar pela socket
        //receber resposta
        return message;
    }

}
