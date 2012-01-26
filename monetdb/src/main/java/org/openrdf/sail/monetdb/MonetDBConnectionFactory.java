/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.monetdb;

import static java.sql.Connection.TRANSACTION_READ_COMMITTED;
import info.aduna.concurrent.locks.Lock;

import java.sql.Connection;
import java.sql.SQLException;

import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.generaldb.GeneralDBConnection;
import org.openrdf.sail.generaldb.GeneralDBConnectionFactory;
import org.openrdf.sail.generaldb.GeneralDBTripleRepository;
import org.openrdf.sail.generaldb.evaluation.GeneralDBEvaluationFactory;
import org.openrdf.sail.generaldb.evaluation.GeneralDBQueryBuilderFactory;
import org.openrdf.sail.generaldb.optimizers.GeneralDBQueryOptimizer;
import org.openrdf.sail.generaldb.optimizers.GeneralDBSelectQueryOptimizerFactory;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.monetdb.evaluation.MonetDBEvaluationFactory;
import org.openrdf.sail.monetdb.evaluation.MonetDBQueryBuilderFactory;
import org.openrdf.sail.monetdb.util.MonetDBLockManager;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.generaldb.managers.TransTableManager;
import org.openrdf.sail.generaldb.managers.TripleManager;
import org.openrdf.sail.rdbms.schema.TableFactory;
import org.openrdf.sail.generaldb.schema.ValueTableFactory;

/**
 * Responsible to initialise and wire all components together that will be
 * needed to satisfy any sail connection request.
 * 
 * @author James Leigh
 */
public class MonetDBConnectionFactory extends GeneralDBConnectionFactory {

	@Override
	protected TableFactory createTableFactory() {
		return new MonetDBSqlTableFactory();
	}
	
	@Override
	protected ValueTableFactory createValueTableFactory() {
		return new MonetDBSqlValueTableFactory();
	}
	
	@Override
	protected Lock createDatabaseLock()
		throws SailException
	{
		MonetDBLockManager manager;
		manager = new MonetDBLockManager(ds, user, password);
		if (manager.isDebugEnabled())
			return manager.tryLock();
		return manager.lockOrFail();
	}

	@Override
	protected GeneralDBQueryBuilderFactory createQueryBuilderFactory() {
		return new MonetDBQueryBuilderFactory();
	}
	
	@Override
	public SailConnection createConnection()
	throws SailException
	{
		try {
			Connection db = getConnection();
			db.setAutoCommit(true);
			if (db.getTransactionIsolation() != TRANSACTION_READ_COMMITTED) {
				db.setTransactionIsolation(TRANSACTION_READ_COMMITTED);
			}
			TripleManager tripleManager = new TripleManager();
			GeneralDBTripleRepository s = new MonetDBTripleRepository();
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
			GeneralDBEvaluationFactory efactory = new MonetDBEvaluationFactory();
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

	
	/**
	 * FROM DUAL
	 * 
	 * @return from clause or empty string
	 */
	@Override
	protected String getFromDummyTable() {
		return " FROM sys.uri_values ";
	}
}
