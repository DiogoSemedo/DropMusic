import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Scanner;

public class RMIServer extends UnicastRemoteObject implements RMIInterfaceServer {
    private int BUFFER_SIZE = 2048;
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4321;
    private String MULTICAST_ADDRESS_2 = "224.0.224.1";

    public RMIServer() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws RemoteException {
        RMIInterfaceServer rmi = new RMIServer();
        LocateRegistry.createRegistry(1099).rebind("dropmusic", rmi);
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

    public String review() {
        Scanner keyboardScanner = new Scanner(System.in);
        System.out.println("Write your review:");
        String read;
        while ((read = keyboardScanner.nextLine()).length() > 300) {
            System.out.println("Less than 300 characters pls!!! Write your review:");
        }
        return read;
    }

    public String selectId() {
        Scanner keyboardScanner = new Scanner(System.in);
        String read = keyboardScanner.nextLine();
        while (true) {
            try {
                System.out.println("Select ID");
                return String.valueOf(Integer.parseInt(read));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public String select() {

        Scanner keyboardScanner = new Scanner(System.in);
        while (true) {
            System.out.println("Choose: 1-artists 2-albums 3-musics");
            String read = keyboardScanner.nextLine();
            switch (read) {
                case "1":
                    return "artists";
                case "2":
                    return "albums";
                case "3":
                    return "musics";
                default:
                    System.out.println("Try again");
                    break;
            }
        }
    }

    public HashMap<String, String> insert(HashMap<String, String> message) {
        Scanner keyboardScanner = new Scanner(System.in);
        message.put("type", "insert");
        String select = select();
        message.put("select", select);
        //sem defesa de inputs
        switch (select) {
            case "artists":
                System.out.println("Title:");
                message.put("title", keyboardScanner.nextLine());
                System.out.println("Description:");
                message.put("description", keyboardScanner.nextLine());
                break;
            case "albums":
                System.out.println("Title:");
                message.put("title", keyboardScanner.nextLine());
                System.out.println("Description:");
                message.put("description", keyboardScanner.nextLine());
                System.out.println("Rate:");
                message.put("rate", String.valueOf(keyboardScanner.nextDouble()));
                break;
            case "musics":
                System.out.println("Title:");
                message.put("title", keyboardScanner.nextLine());
                System.out.println("Compositor:");
                message.put("compositor", keyboardScanner.nextLine());
                System.out.println("Duration:");
                message.put("duration", keyboardScanner.nextLine());
                System.out.println("Genre:");
                message.put("genre", keyboardScanner.nextLine());
                break;
        }
        return message;

    }

    public String rate() {
        Scanner keyboardScanner = new Scanner(System.in);
        System.out.println("Rate between 0 - 10:");
        return String.valueOf(keyboardScanner.nextDouble());

    }

    public void printMessage(HashMap<String, String> message, RMIInterfaceClient client) throws RemoteException {
        for (HashMap.Entry<String, String> entry : message.entrySet()) {
            client.print_on_client(entry.getKey() + " : " + entry.getValue());
        }
    }


}
