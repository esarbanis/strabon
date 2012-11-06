/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2012, Pyravlos Team
 *
 * http://www.strabon.di.uoa.gr/
 */
package org.openrdf.query.algebra.evaluation.function.temporal.stsparql.relation;

/**
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 *
 */
public class AdjacentPeriodFunc extends TemporalRelationFunc {
	
    
	@Override
	public String getURI() {
	
		return TemporalConstants.adjacentPeriod;
	}

	//there is no operator assigned to this function
	@Override
	public String getOperator() {
		return null;
	}

	
	@Override
	public String getPostgresFunction() {
		return "adjacent";
	}

}