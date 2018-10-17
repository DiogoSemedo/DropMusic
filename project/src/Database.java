
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
            this.c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/DropMusic.Database", "postgres", "surawyk");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private ArrayList<User> users = new ArrayList<>();

    public HashMap<String, String> process(HashMap<String, String> message) {
        HashMap<String, String> reply = new HashMap<String, String>();
        switch (message.get("type")) {
            case "regist":
                reply = regist(message);
                break;
            case "login":
                reply = login(message);
                break;
            case "show all":
                reply = showall(message);
                break;
            case "show details":
                reply = showdetails(message);
                break;
            case "write review":
                reply = writereview(message);
                break;
            default:
                break;
        }
        return reply;
    }

    public HashMap<String, String> regist(HashMap<String, String> message) {
        HashMap<String, String> reply = new HashMap<String, String>();
        //exemplo --> type|regist;username|name;password|pass
        try {
            st = c.prepareStatement("select name from public.users where name = '" + message.get("username") + "'");
            rs = st.executeQuery();
            if (rs.next()) {
                reply.put("type", "status");
                reply.put("regist", "failed");
                reply.put("msg", "Username already in use.");
                return reply;
            }
            st = c.prepareStatement("select count(name) from public.users");
            rs = st.executeQuery();
            st = c.prepareStatement("INSERT INTO public.users(id,name, password, permission) VALUES (DEFAULT,?, ?, ?);");
            st.setString(1, message.get("username"));
            st.setString(2, message.get("password"));
            if (rs.next() && rs.getInt(1) == 0) {
                st.setBoolean(3, true);
            } else {
                st.setBoolean(3, false);
            }
            st.executeUpdate();
            reply.put("type", "status");
            reply.put("regist", "successful");
            reply.put("msg", "Registry done.Enjoy DropMusic.");
            return reply;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            reply.put("type", "status");
            reply.put("regist", "failed");
            reply.put("msg", e.getMessage());
            return reply;
        }
    }

    public HashMap<String, String> login(HashMap<String, String> message) {
        //exemplo --> type|regist;username|name;password|pass;
        HashMap<String, String> reply = new HashMap<String, String>();
        try {
            st = c.prepareStatement("select exists( select * from public.users where not status and name='" + message.get("username") + "' and password='" + message.get("password") + "' );");
            rs = st.executeQuery();
            if (rs.next() && rs.getBoolean(1)) {
                st = c.prepareStatement("update public.users set status=true where name='" + message.get("username") + "';");
                reply.put("type", "status");
                reply.put("login", "successful");
                reply.put("msg", "You're logged in.");
                return reply;
            }
            reply.put("type", "status");
            reply.put("login", "failed");
            reply.put("msg", "Credentials wrong.");
            return reply;
        } catch (Exception e) {
            reply.put("type", "status");
            reply.put("login", "failed");
            reply.put("msg", e.getMessage());
            return reply;
        }
    }

    public HashMap<String, String> showall(HashMap<String, String> message) {
        //exemplo --> type|show all;select|(artists,albums,musics)
        HashMap<String, String> reply = new HashMap<String, String>();
        try {
            st = c.prepareStatement("select id,title from public." + message.get("select") + ";");
            rs = st.executeQuery();
            while (rs.next()) {
                reply.put(String.valueOf(rs.getInt(1)), rs.getString(2));
            }
            return reply;
        } catch (Exception e) {
            reply.put("type", "show all");
            reply.put("msg", e.getMessage());
            return reply;
        }
    }

    public HashMap<String, String> showdetails(HashMap<String, String> message) {
        //exemplo --> type|show details;select|(artists,albums);identifier|(1,5,19)
        HashMap<String, String> reply = new HashMap<String, String>();
        try {
            if (message.get("select").equals("artists")) {
                st = c.prepareStatement("select * from public.artists where id=" + message.get("identifier") + ";");
                rs = st.executeQuery();
                if (rs.next()) {
                    reply.put("details", String.valueOf(rs.getInt(1)) + " Name:" + rs.getString(2) + " Description:" + rs.getString(3));
                    return reply;
                }
                reply.put("type", "show details");
                reply.put("identifier", "Don't exists.");
                return reply;
            } else {
                st = c.prepareStatement("select * from public.albums where id=" + message.get("identifier") + ";");
                rs = st.executeQuery();
                if (rs.next()) {
                    reply.put("details", String.valueOf(rs.getInt(1)) + " Title:" + rs.getString(2) + " Description:" + rs.getString(3) + " Rate:" + String.valueOf(rs.getDouble(4)));
                    st = c.prepareStatement("select id,title,compositor,duration,genre from public.musics where idalbum=" + message.get("identifier") + ";");
                    rs = st.executeQuery();
                    while (rs.next()) {
                        reply.put("Music:" + String.valueOf(rs.getInt(1)), "Title:" + rs.getString(2) + " Compositor:" + rs.getString(3) + " Duration:" + rs.getString(4) + " Genre:" + rs.getString(5));
                    }
                    st = c.prepareStatement("select id,text,rate from public.reviews where idalbum=" + message.get("identifier") + ";");
                    rs = st.executeQuery();
                    while (rs.next()) {
                        reply.put("Review:" + String.valueOf(rs.getInt(1)), "Rate:" + String.valueOf(rs.getDouble(3)) + " Text:" + rs.getString(2));
                    }
                    return reply;
                } else {
                    reply.put("type", "show details");
                    reply.put("identifier", "Don't exists.");
                    return reply;
                }
            }
        } catch (Exception e) {
            reply.put("type", "show details");
            reply.put("msg", e.getMessage());
            return reply;
        }
    }

    public HashMap<String, String> writereview(HashMap<String, String> message) {
        //exemplo --> type|write review;identifier|idalbum;rate|4.5;text|asdkjasdkasjkd
        HashMap<String, String> reply = new HashMap<String, String>();
        try {
            st = c.prepareStatement("insert into public.reviews (id,text,idalbum,rate) values (default,?,?,?)");
            st.setString(1, message.get("text"));
            st.setInt(2, Integer.parseInt(message.get("identifier")));
            st.setDouble(3, Double.parseDouble(message.get("rate")));
            st.executeUpdate();
            st = c.prepareStatement("update public.albums set rate=(select avg(public.reviews.rate) from public.albums,public.reviews where public.reviews.idalbum=" + message.get("identifier") + "and public.albums.id=" + message.get("identifier") + ");");
            st.executeUpdate();
            reply.put("type", "write review");
            reply.put("msg", "sucessful");
            return reply;
        } catch (Exception e) {
            reply.put("type", "write review");
            reply.put("msg", e.getMessage());
            return reply;
        }
    }

    public HashMap<String, String> searchmusic(HashMap<String, String> message) {
        //exemplo --> type|search music;select|(artist,album,music);text|asdasd
        HashMap<String, String> reply = new HashMap<String, String>();
        try {
            if (message.get("select").equals("artist")) {
                st = c.prepareStatement("select id,title from public.musics where idartist=(select id from public.artists where title='" + message.get("text") + "');");
            } else if (message.get("select").equals("album")) {
                st = c.prepareStatement("select id,title from public.musics where idalbum=(select id from public.albums where title='" + message.get("text") + "');");
            } else {
                st = c.prepareStatement("select id,title from public.musics where genre=" + message.get("text") + "';");
            }
            rs = st.executeQuery();
            while (rs.next()) {
                reply.put(String.valueOf(rs.getInt(1)), rs.getString(2));
            }
            return reply;
        } catch (Exception e) {
            reply.put("type", "search music");
            reply.put("msg", e.getMessage());
            return reply;
        }
    }
}
