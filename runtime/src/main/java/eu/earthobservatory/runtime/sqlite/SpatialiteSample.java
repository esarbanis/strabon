package eu.earthobservatory.runtime.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import org.sqlite.SQLiteConfig;

public class SpatialiteSample
{
  public static void main(String[] args) throws ClassNotFoundException
  {
    // load the sqlite-JDBC driver using the current class loader
    Class.forName("org.sqlite.JDBC");

    Connection conn = null;
    try
    {
      // enabling dynamic extension loading
      // absolutely required by SpatiaLite
      SQLiteConfig config = new SQLiteConfig();
      config.enableLoadExtension(true);

      // create a database connection
      conn = DriverManager.getConnection("jdbc:sqlite:/tmp/000.db",
      config.toProperties());
      Statement stmt = conn.createStatement();
      stmt.setQueryTimeout(30); // set timeout to 30 sec.

      // loading SpatiaLite
      stmt.execute("SELECT load_extension('/usr/lib/x86_64-linux-gnu/libspatialite.so')");
      stmt.execute("SELECT load_extension(' /usr/lib/x86_64-linux-gnu/libpcre.so')");
      // enabling Spatial Metadata
      // using v.2.4.0 this automatically initializes SPATIAL_REF_SYS and GEOMETRY_COLUMNS
      String sql = "SELECT InitSpatialMetadata()";
      stmt.execute(sql);
      
//      sql="SELECT t0.obj,"+
// "l_title.value,"+
// " NULL ,"+
// " NULL ,"+
// "p1.obj,"+
// "l_price.value,"+
// " NULL ,"+
// "d_price.value"+
//"FROM title_47 t0"+
//" LEFT JOIN (price_50 p1"+
//" LEFT JOIN numeric_values n_price ON (n_price.id = p1.obj)"+
//" LEFT JOIN datatype_values d_price ON (d_price.id = p1.obj)"+
//" LEFT JOIN label_values l_price ON (l_price.id = p1.obj)) ON (p1.subj = t0.subj"+
//" AND n_price.value <  ? )"+
//" LEFT JOIN label_values l_title ON (l_title.id = t0.obj)";
//      stmt.execute(sql);
//      // creating a POINT table
      sql = "CREATE TABLE test_pt (";
     sql += "id INTEGER NOT NULL PRIMARY KEY,";
      sql += "name TEXT NOT NULL)";      stmt.execute(sql);
//      // creating a POINT Geometry column
      sql = "SELECT AddGeometryColumn('test_pt', ";
      sql += "'geom', 4326, 'POINT', 'XY')";
      stmt.execute(sql);

      // creating a LINESTRING table
      sql = "CREATE TABLE test_ln (";
      sql += "id INTEGER NOT NULL PRIMARY KEY,";
      sql += "name TEXT NOT NULL)";
      stmt.execute(sql);
      // creating a LINESTRING Geometry column
      sql = "SELECT AddGeometryColumn('test_ln', ";
      sql += "'geom', 4326, 'LINESTRING', 'XY')";
      stmt.execute(sql);

      // creating a POLYGON table
      sql = "CREATE TABLE test_pg (";
      sql += "id INTEGER NOT NULL PRIMARY KEY,";
      sql += "name TEXT NOT NULL)";
      stmt.execute(sql);
      // creating a POLYGON Geometry column
      sql = "SELECT AddGeometryColumn('test_pg', ";
      sql += "'geom', 4326, 'POLYGON', 'XY')";
      stmt.execute(sql);

      // inserting some POINTs
      // please note well: SQLite is ACID and Transactional,
      // so (to get best performance) the whole insert cycle
      // will be handled as a single TRANSACTION
      conn.setAutoCommit(false);
      int i;
      for (i = 0; i < 100000; i++)
      {
        // for POINTs we'll use full text sql statements
        sql = "INSERT INTO test_pt (id, name, geom) VALUES (";
        sql += i + 1;
        sql += ", 'test POINT #";
        sql += i + 1;
        sql += "', GeomFromText('POINT(";
        sql += i / 1000.0;
        sql += " ";
        sql += i / 1000.0;
        sql += ")', 4326))";
        stmt.executeUpdate(sql);
      }
      conn.commit();

      // checking POINTs
      sql = "SELECT DISTINCT Count(*), ST_GeometryType(geom), ";
      sql += "ST_Srid(geom) FROM test_pt";
      ResultSet rs = stmt.executeQuery(sql);
      while(rs.next())
      {
        // read the result set
        String msg = "> Inserted ";
        msg += rs.getInt(1);
        msg += " entities of type ";
        msg += rs.getString(2);
        msg += " SRID=";
        msg += rs.getInt(3);
        System.out.println(msg);
      }

      // inserting some LINESTRINGs
      // this time we'll use a Prepared Statement
      sql = "INSERT INTO test_ln (id, name, geom) ";
      sql += "VALUES (?, ?, GeomFromText(?, 4326))";
      PreparedStatement ins_stmt = conn.prepareStatement(sql);
      conn.setAutoCommit(false);
      for (i = 0; i < 100000; i++)
      {
        // setting up values / binding
        String name = "test LINESTRING #";
        name += i + 1;
        String geom = "LINESTRING (";
        if ((i%2) == 1)
        {
          // odd row: five points
          geom += "-180.0 -90.0, ";
          geom += -10.0 - (i / 1000.0);
          geom += " ";
          geom += -10.0 - (i / 1000.0);
          geom += ", ";
          geom += -10.0 - (i / 1000.0);
          geom += " ";
          geom += 10.0 + (i / 1000.0);
          geom += ", ";
          geom += 10.0 + (i / 1000.0);
          geom += " ";
          geom += 10.0 + (i / 1000.0);
          geom += ", 180.0 90.0";
        }
        else
        {
          // even row: two points
          geom += -10.0 - (i / 1000.0);
          geom += " ";
          geom += -10.0 - (i / 1000.0);
          geom += ", ";
          geom += 10.0 + (i / 1000.0);
          geom += " ";
          geom += 10.0 + (i / 1000.0);
        }
        geom += ")";
        ins_stmt.setInt(1, i+1);
        ins_stmt.setString(2, name);
        ins_stmt.setString(3, geom);
        ins_stmt.executeUpdate();
      }
      conn.commit();

      // checking LINESTRINGs
      sql = "SELECT DISTINCT Count(*), ST_GeometryType(geom), ";
      sql += "ST_Srid(geom) FROM test_ln";
      rs = stmt.executeQuery(sql);
      while(rs.next())
      {
        // read the result set
        String msg = "> Inserted ";
        msg += rs.getInt(1);
        msg += " entities of type ";
        msg += rs.getString(2);
        msg += " SRID=";
        msg += rs.getInt(3);
        System.out.println(msg);
      }

      // inserting some POLYGONs
      // this time too we'll use a Prepared Statement
      sql = "INSERT INTO test_pg (id, name, geom) ";
      sql += "VALUES (?, ?, GeomFromText(?, 4326))";
      ins_stmt = conn.prepareStatement(sql);
      conn.setAutoCommit(false);
      for (i = 0; i < 100000; i++)
      {
        // setting up values / binding
        String name = "test POLYGON #";
        name += i + 1;
        ins_stmt.setInt(1, i+1);
        ins_stmt.setString(2, name);
        String geom = "POLYGON((";
        geom += -10.0 - (i / 1000.0);
        geom += " ";
        geom += -10.0 - (i / 1000.0);
        geom += ", ";
        geom += 10.0 + (i / 1000.0);
        geom += " ";
        geom += -10.0 - (i / 1000.0);
        geom += ", ";
        geom += 10.0 + (i / 1000.0);
        geom += " ";
        geom += 10.0 + (i / 1000.0);
        geom += ", ";
        geom += -10.0 - (i / 1000.0);
        geom += " ";
        geom += 10.0 + (i / 1000.0);
        geom += ", ";
        geom += -10.0 - (i / 1000.0);
        geom += " ";
        geom += -10.0 - (i / 1000.0);
        geom += "))";
        ins_stmt.setInt(1, i+1);
        ins_stmt.setString(2, name);
        ins_stmt.setString(3, geom);
        ins_stmt.executeUpdate();
      }
      conn.commit();

      // checking POLYGONs
      sql = "SELECT DISTINCT Count(*), ST_GeometryType(geom), ";
      sql += "ST_Srid(geom) FROM test_pg";
      rs = stmt.executeQuery(sql);
      while(rs.next())
      {
        // read the result set
        String msg = "> Inserted ";
        msg += rs.getInt(1);
        msg += " entities of type ";
        msg += rs.getString(2);
        msg += " SRID=";
        msg += rs.getInt(3);
        System.out.println(msg);
      }
    }
    catch(SQLException e)
    {
      // if the error message is "out of memory",
      // it probably means no database file is found
      System.err.println(e.getMessage());
    }
    finally
    {
      try
      {
        if(conn != null)
          conn.close();
      }
      catch(SQLException e1)
      {
        // connection close failed.
        System.err.println(e1);
      }
    }
  }
}