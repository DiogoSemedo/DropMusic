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
                        message = rmi.regist();
                        printMessage(message);
                        break;
                    case 2:
                         message = rmi.login();
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


                        break;
                    case "show all":
                        message.put("type","show all");
                        message.put("select",rmi.select());
                        //done
                        break;
                    case "show details":
                        message.put("type","more details");
                        message.put("select",rmi.select());
                        message.put("id",rmi.selectId());
                        //done
                        break;
                    case "insert":
                        message = rmi.insert(message);
                        //done
                        break;
                    case "remove":
                        message.put("type","remove");
                        message.put("select",rmi.selectId());
                        message.put("identifier",rmi.selectId());
                        //done
                        break;
                    case "write review":
                        message.put("type","write review");
                        message.put("identifier",rmi.selectId());
                        message.put("rate",rmi.rate());
                        message.put("text",rmi.review());
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
                //message tem a request falta enviar e receber a reply

            }
        }

    }

    public static void printMessage(HashMap<String,String> message){
        for (HashMap.Entry<String, String> entry : message.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
}
