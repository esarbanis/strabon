/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb;

import org.openrdf.sail.rdbms.RdbmsProvider;

 
/**
 * Checks the database product name and version to be compatible with this
 * Sesame store.
 * 
 * @author James Leigh
 * 
 */
public abstract class GeneralDBProvider  {

	public abstract GeneralDBConnectionFactory createRdbmsConnectionFactory(String dbName, String dbVersion);

}
