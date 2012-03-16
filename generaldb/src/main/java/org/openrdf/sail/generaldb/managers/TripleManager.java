/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.managers;

import java.sql.SQLException;
import java.sql.Timestamp;

import org.openrdf.generaldb.managers.base.ManagerBase;
import org.openrdf.sail.generaldb.managers.TransTableManager;
/**
 * 
 * @author James Leigh
 */
public class TripleManager extends ManagerBase {

	public static TripleManager instance;

	private TransTableManager statements;

	public TripleManager() {
		instance = this;
	}

	public void setTransTableManager(TransTableManager statements) {
		this.statements = statements;
	}

	@Override
	public void close()
		throws SQLException
	{
		super.close();
		statements.close();
	}

	//FIXME 2 last arguments used to accommodate need for temporal
	public void insert(Number ctx, Number subj, Number pred, Number obj)//,Timestamp intervalStart, Timestamp intervalEnd)
		throws SQLException, InterruptedException
	{
		statements.insert(ctx, subj, pred, obj);//,intervalStart,intervalEnd);
//		System.err.println(subj+", "+pred+", "+obj);
	}

}
