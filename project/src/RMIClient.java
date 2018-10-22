import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
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

    public static void Download(String port, String id){

    }

    public static void TCPConnectionUp(String port,String id) {
        Socket s = null;
        DataOutputStream out = null;
        DataInputStream in = null;
        try {
            Scanner read = new Scanner(System.in);
            s = new Socket("localhost", 6000);
            in = new DataInputStream(s.getInputStream());
            out = new DataOutputStream(s.getOutputStream());

            //System.out.println("Insert File path:");
            byte[] array = Files.readAllBytes(new File("C:\\Users\\User\\Desktop\\series.txt").toPath());
            out.writeUTF(id);
            out.writeUTF(String.valueOf(array.length));
            out.write(array);
            System.out.println(in.readUTF() );
            /*
            //System.out.println(in.readUTF());
            byte[] ar= new byte[Integer.parseInt(in.readUTF())];
            in.read(ar);
            Files.write(new File("C:\\Users\\User\\Desktop\\euzinho1.jpeg").toPath(), ar);
            */
        } catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        } finally {
            if (s != null)
                try {
                    in.close();
                    out.close();
                    s.close();

                } catch (IOException e) {
                    System.out.println("close:" + e.getMessage());
                }

        }
    }
    public static void TCPConnectionDown(String port,String id) {
        Socket s = null;
        DataOutputStream out = null;
        DataInputStream in = null;
        try {
            s = new Socket("localhost", Integer.parseInt(port));
            in = new DataInputStream(s.getInputStream());
            out = new DataOutputStream(s.getOutputStream());
            out.writeUTF(id);
            byte[] array = new byte[Integer.parseInt(in.readUTF())];
            in.read(array);
            Files.write(new File("C:\\Users\\User\\Desktop\\teste.txt").toPath(), array);

        } catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        } finally {
            if (s != null)
                try {
                    in.close();
                    out.close();
                    s.close();

                } catch (IOException e) {
                    System.out.println("close:" + e.getMessage());
                }

        }
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
                switch (keyboardScanner.nextLine()) {
                    case "1":
                        message = rmi.regist((RMIInterfaceClient) c);
                        rmi.printMessage(message, (RMIInterfaceClient) c);
                        break;
                    case "2":
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

            } else if (login) {

                switch (keyboardScanner.nextLine()) {
                    case "help":
                        System.out.println("         search music");
                        System.out.println("           show all");
                        System.out.println("         show details");
                        System.out.println("          write review");
                        System.out.println("            log out");
                        System.out.println(" ---- Editor Permission ----");
                        System.out.println("           insert");
                        System.out.println("           remove");
                        System.out.println("            edit");
                        System.out.println("            promote");

                        break;
                    case "search music":
                        message.put("type", "search music");
                        message.put("select",rmi.selectMusic((RMIInterfaceClient) c));
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
                        message.put("identifier", rmi.selectId( (RMIInterfaceClient) c, message.get("select")));
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
                        message.put("id", rmi.selectId( (RMIInterfaceClient) c,message.get("select")));
                        message = rmi.request(message);
                        rmi.printMessage(message, (RMIInterfaceClient) c);
                        //done
                        break;
                    case "write review":
                        message.put("type", "write review");
                        message.put("identifier", rmi.selectId((RMIInterfaceClient) c,message.get("select")));
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
                        message.put("id",rmi.selectId((RMIInterfaceClient) c,message.get("select")));
                        HashMap<String,String> r = message;
                        message = rmi.request(message);
                        if(r.get("select").equals("album") && r.get("key").equals("description") && message.get("msg").equals("sucessful")){
                            //quando é preciso enviar notificações
                            rmi.sendNotification(r.get("select"),r.get("id"));
                        }
                        rmi.printMessage(message,(RMIInterfaceClient) c);
                        break;
                    case "promote":
                        message.put("type","promote");
                        message.put("identifier",ClientID);
                        System.out.println("Username to promote:");
                        message.put("username",keyboardScanner.nextLine());
                        message = rmi.promote(message);
                        rmi.printMessage(message,(RMIInterfaceClient) c);
                        break;
                    case "upload":
                        message.put("type","get port");
                        System.out.println("Select ID of the music you want to upload");
                        message.put("status","upload");
                        message.put("idmusic",keyboardScanner.nextLine());
                        message = rmi.request(message);
                        TCPConnectionUp(message.get("port"), ClientID);

                        break;
                    case "downlaod":
                        message.put("type","get port");
                        message.put("status","download");
                        message.put("idmusic",keyboardScanner.nextLine());
                        message = rmi.request(message);
                        TCPConnectionDown(message.get("port"), ClientID);
                        break;

                    case"share":
                        message.put("type", "share");
                        message.put("identifier", ClientID);
                        System.out.println("Select id of user you want to share with");
                        message.put("iduser", keyboardScanner.nextLine());
                        System.out.println("Select id from which music you want to share");
                        message.put("idmusic", keyboardScanner.nextLine());
                        message = rmi.request(message);
                        rmi.printMessage(message, (RMIInterfaceClient) c);
                        break;
                    case "log out":
                        message.put("type","log out");
                        message.put("identifier",ClientID);
                        message = rmi.logOut(message);
                        if(message.get("msg").equals("successful")){
                            System.out.println("Log Out Done!");
                            login = false;
                            ClientID = null;
                        }
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
