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

import net.sf.jtemporal.Instant;

/**
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 *
 */
public class StrabonInstant implements Instant{

	/* (non-Javadoc)
	 * @see net.sf.jtemporal.Instant#compareTo(java.lang.Object)
	 */
	
	private final GregorianCalendar value;

	public GregorianCalendar getValue() {
		return value;
	}



	public StrabonInstant(GregorianCalendar value) throws ParseException{
		this.value = value;
	}
	@Override
	public int compareTo(Object arg0) {
		return this.value.compareTo((GregorianCalendar)arg0);
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

}
