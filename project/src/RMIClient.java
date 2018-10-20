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
        String ClientID = null;
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
                            ClientID = message.get("identifier");
                            login = true;
                            rmi.addRef(ClientID,(RMIInterfaceClient) c);
                        }
                        break;
                    default:
                        System.out.println("Wrong command");
                        break;

                }
                keyboardScanner.nextLine();//limpa a buffershow

            } else if (login) {

                switch (keyboardScanner.nextLine()) {
                    case "help":
                        System.out.println("         search music");
                        System.out.println("           show all");
                        System.out.println("         show details");
                        System.out.println("          write review");
                        System.out.println(" ---- Editor Permission ----");
                        System.out.println("           insert");
                        System.out.println("           remove");
                        System.out.println("            edit");

                        break;
                    case "search music":
                        message.put("type", "search music");
                        message.put("select",rmi.select((RMIInterfaceClient) c));
                        System.out.println("Search input:");
                        message.put("text", keyboardScanner.nextLine());
                        message = rmi.request(message);
                        rmi.printMessage(message, (RMIInterfaceClient) c);
                        //done

                        break;
                    case "show all":
                        message.put("type", "show all");
                        message.put("select", rmi.select( (RMIInterfaceClient) c));
                        message = rmi.request(message);
                        rmi.printMessage(message, (RMIInterfaceClient) c);
                        //done
                        break;
                    case "show details":
                        message.put("type", "show details");
                        message.put("select", rmi.select( (RMIInterfaceClient) c));
                        message.put("identifier", rmi.selectId( (RMIInterfaceClient) c));
                        message = rmi.request(message);
                        rmi.printMessage(message, (RMIInterfaceClient) c);
                        //done
                        break;
                    case "insert":
                        message.put("identifier",ClientID);
                        message = rmi.insert(message, (RMIInterfaceClient) c);
                        message = rmi.request(message);
                        rmi.printMessage(message, (RMIInterfaceClient) c);
                        //done
                        break;
                    case "remove":
                        message.put("identifier",ClientID);
                        message.put("type", "remove");
                        message.put("select", rmi.select( (RMIInterfaceClient) c));
                        message.put("id", rmi.selectId( (RMIInterfaceClient) c));
                        message = rmi.request(message);
                        rmi.printMessage(message, (RMIInterfaceClient) c);
                        //done
                        break;
                    case "write review":
                        message.put("type", "write review");
                        message.put("identifier", rmi.selectId((RMIInterfaceClient) c));
                        message.put("rate", rmi.rate( (RMIInterfaceClient) c));
                        message.put("text", rmi.review( (RMIInterfaceClient) c));
                        message = rmi.request(message);
                        rmi.printMessage(message, (RMIInterfaceClient) c);
                        //done
                        break;
                    case "edit":
                        //done
                        message.put("identifier",ClientID);
                        message.put("type","edit");
                        message.put("select",rmi.select((RMIInterfaceClient) c));
                        message.put("key",rmi.selectKey( (RMIInterfaceClient) c, message.get("select")));
                        message.put("value",rmi.selectValue((RMIInterfaceClient) c));
                        message.put("id",rmi.selectId((RMIInterfaceClient) c));
                        message = rmi.request(message);
                        rmi.printMessage(message,(RMIInterfaceClient) c);
                        break;
                    case "promote":
                        message.put("type","promote");
                        message.put("identifier",ClientID);
                        System.out.println("Username to promote:");
                        message.put("username",keyboardScanner.nextLine());
                        message = rmi.promote(message);
                        rmi.printMessage(message,(RMIInterfaceClient) c);
                    default:
                        System.out.println("Wrong comand");
                        break;
                    //nota write review
                }
            }
        }
    }
}
