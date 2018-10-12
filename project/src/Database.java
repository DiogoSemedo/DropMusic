import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

public class Database {
    public class User {
        private String username;
        private String password;
        private boolean permissao; //false-normal true-editor

        public User(String username, String password, boolean permissao) {
            this.username = username;
            this.password = password;
            this.permissao = permissao;
        }
    }

    private Connection c;
    private PreparedStatement st;
    private ResultSet rs;

    public Database() {
        try {
            Class.forName("org.postgresql.Driver");
            this.c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/DropMusic.Database", "postgres", "zubiru");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private ArrayList<User> users = new ArrayList<>();

    public String process(String message) {
        String[] sm = message.split(";");
        String[] type = sm[0].split("\\|");
        String reply = "";

        switch (type[1]) {
            case "regist":
                reply = regist(sm);
                break;
            case "login":
                break;
            default:
                break;
        }

        return reply;
    }

    public String regist(String[] message) {
        if (message.length == 3) {
            //exemplo --> type|regist;username|name;password|pass;
            String[] username = message[1].split("\\|");
            String[] password = message[2].split("\\|");
            //verificar a validade do request
            if (username[0].equals("username") && password[0].equals("password")) {
                try {
                    st = c.prepareStatement("select name from public.users where name = '" + username[1] + "'");
                    rs = st.executeQuery();
                    if (rs.next()) {
                        return "type | status; regist | wrong; msg | Error!!!";
                    }
                    st = c.prepareStatement("select count(name) from public.users");
                    rs = st.executeQuery();
                    st = c.prepareStatement("INSERT INTO public.users(name, password, permission) VALUES (?, ?, ?);");
                    st.setString(1, username[1]);
                    st.setString(2, password[1]);
                    if (rs.next() && rs.getInt(1) == 0) {
                        st.setBoolean(3, true);
                    } else {
                        st.setBoolean(3, false);
                    }
                    st.executeUpdate();
                    return "type | status; regist | done; msg | Registry Sucessfull! Enjoy DropMusic";
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        return "Wrong format of request";
    }

    public String login(String[] message) {
        if (message.length == 2) {
            //exemplo --> type|regist;username|name;password|pass;
            String[] username = message[1].split("|");
            String[] password = message[2].split("|");
            //verificar a validade do request
            if (username[0].equals("username") && password[0].equals("password")) {
                User u = new User(username[1], password[1], false);
                if (users.contains(u)) {
                    //login
                    return "type | status; login | on; msg | Welcome to DropMusic";
                } else {
                    return "type | status; login | wrong; msg | Error!!!";
                }
            } else {
                return "Wrong format of request";
            }
        } else {
            return "Wrong format of request";
        }
    }
}
