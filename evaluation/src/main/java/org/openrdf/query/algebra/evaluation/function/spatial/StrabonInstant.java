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
import java.util.GregorianCalendar;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.algebra.evaluation.function.temporal.stsparql.relation.TemporalConstants;

import net.sf.jtemporal.Instant;
import net.sf.jtemporal.Period;

/**
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 *
 */
public class StrabonInstant extends StrabonTemporalElement implements Instant{

	/* (non-Javadoc)
	 * @see net.sf.jtemporal.Instant#compareTo(java.lang.Object)
	 */
	
	private final GregorianCalendar value;

	public GregorianCalendar getValue() {
		return value;
	}

	public String stringValue(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD'T'HH:mm:ss");
		return sdf.format(this.value.getTime()).toString();
	}
	public static StrabonInstant read(String instant) throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD'T'HH:mm:ss");
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(sdf.parse(instant.substring(instant.indexOf('"')+1,instant.lastIndexOf('"') )));
		return new StrabonInstant(cal);
	}

	public StrabonInstant(GregorianCalendar value) throws ParseException{
		this.value = value;
	}
	@Override
	public int compareTo(Object arg0) 
	{
		if(arg0 instanceof StrabonInstant)
			return this.value.compareTo(((StrabonInstant)arg0).getValue());
		return 0;
	}

	/* (non-Javadoc)
	 * @see net.sf.jtemporal.Instant#isNegativeInfinity()
	 */
	@Override
	public boolean isNegativeInfinity() {		
		// TODO Fix NegativeInfinity
		return false;
	}

	/* (non-Javadoc)
	 * @see net.sf.jtemporal.Instant#isPositiveInfinity()
	 */
	@Override
	public boolean isPositiveInfinity() {
		// TODO FIXME PositiveInfinity
		return false;
	}

	/* (non-Javadoc)
	 * @see org.openrdf.query.algebra.evaluation.function.spatial.StrabonTemporalElement#setDatatype(org.openrdf.model.URI)
	 */
	@Override
	public void setDatatype(URI datatype) {
		
		this.setDatatype(new URIImpl(TemporalConstants.INSTANT));
	}

}
