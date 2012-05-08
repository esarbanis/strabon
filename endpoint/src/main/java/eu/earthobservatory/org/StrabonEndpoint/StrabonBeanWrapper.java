package eu.earthobservatory.org.StrabonEndpoint;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.sql.SQLException;

import org.openrdf.model.Resource;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;

import eu.earthobservatory.runtime.postgis.Strabon;

public class StrabonBeanWrapper implements org.springframework.beans.factory.DisposableBean {
	private String serverName;
	private int port;
	private String databaseName;
	private String user;
	private String password;
	
	private Strabon strabon = null;
	private boolean checkForLockTable;
	private String query1;
	private String query2;
	private String query3;
	private String query4;
	private String query5;
	private String query6;
	private String query7;
	private String query8;
	private String format;
	
	public StrabonBeanWrapper() {
		this.strabon = null;
	}
	
	public StrabonBeanWrapper(Strabon strabon) {
		this.strabon = strabon;
	}
	
	public StrabonBeanWrapper(String databaseName, String user, String password, int port, String serverName, boolean checkForLockTable, String query1, String query2, String query3, String query4, String query5, String query6, String query7, String query8) {
		this.serverName = serverName;
		this.port = port;
		this.databaseName = databaseName;
		this.user = user;
		this.password = password;
		this.checkForLockTable = checkForLockTable;
		//this.strabon = new Strabon(databaseName, user, password, port, serverName, checkForLockTable);
		this.query1 = query1;
		this.query2 = query2;
		this.query3 = query3;
		this.query4 = query4;
		this.query5 = query5;
		this.query6 = query6;
		this.query7 = query7;
		this.query8 = query8;
		format = "format=HTML";
		init();
	}
	
	public StrabonBeanWrapper(String databaseName, String user, String password, int port, String serverName, boolean checkForLockTable, String query1, String query2, String query3, String query4, String query5, String query6, String query7, String query8, String format) {
		this.serverName = serverName;
		this.port = port;
		this.databaseName = databaseName;
		this.user = user;
		this.password = password;
		this.checkForLockTable = checkForLockTable;
		//this.strabon = new Strabon(databaseName, user, password, port, serverName, checkForLockTable);
		this.query1 = query1;
		this.query2 = query2;
		this.query3 = query3;
		this.query4 = query4;
		this.query5 = query5;
		this.query6 = query6;
		this.query7 = query7;
		this.query8 = query8;
		this.format = format;
		
		init();
	}
	
	private boolean init() {
		if (this.strabon == null) {
			try {
				System.err.println("Strabon not initialized yet.");
				System.err.println("Initializing strabon.");
				System.out.println(this.getDetails());
				this.strabon = new Strabon(databaseName, user, password, port, serverName, checkForLockTable);
			} catch (Exception e) {
				System.err.println("Exception occured while creating Strabon.\n"+this.getDetails());
				e.printStackTrace();
				return false;
			}
		}
		
		return true;
	}
	
	public StrabonBeanWrapper(String databaseName, String user, String password, int port, String serverName) throws SQLException, ClassNotFoundException {
		this.strabon = new Strabon(databaseName, user, password, port, serverName, true);
	}

	public Strabon getStrabon() {
		return strabon;
	}

	public void setStrabon(Strabon strabon) {
		this.strabon = strabon;
	}

	public void destroy() throws Exception {
		strabon.close();
	}
	
	
	
	public Object query(String queryString, String answerFormatStrabon) throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, ClassNotFoundException {
		if ((this.strabon == null) && (!init())) {
			throw new RepositoryException("Could not connect to Strabon.");
		} 
		
		return strabon.query(queryString, answerFormatStrabon, strabon.getSailRepoConnection());
	}
	
	public Object update(String updateString, String answerFormatStrabon) throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, ClassNotFoundException {
		if ((this.strabon == null) && (!init())) {
			throw new RepositoryException("Could not connect to Strabon.");
		} 
		
    	//String newQueryString = TemporalWrapper.rebuildQuery(queryString);
    	
    	System.out.println("================================================================");
    	System.out.println("Update:");
    	System.out.println(updateString);
    	System.out.println("================================================================");
    	System.out.println("Answer format: " + answerFormatStrabon);
    	System.out.println("================================================================");
    	//System.out.println("Rewritten query:");
    	//System.out.println(newQueryString);
    	//System.out.println("================================================================");
		
   		//return strabon.query(newQueryString, answerFormatStrabon, strabon.getSailRepoConnection());
		strabon.update(updateString, strabon.getSailRepoConnection());
		return "OK!";
	}

	/**
	 * Store the given data in the given format into Strabon repository. If url is true, then
	 * input comes from a URL. 
	 * 
	 * Returns true on success, false otherwise.
	 * 
	 * @param source_data
	 * @param format
	 * @param url
	 * @return
	 * @throws MalformedQueryException
	 * @throws RepositoryException
	 * @throws QueryEvaluationException
	 * @throws TupleQueryResultHandlerException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public boolean store(String source_data, RDFFormat format, boolean url) throws Exception {
		if ((this.strabon == null) && (!init())) {
			throw new RepositoryException("Could not connect to Strabon.");
		}
		
		// get sail connection
		SailRepositoryConnection conn = strabon.getSailRepoConnection();
		
		try {
			// store data
			if (url) {
				conn.add(new URL(source_data), "", format, new Resource[1]);
				
			} else {
				conn.add(new StringReader(source_data), "", format, new Resource[1]);
			}
			
		} catch (Exception e) {
			throw e;
		}
		
    	return true;
	}
	
	private String getDetails() {
		String details = "-----------------------------------------\n";
		details += "Connection details:\n";
		details += "host     : " + serverName + "\n";
		details += "port     : " + port + "\n";
		details += "database : " + databaseName + "\n";
		details += "user     : " + user + "\n";
		details += "password : " + password + "\n";
		details += "-----------------------------------------\n";

		return details;
	}

	public String getQuery1() {
		return query1;
	}

	public void setQuery1(String query1) {
		this.query1 = query1;
	}

	public String getQuery2() {
		return query2;
	}

	public void setQuery2(String query2) {
		this.query2 = query2;
	}

	public String getQuery3() {
		return query3;
	}

	public void setQuery3(String query3) {
		this.query3 = query3;
	}
	
	public String getQuery4() {
		return query4;
	}

	public void setQuery4(String query4) {
		this.query4 = query4;
	}

	public String getQuery5() {
		return query5;
	}

	public void setQuery5(String query5) {
		this.query5 = query5;
	}

	public String getQuery6() {
		return query6;
	}

	public void setQuery6(String query6) {
		this.query6 = query6;
	}

	public String getQuery7() {
		return query7;
	}

	public void setQuery7(String query7) {
		this.query7 = query7;
	}
	public String getQuery8() {
		return query8;
	}

	public void setQuery8(String query8) {
		this.query8 = query8;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
	
	
}

