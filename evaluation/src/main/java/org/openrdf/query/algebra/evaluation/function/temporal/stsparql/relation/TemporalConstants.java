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

import org.openrdf.query.algebra.evaluation.function.spatial.GeoConstants;

/**
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 *
 */
public class TemporalConstants extends GeoConstants {
	
	public static final String PERIOD                = stRDF + "period";
	
	public static final String INSTANT = "http://www.w3.org/2001/XMLSchema#dateTime";
	
	//Temporal Relationships
	public static final String periodContains= stRDF+ "PeriodContains";

	public static final String during= stRDF+ "during";

	public static final String periodOverlaps= stRDF+ "PeriodOverlaps";

	public static final String equalsPeriod= stRDF+ "equalsPeriod";

	public static final String nequalsPeriod= stRDF+ "nequalsPeriod";

	public static final String adjacent= stRDF+ "adjacent";

	public static final String before= stRDF+ "beforePeriod";

	public static final String after=stRDF+ "after";

	public static final String overleft=stRDF+ "overleft";

	public static final String overright=stRDF+ "overright";

	public static final String meets=stRDF+ "meets";
	public static final String starts= stRDF+ "starts";
	public static final String finishes= stRDF +"finishes";
	
	//Temporal Constructs
	public static final String periodIntersection=stRDF+ "periodIntersection";
	public static final String periodUnion=stRDF+ "periodUnion";
	public static final String minusPeriod=stRDF+ "minusPeriod";

}
