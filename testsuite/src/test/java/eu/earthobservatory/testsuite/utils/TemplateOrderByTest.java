/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, 2013 Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.testsuite.utils;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * 4) checks if the results of the query are the expected considering ORDER of results
 * 	  If you don't explicitly use ORDER BY do NOT use this class. Use instead {@link TemplateTest}
 * @author Dimitrianos Savva <dimis@di.uoa.gr>
 */
public abstract class TemplateOrderByTest extends TemplateTest
{	
	@Override
	@Test
	public void test() throws Exception
	{
		Iterator<String> queryFileIterator = queryFile.iterator();
		Iterator<String> resultsFileIterator = resultsFile.iterator();
		
		while(queryFileIterator.hasNext() && resultsFileIterator.hasNext())
		{
			boolean take_into_account_order=true;
			Utils.testQuery(queryFileIterator.next(), resultsFileIterator.next(),take_into_account_order);
		}
	}
}
