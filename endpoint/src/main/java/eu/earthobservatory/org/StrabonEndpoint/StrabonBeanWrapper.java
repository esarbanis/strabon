/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.earthobservatory.runtime.generaldb.InvalidDatasetFormatFault;
import eu.earthobservatory.runtime.generaldb.Strabon;
import eu.earthobservatory.utils.Format;


public class StrabonBeanWrapper implements org.springframework.beans.factory.DisposableBean {
	
	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.org.StrabonEndpoint.StrabonBeanWrapper.class);
	
	private static final String FILE_PROTOCOL = "file";
	
	private String serverName;
	private int port;
	private String databaseName;
	private String user;
	private String password;
	private String dbBackend;
	private int maxLimit;
	private String prefixes;
	
	private Strabon strabon = null;
	
	private boolean checkForLockTable;
	private List<StrabonBeanWrapperConfiguration> entries;

	public StrabonBeanWrapper(String databaseName, String user, String password, 
			int port, String serverName, boolean checkForLockTable, String dbBackend, 
			int maxLimit, String prefixes, 	List<List<String>> args) {
		this.serverName = serverName;
		this.port = port;
		this.databaseName = databaseName;
		this.user = user;
		this.password = password;
		this.checkForLockTable = checkForLockTable;
		this.dbBackend = dbBackend;
		this.maxLimit = maxLimit;		
		this.prefixes = prefixes;
		this.entries = new ArrayList<StrabonBeanWrapperConfiguration>(args.size());
		
		Iterator<List<String>> entryit = args.iterator();
		
		while (entryit.hasNext()) {
			List<String> list = entryit.next();
			Iterator<String> it = list.iterator();
			
			while (it.hasNext()) {
				int items = 0;
				//Header:label        
				//Bean  :label      bean         
				//Entry :label      bean         statement    format       title      handle
				String param1 = "", param2 = "", param3 = "", param4 = "", param5="", param6=""; 
	
				if (it.hasNext()) {
					param1 = it.next();
					items++;
				}
				if (it.hasNext()) {
					param2 = it.next();
					items++;
				}
				if (it.hasNext()) {
					param3 = it.next();
					items++;
				}
				if (it.hasNext()) {
					param4 = it.next();
					items++;
				}
				if (it.hasNext()) {
					param5 = it.next();
					items++;
				}
				if (it.hasNext()) {
					param6 = it.next();
					items++;
				}
				
				if (items == 1) {
					//the first element corresponds to the label
					StrabonBeanWrapperConfiguration entry = new StrabonBeanWrapperConfiguration(param1);
					this.entries.add(entry);
				} else if (items == 2) {
					//the first element corresponds to the label
					StrabonBeanWrapperConfiguration entry = new StrabonBeanWrapperConfiguration(param1, param2);
					this.entries.add(entry);
				} else if (items == 6) {
					StrabonBeanWrapperConfiguration entry = new StrabonBeanWrapperConfiguration(param3, param1, param4, param2, param5, param6);
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
				//logger.info("[StrabonEndpoint] Connection details:\n" + this.getDetails());
				
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
				logger.error("[StrabonEndpoint] Exception occured while creating Strabon. {}\n{}", e.getMessage(), this.getDetails());
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

	public void closeConnection() {
		if (strabon != null) {
			strabon.close();
			strabon = null;
		}
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
	 * @throws InvalidDatasetFormatFault 
	 * @throws RDFHandlerException 
	 * @throws RDFParseException 
	 * @throws QueryEvaluationException
	 * @throws TupleQueryResultHandlerException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public boolean store(String src, String context, String format, Boolean inference, Boolean url) throws RepositoryException, RDFParseException, RDFHandlerException, IOException, InvalidDatasetFormatFault {
		logger.info("[StrabonEndpoint] Received STORE request.");
		
		if ((this.strabon == null) && (!init())) {
			throw new RepositoryException("Could not connect to Strabon.");
		}

		if (url) {
			URL source = new URL(src);
			if (source.getProtocol().equalsIgnoreCase(FILE_PROTOCOL)) {
				// it would be a security issue if we read from the server's filesystem
				throw new IllegalArgumentException("The protocol of the URL should be one of http or ftp.");
			}
		}

		strabon.storeInRepo(src, null, context, format, inference);
		
		logger.info("[StrabonEndpoint] STORE was successful.");
		
		return true;
	}

	public void setConnectionDetails(String dbname, String username, String password, String port, String hostname, String dbengine) {
		this.databaseName = dbname;
		this.user = username;
		this.password = password;
		try { 
			this.port = Integer.valueOf(port);
		} catch (NumberFormatException e) {
			this.port = 5432;
		}
		this.serverName = hostname;
		this.dbBackend = dbengine;
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
	
	/*
	 * Limit the number of solutions returned.
	 * */
	public String addLimit(String queryString, String maxLimit){
		String limitedQuery = queryString;
		String lowerLimit = null;
		int max;
		
		if(maxLimit == null)
			max = this.maxLimit;
		else
			max = Integer.valueOf(maxLimit);		
		
		if(max > 0)
		{	
			queryString = queryString.trim();		
			Pattern limitPattern = Pattern.compile("limit(\\s*)(\\d+)(\\s*)(offset(\\s*)\\d+)?$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			Matcher limitMatcher = limitPattern.matcher(queryString);
			
			// check whether the query contains a limit clause
			if(limitMatcher.find())
			{					
				Pattern rowsNumberPattern = Pattern.compile("\\d+");
				Matcher rowsNumberMatcher = rowsNumberPattern.matcher(limitMatcher.group());
				rowsNumberMatcher.find();
				
				// if the initial limit is greater than the maximum, set it to the maximum
				if(Integer.valueOf(rowsNumberMatcher.group()) > max)
				{	
					lowerLimit = rowsNumberMatcher.replaceFirst(String.valueOf(max));					
					limitedQuery = limitMatcher.replaceFirst(lowerLimit); 					
				}								
			}	
			else // add a limit to the query 
				limitedQuery = queryString+" limit "+max;			
		}
		return limitedQuery;
	}
	
	public String getPrefixes() {
		return prefixes;
	}

}

