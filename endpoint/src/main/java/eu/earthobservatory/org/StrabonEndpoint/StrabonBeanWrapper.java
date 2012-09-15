package eu.earthobservatory.org.StrabonEndpoint;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.Format;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.earthobservatory.org.StrabonEndpoint.StrabonBeanWrapperConfiguration;
import eu.earthobservatory.runtime.generaldb.Strabon;


public class StrabonBeanWrapper implements org.springframework.beans.factory.DisposableBean {
	
	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.org.StrabonEndpoint.StrabonBeanWrapper.class);
	
	private static final String FILE_PROTOCOL = "file";
	
	private String serverName;
	private int port;
	private String databaseName;
	private String user;
	private String password;
	private String dbBackend;
	
	private Strabon strabon = null;
	
	private boolean checkForLockTable;
	private List<StrabonBeanWrapperConfiguration> entries;

	public StrabonBeanWrapper(String databaseName, String user, String password, 
			int port, String serverName, boolean checkForLockTable, String dbBackend, List<List<String>> args) {
		this.serverName = serverName;
		this.port = port;
		this.databaseName = databaseName;
		this.user = user;
		this.password = password;
		this.checkForLockTable = checkForLockTable;
		this.dbBackend = dbBackend;
		this.entries = new ArrayList<StrabonBeanWrapperConfiguration>(args.size());
		
		Iterator<List<String>> entryit = args.iterator();
		
		while (entryit.hasNext()) {
			List<String> list = entryit.next();
			Iterator<String> it = list.iterator();
			
			while (it.hasNext()) {
				int items = 0;
				String label = "", bean = "", statement = "", format = "", title=""; 
	
				if (it.hasNext()) {
					bean = it.next();
					items++;
				}
				if (it.hasNext()) {
					format = it.next();
					items++;
				}
				if (it.hasNext()) {
					label = it.next();
					items++;
				}
				if (it.hasNext()) {
					statement = it.next();
					items++;
				}
				if (it.hasNext()) {
					title = it.next();
					System.out.println("TITLE= "+title);
					items++;
				}
				if (items == 5) {
					StrabonBeanWrapperConfiguration entry = new StrabonBeanWrapperConfiguration(label, bean, statement, format, title);
					this.entries.add(entry);
				}
			
				
			}
		}

		init();
	}

	public boolean init() {
		if (this.strabon == null) {
			try {
				logger.warn("[StrabonEndpoint] Strabon not initialized yet.");
				logger.warn("[StrabonEndpoint] Initializing Strabon.");
				logger.info("[StrabonEndpoint] Connection details:\n" + this.getDetails());
				
				// initialize Strabon according to user preference
				if (Common.DBBACKEND_MONETDB.equalsIgnoreCase(dbBackend)) {
					this.strabon = new eu.earthobservatory.runtime.monetdb.Strabon(databaseName, user, password, port, serverName, checkForLockTable);
					
				} else {
					// check whether the user typed wrong database backend and report
					if (!Common.DBBACKEND_POSTGIS.equalsIgnoreCase(dbBackend)) {
						logger.warn("[StrabonEndpoint] Unknown database backend \""+dbBackend+"\". Assuming PostGIS.");
					}
					
					// use PostGIS as the default database backend
					this.strabon = new eu.earthobservatory.runtime.postgis.Strabon(databaseName, user, password, port, serverName, checkForLockTable);	
				}
				
				
			} catch (Exception e) {
				logger.error("[StrabonEndpoint] Exception occured while creating Strabon.\n" + this.getDetails(), e);
				return false;
			}
		}

		return true;
	}

	public Strabon getStrabon() {
		return strabon;
	}

	public void setStrabon(Strabon strabon) {
		this.strabon = strabon;
	}

	public void destroy() throws Exception {
		if (strabon != null) {
			strabon.close();
			
			// deregister jdbc driver
			strabon.deregisterDriver();
		}
	}

	public void query(String queryString, String answerFormatStrabon, OutputStream out)
	throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, ClassNotFoundException {
		logger.info("[StrabonEndpoint] Received SELECT query.");
		if ((this.strabon == null) && (!init())) {
			throw new RepositoryException("Could not connect to Strabon.");
		} 

		strabon.query(queryString, Format.fromString(answerFormatStrabon), strabon.getSailRepoConnection(), out);
		
	}
	
	/**
	 * Wrapper around Strabon.describeOp which takes an OutputStream to use for writing
	 * the answer to a DESCRIBE query.
	 * 
	 * @param queryString
	 * @param answerFormatStrabon
	 * @param out
	 * @throws MalformedQueryException
	 * @throws RepositoryException
	 * @throws QueryEvaluationException
	 * @throws TupleQueryResultHandlerException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void describe(String queryString, String answerFormatStrabon, OutputStream out)
	throws MalformedQueryException, RepositoryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException, ClassNotFoundException {
		logger.info("[StrabonEndpoint] Received DESCRIBE query.");
		if ((this.strabon == null) && (!init())) {
			throw new RepositoryException("Could not connect to Strabon.");
		} 

		strabon.describe(queryString, answerFormatStrabon, strabon.getSailRepoConnection(), out);
	}

	public Object update(String updateString, String answerFormatStrabon) 
	throws MalformedQueryException, RepositoryException, QueryEvaluationException, 
	TupleQueryResultHandlerException, IOException, ClassNotFoundException {
		logger.info("[StrabonEndpoint] Received UPDATE query.");
		logger.info("[StrabonEndpoint] Answer format: " + answerFormatStrabon);
		
		if ((this.strabon == null) && (!init())) {
			throw new RepositoryException("Could not connect to Strabon.");
		} 

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
		logger.info("[StrabonEndpoint] Received STORE request.");
		
		if ((this.strabon == null) && (!init())) {
			throw new RepositoryException("Could not connect to Strabon.");
		}

		// get sail connection
		SailRepositoryConnection conn = strabon.getSailRepoConnection();

		try {
			// store data
			if (url) {
				URL source = new URL(source_data);
				if (source.getProtocol().equalsIgnoreCase(FILE_PROTOCOL)) {
					// it would be a security issue if we read from the server's filesystem
					throw new IllegalArgumentException("The protocol of the URL should be one of http or ftp.");
				} 
				conn.add(source, "", format, new Resource[1]);

			} else {
				conn.add(new StringReader(source_data), "", format, new Resource[1]);
			}
			
			logger.info("[StrabonEndpoint] STORE was successful.");

		} catch (Exception e) {
			throw e;
		}

		return true;
	}

	public void setConnectionDetails(String username, String password, String dbname, String hostname, String port, String dbengine) {
		this.user = username;
		this.password = password;
		this.databaseName = dbname;
		this.dbBackend = dbengine;
		this.password = password;
		try { 
			this.port = Integer.valueOf(port);
		} catch (NumberFormatException e) {
			this.port = 5432;
		}
		this.serverName = hostname;
		this.checkForLockTable = true;		
	}
	
	public String getUsername() {
		return user;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getDatabaseName() {
		return databaseName;
	}
	
	public String getDBEngine() {
		return dbBackend;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getHostName() {
		return serverName;
	}
	
	private String getDetails() {
		String details = "-----------------------------------------\n";
		details += "host     : " + serverName + "\n";
		details += "port     : " + port + "\n";
		details += "database : " + databaseName + "\n";
		details += "user     : " + user + "\n";
		details += "password : " + password + "\n";
		details += "-----------------------------------------\n";

		return details;
	}

	public List<StrabonBeanWrapperConfiguration> getEntries() {
		return this.entries;
	}
	
	public void setEntries(List<StrabonBeanWrapperConfiguration> entries) {
		this.entries = entries;
	}
	
	public StrabonBeanWrapperConfiguration getEntry(int i) {
		if (i < 0 || i >= this.entries.size())
			return null;
		
		return this.entries.get(i);
	}
	
}

