/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.evaluation;

import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geoBoundary;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geoBuffer;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geoConvexHull;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geoDifference;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geoEnvelope;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geoIntersection;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geoSymDifference;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geoUnion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;

import org.opengis.filter.spatial.Disjoint;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Avg;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.GroupElem;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialConstructFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialPropertyFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialRelationshipFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.nontopological.GeoSparqlBoundaryFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.nontopological.GeoSparqlConvexHullFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.nontopological.GeoSparqlEnvelopeFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.BoundaryFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.ConvexHullFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.EnvelopeFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.AboveFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.AnyInteractFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.BelowFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.ContainsFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.CoveredByFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.CoversFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.DisjointFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.EqualsFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.InsideFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.LeftFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.OverlapFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.RightFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.TouchFunc;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.algebra.evaluation.iterator.ExtensionIterator;
import org.openrdf.query.algebra.evaluation.iterator.StSPARQLGroupIterator;
import org.openrdf.query.algebra.evaluation.iterator.OrderIterator;
import org.openrdf.query.algebra.evaluation.util.StSPARQLOrderComparator;
import org.openrdf.sail.generaldb.util.StSPARQLValueComparator;
import org.openrdf.sail.generaldb.GeneralDBSpatialFuncInfo;
import org.openrdf.sail.generaldb.GeneralDBSpatialFuncInfo.typeOfField;
import org.openrdf.sail.generaldb.GeneralDBTripleRepository;
import org.openrdf.sail.generaldb.GeneralDBValueFactory;
import org.openrdf.sail.generaldb.algebra.GeneralDBColumnVar;
import org.openrdf.sail.generaldb.algebra.GeneralDBLabelColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBLongLabelColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBNumericColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBSelectProjection;
import org.openrdf.sail.generaldb.algebra.GeneralDBSelectQuery;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlCase;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlDisjoint;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoAsText;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoDimension;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoGeometryType;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoIsEmpty;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoIsSimple;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlGeoSrid;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlMathExpr;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlIsNull;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlNot;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialConstructUnary;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialMetricBinary;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialMetricUnary;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialProperty;
import org.openrdf.sail.generaldb.algebra.GeneralDBSelectQuery.OrderElem;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlSpatialConstructBinary;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.generaldb.model.GeneralDBPolyhedron;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.exceptions.RdbmsQueryEvaluationException;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;
import org.openrdf.sail.rdbms.model.RdbmsLiteral;
import org.openrdf.sail.generaldb.schema.IdSequence;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Extends the default strategy by accepting {@link GeneralDBSelectQuery} and evaluating
 * them on a database.
 * 
 * @author James Leigh
 * 
 */
public abstract class GeneralDBEvaluation extends EvaluationStrategyImpl {

	public Logger logger;

	protected GeneralDBQueryBuilderFactory factory;

	protected GeneralDBValueFactory vf;

	protected GeneralDBTripleRepository triples;

	protected IdSequence ids;

	protected HashMap<Integer,String> geoNames = new HashMap<Integer,String>();

	//used to retrieve the appropriate column in the Binding Iteration
	protected HashMap<GeneralDBSpatialFuncInfo, Integer> constructIndexesAndNames = new HashMap<GeneralDBSpatialFuncInfo, Integer>();
	//private HashMap<String, Integer> constructIndexesAndNames = new HashMap<String, Integer>();
	//	private HashMap<String, Integer> metricIndexesAndNames = new HashMap<String, Integer>();
	//	private HashMap<String, Integer> intPropertiesIndexesAndNames = new HashMap<String, Integer>();
	//	private HashMap<String, Integer> boolPropertiesIndexesAndNames = new HashMap<String, Integer>();
	//	private HashMap<String, Integer> stringPropertiesIndexesAndNames = new HashMap<String, Integer>();

