import java.io.*;
import java.net.*;
import java.util.HashMap;

public class MulticastServer extends Thread {
    private int BUFFER_SIZE = 2048;
    private String MULTICAST_ADDRESS = "224.0.224.0"; //para receber requests
    private String MULTICAST_ADDRESS_2 = "224.0.224.1"; //resposta a um request da funcao request
    private String MULTICAST_ADDRESS_3 = "224.0.224.2"; //resposta à thread check multicast
    private int PORT = 4321;
    private Database db;
    private HashMap<String, HashMap<String, String>> allrequest;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Input invalid. Format:java MulticastServer <num> <dbpassword>");
            System.exit(-1);
        }
        MulticastServer server = new MulticastServer(args[0],args[1]);
        server.start();
    }

    public MulticastServer(String num,String pw) {
        super("Server " + (long) (Math.random() * 1000));
        db = new Database(num,pw);
        allrequest = new HashMap<>();
    }

    public void run() {
        MulticastSocket socket = null;
        MulticastSocket reply = null;
        System.out.println(this.getName() + " running...");
        try {
            socket = new MulticastSocket(PORT);
            reply = new MulticastSocket();
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            InetAddress groupR = InetAddress.getByName(MULTICAST_ADDRESS_2);
            InetAddress groupS = InetAddress.getByName(MULTICAST_ADDRESS_3);
            socket.joinGroup(group);
            ByteArrayInputStream byteIn;
            ObjectInputStream in;
            HashMap<String, String> message;
            HashMap<String, String> replyM = new HashMap<>();

            while (true) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(byteOut);
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);//espera até receber

                byteIn = new ByteArrayInputStream(packet.getData());
                in = new ObjectInputStream(byteIn);
                message = (HashMap<String, String>) in.readObject();

                DatagramPacket packetReply = null;
                if (message.get("default").equals("false")) {  //verificar se é um pacote de verificao de multicast ou um request
                    if (!allrequest.containsKey(message.get("requestID"))) { //verifica o pedido já foi feito alguma vez
                        replyM = db.process(message);
                        allrequest.put(message.get("requestID"), replyM);
                        if (message.get("MulticastPort").equals(String.valueOf(reply.getLocalPort()))) { //verifica se vai responder ou só processar
                            if (message.get("type").equals("get port") && (message.get("status").equals("upload") || message.get("status").equals("download"))) {
                                new TCP(replyM); //cria thread para ligação tcp
                            }
                            out.writeObject(replyM);
                            byte[] replyBuffer = byteOut.toByteArray();
                            packetReply = new DatagramPacket(replyBuffer, replyBuffer.length, groupR, PORT);
                            reply.send(packetReply);
                        }
                    } else { //se o pedido já tiver sido feito não é processado de novo
                        replyM = allrequest.get(message.get("requestID"));
                        out.writeObject(replyM);
                        byte[] replyBuffer = byteOut.toByteArray();
                        packetReply = new DatagramPacket(replyBuffer, replyBuffer.length, groupR, PORT);
                        reply.send(packetReply);
                    }
                } else if (message.get("default").equals("true")) { //pacote para saber o server ainda está de pé
                    String pacote = "nada";
                    byte[] replyBuffer = pacote.getBytes();
                    packetReply = new DatagramPacket(replyBuffer, replyBuffer.length, groupS, PORT);
                    reply.send(packetReply);
                }
                message.clear();
                replyM.clear();
                byteOut.close();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    class TCP extends Thread {
        DataInputStream in;
        DataOutput out;
        byte[] buffer;
        ServerSocket listenSocket;
        HashMap<String, String> message;
        int idmusic;
        int iduser;
        String status;

        public TCP(HashMap<String, String> message) {
            try {
                this.message = message;
                this.idmusic = Integer.parseInt(message.get("idmusic"));
                this.status = message.get("status");
                int serverPort = Integer.parseInt(this.message.get("port"));
                this.listenSocket = new ServerSocket(serverPort);
                this.start();
            } catch (BindException e) {
                System.out.println("listenScoket respeitar o parceiro");
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
                if (status.equals("upload")) {
                    iduser = Integer.parseInt(in.readUTF());
                    buffer = new byte[Integer.parseInt(in.readUTF())];
                    int offset = 0;
                    int count;
                    while ((count = in.read(buffer, offset, buffer.length - offset)) != 0) {
                        offset += count;
                    }
                    System.out.println(offset);
                    if (db.upload(buffer, idmusic, iduser)) {
                        out.writeUTF("upload com sucesso");
                    } else {
                        out.writeUTF("upload falhado");
                    }
                } else {
                    iduser = Integer.parseInt(in.readUTF());
                    buffer = db.download(idmusic, iduser);
                    if (buffer != null) {
                        out.writeUTF(String.valueOf(buffer.length));
                        out.write(buffer);
                    } else {
                        out.writeUTF("ERRO");
                        System.out.println("ERRRRROOOOOOOOOOOOOOOOOO!");
                    }
                }
                return;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    this.listenSocket.close();
                    clientSocket.close();
                    ((DataOutputStream) out).close();
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}