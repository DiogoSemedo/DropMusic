import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.Scanner;

public class RMIClient {

    public void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException{
        RMIInterface rmi = (RMIInterface) Naming.lookup("dropmusic");
        System.out.println("Client ready...");
        boolean login = false;
        HashMap<String,String> message = new HashMap<>();
        Scanner keyboardScanner = new Scanner(System.in);
        while(true){
            message.clear();
            if(!login){
                System.out.println("Menu: \n1 - Regist\n2 - Login");
                switch (keyboardScanner.nextInt()){
                    case 1:
                        message = rmi.regist();
                        printMessage(message);
                        break;
                    case 2:
                         message = rmi.login();
                         if(message.get("login").equals("sucessful")){//verificar se o regist foi bem sucedido
                            //login sucessful
                            login = true;
                         }
                        printMessage(message);
                        break;
                    default:
                        System.out.println("Wrong command");
                        break;
                }

            }
            else if(login){

            }
        }

    }

    public void printMessage(HashMap<String,String> message){
        for (HashMap.Entry<String, String> entry : message.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
}
