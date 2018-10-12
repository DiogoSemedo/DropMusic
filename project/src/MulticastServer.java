import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;

public class MulticastServer extends Thread {
    private String MULTICAST_ADDRESS = "224.0.224.0";
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
        MulticastSocket listen = null;
        MulticastSocket reply = null;
        long counter = 0;
        String messageReply = "teste";
        System.out.println(this.getName() + " running...");
        try {
            listen = new MulticastSocket(PORT);  // create socket without binding it (only for receving)
            reply = new MulticastSocket();
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            listen.joinGroup(group);
            reply.joinGroup(group);
            while (true) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                listen.receive(packet);//espera até receber
                if(reply.getLocalPort() != packet.getPort()) {
                    System.out.println("Recebi: " + packet.getAddress().getHostAddress() + ":" + packet.getPort());
                    String message = new String(packet.getData(), 0, packet.getLength());
                    System.out.println(message);
                    String m = db.process("type|regist;username|Maria;password|123");
                    System.out.println(m);
                    //suposto tratamento de request e envio da reply
                    //criar thread para enviar reply ou trata-se logo aqui com uma socket send?
                    //esta solução é mais simples que thread
                    byte[] replyBuffer = m.getBytes();
                    DatagramPacket packet2 = new DatagramPacket(replyBuffer, replyBuffer.length, group, PORT);
                    //testar funcao packet.setAddress
                    reply.send(packet2);
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            listen.close();
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

