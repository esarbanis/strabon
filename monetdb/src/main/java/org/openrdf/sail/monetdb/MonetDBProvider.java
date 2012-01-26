/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.monetdb;

import org.openrdf.sail.generaldb.GeneralDBProvider;


 
/**
 * Checks the database product name and version to be compatible with this
 * Sesame store.
 * 
 * @author James Leigh
 * 
 */
public class MonetDBProvider extends GeneralDBProvider {

	public MonetDBConnectionFactory createRdbmsConnectionFactory(String dbName, String dbVersion) {
		if ("MonetDB".equalsIgnoreCase(dbName))
			return new MonetDBConnectionFactory();
		return null;
	}

}
