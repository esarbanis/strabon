package org.openrdf.query.algebra.evaluation.function.temporal.stsparql.relation;

import eu.earthobservatory.constants.TemporalConstants;

public class startsFunc extends TemporalRelationFunc {
	
    
	@Override
	public String getURI() {
	
		return TemporalConstants.starts;
	}


}
