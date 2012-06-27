/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.util;

import java.util.Comparator;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.spatial.GeoConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author james
 */
public class StSPARQLOrderComparator implements Comparator<BindingSet> {

	private final Logger logger = LoggerFactory.getLogger(StSPARQLOrderComparator.class);

	private final EvaluationStrategy strategy;

	private final Order order;

	private final Comparator cmp;

	public StSPARQLOrderComparator(EvaluationStrategy strategy, Order order, Comparator vcmp) {
		this.strategy = strategy;
		this.order = order;
		this.cmp = vcmp;
	}

	public int compare(BindingSet o1, BindingSet o2) {
		try {
			for (OrderElem element : order.getElements()) {
				//Flag used to denote a binding brought to compare two Polyhedra will be used
				boolean mbbFlag = false;

				Value v1;
				Value v2;
				if(element.getExpr() instanceof FunctionCall)
				{
					FunctionCall fc = (FunctionCall) element.getExpr();
					if(fc.getURI().equals(GeoConstants.envelope) && fc.getArgs().size()==2)
					{
						mbbFlag = true;
						FunctionCall expr = (FunctionCall) element.getExpr();
						//I know it is a var cause I 'planted' it earlier
						Var lastArg = (Var) fc.getArgs().get(1);
						String bindingName = lastArg.getName();

						v1 = o1.getValue(bindingName);
						v2 = o2.getValue(bindingName);
						//XXX unfinished
						int compare = cmp.compare(v1, v2);

						if (compare != 0) {
							return element.isAscending() ? compare : -compare;
						}
					}
				}

				if(!mbbFlag)
				{
					v1 = evaluate(element.getExpr(), o1);
					v2 = evaluate(element.getExpr(), o2);


					int compare = cmp.compare(v1, v2);

					if (compare != 0) {
						return element.isAscending() ? compare : -compare;
					}
				}
			}
			return 0;
		}
		catch (QueryEvaluationException e) {
			logger.error(e.getMessage(), e);
			return 0;
		}
		catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
			return 0;
		}
	}

	private Value evaluate(ValueExpr valueExpr, BindingSet o)
	throws QueryEvaluationException
	{
		try {
			return strategy.evaluate(valueExpr, o);
		}
		catch (ValueExprEvaluationException exc) {
			return null;
		}
	}
}
