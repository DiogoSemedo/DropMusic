import java.io.*;
import java.net.*;
import java.sql.SQLOutput;
import java.util.HashMap;

public class MulticastServer extends Thread {
    private int BUFFER_SIZE = 2048;
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private String MULTICAST_ADDRESS_2 = "224.0.224.1";
    private int PORT = 4321;
    private Database db = new Database();
    public static void main(String[] args) {
        MulticastServer server = new MulticastServer();
        server.start();

    }
    public MulticastServer() {
        super("Server " + (long) (Math.random() * 1000));
    }

    public void run() {
        MulticastSocket socket = null;
        MulticastSocket reply = null;
        long counter = 0;
        System.out.println(this.getName() + " running...");
        try {
            socket = new MulticastSocket(PORT);  // create socket without binding it (only for receving)
            reply = new MulticastSocket();
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            InetAddress groupR = InetAddress.getByName(MULTICAST_ADDRESS_2);
            socket.joinGroup(group);
            ByteArrayInputStream byteIn;
            ObjectInputStream in;
            HashMap<String,String> message;
            HashMap<String,String> replyM = new HashMap<>();

            while (true) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(byteOut);
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);//espera até receber
                if(reply.getLocalPort() != packet.getPort()) {
                    System.out.println("Recebi: " + packet.getAddress().getHostAddress() + ":" + packet.getPort());

                    byteIn = new ByteArrayInputStream(packet.getData());
                    in = new ObjectInputStream(byteIn);
                    message = (HashMap<String,String>) in.readObject();
                    System.out.println("\no que recebi");
                    for (HashMap.Entry<String, String> entry : message.entrySet()) {
                        System.out.println(entry.getKey() + " : " + entry.getValue());
                    }
                    System.out.println("fim do que recebi");
                    replyM = db.process(message);
                    if(message.get("status").equals("upload") || message.get("status").equals("download")){
                        new Upload(replyM);
                    }
                    /*else if(message.get("status").equals("download")){
                        new Download(replyM);
                    }*/
                    System.out.println("\no que vou enviar");
                    for (HashMap.Entry<String, String> entry : replyM.entrySet()) {
                        System.out.println(entry.getKey() + " : " + entry.getValue());
                    }
                    System.out.println("fim do que vou enviar");
                    message.clear();
                    out.writeObject(replyM);
                    byte[] replyBuffer = byteOut.toByteArray();
                    DatagramPacket packetReply = new DatagramPacket(replyBuffer, replyBuffer.length, groupR, PORT);
                    //testar funcao packet.setAddress
                    //socket.setTimeToLive(100);
                    reply.send(packetReply);
                    replyM.clear();
                    byteOut.close();
                    out.close();
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally
        {
            socket.close();
        }
    }
    class Upload extends Thread {
        DataInputStream in;
        DataOutput out;
        byte[] buffer;
        ServerSocket listenSocket;
        HashMap<String,String> message;
        int idmusic;
        int iduser;
        String status;
        public Upload(HashMap<String, String> message) {
            try {
                this.message = message;
                this.idmusic = Integer.parseInt(message.get("idmusic"));
                this.status = message.get("status");
                int serverPort = Integer.parseInt(this.message.get("port"));
                this.listenSocket = new ServerSocket(serverPort);
                this.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run() {
            Socket clientSocket = null;
            try {
                clientSocket = listenSocket.accept();
                in = new DataInputStream(clientSocket.getInputStream());
                out = new DataOutputStream(clientSocket.getOutputStream());
                if(status.equals("upload")) {
                    iduser = Integer.parseInt(in.readUTF());
                    buffer = new byte[Integer.parseInt(in.readUTF())];
                    in.read(buffer);
                    if (db.upload(buffer, idmusic,iduser)) {
                        out.writeUTF("upload com sucesso");
                    } else {
                        out.writeUTF("upload falhado");
                    }
                }
                else {
                   iduser =  Integer.parseInt(in.readUTF());
                   buffer = db.download(idmusic,iduser);
                   if(buffer!=null){
                       out.writeUTF(String.valueOf(buffer.length));
                       out.write(buffer);
                   }else{
                       System.out.println("ERRRRROOOOOOOOOOOOOOOOOO!");
                   }
                }
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try{
                    clientSocket.close();
                    ((DataOutputStream) out).close();
                    in.close();
                }catch (IOException e){
                    e.printStackTrace();
                }

            }
        }
    }
    /*
    class Reply extends Thread {
        MulticastSocket socket = null;
        String message = "teste";
        public Reply() {
            try {
                this.socket = new MulticastSocket();
                this.start();
            } catch (IOException e) {
                System.out.println("Exception");
            }
        }
        public void run(){
            try {
                //criar a mensagem ou receber como atributo da thread
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                socket.joinGroup(group);
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                //testar funcao packet.setAddress
                socket.send(packet);
            }
            catch (IOException e){
                e.printStackTrace();
            }
            finally {
                socket.close();
            }
        }
    }*/
}