/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2013, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.runtime.generaldb;

import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.sail.SailConnection;

/**
 * This class extends {@link SailRepositoryConnection} only to allow
 * for overriding insertion of triples by invoking our implementation
 * of {@link RDFInserter} so that GeoSPARQL Entailment Extension is 
 * incorporated there in a seamless way.  
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class GeneralDBSailRepositoryConnection extends SailRepositoryConnection {

	protected GeneralDBSailRepositoryConnection(SailRepository repository, SailConnection sailConnection) {
		super(repository, sailConnection);
	}
	
	// TODO add the respective add method that will call an extension of the RDFInserter

}
