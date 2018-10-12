import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

public class Database {
    public class User{
        private String username;
        private String password;
        private boolean permissao; //false-normal true-editor

        public User(String username, String password, boolean permissao) {
            this.username = username;
            this.password = password;
            this.permissao = permissao;
        }
    }
    private HashMap<String,String> commands = new HashMap<String,String>();
    public void initCommands(){
        commands.put("type","regist");
        commands.put("type","login");
    }
    private ArrayList <user> users = new ArrayList<>();
    public String process(String message){
        String [] sm = message.split(";");
        String [] type = sm[0].split("|");
        String reply ="";
        if(commands.containsValue(type[1])) {
            switch (type[1]) {
                case "regist":
                    reply=regist(sm);
                    break;
                case "login":
                    break;
                default:
                    break;
            }
        }
        return reply;
    }
    public String regist(String [] message){
        try{
            if(message.size()==2){//exemplo --> type|regist;username|__name__;password|__pass__;
                String [] username = message[1].split("|");
                String [] password = message[2].split("|");
            }
            else{
              return "Wrong format of request";
            }
        }catch(Exception e){
            return "Wrong format of request";
        }
        //verificar a validade do request
        if(username[0].equals("username") && password[0].equals("password")){
            User u = new User(username[1],password[1],false);
            //verificamos se o username nao existe
            if(!users.contains(u)){
                re
            }
        }
        return "";
    }
}
    /*
    public static void main(String[] args){
        try{
            Class.forName("org.postgresql.Driver");
            Connection c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/DropMusic.Database","postgres","zubiru");
            PreparedStatement st = c.prepareStatement("select *from public.users");
            ResultSet rs = st.executeQuery();
            while(rs.next()){
                System.out.println(rs.getInt(1)+" "+rs.getString(2));
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }*/
