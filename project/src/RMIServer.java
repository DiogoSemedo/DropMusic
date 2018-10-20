import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class RMIServer extends UnicastRemoteObject implements RMIInterfaceServer {
    private int BUFFER_SIZE = 2048;
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4321;
    private String MULTICAST_ADDRESS_2 = "224.0.224.1";
    private HashMap<String,RMIInterfaceClient> references = new HashMap<String,RMIInterfaceClient>();
    public RMIServer() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws RemoteException, MalformedURLException, InterruptedException {
        RMIInterfaceServer rmi = new RMIServer();
        /*while(true) {
            try {
                Naming.bind("dropmusic", rmi);

                System.out.println("Server ready...");
                return;
            } catch (AlreadyBoundException e) {
                System.out.println("sou secundário");
                Thread.sleep(1000);
            }
        }*/
        LocateRegistry.createRegistry(1099).rebind("dropmusic",rmi);
        System.out.println("Server ready...");
    }

    //methods

/*
    public HashMap<String, String> request(HashMap<String, String> message) {

        try {
            MulticastSocket sender = new MulticastSocket();  // create socket without binding it (only for receving)
            MulticastSocket receiver = new MulticastSocket(PORT);
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            receiver.joinGroup(group);

            //converter message in bytes
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            ByteArrayInputStream byteIn;
            ObjectInputStream in;
            out.writeObject(message);
            HashMap<String,String> replyM = new HashMap<>();
            byte[] bufferS = byteOut.toByteArray();
            //enviar
            DatagramPacket packetS = new DatagramPacket(bufferS, bufferS.length, group, PORT);
            sender.send(packetS);

            //receber
            byte[] bufferR = new byte[BUFFER_SIZE];
            DatagramPacket packetR = new DatagramPacket(bufferR, bufferS.length);
            do {

                receiver.receive(packetR);
                if(sender.getLocalPort() != packetR.getPort()) {
                    //converter bytes para HashMap
                    byteIn = new ByteArrayInputStream(packetR.getData());
                    in = new ObjectInputStream(byteIn);

                    try {
                        System.out.println(packetR.getPort() + " " + packetR.getAddress());
                        replyM = (HashMap<String, String>) in.readObject();
                    } catch (ClassNotFoundException e) {
                    e.printStackTrace();}
                    byteIn.close();
                    in.close();
                }
            } while (replyM.size()==0);
            return replyM;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }*/
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

    public HashMap<String,String> promote(HashMap<String,String> message) throws RemoteException{
        message = request(message);
        if(message.get("msg").equals("successful")) {
            RMIInterfaceClient c = (RMIInterfaceClient) references.get(message.get("identifier"));
            if(c==null){
                HashMap<String,String> reply = new HashMap<String,String>();
                reply.put("identifier",message.get("identifier"));
                reply.put("msg","Promoted to editor.");
                reply.put("type","insert notification");
                request(message);
            }
            else {
                c.print_on_client("Promoted to editor.");
            }
        }
        return message;
    }

    public String review(RMIInterfaceClient client) throws RemoteException{
        client.print_on_client("Write your review:");
        String read;
        while ((read = client.getInput()).length() > 300) {
            client.print_on_client("Less than 300 characters pls!!! Write your review:");
        }
        return read;
    }

    public String selectId(RMIInterfaceClient client) throws RemoteException{
        client.print_on_client("Select ID");
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

    public HashMap<String, String> insert(HashMap<String, String> message, RMIInterfaceClient client) throws RemoteException{
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
                client.print_on_client("Rate:");
                message.put("rate", client.getInput());
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
                message.put("idartist",client.getInput());
                client.print_on_client("Album Id:");
                message.put("idalbum",client.getInput());
                break;
        }
        return message;

    }

    public String rate(RMIInterfaceClient client) throws  RemoteException{

        client.print_on_client("Rate between 0 - 10:");
        return client.getInput();

    }

    public void printMessage(HashMap<String, String> message, RMIInterfaceClient client) throws RemoteException {
        for (HashMap.Entry<String, String> entry : message.entrySet()) {
            client.print_on_client(entry.getKey() + " : " + entry.getValue());
        }
    }

    public String selectKey(RMIInterfaceClient client, String select) throws RemoteException{
        switch (select){

            case"artist":
                client.print_on_client("Choose what you want do edit:\n1 - Name\n2 - Description");
                switch (client.getInput()){
                    case"1":
                        return "title";
                    case "2":
                        return "description";
                }
                break;
            case"album":
                client.print_on_client("Choose what you want to edit:\n1 - Title\n2 - Description");
                switch (client.getInput()){
                    case"1":
                        return "title";
                    case "2":
                        return "description";
                }
                break;
            case"music":
                client.print_on_client("Choose what you want to edit:\n1 - Title\n2 - Compositor\n3 - Duration\n4 - Genre\n5 - Id Album\n6 - Id Artist");
                switch (client.getInput()){
                    case"1":
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

    public String selectValue(RMIInterfaceClient client) throws RemoteException{
        client.print_on_client("Input the new value:");
        return client.getInput();
    }

    public void addRef(String ClientID,RMIInterfaceClient client) throws RemoteException{
        references.put(ClientID, (RMIInterfaceClient) client);
    }

}
