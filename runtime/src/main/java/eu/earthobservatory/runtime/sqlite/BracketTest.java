package eu.earthobservatory.runtime.sqlite;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.sqlite.SQLiteConfig;

public class BracketTest {
    public static void main(String args[]) throws Exception {
        Class.forName("org.sqlite.JDBC");
      //  SQLiteConfig config = new SQLiteConfig();
     //   config.enableLoadExtension(true);

        // create a database connection
     //   Connection conn = DriverManager.getConnection("jdbc:sqlite:/tmp/666.db",
     //   config.toProperties());
     //   Statement stmt1 = conn.createStatement();
     //   stmt1.setQueryTimeout(30); // set timeout to 30 sec.

        // loading SpatiaLite
     //   stmt1.execute("SELECT load_extension('/usr/local/lib/libspatialite.so')");

        // enabling Spatial Metadata
        // using v.2.4.0 this automatically initializes SPATIAL_REF_SYS and GEOMETRY_COLUMNS
    //    String sql = "SELECT InitSpatialMetadata()";
    //    stmt1.execute(sql);

       Connection conn = DriverManager.getConnection("jdbc:sqlite:/tmp/11117.db");

        Statement stat = conn.createStatement();

        stat.execute("create table person (p_id primary key, p_name)");
        stat.execute("insert into person values (1, 'employee')");
        stat.execute("create table contact (p_id primary key, p_name)");
        stat.execute("insert into contact values (1, 'contact')");
        stat.execute("create table company (p_id primary key, p_name)");
        stat.execute("insert into company values (1, 'company')");
    //    stat.close();
   //     conn.close();

        String sql2="select * from contact as ct inner join ( person as p " + 
                "inner join company as c on p.p_id=c.p_id " +
                ") " +
                " on p.p_id=ct.p_id " +
            "where p.p_id=1;";
        System.out.println(sql2);
        //PreparedStatement stmt = conn.prepareStatement(sql2);

        //stmt.setInt(1, 1);

        ResultSet rs = conn.createStatement().executeQuery(sql2);

        rs.next();
        System.out.println(
                rs.getString(1) + "|" + rs.getString(2) + "|" +
                rs.getString(3) + "|" + rs.getString(4) + "|" +
                rs.getString(5) + "|" + rs.getString(6));
   }
}