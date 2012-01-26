/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.postgis;

import org.openrdf.sail.generaldb.GeneralDBProvider;


 
/**
 * Checks the database product name and version to be compatible with this
 * Sesame store.
 * 
 * @author James Leigh
 * 
 */
public class PostGISProvider extends GeneralDBProvider {

	public PostGISConnectionFactory createRdbmsConnectionFactory(String dbName, String dbVersion) {
		if ("PostGIS".equalsIgnoreCase(dbName))
			return new PostGISConnectionFactory();
		return null;
	}

}
