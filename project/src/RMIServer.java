import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class RMIServer extends UnicastRemoteObject implements RMIInterfaceServer {
    private int BUFFER_SIZE = 2048;
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4321;
    private String MULTICAST_ADDRESS_2 = "224.0.224.1";
    private HashMap<String, RMIInterfaceClient> references = new HashMap<String, RMIInterfaceClient>();
    public static RMIInterfaceServer connection;
    public RMIServer() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws RemoteException, MalformedURLException, InterruptedException {
        RMIInterfaceServer rmi = new RMIServer();
        connection = null;
        try {//Só o primário entra aqui

            LocateRegistry.createRegistry(1099);
        }catch (ExportException e){
            System.out.println("Falhei a criar registro para ser principal");
        }
        //os sleeps aqui não afetam a comunicão entre rmiServer e rmiClient
        while(true) {
            try {
                //tentar ser principal----------------------------------------------
                Naming.bind("//0.0.0.0:1099/dropmusic", rmi);
                System.out.println("Server ready...");
                //testar o secundário
                while(true) {
                    Thread.sleep(5000);
                    try {
                        connection = (RMIInterfaceServer) Naming.lookup("//0.0.0.0:2000/servers");
                    } catch (NotBoundException e) {

                    }catch (ConnectException e){
                        System.out.println("Testar Secundário: fail (isn't running)");
                    }
                    if(connection!= null){
                        try{
                            if (connection.callPrimaryToSecondary()){
                                System.out.println("Secundário ON");

                            }
                        }catch (Exception i){//só para ignorar
                            }
                    }
                }
            } catch (AlreadyBoundException e) {
                //fica secundário--------------------------------------------------
                System.out.println("sou secundário...");
                try {//cria registro para o principal poder testar o secundário
                    LocateRegistry.createRegistry(2000);
                    Naming.rebind("//0.0.0.0:2000/servers",rmi);
                } catch (Exception i){}
                Thread.sleep(30000); //Secundario testa substituir o primario se o primario estiver em baixo
            }catch (ConnectException e){
                //o principal vai abaixo e o sencundário assume ser principal apartir daqui
                LocateRegistry.createRegistry(1099);
                Naming.rebind("//0.0.0.0:2000/servers",rmi);
                Naming.rebind("//0.0.0.0:1099/dropmusic", rmi);
                System.out.println("O servidor principal foi abaixo, assumi o trabalho como principal");
                //tratamento do secundario como principal para o secundario
                while(true) {
                    Thread.sleep(5000);
                    try {
                        connection = (RMIInterfaceServer) Naming.lookup("//0.0.0.0:2000/servers");
                    } catch (NotBoundException i) {

                    }catch (ConnectException i){
                        System.out.println("Testar Secundário: fail (isn't running)");
                    }
                    if(connection!= null){
                        try{
                            if (connection.callPrimaryToSecondary()){
                                System.out.println("Secundário ON");
                            }
                        }catch (Exception i){//só para ignorar
                        }
                    }
                }
            }
        }

    }

    //O server chama sobre o secundário
    public boolean callPrimaryToSecondary(){
        return true;
    }

    public void updateReferences(HashMap<String,RMIInterfaceClient> refs){
        references = refs;
    }


    public HashMap<String, String> request(HashMap<String, String> message) {
        MulticastSocket receiver = null;
        MulticastSocket sender = null;
        HashMap<String, String> map = new HashMap<>();
        try {
            sender = new MulticastSocket();  // create socket without binding it (only for receving)
            receiver = new MulticastSocket(PORT);
            InetAddress groupS = InetAddress.getByName(MULTICAST_ADDRESS);
            InetAddress groupR = InetAddress.getByName(MULTICAST_ADDRESS_2);
            //converter message in bytes
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(message);
            byte[] bufferS = byteOut.toByteArray();
            //envia pacote
            DatagramPacket packetS = new DatagramPacket(bufferS, bufferS.length, groupS, PORT);
            sender.send(packetS);
            receiver.joinGroup(groupR);

            out.close();
            byteOut.close();
            message.clear();
            //Receber
            ByteArrayInputStream byteIn;
            ObjectInputStream in;
            byte[] bufferR = new byte[BUFFER_SIZE];
            DatagramPacket packetR = new DatagramPacket(bufferR, bufferR.length);

            receiver.receive(packetR);
            byteIn = new ByteArrayInputStream(packetR.getData());
            in = new ObjectInputStream(byteIn);
            try {
                map = (HashMap<String, String>) in.readObject();
            } catch (ClassNotFoundException e) {
                System.out.println(e.getException());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            receiver.close();
            sender.close();
            return map;
        }
    }


    public HashMap<String, String> regist(RMIInterfaceClient client) throws RemoteException {
        //exemplo --> type|regist;username|name;password|pass
        HashMap<String, String> message = new HashMap<>();
        message.put("type", "regist");
        client.print_on_client("Username:");
        message.put("username", client.getInput());
        client.print_on_client("Password:");
        message.put("password", client.getInput());
        //enviar pela socket
        message = request(message);
        //receber resposta
        return message;
    }

    public HashMap<String, String> login(RMIInterfaceClient client) throws RemoteException {
        HashMap<String, String> message = new HashMap<>();
        message.put("type", "login");
        client.print_on_client("Username:");
        message.put("username", client.getInput());
        client.print_on_client("Password:");
        message.put("password", client.getInput());
        //enviar pela socket
        message = request(message);
        //receber resposta
        return message;
    }

    public HashMap<String, String> promote(HashMap<String, String> message) throws RemoteException {
        message = request(message);
        if (message.get("msg").equals("successful")) {
            RMIInterfaceClient c = (RMIInterfaceClient) references.get(message.get("identifier"));
            if (c == null) {
                HashMap<String, String> reply = new HashMap<String, String>();
                reply.put("identifier", message.get("identifier"));
                reply.put("msg", "Promoted to editor.");
                reply.put("type", "insert notification");
                request(reply);
            } else {
                c.print_on_client("Promoted to editor.");
            }
        }
        return message;
    }

    public String review(RMIInterfaceClient client) throws RemoteException {
        client.print_on_client("Write your review:");
        String read;
        while ((read = client.getInput()).length() > 300) {
            client.print_on_client("Less than 300 characters pls!!! Write your review:");
        }
        return read;
    }

    public String selectId(RMIInterfaceClient client, String value) throws RemoteException {
        client.print_on_client("Select ID for " + value);
        return client.getInput();
    }

    public String select(RMIInterfaceClient client) throws RemoteException {

        while (true) {
            client.print_on_client("Choose: 1-artist 2-album 3-music");

            switch (client.getInput()) {
                case "1":
                    return "artist";
                case "2":
                    return "album";
                case "3":
                    return "music";
                default:
                    client.print_on_client("Try again");
                    break;
            }
        }
    }

    public String selectMusic(RMIInterfaceClient client) throws RemoteException {

        while (true) {
            client.print_on_client("Choose: 1-artist 2-album 3-genre");

            switch (client.getInput()) {
                case "1":
                    return "artist";
                case "2":
                    return "album";
                case "3":
                    return "genre";
                default:
                    client.print_on_client("Try again");
                    break;
            }
        }
    }

    public HashMap<String, String> insert(HashMap<String, String> message, RMIInterfaceClient client) throws RemoteException {
        message.put("type", "insert");
        String select = select(client);
        message.put("select", select);
        //sem defesa de inputs
        switch (select) {
            case "artist":
                client.print_on_client("Name:");
                message.put("name", client.getInput());
                client.print_on_client("Description:");
                message.put("description", client.getInput());
                break;
            case "album":
                client.print_on_client("Title:");
                message.put("title", client.getInput());
                client.print_on_client("Description:");
                message.put("description", client.getInput());
                //client.print_on_client("Rate:");
                //message.put("rate", client.getInput());
                break;
            case "music":
                client.print_on_client("Title:");
                message.put("title", client.getInput());
                client.print_on_client("Compositor:");
                message.put("compositor", client.getInput());
                client.print_on_client("Duration:");
                message.put("duration", client.getInput());
                client.print_on_client("Genre:");
                message.put("genre", client.getInput());
                client.print_on_client("Artist Id:");
                message.put("idartist", client.getInput());
                client.print_on_client("Album Id:");
                message.put("idalbum", client.getInput());
                break;
        }
        return message;

    }

    public String rate(RMIInterfaceClient client) throws RemoteException {

        client.print_on_client("Rate between 0 - 10:");
        return client.getInput();

    }

    public void printMessage(HashMap<String, String> message, RMIInterfaceClient client) throws RemoteException {
        for (HashMap.Entry<String, String> entry : message.entrySet()) {
            client.print_on_client(entry.getKey() + " : " + entry.getValue());
        }
    }

    public String selectKey(RMIInterfaceClient client, String select) throws RemoteException {
        switch (select) {

            case "artist":
                client.print_on_client("Choose what you want do edit:\n1 - Name\n2 - Description");
                switch (client.getInput()) {
                    case "1":
                        return "title";
                    case "2":
                        return "description";
                }
                break;
            case "album":
                client.print_on_client("Choose what you want to edit:\n1 - Title\n2 - Description");
                switch (client.getInput()) {
                    case "1":
                        return "title";
                    case "2":
                        return "description";
                }
                break;
            case "music":
                client.print_on_client("Choose what you want to edit:\n1 - Title\n2 - Compositor\n3 - Duration\n4 - Genre\n5 - Id Album\n6 - Id Artist");
                switch (client.getInput()) {
                    case "1":
                        return "title";
                    case "2":
                        return "compositor";
                    case "3":
                        return "duration";
                    case "4":
                        return "genre";
                    case "5":
                        return "idalbum";
                    case "6":
                        return "idartist";
                }
                break;

        }
        return "Wrong Input";
    }

    public String selectValue(RMIInterfaceClient client) throws RemoteException {
        client.print_on_client("Input the new value:");
        return client.getInput();
    }
    //put references
    public void addRef(String ClientID, RMIInterfaceClient client) throws RemoteException {
        references.put(ClientID, (RMIInterfaceClient) client);
        try{
            connection.updateReferences(references);
        }catch (Exception e){
            //só ignorar
        }
        HashMap<String, String> message = new HashMap<>();
        message.put("type", "remove notification");
        message.put("identifier", ClientID);
        message = request(message);
        client.print_on_client(message.get("msg"));
    }

    public void sendNotification(String select, String id) throws RemoteException {
        //method para receber os id's aos quais tem de mandar notificaçoes
        HashMap<String, String> map = new HashMap<>();
        map.put("type", "get history");
        map.put("select", select);
        map.put("idalbum", id);
        map = request(map);
        RMIInterfaceClient c;
        for (HashMap.Entry<String, String> entry : map.entrySet()) {
            if ((c = references.get(entry.getKey())) != null) {
                c.print_on_client("Description from " + select + " with " + id + " has been edited!");
            } else {
                //insert notification
                HashMap<String, String> reply = new HashMap<String, String>();
                reply.put("identifier", entry.getKey());
                reply.put("msg", "Description from " + select + " with " + id + " has been edited!");
                reply.put("type", "insert notification");
                request(reply);
            }
        }
    }
    //
    public HashMap<String, String> logOut(HashMap<String, String> message) throws RemoteException {
        references.remove(message.get("identifier"));
        try{
            connection.updateReferences(references);
        }catch (Exception e){
            //só ignorar
        }
        return request(message);
    }
}
