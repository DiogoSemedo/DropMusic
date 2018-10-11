import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
public class Database {
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
    }
}
