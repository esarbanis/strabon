/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 * http://www.strabon.di.uoa.gr/
 */

package org.openrdf.query.algebra.evaluation.function.spatial;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import net.sf.jtemporal.Instant;
import net.sf.jtemporal.Period;

import org.openrdf.model.Value;


/**
 * This class provides a java implementation of a set of temporal functinos using the JTemporal library. 
 * This implementation is respective to the Postgresql Temporal implementation of these functions
 * 
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 *
 */
public class StrabonPeriod implements Value {
	
	private Period period;

	public StrabonPeriod()
	{
		this.period = null;
	}
	public StrabonPeriod(String period) throws ParseException
	{
		int i = period.indexOf('[');
		int j = period.indexOf(')');
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
		GregorianCalendar startCal = new GregorianCalendar();
		GregorianCalendar endCal = new GregorianCalendar();
		startCal.setTime(sdf.parse(period.substring(period.indexOf('[')+1,period.indexOf(',') )));
		endCal.setTime(sdf.parse(period.substring(period.indexOf(',')+1,period.indexOf(')') )));
		Instant start = new StrabonInstant(startCal);
		Instant end = new StrabonInstant(endCal);
		this.period = new Period(start, end);
	}
	public Period getPeriod() {
		return period;
	}
	public Instant getStart()
	{
		return period.getStart();
	}
	public Instant getEnd()
	{
		return period.getStart();
	}
	
	public void setPeriod(Period period) {
		this.period = period;
	}

	@Override
	public String stringValue() 
	{
		// TODO Auto-generated method stub
		return period.toString();
	}

	public static StrabonPeriod union(StrabonPeriod A, StrabonPeriod B)
	{
		StrabonPeriod period = new StrabonPeriod();
		period.setPeriod( A.getPeriod().union(B.getPeriod()));
		return period;
	}
	public static StrabonPeriod intersection(StrabonPeriod A, StrabonPeriod B)
	{
		StrabonPeriod period = new StrabonPeriod();
		period.setPeriod( A.getPeriod().intersect(B.getPeriod()));
		return period;
	}
	public static StrabonPeriod except(StrabonPeriod A, StrabonPeriod B)
	{
		StrabonPeriod period = new StrabonPeriod();
		period.setPeriod( A.getPeriod().except(B.getPeriod()));
		return period;
	}
	public static StrabonPeriod precedingPeriod(StrabonPeriod A, StrabonPeriod B)
	{
		StrabonPeriod period = new StrabonPeriod();
		period.setPeriod( A.getPeriod().precedingPeriod(B.getPeriod()));
		return period;
	}
	public static StrabonPeriod succedingPeriod(StrabonPeriod A, StrabonPeriod B)
	{
		StrabonPeriod period = new StrabonPeriod();
		period.setPeriod( A.getPeriod().succedingPeriod(B.getPeriod()));
		return period;
	}
	public static int compareTo(StrabonPeriod A, StrabonPeriod B)
	{
		return A.getPeriod().compareTo(B.getPeriod());
	}
	public static boolean contains(StrabonPeriod A, Instant B)
	{
		return A.getPeriod().contains(B);
	}
	public static boolean contains(StrabonPeriod A, StrabonPeriod B)
	{
		return A.getPeriod().contains(B.getPeriod());
	}
	public static boolean equals(StrabonPeriod A, StrabonPeriod B)
	{
		return A.getPeriod().equals(B.getPeriod());
	}
	public static boolean meets(StrabonPeriod A, StrabonPeriod B)
	{
		return A.getPeriod().meets(B.getPeriod());
	}
	public static boolean meetsAfter(StrabonPeriod A, StrabonPeriod B)
	{
		return A.getPeriod().meetsAfter(B.getPeriod());
	}
	public static boolean meetsBefore(StrabonPeriod A, StrabonPeriod B)
	{
		return A.getPeriod().meetsBefore(B.getPeriod());
	}
	public static boolean overlaps(StrabonPeriod A, StrabonPeriod B)
	{
		return A.getPeriod().overlaps(B.getPeriod());
	}
	public static boolean precedes(StrabonPeriod A, Instant B)
	{
		return A.getPeriod().precedes(B);
	}
	public static boolean precedes(StrabonPeriod A, StrabonPeriod B)
	{
		return A.getPeriod().precedes(B.getPeriod());
	}
	public static boolean succedes(StrabonPeriod A, StrabonPeriod B)
	{
		return A.getPeriod().succeeds(B.getPeriod());
	}
	
}
