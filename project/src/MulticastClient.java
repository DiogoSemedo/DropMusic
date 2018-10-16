import java.io.*;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Scanner;

/**
 * The MulticastClient class joins a multicast group and loops receiving
 * messages from that group. The client also runs a MulticastUser thread that
 * loops reading a string from the keyboard and multicasting it to the group.
 * <p>
 * The example IPv4 address chosen may require you to use a VM option to
 * prefer IPv4 (if your operating system uses IPv6 sockets by default).
 * <p>
 * Usage: java -Djava.net.preferIPv4Stack=true MulticastClient
 *
 * @author Raul Barbosa
 * @version 1.0
 */
public class MulticastClient extends Thread {
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4321;
    private int BUFFER_SIZE = 2048;

    public static void main(String[] args) {
        MulticastClient client = new MulticastClient();
        client.start();
        MulticastUser user = new MulticastUser();
        user.start();
    }

    public void run() {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(PORT);  // create socket and bind it
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);
            ByteArrayInputStream byteIn;
            ObjectInputStream in;
            HashMap<String,String> map = new HashMap<>();

            while (true) {
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                byteIn = new ByteArrayInputStream(packet.getData());
                in = new ObjectInputStream(byteIn);
                try {
                    map = (HashMap<String, String>) in.readObject();
                } catch (ClassNotFoundException e){
                    System.out.println(e.getException());}

                //System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                //System.out.println(map.get("id"));
                //System.out.println(map.values());
                System.out.println("----------message---------------");
                for (HashMap.Entry<String, String> entry : map.entrySet()) {
                    System.out.println(entry.getKey() + " : " + entry.getValue());
                }

                map.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}

class MulticastUser extends Thread {
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int PORT = 4321;

    public MulticastUser() {
        super("User " + (long) (Math.random() * 1000));
    }

    public String selectId(){
        Scanner keyboardScanner = new Scanner(System.in);
        String read = keyboardScanner.nextLine();
        while(true) {
            try {
                System.out.println("Select ID");
                return String.valueOf(Integer.parseInt(read));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public String select(){

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

    public HashMap<String,String> insert(HashMap<String,String> message){
        Scanner keyboardScanner = new Scanner(System.in);
        message.put("type","insert");
        String select = select();
        message.put("select",select);
        //sem defesa de inputs
        switch (select){
            case "artists":
                System.out.println("Title:");
                message.put("title",keyboardScanner.nextLine());
                System.out.println("Description:");
                message.put("description",keyboardScanner.nextLine());
                break;
            case "albums":
                System.out.println("Title:");
                message.put("title",keyboardScanner.nextLine());
                System.out.println("Description:");
                message.put("description",keyboardScanner.nextLine());
                System.out.println("Rate:");
                message.put("rate",String.valueOf(keyboardScanner.nextDouble()));
                break;
            case "musics":
                System.out.println("Title:");
                message.put("title",keyboardScanner.nextLine());
                System.out.println("Compositor:");
                message.put("compositor",keyboardScanner.nextLine());
                System.out.println("Duration:");
                message.put("duration",keyboardScanner.nextLine());
                System.out.println("Genre:");
                message.put("genre",keyboardScanner.nextLine());
                break;
        }
        return message;

    }
    public void run() {
        MulticastSocket socket = null;
        System.out.println(this.getName() + " ready...");
        HashMap<String,String> message = new HashMap<>();
        try {
            socket = new MulticastSocket();  // create socket without binding it (only for sending)
            Scanner keyboardScanner = new Scanner(System.in);
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            while (true) {
                System.out.println("Select option: (help to get help) ");
                String readKeyboard = keyboardScanner.nextLine();
                /*switch (readKeyboard){
                    case "help":
                        System.out.println("         search music");
                        System.out.println("           show all");
                        System.out.println("         more details");
                        System.out.println(" ---- Editor Permission ----");
                        System.out.println("           insert");
                        System.out.println("           remove");

                        break;
                    case "search music":
                        message.put("type","search music");


                        break;
                    case "show all":
                        message.put("type","show all");
                        message.put("select",select());
                        //done
                        break;
                    case "show details":
                        message.put("type","more details");
                        message.put("select",select());
                        message.put("id",selectId());
                        //done
                        break;
                    case "insert":
                        message = insert(message);
                        //done

                        break;
                    case "remove":
                        message.put("type","remove");
                        message.put("select", selectId());
                        message.put("identifier",selectId());
                        //done
                        break;
                    default:
                        System.out.println("Wrong comand");
                        break;
                        //nota write review
                }*/
                message.put("type","show details");
                message.put("select","albums");
                message.put("identifier","1");
                out.writeObject(message);
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                byte[] buffer = byteOut.toByteArray();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length  , group, PORT);
                socket.send(packet);
                message.clear();
                out.close();
                byteOut.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            socket.close();
        }
    }
}

