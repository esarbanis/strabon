/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2012, 2013, Pyravlos Team
 *
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.constants;


import eu.earthobservatory.constants.GeoConstants;

/**
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 *
 */
public class TemporalConstants extends GeoConstants {
	
	public static final String PERIOD                = stRDF + "period";
	
	public static final String INSTANT = "http://www.w3.org/2001/XMLSchema#dateTime";
	
	public static final String UNTIL_CHANGED = "UC";
	public static final String UNTIL_CHANGED_VALUE = "9999-9-9:00:00:00";
	
	
	//Temporal Relationships
	public static final String periodContains= stRDF+ "PeriodContains";

	public static final String during= stRDF+ "during";

	public static final String periodOverlaps= stRDF+ "PeriodOverlaps";
	
	public static final String periodIntersects= stRDF+ "PeriodIntersects";

	public static final String equalsPeriod= stRDF+ "equalsPeriod";

	public static final String nequalsPeriod= stRDF+ "nequalsPeriod";

	public static final String adjacent= stRDF+ "adjacent";

	public static final String before= stRDF+ "before";

	public static final String after=stRDF+ "after";

	public static final String overleft=stRDF+ "overleft";

	public static final String overright=stRDF+ "overright";
	
	public static final String meetsBefore=stRDF+ "meetsBefore";
	public static final String meetsAfter=stRDF+ "meetsAfter";

	public static final String meets=stRDF+ "meets";
	public static final String starts= stRDF+ "starts";
	public static final String finishes= stRDF +"finishes";
	
	//Temporal Constructs
	public static final String periodIntersection=stRDF+ "period_intersect";
	public static final String periodUnion=stRDF+ "period_union";
	public static final String minusPeriod=stRDF+ "period_minus";
	public static final String precedingPeriod=stRDF+ "preceding_period";
	public static final String succedingPeriod=stRDF+ "succeeding_period";
	public static final String start=stRDF+ "period_start";
	public static final String end=stRDF+ "period_end";
}
