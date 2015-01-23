package org.openrdf.sail.sqlite;

/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
import static java.sql.Connection.TRANSACTION_SERIALIZABLE;
import info.aduna.concurrent.locks.Lock;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.generaldb.GeneralDBConnection;
import org.openrdf.sail.generaldb.GeneralDBConnectionFactory;
import org.openrdf.sail.generaldb.GeneralDBTripleRepository;
import org.openrdf.sail.generaldb.GeneralDBValueFactory;
import org.openrdf.sail.generaldb.evaluation.GeneralDBEvaluationFactory;
import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.optimizers.GeneralDBQueryOptimizer;
import org.openrdf.sail.generaldb.optimizers.GeneralDBSelectQueryOptimizerFactory;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.sqlite.evaluation.SqliteEvaluationFactory;
import org.openrdf.sail.sqlite.evaluation.SqliteQueryBuilderFactory;
import org.openrdf.sail.rdbms.util.DatabaseLockManager;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.generaldb.managers.BNodeManager;
import org.openrdf.sail.generaldb.managers.HashManager;
import org.openrdf.sail.generaldb.managers.LiteralManager;
import org.openrdf.sail.generaldb.managers.PredicateManager;
import org.openrdf.sail.generaldb.managers.TransTableManager;
import org.openrdf.sail.generaldb.managers.TripleManager;
import org.openrdf.sail.generaldb.managers.TripleTableManager;
import org.openrdf.sail.generaldb.managers.UriManager;
import org.openrdf.sail.rdbms.managers.NamespaceManager;
import org.openrdf.sail.rdbms.schema.NamespacesTable;
import org.openrdf.sail.rdbms.schema.TableFactory;
import org.openrdf.sail.generaldb.schema.IntegerIdSequence;
import org.openrdf.sail.generaldb.schema.LongIdSequence;
import org.openrdf.sail.generaldb.schema.ValueTableFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible to initialise and wire all components together that will be
 * needed to satisfy any sail connection request.
 * 
 * @author James Leigh
 */
public class SqliteConnectionFactory extends GeneralDBConnectionFactory {
	private String spatiaLite;
	private String pcre;
	private static Logger logger = LoggerFactory.getLogger(org.openrdf.sail.sqlite.SqliteConnectionFactory.class);
	protected Connection singleSQLiteCon;
//	protected SqliteTripleTableManager sqliteTripleTableManager;

	@Override
	protected TableFactory createTableFactory() {
		return new SqliteSqlTableFactory();
	}
	
	@Override
	protected SqliteSqlValueTableFactory createValueTableFactory() {
		return new SqliteSqlValueTableFactory();
	}
	
	@Override
	protected Lock createDatabaseLock()
		throws SailException
	{
		DatabaseLockManager manager;
		manager = new DatabaseLockManager(ds, user, password);
		if (manager.isDebugEnabled())
			return manager.tryLock();
		return manager.lockOrFail();
	}

	@Override
	protected GeneralDBQueryBuilderFactory createQueryBuilderFactory() {
		return new SqliteQueryBuilderFactory();
	}
	
