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

import eu.earthobservatory.constants.TemporalConstants;

/**
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 *
 */
public class OverrightPeriodFunc extends TemporalRelationFunc {
	
    
	@Override
	public String getURI() {
	
		return TemporalConstants.overright;
	}

	

}
