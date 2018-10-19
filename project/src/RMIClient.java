import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.Scanner;

public class RMIClient extends UnicastRemoteObject implements RMIInterfaceClient {

    public RMIClient() throws RemoteException {
        super();
    }

    public void print_on_client(String s) throws RemoteException {
        System.out.println(s);
    }

    public String getInput() {
        return new Scanner(System.in).nextLine();
    }

    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
        RMIInterfaceServer rmi = (RMIInterfaceServer) Naming.lookup("dropmusic");
        System.out.println("Client ready...");
        RMIClient c = new RMIClient();
        boolean login = false;
        HashMap<String, String> message = new HashMap<>();
        Scanner keyboardScanner = new Scanner(System.in);
        while (true) {
            message.clear();
            if (!login) {
                System.out.println("Menu: \n1 - Regist\n2 - Login");
                switch (keyboardScanner.nextInt()) {
                    case 1:
                        message = rmi.regist((RMIInterfaceClient) c);
                        rmi.printMessage(message, (RMIInterfaceClient) c);
                        break;
                    case 2:
                        message = rmi.login((RMIInterfaceClient) c);
                        rmi.printMessage(message, (RMIInterfaceClient) c);

                        if (message.get("login").equals("successful")) {//verificar se o regist foi bem sucedido
                            //login sucessful
                            login = true;
                        }
                        break;
                    default:
                        System.out.println("Wrong command");
                        break;
                }

            } else if (login) {
                String readKeyboard = keyboardScanner.nextLine();
                switch (readKeyboard) {
                    case "help":
                        System.out.println("         search music");
                        System.out.println("           show all");
                        System.out.println("         more details");
                        System.out.println(" ---- Editor Permission ----");
                        System.out.println("           insert");
                        System.out.println("           remove");

                        break;
                    case "search music":
                        message.put("type", "search music");
                        message = rmi.request(message);
                        rmi.printMessage(message, (RMIInterfaceClient) c);
                        //to implement

                        break;
                    case "show all":
                        message.put("type", "show all");
                        message.put("select", rmi.select());
                        message = rmi.request(message);
                        rmi.printMessage(message, (RMIInterfaceClient) c);
                        //done
                        break;
                    case "show details":
                        message.put("type", "more details");
                        message.put("select", rmi.select());
                        message.put("id", rmi.selectId());
                        message = rmi.request(message);
                        rmi.printMessage(message, (RMIInterfaceClient) c);
                        //done
                        break;
                    case "insert":
                        message = rmi.insert(message);
                        message = rmi.request(message);
                        rmi.printMessage(message, (RMIInterfaceClient) c);
                        //done
                        break;
                    case "remove":
                        message.put("type", "remove");
                        message.put("select", rmi.selectId());
                        message.put("identifier", rmi.selectId());
                        message = rmi.request(message);
                        rmi.printMessage(message, (RMIInterfaceClient) c);
                        //done
                        break;
                    case "write review":
                        message.put("type", "write review");
                        message.put("identifier", rmi.selectId());
                        message.put("rate", rmi.rate());
                        message.put("text", rmi.review());
                        message = rmi.request(message);
                        rmi.printMessage(message, (RMIInterfaceClient) c);
                        //done
                        break;
                    case "update":
                        //to implement
                        break;
                    default:
                        System.out.println("Wrong comand");
                        break;
                    //nota write review
                }
            }
        }

    }
}