	public GeneralDBEvaluation(GeneralDBQueryBuilderFactory factory, GeneralDBTripleRepository triples, Dataset dataset,
			IdSequence ids)
	{
		super(new GeneralDBTripleSource(triples), dataset);
		this.logger = LoggerFactory.getLogger(GeneralDBEvaluation.class);
		this.factory = factory;
		this.triples = triples;
		this.vf = triples.getValueFactory();
		this.ids = ids;
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(TupleExpr expr,
			BindingSet bindings)
	throws QueryEvaluationException
	{
		if (expr instanceof GeneralDBSelectQuery)
			return evaluate((GeneralDBSelectQuery)expr, bindings);
		else if (expr instanceof Group) {
			return evaluate((Group)expr, bindings);
		}
		else if (expr instanceof Order) {
			return evaluate((Order)expr, bindings);
		}
		return super.evaluate(expr, bindings);
	}
	@Override
	public Value evaluate(ValueExpr expr, BindingSet bindings)
			throws ValueExprEvaluationException, QueryEvaluationException
			{
		if (expr instanceof Var) {
			return evaluate((Var)expr, bindings);
		}
		else if (expr instanceof FunctionCall) {
			return evaluate((FunctionCall)expr, bindings);
		}
		return super.evaluate(expr, bindings);
			}

	/**
	 * Had to use it for the cases met in group by (Union as an aggregate)
	 */
	@Override
	public Value evaluate(Var var, BindingSet bindings) throws ValueExprEvaluationException, QueryEvaluationException
	{
		boolean groupBy = false;
		//Case met when evaluating a construct function inside an aggregate 
		if(var.getName().endsWith("?spatial"))
		{
			var.setName(var.getName().replace("?spatial",""));
		}

		if(var.getName().endsWith("?forGroupBy"))
		{
			var.setName(var.getName().replace("?forGroupBy",""));
			groupBy = true;
		}
		Value value = var.getValue();

		if (value == null) {
			value = bindings.getValue(var.getName());
		}

		if (value == null) {
			throw new ValueExprEvaluationException();
		}

		if(!groupBy)
		{
			return value;
		}
		else
		{
			GeneralDBPolyhedron poly = (GeneralDBPolyhedron) value;
			return poly.getPolyhedron();
		}
	}

	/**
	 * Had to use it for the cases met in group by (Union as an aggregate)
	 */
	@Override
	public Value evaluate(FunctionCall fc, BindingSet bindings)
	{
		System.out.println("FunctionCall placeholder");

		if(fc.getParentNode() instanceof Avg)
		{
			if(fc.getParentNode().getParentNode() instanceof GroupElem)
			{
				GroupElem original = (GroupElem) fc.getParentNode().getParentNode();
				Value val = bindings.getValue(original.getName());
				if(val!=null)
				{
					return val;
				}
			}
		}
		
		Function function = FunctionRegistry.getInstance().get(fc.getURI());

//		if(fc.getParentNode() instanceof Filter)
//		{
//			//Traditional Behavior!
//			try {
//				if (function == null) {
//					throw new QueryEvaluationException("Unknown function '" + fc.getURI() + "'");
//				}
//
//				List<ValueExpr> args = fc.getArgs();
//
//				Value[] argValues = new Value[args.size()];
//
//				for (int i = 0; i < args.size(); i++) {
//
//					argValues[i] = evaluate(args.get(i), bindings);
//
//				}
//
//				return function.evaluate(tripleSource.getValueFactory(), argValues);} catch (ValueExprEvaluationException e) {
//					e.printStackTrace();
//				} catch (QueryEvaluationException e) {
//					e.printStackTrace();
//				}
//		}
		ValueExpr left = fc.getArgs().get(0);


		Value leftResult = null;
		Value rightResult = null;

		try {
			leftResult = evaluate(left,bindings);
		} catch (ValueExprEvaluationException e) {
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}


		if(!(function instanceof EnvelopeFunc) 
				&& !(function instanceof ConvexHullFunc) 
				&& !(function instanceof BoundaryFunc))
		{
			ValueExpr right = fc.getArgs().get(1);
			try {
				rightResult = evaluate(right,bindings);
			} catch (ValueExprEvaluationException e) {
				e.printStackTrace();
			} catch (QueryEvaluationException e) {
				e.printStackTrace();
			}

		}
		try {
			if ( function instanceof SpatialConstructFunc ) 
				return spatialConstructPicker(function, leftResult, rightResult);
			//Any boolean function present in HAVING - Must evaluate here!
			else if(function instanceof SpatialRelationshipFunc)
			{
				boolean funcResult = false;
				//For the time being I only include stSPARQL ones
				Geometry leftGeom = null;
				Geometry rightGeom = null;
				if(leftResult instanceof StrabonPolyhedron)
				{
					leftGeom = ((StrabonPolyhedron) leftResult).getGeometry();
				}
				else if(leftResult instanceof GeneralDBPolyhedron)
				{
					leftGeom = ((GeneralDBPolyhedron) leftResult).getPolyhedron().getGeometry();
				}
				else
				{	//SHOULD NEVER REACH THIS CASE!
					return null;
				}
				
				if(rightResult instanceof StrabonPolyhedron)
				{
					rightGeom = ((StrabonPolyhedron) rightResult).getGeometry();
				}
				else if(rightResult instanceof GeneralDBPolyhedron)
				{
					rightGeom = ((GeneralDBPolyhedron) rightResult).getPolyhedron().getGeometry();
				}
				else
				{	//SHOULD NEVER REACH THIS CASE!
					return null;
				}
				
				
				if(function instanceof AboveFunc)
				{
					funcResult = leftGeom.getEnvelopeInternal().getMinY() > rightGeom.getEnvelopeInternal().getMaxY();
				}
				else if(function instanceof AnyInteractFunc)
				{
					funcResult = leftGeom.intersects(rightGeom);
				}
				else if(function instanceof BelowFunc)
				{
					funcResult = leftGeom.getEnvelopeInternal().getMaxY() < rightGeom.getEnvelopeInternal().getMinY();
				}
				else if(function instanceof ContainsFunc)
				{
					funcResult = leftGeom.contains(rightGeom);
				}
				else if(function instanceof CoveredByFunc)
				{
					funcResult = leftGeom.coveredBy(rightGeom);
				}
				else if(function instanceof CoversFunc)
				{
					funcResult = leftGeom.covers(rightGeom);
				}
				else if(function instanceof DisjointFunc)
				{
					funcResult = leftGeom.disjoint(rightGeom);
				}
				else if(function instanceof EqualsFunc)
				{
					funcResult = leftGeom.equals(rightGeom);
				}
				else if(function instanceof InsideFunc)
				{
					funcResult = leftGeom.within(rightGeom);
				}
				else if(function instanceof LeftFunc)
				{
					funcResult = leftGeom.getEnvelopeInternal().getMaxX() < rightGeom.getEnvelopeInternal().getMinX();
				}
				else if(function instanceof OverlapFunc)
				{
					funcResult = leftGeom.overlaps(rightGeom);
				}
				else if(function instanceof RightFunc)
				{
					funcResult = leftGeom.getEnvelopeInternal().getMinX() > rightGeom.getEnvelopeInternal().getMaxX();

				}
				else if(function instanceof TouchFunc)
				{
					funcResult = leftGeom.touches(rightGeom);
				}
				
				return funcResult ? BooleanLiteralImpl.TRUE : BooleanLiteralImpl.FALSE;
			}
			else {
				List<ValueExpr> args = fc.getArgs();

				Value[] argValues = new Value[args.size()];

				for (int i = 0; i < args.size(); i++) {
					argValues[i] = evaluate(args.get(i), bindings);
				}
				return function.evaluate(tripleSource.getValueFactory(), argValues);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public StrabonPolyhedron spatialConstructPicker(Function function, Value left, Value right) throws Exception
	{
		StrabonPolyhedron leftArg = ((GeneralDBPolyhedron) left).getPolyhedron();
		if(function.getURI().equals(StrabonPolyhedron.union))
		{
			StrabonPolyhedron rightArg = ((GeneralDBPolyhedron) right).getPolyhedron();

			return new StrabonPolyhedron(leftArg.getGeometry().union(rightArg.getGeometry()));
		}
		else if(function.getURI().equals(StrabonPolyhedron.buffer))
		{
			if(right instanceof LiteralImpl)
			{
				LiteralImpl radius = (LiteralImpl) right;
				return new StrabonPolyhedron(leftArg.getGeometry().buffer(radius.doubleValue()));
			}
			else if(right instanceof RdbmsLiteral)
			{
				RdbmsLiteral radius = (RdbmsLiteral) right;
				return new StrabonPolyhedron(leftArg.getGeometry().buffer(radius.doubleValue()));
			}

		}
		else if(function.getURI().equals(StrabonPolyhedron.envelope))
		{
			return new StrabonPolyhedron(leftArg.getGeometry().getEnvelope());
		}
		else if(function.getURI().equals(StrabonPolyhedron.convexHull))
		{
			return new StrabonPolyhedron(leftArg.getGeometry().convexHull());
		}
		else if(function.getURI().equals(StrabonPolyhedron.boundary))
		{
			return new StrabonPolyhedron(leftArg.getGeometry().getBoundary());
		}
		else if(function.getURI().equals(StrabonPolyhedron.intersection))
		{
			StrabonPolyhedron rightArg = ((GeneralDBPolyhedron) right).getPolyhedron();
			return new StrabonPolyhedron(leftArg.getGeometry().intersection(rightArg.getGeometry()));
		}
		else if(function.getURI().equals(StrabonPolyhedron.difference))
		{
			StrabonPolyhedron rightArg = ((GeneralDBPolyhedron) right).getPolyhedron();
			return new StrabonPolyhedron(leftArg.getGeometry().difference(rightArg.getGeometry()));
		}
		else if(function.getURI().equals(StrabonPolyhedron.symDifference))
		{
			StrabonPolyhedron rightArg = ((GeneralDBPolyhedron) right).getPolyhedron();
			return new StrabonPolyhedron(leftArg.getGeometry().symDifference(rightArg.getGeometry()));
		}
		return null;

	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Group node, BindingSet bindings)
			throws QueryEvaluationException
			{
//		Set<String> tmp1 = node.getAggregateBindingNames();
//		Set<String> tmp2 = node.getAssuredBindingNames();
//		Set<String> tmp3 = node.getBindingNames();
//		Set<String> tmp4 = node.getGroupBindingNames();
//		for(String tmp : tmp4)
//		{
//			//System.out.println(node.g);
//		}
		return new StSPARQLGroupIterator(this, node, bindings);
			}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Order node, BindingSet bindings)
			throws QueryEvaluationException
			{
		StSPARQLValueComparator vcmp = new StSPARQLValueComparator();
		StSPARQLOrderComparator cmp = new StSPARQLOrderComparator(this, node, vcmp);
		boolean reduced = isReduced(node);
		long limit = getLimit(node);
		return new OrderIterator(evaluate(node.getArg(), bindings), cmp, limit, reduced);
			}

	//Duplicated from EvaluationStrategyImpl
	private boolean isReduced(QueryModelNode node) {
		QueryModelNode parent = node.getParentNode();
		if (parent instanceof Slice) {
			return isReduced(parent);
		}
		return parent instanceof Distinct || parent instanceof Reduced;
	}

	//Duplicated from EvaluationStrategyImpl
	private long getLimit(QueryModelNode node) {
		long offset = 0;
		if (node instanceof Slice) {
			Slice slice = (Slice)node;
			if (slice.hasOffset() && slice.hasLimit()) {
				return slice.getOffset() + slice.getLimit();
			}
			else if (slice.hasLimit()) {
				return slice.getLimit();
			}
			else if (slice.hasOffset()) {
				offset = slice.getOffset();
			}
		}
		QueryModelNode parent = node.getParentNode();
		if (parent instanceof Distinct || parent instanceof Reduced || parent instanceof Slice) {
			long limit = getLimit(parent);
			if (offset > 0L && limit < Long.MAX_VALUE) {
				return offset + limit;
			}
			else {
				return limit;
			}
		}
		return Long.MAX_VALUE;
	}

	//XXX brought it here to override it somehow..
	//	@Override
	//	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Extension extension,
	//			BindingSet bindings)
	//		throws QueryEvaluationException
	//	{
	//		CloseableIteration<BindingSet, QueryEvaluationException> result;
	//		
	//		/**
	//		 * XXX additions
	//		 */
	//		Iterator<ExtensionElem> iter = extension.getElements().iterator();
	//		//for(ExtensionElem elem : extension.getElements())
	//		while(iter.hasNext())
	//		{
	//			ExtensionElem elem = iter.next();
	//			if(elem.getExpr() instanceof FunctionCall)
	//			{
	//				Function function = FunctionRegistry.getInstance().get(((FunctionCall) elem.getExpr()).getURI());
	//				if(function instanceof SpatialPropertyFunc || function instanceof SpatialRelationshipFunc || function instanceof SpatialConstructFunction)
	//				{
	//					System.out.println("stopper");
	//					spatialFunctionsInSelect.put(elem.getName(),elem.getExpr());
	//					iter.remove();
	//				}
	//			}
	//		}
	//		/**
	//		 * 	
	//		 */
	//		try {
	//			result = this.evaluate(extension.getArg(), bindings);
	//		}
	//		catch (ValueExprEvaluationException e) {
	//			// a type error in an extension argument should be silently ignored and
	//			// result in zero bindings.
	//			result = new EmptyIteration<BindingSet, QueryEvaluationException>();
	//		}
	//
	//		result = new ExtensionIterator(extension, result, this);
	//		return result;
	//	}

	protected abstract CloseableIteration<BindingSet, QueryEvaluationException> evaluate(GeneralDBSelectQuery qb, BindingSet b)
			throws UnsupportedRdbmsOperatorException, RdbmsQueryEvaluationException;

	protected String toQueryString(GeneralDBSelectQuery qb, QueryBindingSet bindings, List<Object> parameters)
			throws RdbmsException, UnsupportedRdbmsOperatorException
			{
		GeneralDBQueryBuilder query = factory.createQueryBuilder();
		if (qb.isDistinct()) {
			query.distinct();
		}
		query.from(qb.getFrom());
		for (GeneralDBColumnVar var : qb.getVars()) {
			for (String name : qb.getBindingNames(var)) {
				if (var.getValue() == null && bindings.hasBinding(name)) {
					query.filter(var, bindings.getValue(name));
				}
				else if (var.getValue() != null && !bindings.hasBinding(name)
						&& qb.getBindingNames().contains(name))
				{
					bindings.addBinding(name, var.getValue());
				}
			}
		}

		List<GeneralDBSelectProjection> projForOrderBy = new ArrayList<GeneralDBSelectProjection>();

		int index = 0;
		for (GeneralDBSelectProjection proj : qb.getSqlSelectVar()) {
			GeneralDBColumnVar var = proj.getVar();
			if (!var.isHiddenOrConstant()) {
				for (String name : qb.getBindingNames(var)) {
					if (!bindings.hasBinding(name)) {
						var.setIndex(index);
						//XXX if the variable is actually a GeoVar
						if(var.isSpatial())
						{
							this.geoNames.put(var.getIndex()+2,var.getName());
						}
						query.select(proj.getId());
						query.select(proj.getStringValue());
						index += 2;
						if (var.getTypes().isLiterals()) {
							//FIXME changed  to remove extra unneeded joins + selections
							//Original:
							//query.select(proj.getLanguage());
							//query.select(proj.getDatatype());
							//index += 2;

							//Altered:
							if(proj.getLanguage()!=null)
							{
								query.select(proj.getLanguage());
								index++;
							}
							if(proj.getDatatype()!=null)
							{
								query.select(proj.getDatatype());
								index++;
							}

						}
					}
				}
			}
		}



		//XXX Attention: Will try to add projections in select for the constructs
		Iterator it = qb.getSpatialConstructs().entrySet().iterator();
		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();
			//System.out.println(pairs.getKey() + " = " + pairs.getValue());

			//Trying to fill what's missing
			GeneralDBSqlExpr expr = (GeneralDBSqlExpr) pairs.getValue();
			locateColumnVars(expr,qb.getVars());

			query.construct(expr); 
			GeneralDBSpatialFuncInfo info = null;
			switch(constructReturnType(expr))
			{
			case 1:
				//Integer
				info = new GeneralDBSpatialFuncInfo((String) pairs.getKey(), typeOfField.Integer);
				break;
			case 2: 
				//String
				info = new GeneralDBSpatialFuncInfo((String) pairs.getKey(), typeOfField.String);
				break;
			case 3: 
				//Boolean
				info = new GeneralDBSpatialFuncInfo((String) pairs.getKey(), typeOfField.Boolean);
				break;
			case 4: 
				//WKB
				info = new GeneralDBSpatialFuncInfo((String) pairs.getKey(), typeOfField.WKB);
				break;
			case 5: 
				//DOUBLE
				info = new GeneralDBSpatialFuncInfo((String) pairs.getKey(), typeOfField.Double);
				break;
			default: throw new UnsupportedRdbmsOperatorException("No such spatial expression exists!");
			}

			//constructIndexesAndNames.put((String) pairs.getKey(),index++);
			constructIndexesAndNames.put(info,index++);
		}
		//

		for (OrderElem by : qb.getOrderElems()) {
			query.orderBy(by.sqlExpr, by.isAscending);
			if (qb.isDistinct()) {
				query.select(by.sqlExpr);
			}
		}
		if (qb.getLimit() != null) {
			query.limit(qb.getLimit());
		}
		if (qb.getOffset() != null) {
			query.offset(qb.getOffset());
		}
		parameters.addAll(query.getParameters());
		if (logger.isDebugEnabled()) {
			logger.debug(query.toString());
			logger.debug(parameters.toString());
		}
		return query.toString();
			}

	/**
	 * Function used to locate all ColumnVars from the select's spatial constructs so that they can later 
	 * be mapped if they have a missing Column Var
	 */
	private void locateColumnVars(GeneralDBSqlExpr expr, Collection<GeneralDBColumnVar> allKnown)
	{
		ArrayList<GeneralDBColumnVar> allVars = new ArrayList<GeneralDBColumnVar>();
		if(expr instanceof GeneralDBSqlSpatialProperty) //1 arg
		{

			//allVars.addAll(locateColumnVars(((GeneralDBSqlSpatialProperty)expr).getArg(),allKnown));
			locateColumnVars(((GeneralDBSqlSpatialProperty)expr).getArg(),allKnown);
		}
		else if(expr instanceof GeneralDBSqlSpatialConstructBinary)
		{
			//allVars.addAll(locateColumnVars(((GeneralDBSqlSpatialConstruct)expr).getLeftArg(),allKnown));
			//allVars.addAll(locateColumnVars(((GeneralDBSqlSpatialConstruct)expr).getRightArg(),allKnown));
			locateColumnVars(((GeneralDBSqlSpatialConstructBinary)expr).getLeftArg(),allKnown);
			locateColumnVars(((GeneralDBSqlSpatialConstructBinary)expr).getRightArg(),allKnown);
		}
		else if(expr instanceof GeneralDBSqlSpatialConstructUnary)
		{
			locateColumnVars(((GeneralDBSqlSpatialConstructUnary)expr).getArg(),allKnown);
		}
		else if(expr instanceof GeneralDBSqlSpatialMetricBinary)
		{
			locateColumnVars(((GeneralDBSqlSpatialMetricBinary)expr).getLeftArg(),allKnown);
			locateColumnVars(((GeneralDBSqlSpatialMetricBinary)expr).getRightArg(),allKnown);
		}
		else if(expr instanceof GeneralDBSqlSpatialMetricUnary)
		{
			locateColumnVars(((GeneralDBSqlSpatialMetricUnary)expr).getArg(),allKnown);
		}
		else if(expr instanceof GeneralDBLongLabelColumn)//Don't think this case is visited any more
		{
			//			GeneralDBColumnVar var = ((GeneralDBLongLabelColumn) expr).getRdbmsVar();
			//			for(GeneralDBColumnVar reference: allKnown)
			//			{
			//				if(var.getName().equals(reference.getName()))
			//				{
			//					var = reference;
			//				}
			//			}
			String name = ((GeneralDBLongLabelColumn) expr).getVarName();

			for(GeneralDBColumnVar reference: allKnown)
			{
				if(name.equals(reference.getName()))
				{
					((GeneralDBLongLabelColumn) expr).setRdbmsVar(reference);

				}
			}

		}
		else if(expr instanceof GeneralDBLabelColumn)//ColumnVar at least
		{
			String name = ((GeneralDBLabelColumn) expr).getVarName();

			for(GeneralDBColumnVar reference: allKnown)
			{
				if(name.equals(reference.getName()))
				{
					((GeneralDBLabelColumn) expr).setRdbmsVar(reference);

				}
			}
			//System.out.println("stopper");
		}
		else if(expr instanceof GeneralDBNumericColumn)//ColumnVar at least
		{
			String name = ((GeneralDBNumericColumn) expr).getVarName();

			//String alias =  ((GeneralDBNumericColumn) expr).getAlias();
			//alias.replaceFirst("n","l");
			for(GeneralDBColumnVar reference: allKnown)
			{
				if(name.equals(reference.getName()))
				{
					//((GeneralDBNumericColumn) expr).setRdbmsVar(reference);

					GeneralDBSqlExpr exprCopy = new GeneralDBLabelColumn(reference,false);
					expr.replaceWith(exprCopy);
				}
			}
			//System.out.println("stopper");
		}
		else if(expr instanceof GeneralDBSqlMathExpr)//Case when I have calculations in select
		{
			locateColumnVars(((GeneralDBSqlMathExpr)expr).getLeftArg(),allKnown);
			locateColumnVars(((GeneralDBSqlMathExpr)expr).getRightArg(),allKnown);
		}
		else
		{
			//must recurse
			if(expr instanceof GeneralDBSqlCase)
			{
				for (GeneralDBSqlCase.Entry e : ((GeneralDBSqlCase) expr).getEntries()) {
					locateColumnVars(e.getCondition(),allKnown);
					locateColumnVars(e.getResult(),allKnown);
					//					allVars.addAll(locateColumnVars(e.getCondition(),allKnown));
					//					allVars.addAll(locateColumnVars(e.getResult(),allKnown));
				}
			}

			if(expr instanceof GeneralDBSqlIsNull)
			{
				//allVars.addAll(locateColumnVars(((GeneralDBSqlIsNull) expr).getArg(),allKnown));
				locateColumnVars(((GeneralDBSqlIsNull) expr).getArg(),allKnown);
			}

			if(expr instanceof GeneralDBSqlNot)
			{
				//allVars.addAll(locateColumnVars(((GeneralDBSqlNot) expr).getArg(),allKnown));
				locateColumnVars(((GeneralDBSqlNot) expr).getArg(),allKnown);
			}

		}

		//return allVars;
	}

	private int constructReturnType(GeneralDBSqlExpr expr)
	{
		if(expr instanceof GeneralDBSqlSpatialProperty)
		{
			if(expr instanceof GeneralDBSqlGeoDimension ||
					expr instanceof GeneralDBSqlGeoSrid	)
			{
				return 1; //INTEGER
			}
			else if(expr instanceof GeneralDBSqlGeoGeometryType ||
					expr instanceof GeneralDBSqlGeoAsText	)
			{
				return 2; //STRING
			}
			else if(expr instanceof GeneralDBSqlGeoIsSimple ||
					expr instanceof GeneralDBSqlGeoIsEmpty	)
			{
				return 3; //Boolean
			}

		}
		else if(expr instanceof GeneralDBSqlSpatialConstructBinary ||
				expr instanceof GeneralDBSqlSpatialConstructUnary)
		{
			return 4; //WKB
		}
		else if(expr instanceof GeneralDBSqlSpatialMetricBinary ||
				expr instanceof GeneralDBSqlSpatialMetricUnary ||
				expr instanceof GeneralDBSqlMathExpr)
		{
			return 5; //FLOAT
		}
		return 0;//SHOULD NEVER REACH THIS CASE
	}
}