	@Override
	public SailConnection createConnection()
	throws SailException
	{
		try {
			Connection db = singleSQLiteCon;
			db.setAutoCommit(true);
			if (db.getTransactionIsolation() != TRANSACTION_SERIALIZABLE ) {
				db.setTransactionIsolation(TRANSACTION_SERIALIZABLE );
			}
			TripleManager tripleManager = new TripleManager();
			GeneralDBTripleRepository s = new SqliteTripleRepository();
			s.setTripleManager(tripleManager);
			s.setValueFactory(vf);
			s.setConnection(db);
			s.setBNodeTable(bnodeTable);
			s.setURITable(uriTable);
			s.setLiteralTable(literalTable);
			s.setIdSequence(ids);
			DefaultSailChangedEvent sailChangedEvent = new DefaultSailChangedEvent(sail);
			s.setSailChangedEvent(sailChangedEvent);
			TableFactory tables = createTableFactory();
			TransTableManager trans = createTransTableManager();
			trans.setIdSequence(ids);
			tripleManager.setTransTableManager(trans);
			trans.setBatchQueue(tripleManager.getQueue());
			trans.setSailChangedEvent(sailChangedEvent);
			trans.setConnection(db);
			trans.setTemporaryTableFactory(tables);
			trans.setStatementsTable(tripleTableManager);
			trans.setFromDummyTable(getFromDummyTable());
			trans.initialize();
			s.setTransaction(trans);
			GeneralDBQueryBuilderFactory bfactory = createQueryBuilderFactory();
			bfactory.setValueFactory(vf);
			bfactory.setUsingHashTable(hashManager != null);
			s.setQueryBuilderFactory(bfactory);
			GeneralDBConnection conn = new GeneralDBConnection(sail, s);
			conn.setNamespaces(namespaces);
			GeneralDBEvaluationFactory efactory = new SqliteEvaluationFactory();
			efactory.setQueryBuilderFactory(bfactory);
			efactory.setRdbmsTripleRepository(s);
			efactory.setIdSequence(ids);
			conn.setRdbmsEvaluationFactory(efactory);
			GeneralDBQueryOptimizer optimizer = createOptimizer();
			GeneralDBSelectQueryOptimizerFactory selectOptimizerFactory = createSelectQueryOptimizerFactory();
			selectOptimizerFactory.setTransTableManager(trans);
			selectOptimizerFactory.setValueFactory(vf);
			selectOptimizerFactory.setIdSequence(ids);
			optimizer.setSelectQueryOptimizerFactory(selectOptimizerFactory);
			optimizer.setValueFactory(vf);
			optimizer.setBnodeTable(bnodeTable);
			optimizer.setUriTable(uriTable);
			optimizer.setLiteralTable(literalTable);
			optimizer.setHashTable(hashTable);
			conn.setRdbmsQueryOptimizer(optimizer);
			conn.setLockManager(lock);
			return conn;
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}
	
	@Override
	protected Connection getConnection()
			throws SQLException 
		{
			Connection conn;
			if (user == null)
				// return ds.getConnection();
				conn = ds.getConnection();
			else
				// return ds.getConnection(user, password);
				conn = ds.getConnection(user, password);
			
			Statement stmt = conn.createStatement();
		      stmt.setQueryTimeout(30); // set timeout to 30 sec.

		      // loading SpatiaLite
		      stmt.execute("SELECT load_extension('" +this.spatiaLite+"')");
		      try{
		      stmt.execute("SELECT load_extension('" +this.pcre+"')");
		      }catch(Exception e){
					logger.warn("Error loading regex library. Regular expressions will not be supported.");
					}

			return conn;
//			return new net.sf.log4jdbc.ConnectionSpy(conn);
		}

	/**
	 * FROM DUAL
	 * 
	 * @return from clause or empty string
	 */
	@Override
	protected String getFromDummyTable() {
		return " ";
	}
	
	@Override
	public void init()
			throws Exception
		{
			databaseLock = createDatabaseLock();
			try {
				
			//	nsAndTableIndexes = getConnection();
			//	resourceInserts = getConnection();
			//	literalInserts = getConnection();
			//	nsAndTableIndexes.setAutoCommit(true);
			//	resourceInserts.setAutoCommit(true);
			//	literalInserts.setAutoCommit(true);
				singleSQLiteCon=getConnection();
				singleSQLiteCon.setAutoCommit(true);
				bnodeManager = new BNodeManager();
				uriManager = new UriManager();
				literalManager = new LiteralManager();
				ValueTableFactory tables = createValueTableFactory();
				tables.setSequenced(sequenced);
				if (sequenced) {
					ids = new IntegerIdSequence();
					tables.setIdSequence(ids);
					//hashLookups = getConnection();
					//hashLookups.setAutoCommit(true);
					hashManager = new HashManager();
					hashTable = tables.createHashTable(singleSQLiteCon, hashManager.getQueue());
					ids.setHashTable(hashTable);
					ids.init();
					hashManager.setHashTable(hashTable);
					hashManager.setBNodeManager(bnodeManager);
					hashManager.setLiteralManager(literalManager);
					hashManager.setUriManager(uriManager);
					hashManager.setIdSequence(ids);
					hashManager.init();
					//hashLookups.close();
				} else {
					ids = new LongIdSequence();
					ids.init();
					tables.setIdSequence(ids);
				}
				namespaces = new NamespaceManager();
				namespaces.setConnection(singleSQLiteCon);
				NamespacesTable nsTable = tables.createNamespacesTable(singleSQLiteCon);
				nsTable.initialize();
				namespaces.setNamespacesTable(nsTable);
				namespaces.initialize();
				//nsAndTableIndexes.close();
				bnodeManager.setHashManager(hashManager);
				bnodeManager.setIdSequence(ids);
				uriManager.setHashManager(hashManager);
				uriManager.setIdSequence(ids);
				bnodeTable = tables.createBNodeTable(singleSQLiteCon, bnodeManager.getQueue());
				uriTable = tables.createURITable(singleSQLiteCon, uriManager.getQueue());
				literalManager.setHashManager(hashManager);
				literalManager.setIdSequence(ids);
				//resourceInserts.close();
				literalTable = tables.createLiteralTable(singleSQLiteCon, literalManager.getQueue());
				literalTable.setIdSequence(ids);
				vf = new GeneralDBValueFactory();
				vf.setDelegate(ValueFactoryImpl.getInstance());
				vf.setIdSequence(ids);
				uriManager.setUriTable(uriTable);
				uriManager.init();
				predicateManager = new PredicateManager();
				predicateManager.setUriManager(uriManager);
				tripleTableManager = (TripleTableManager) new SqliteTripleTableManager(tables);
				//nsAndTableIndexes = getConnection();
				tripleTableManager.setConnection(singleSQLiteCon);
				tripleTableManager.setIdSequence(ids);
				tripleTableManager.setBNodeManager(bnodeManager);
				tripleTableManager.setUriManager(uriManager);
				tripleTableManager.setLiteralManager(literalManager);
				tripleTableManager.setHashManager(hashManager);
				tripleTableManager.setPredicateManager(predicateManager);
				tripleTableManager.setMaxNumberOfTripleTables(maxTripleTables);
				tripleTableManager.setIndexingTriples(triplesIndexed);
				tripleTableManager.initialize();
				if (triplesIndexed) {
					tripleTableManager.createTripleIndexes();
				} else {
					tripleTableManager.dropTripleIndexes();
				}
				//nsAndTableIndexes.close();
				bnodeManager.setTable(bnodeTable);
				bnodeManager.init();
				vf.setBNodeManager(bnodeManager);
				vf.setURIManager(uriManager);
				literalManager.setTable(literalTable);
				literalManager.init();
				vf.setLiteralManager(literalManager);
				vf.setPredicateManager(predicateManager);
				//literalInserts.close();
			} catch (SQLException e) {
				throw new RdbmsException(e);
			}
		}
	
	@Override
	public void shutDown() throws SailException {
		try {
			if (tripleTableManager != null) {
				tripleTableManager.close();
			}
			if (uriManager != null) {
				uriManager.close();
			}
			if (bnodeManager != null) {
				bnodeManager.close();
			}
			if (literalManager != null) {
				literalManager.close();
			}
			if (hashManager != null) {
				hashManager.close();
			}
			if (resourceInserts != null) {
				resourceInserts.close();
				resourceInserts = null;
			}
			if (singleSQLiteCon != null) {
				singleSQLiteCon.close();
				singleSQLiteCon = null;
			}
		} catch (SQLException e) {
			throw new RdbmsException(e);
		} finally {
			if (databaseLock != null) {
				databaseLock.release();
			}
		}
	}
	public void setSpatiaLite(String spatiaLite) {
		this.spatiaLite = spatiaLite;
	}

	public void setPcre(String pcre) {
		this.pcre = pcre;
	}
}
