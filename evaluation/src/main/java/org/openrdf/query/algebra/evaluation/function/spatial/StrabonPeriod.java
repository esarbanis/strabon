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

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.algebra.evaluation.function.temporal.stsparql.relation.TemporalConstants;


/**
 * This class provides a java implementation of a set of temporal functions using the JTemporal library. 
 * This implementation is respective to the Postgresql Temporal implementation of these functions
 * 
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 *
 */
public class StrabonPeriod extends StrabonTemporalElement implements Value {
	
	private Period period;

	public StrabonPeriod()
	{
		this.period = null;
		this.datatype = new URIImpl(TemporalConstants.PERIOD);
	}
	public StrabonPeriod(String period) throws ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD'T'HH:mm:ss");
		if(period.contains(","))
		{
			GregorianCalendar startCal = new GregorianCalendar();
			GregorianCalendar endCal = new GregorianCalendar();
			startCal.setTime(sdf.parse(period.substring(period.indexOf('[')+1,period.indexOf(',') )));
			endCal.setTime(sdf.parse(period.substring(period.indexOf(',')+1,period.indexOf(')') )));
			StrabonInstant start = new StrabonInstant(startCal);
			StrabonInstant end = new StrabonInstant(endCal);
			this.datatype = new URIImpl(TemporalConstants.PERIOD);
			this.period = new Period(start, end);

		}
	
	}
	
	public StrabonPeriod(String period1, String period2) throws ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD'T'HH:mm:ss");
	
			GregorianCalendar startCal = new GregorianCalendar();
			GregorianCalendar endCal = new GregorianCalendar();
			startCal.setTime(sdf.parse(period1.substring(period1.indexOf('"')+1,period1.lastIndexOf('"') )));
			endCal.setTime(sdf.parse(period2.substring(period2.indexOf('"')+1,period2.lastIndexOf('"') )));
			StrabonInstant start = new StrabonInstant(startCal);
			StrabonInstant end = new StrabonInstant(endCal);
			this.period = new Period(start, end);
			this.datatype = new URIImpl(TemporalConstants.PERIOD);

	}
	
	public void setDatatype(URI datatype) {
		
		this.setDatatype(datatype);
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
		if(this.period == null)
			return null;
		return period.toString().replace("Period:(","[");
	}

	public static StrabonPeriod union(StrabonTemporalElement A, StrabonTemporalElement B)
	{
		if(A instanceof StrabonPeriod && B instanceof StrabonPeriod)
		{
			if(((StrabonPeriod)A).getPeriod().toString().equals(((StrabonPeriod)B).getPeriod().toString()))
			{
				return (StrabonPeriod)A;
			}
			if(((StrabonPeriod)A).getPeriod().contains(((StrabonPeriod)B).getPeriod()))
			{
				return (StrabonPeriod)A;
			}
			if(((StrabonPeriod)B).getPeriod().contains(((StrabonPeriod)A).getPeriod()))
			{
				return (StrabonPeriod)B;
			}
			StrabonPeriod period = new StrabonPeriod();
			period.setPeriod( ((StrabonPeriod)A).getPeriod().union(((StrabonPeriod)B).getPeriod()));
			return period;
			
		}
		else if(A instanceof StrabonPeriod && B instanceof StrabonInstant)
		{
		if(((StrabonPeriod) A).contains((StrabonPeriod)A ,B ))
		{
			return (StrabonPeriod) A;
		}
		else
			return null;
		}
		
		else if(B instanceof StrabonPeriod && A instanceof StrabonInstant)
		{
			if(((StrabonPeriod) B).contains((StrabonPeriod)B ,A ))
			{
				return (StrabonPeriod) B;
			}
				else
					return null;
		}
		else
			return null;
	}
	public static StrabonTemporalElement intersection(StrabonTemporalElement A, StrabonTemporalElement B)
	{
		if(A instanceof StrabonPeriod && B instanceof StrabonPeriod)
		{
			if(((StrabonPeriod)A).getPeriod().equals(((StrabonPeriod)B).getPeriod()))
			{
				return (StrabonPeriod)A;
			}
			if(((StrabonPeriod)A).getPeriod().contains(((StrabonPeriod)B).getPeriod()))
			{
				return (StrabonPeriod)B;
			}
			if(((StrabonPeriod)B).getPeriod().contains(((StrabonPeriod)A).getPeriod()))
			{
				return (StrabonPeriod)A;
			}
			StrabonPeriod period = new StrabonPeriod();
			period.setPeriod( ((StrabonPeriod)A).getPeriod().intersect(((StrabonPeriod)B).getPeriod()));
			return period;
			
		}
		else if(A instanceof StrabonPeriod && B instanceof StrabonInstant)
		{
		if(((StrabonPeriod) A).contains((StrabonPeriod)A ,B ))
		{
			return (StrabonInstant) B;
		}
		else
			return null;
		}
		
		else if(B instanceof StrabonPeriod && A instanceof StrabonInstant)
		{
			if(((StrabonPeriod) B).contains((StrabonPeriod)B ,A ))
			{
				return (StrabonInstant)A;
			}
				else
					return null;
		}
		else
			return null;
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
	public static boolean contains(StrabonPeriod A, StrabonTemporalElement B)
	{
		if(B instanceof StrabonInstant)
			return A.getPeriod().contains((StrabonInstant)B);
		else 
			return A.getPeriod().contains(((StrabonPeriod)B).getPeriod());
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
	public static boolean precedes(StrabonPeriod A, StrabonTemporalElement B)
	{
		if(B instanceof StrabonInstant)
			return A.getPeriod().precedes((StrabonInstant)B);
		else
			return A.getPeriod().precedes(((StrabonPeriod)B).getPeriod());
	}
	public static boolean succedes(StrabonPeriod A, StrabonTemporalElement B)
	{
		if(B instanceof StrabonPeriod)
			return A.getPeriod().succeeds(((StrabonPeriod)B).getPeriod());
		else
			return A.getPeriod().succeeds((StrabonInstant)B);
	}
	
}
