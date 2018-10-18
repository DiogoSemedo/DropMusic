import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Scanner;

public class RMIServer extends UnicastRemoteObject implements RMIInterface{

    public RMIServer() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws RemoteException{
        RMIInterface rmi = new RMIServer();
        LocateRegistry.createRegistry(1099).rebind("dropmusic",rmi);
        System.out.println("Server ready...");
    }

    //methods
    public HashMap<String,String> regist(){
        Scanner read = new Scanner(System.in);
        //exemplo --> type|regist;username|name;password|pass
        HashMap<String,String> message = new HashMap<>();
        message.put("type","regist");
        System.out.println("Username:");
        message.put("username",read.nextLine());
        System.out.println("Password:");
        message.put("password",read.nextLine());
        //enviar pela socket
        //receber resposta
        return message;
    }

    public HashMap<String,String> login(){
        Scanner read = new Scanner(System.in);
        HashMap<String,String> message = new HashMap<>();
        message.put("type","login");
        System.out.println("Username:");
        message.put("username",read.nextLine());
        System.out.println("Password:");
        message.put("password",read.nextLine());
        //enviar pela socket
        //receber resposta
        return message;
    }
    public String review(){
        Scanner keyboardScanner = new Scanner(System.in);
        System.out.println("Write your review:");
        String read;
        while((read=keyboardScanner.nextLine()).length() > 300){
            System.out.println("Less than 300 characters pls!!! Write your review:");
        }
        return read;
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

    public String rate(){
        Scanner keyboardScanner = new Scanner(System.in);
        System.out.println("Rate between 0 - 10:");
        return String.valueOf(keyboardScanner.nextDouble());

    }

}
