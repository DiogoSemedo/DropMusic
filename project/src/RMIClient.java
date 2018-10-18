import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.Scanner;

public class RMIClient {

    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException{
        RMIInterface rmi = (RMIInterface) Naming.lookup("dropmusic");
        System.out.println("Client ready...");
        boolean login = false;
        HashMap<String,String> message = new HashMap<>();
        Scanner keyboardScanner = new Scanner(System.in);
        while(true){
            message.clear();
            if(!login){
                System.out.println("Menu: \n1 - Regist\n2 - Login");
                switch (keyboardScanner.nextInt()){
                    case 1:
                        message = regist(rmi);
                        printMessage(message);
                        break;
                    case 2:
                         message = login(rmi);
                        printMessage(message);

                        if(message.get("login").equals("sucessful")){//verificar se o regist foi bem sucedido
                            //login sucessful
                            login = true;
                         }
                        printMessage(message);
                        break;
                    default:
                        System.out.println("Wrong command");
                        break;
                }

            }
            else if(login){
                String readKeyboard = keyboardScanner.nextLine();
                switch (readKeyboard){
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
                        //to implement

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
                        message.put("select",selectId());
                        message.put("identifier",selectId());
                        //done
                        break;
                    case "write review":
                        message.put("type","write review");
                        message.put("identifier",selectId());
                        message.put("rate",rate());
                        message.put("text",review());
                        //done
                        break;
                    case "update":
                        //to implement
                        break;
                    default:
                        System.out.println("Wrong comand");
                        break;
                        //nota write review
                }
                message = rmi.request(message);
                printMessage(message);

            }
        }

    }
    public static HashMap<String,String> regist(RMIInterface rmi) throws RemoteException{
        Scanner read = new Scanner(System.in);
        //exemplo --> type|regist;username|name;password|pass
        HashMap<String,String> message = new HashMap<>();
        message.put("type","regist");
        System.out.println("Username:");
        message.put("username",read.nextLine());
        System.out.println("Password:");
        message.put("password",read.nextLine());
        //enviar pela socket
        message = rmi.request(message);
        //receber resposta
        return message;
    }

    public static HashMap<String,String> login(RMIInterface rmi) throws RemoteException{
        Scanner read = new Scanner(System.in);
        HashMap<String,String> message = new HashMap<>();
        message.put("type","login");
        System.out.println("Username:");
        message.put("username",read.nextLine());
        System.out.println("Password:");
        message.put("password",read.nextLine());
        //enviar pela socket
        message = rmi.request(message);
        //receber resposta
        return message;
    }
    public static String review(){
        Scanner keyboardScanner = new Scanner(System.in);
        System.out.println("Write your review:");
        String read;
        while((read=keyboardScanner.nextLine()).length() > 300){
            System.out.println("Less than 300 characters pls!!! Write your review:");
        }
        return read;
    }

    public static String selectId(){
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

    public static String select(){

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

    public static HashMap<String,String> insert(HashMap<String,String> message){
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

    public static String rate(){
        Scanner keyboardScanner = new Scanner(System.in);
        System.out.println("Rate between 0 - 10:");
        return String.valueOf(keyboardScanner.nextDouble());

    }

    public static void printMessage(HashMap<String,String> message){
        for (HashMap.Entry<String, String> entry : message.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
}
