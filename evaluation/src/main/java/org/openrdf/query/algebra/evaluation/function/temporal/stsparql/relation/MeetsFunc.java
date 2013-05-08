package org.openrdf.query.algebra.evaluation.function.temporal.stsparql.relation;

import eu.earthobservatory.constants.TemporalConstants;

public class MeetsFunc extends TemporalRelationFunc {
	
    
	@Override
	public String getURI() {
	
		return TemporalConstants.meets;
	}


}
