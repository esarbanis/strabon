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

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.algebra.evaluation.function.temporal.stsparql.relation.TemporalConstants;

/**
 * @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 *
 */
public abstract class StrabonTemporalElement implements Value{
	
	protected URI datatype;

	public URI getDatatype() {
		return datatype;
	}

	
	abstract public String stringValue();
	abstract public void setDatatype(URI datatype);
	abstract public Literal export2Literal();

}
