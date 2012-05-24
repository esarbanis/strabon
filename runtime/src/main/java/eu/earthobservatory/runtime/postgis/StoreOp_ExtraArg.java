package eu.earthobservatory.runtime.postgis;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;

import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import eu.earthobservatory.runtime.generaldb.InvalidDatasetFormatFault;


public class StoreOp_ExtraArg {

	public static void main(String[] args) throws SQLException, ClassNotFoundException
	{
		if (args.length != 8) {
			System.err.println("Usage: eu.ist.semsorgrid4env.strabon.Strabon <HOST> <PORT> <DATABASE> <USERNAME> <PASSWORD> <STRDF_SRC> <FORMAT> <CACHE_PATH>");
			System.err.println("       where <HOST>       is the postgis database host to connect to");
			System.err.println("             <PORT>       is the port to connect to on the database host");		
			System.err.println("             <DATABASE>   is the spatially enabled postgis database that Strabon will use as a backend, ");
			System.err.println("             <USERNAME>   is the username to use when connecting to the database ");
			System.err.println("             <PASSWORD>   is the password to use when connecting to the database");
			System.err.println("             <STRDF_SRC> is the stRDF file or the URL to be stored");
			System.err.println("             <FORMAT> is the format of the file/URL to be stored (available types: NTRIPLES, N3, RDFXML");
			System.err.println("             <CACHE_PATH > is the path where the cached .bin files will be created");
			System.exit(0);
		}

		String host = args[0];
		Integer port = new Integer(args[1]);
		String db = args[2];
		String user = args[3];
		String passwd = args[4];		
		String path = args[5];
		String format = args[6];
		String cachePath = args[7];

		Strabon strabon = new Strabon(db, user, passwd, port, host, true, cachePath);
		Object src = null;
		if(path.startsWith("http"))
		{
			try {
				URL srcUrl = new URL(path);
				src = srcUrl;
			} catch (MalformedURLException e) {
				System.out.println("Please use a well-formed URL!");
				e.printStackTrace();
			}
		}
		else
		{
			File srcFile = new File(path);
			src = srcFile;
		}

		try {

			strabon.storeInRepo(src, format);
		} catch (RDFParseException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidDatasetFormatFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		strabon.close();
	}

}
