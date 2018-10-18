import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Scanner;

public class RMIServer extends UnicastRemoteObject implements RMIInterface{
    private int BUFFER_SIZE = 2048;
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4321;

    public RMIServer() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws RemoteException{
        RMIInterface rmi = new RMIServer();
        LocateRegistry.createRegistry(1099).rebind("dropmusic",rmi);
        System.out.println("Server ready...");
    }

    //methods


    public HashMap<String,String> request(HashMap<String,String> message){

        try{
            MulticastSocket sender = new MulticastSocket();  // create socket without binding it (only for receving)
            MulticastSocket receiver = new MulticastSocket(PORT);
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);

            //converter message in bytes
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(message);
            byte[] bufferS = byteOut.toByteArray();
            //enviar
            DatagramPacket packetS = new DatagramPacket(bufferS, bufferS.length, group, PORT);
            sender.send(packetS);
            //receber
            byte[] bufferR = new byte[BUFFER_SIZE];
            DatagramPacket packetR = new DatagramPacket(bufferR, bufferS.length);
            receiver.joinGroup(group);
            receiver.receive(packetR);
            //converter bytes para HashMap
            ByteArrayInputStream byteIn = new ByteArrayInputStream(packetR.getData());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            message.clear();
            message = (HashMap<String,String>) in.readObject();
            return message;

        }
        catch (IOException e) {e.printStackTrace();}
        catch (ClassNotFoundException e){ e.printStackTrace();}

    return null;
    }

}
