/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra.factories;

import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.anyInteract;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.asText;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.contains;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.coveredBy;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.covers;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.dimension;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.disjoint;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.equalsGeo;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geoArea;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geoBoundary;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geoBuffer;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geoTransform;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geoConvexHull;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geoDifference;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geoDistance;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geoEnvelope;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geoIntersection;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geoSymDifference;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geoUnion;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.geometryType;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.inside;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.isEmpty;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.isSimple;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.overlap;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.sqlNull;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.srid;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.touch;
import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.unsupported;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.query.algebra.Datatype;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Lang;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.MathExpr.MathOp;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.query.algebra.evaluation.function.spatial.GeoConstants;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialConstructFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialMetricFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialPropertyFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialRelationshipFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.nontopological.GeoSparqlBoundaryFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.nontopological.GeoSparqlConvexHullFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.nontopological.GeoSparqlEnvelopeFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.BoundaryFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.ConvexHullFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.EnvelopeFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.metric.AreaFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.metric.DistanceFunc;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.GeneralDBDoubleValue;
import org.openrdf.sail.generaldb.algebra.GeneralDBNumericColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlMathExpr;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;

/**
 * Creates an SQL expression of a literal's numeric value.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBNumericExprFactory extends QueryModelVisitorBase<UnsupportedRdbmsOperatorException> {

	protected GeneralDBSqlExpr result;

	/**
	 * Addition in order to be able to transform the metric expressions I need
	 * I also needed to create uri expressions
	 */
	private GeneralDBLabelExprFactory labelsPeek;
	private GeneralDBURIExprFactory urisPeek;

	public GeneralDBLabelExprFactory getLabelsPeek() {
		return labelsPeek;
	}

	public void setLabelsPeek(GeneralDBLabelExprFactory labelsPeek) {
		this.labelsPeek = labelsPeek;
	}

	public GeneralDBURIExprFactory getUrisPeek() {
		return urisPeek;
	}

	public void setUrisPeek(GeneralDBURIExprFactory labelsPeek) {
		this.urisPeek = urisPeek;
	}
	/**
	 * 
	 */

	public GeneralDBSqlExpr createNumericExpr(ValueExpr expr)
			throws UnsupportedRdbmsOperatorException
			{
		result = null;
		if (expr == null)
			return new GeneralDBSqlNull();
		expr.visit(this);
		if (result == null)
			return new GeneralDBSqlNull();
		return result;
			}

	@Override
	public void meet(Datatype node) {
		result = sqlNull();
	}

	@Override
	public void meet(Lang node)
			throws UnsupportedRdbmsOperatorException
			{
		result = sqlNull();
			}

	/**
	 * XXX changes here to enable more complicated metric expressions
	 */
	@Override
	public void meet(MathExpr node)
			throws UnsupportedRdbmsOperatorException
			{
		GeneralDBSqlExpr left;// = createNumericExpr(node.getLeftArg());
		GeneralDBSqlExpr right;// = createNumericExpr(node.getRightArg());

		if(node.getLeftArg() instanceof FunctionCall)
		{
			Function function = FunctionRegistry.getInstance().get(((FunctionCall)node.getLeftArg()).getURI());


			left  = exportSpatialOperand((FunctionCall) node.getLeftArg(),function); 


		}
		else
		{
			//default
			left = createNumericExpr(node.getLeftArg());
		}

		if(node.getRightArg() instanceof FunctionCall)
		{
			Function function = FunctionRegistry.getInstance().get(((FunctionCall)node.getRightArg()).getURI());

			right  = exportSpatialOperand((FunctionCall) node.getRightArg(),function); 

		}
		else
		{
			//default
			right = createNumericExpr(node.getRightArg());
		}


		MathOp op = node.getOperator();
		result = new GeneralDBSqlMathExpr(left, op, right);
			}

	@Override
	public void meet(Str node) {
		result = sqlNull();
	}

	@Override
	public void meet(ValueConstant vc) {
		result = valueOf(vc.getValue());
	}

	@Override
	public void meet(Var var) {
		if (var.getValue() == null) {
			result = new GeneralDBNumericColumn(var);
		}
		else {
			result = valueOf(var.getValue());
		}
	}

	@Override
	protected void meetNode(QueryModelNode arg)
			throws UnsupportedRdbmsOperatorException
			{
		throw unsupported(arg);
			}

	private GeneralDBSqlExpr valueOf(Value value) {
		if (value instanceof Literal) {
			Literal lit = (Literal)value;
			URI dt = lit.getDatatype();
			if (dt != null && XMLDatatypeUtil.isNumericDatatype(dt)) {
				try {
					return new GeneralDBDoubleValue(lit.doubleValue());
				}
				catch (NumberFormatException e) {
					return null;
				}
			}
		}
		return null;
	}

	private GeneralDBSqlExpr exportSpatialOperand(FunctionCall functionCall, Function function) throws UnsupportedRdbmsOperatorException
	{

		GeneralDBSqlExpr leftArg = null;
		GeneralDBSqlExpr rightArg = null;

		ValueExpr left = functionCall.getArgs().get(0);


		if(left instanceof FunctionCall)
		{
			leftArg = spatialFunction((FunctionCall) left);
		}
		else
		{
			leftArg = label(left);
		}



		if((function instanceof DistanceFunc))
		{
			ValueExpr right = functionCall.getArgs().get(1);
			if(right instanceof FunctionCall)
			{
				rightArg = spatialFunction((FunctionCall) right);
			}
			else
			{
				rightArg = label(right);
			}
		}

		if(function instanceof SpatialMetricFunc)
		{
			return spatialMetricPicker(function, leftArg, rightArg);
		}
		else //if(functionCall instanceof SpatialPropertyFunc)
		{
			return spatialPropertyPicker(function, leftArg);
		}
	}

	public GeneralDBSqlExpr spatialFunction(FunctionCall functionCall) throws UnsupportedRdbmsOperatorException
	{
		Function function = FunctionRegistry.getInstance().get(functionCall.getURI());
		if(function instanceof SpatialConstructFunc)
		{
			return spatialConstructFunction(functionCall,function);	
		}
		else if(function instanceof SpatialMetricFunc)
		{
			return spatialMetricFunction(functionCall,function);	
		}
		else if(function instanceof SpatialPropertyFunc) //1 argument
		{
			return spatialPropertyFunction(functionCall,function);	
		}
		return null;
	}


	GeneralDBSqlExpr spatialConstructFunction(FunctionCall functionCall, Function function) throws UnsupportedRdbmsOperatorException
	{
		GeneralDBSqlExpr leftArg = null;
		GeneralDBSqlExpr rightArg = null;

		ValueExpr left = functionCall.getArgs().get(0);


		if(left instanceof FunctionCall)
		{
			leftArg = spatialFunction((FunctionCall) left);
		}
		else
		{
			leftArg = label(left);
		}



		if(!(function instanceof EnvelopeFunc) 
				&& !(function instanceof ConvexHullFunc) 
				&& !(function instanceof BoundaryFunc)
				&& !(function instanceof GeoSparqlBoundaryFunc)
				&& !(function instanceof GeoSparqlConvexHullFunc)
				&& !(function instanceof GeoSparqlEnvelopeFunc))
		{
			ValueExpr right = functionCall.getArgs().get(1);
			if(right instanceof FunctionCall)
			{
				rightArg = spatialFunction((FunctionCall) right);
			}
			else
			{
				if(function.getURI().equals(GeoConstants.buffer))
				{
					//Be it a Var or a Value Constant, 'numeric' is the way to go
					rightArg = this.createNumericExpr(right);
				}
				else if(function.getURI().equals(GeoConstants.transform))
				{
					//Another special case -> Second argument of this function is a URI
					rightArg = uri(right);
				}
				else 
				{	
					//DEFAULT behavior for constructs! buffer's second argument is a ValueConstant or a Var,
					//thus the special treatment
					rightArg = label(right);
				}




			}
		}

		return spatialConstructPicker(function, leftArg, rightArg);

	}

	GeneralDBSqlExpr spatialMetricFunction(FunctionCall functionCall, Function function) throws UnsupportedRdbmsOperatorException
	{
		GeneralDBSqlExpr leftArg = null;
		GeneralDBSqlExpr rightArg = null;

		ValueExpr left = functionCall.getArgs().get(0);


		if(left instanceof FunctionCall)
		{
			leftArg = spatialFunction((FunctionCall) left);
		}
		else
		{
			leftArg = label(left);
		}



		if(!(function instanceof AreaFunc))
		{
			ValueExpr right = functionCall.getArgs().get(1);
			if(right instanceof FunctionCall)
			{
				rightArg = spatialFunction((FunctionCall) right);
			}
			else
			{
				rightArg = label(right);
			}
		}

		return spatialMetricPicker(function, leftArg, rightArg);

	}

	GeneralDBSqlExpr spatialPropertyFunction(FunctionCall functionCall, Function function) throws UnsupportedRdbmsOperatorException
	{
		GeneralDBSqlExpr expr = null;

		ValueExpr arg = functionCall.getArgs().get(0);


		if(arg instanceof FunctionCall)
		{
			expr = spatialFunction((FunctionCall) arg);
		}
		else
		{
			expr = label(arg);
		}

		return spatialPropertyPicker(function, expr);

	}


	GeneralDBSqlExpr spatialConstructPicker(Function function,GeneralDBSqlExpr leftArg, GeneralDBSqlExpr rightArg)
	{
		if(function.getURI().equals(GeoConstants.union))
		{
			return geoUnion(leftArg, rightArg);
		}
		else if(function.getURI().equals(GeoConstants.buffer))
		{
			return geoBuffer(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.transform))
		{
			return geoTransform(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.envelope))
		{
			return geoEnvelope(leftArg);
		}
		else if(function.getURI().equals(GeoConstants.convexHull))
		{
			return geoConvexHull(leftArg);
		}
		else if(function.getURI().equals(GeoConstants.boundary))
		{
			return geoBoundary(leftArg);
		}
		else if(function.getURI().equals(GeoConstants.intersection))
		{
			return geoIntersection(leftArg, rightArg);
		}
		else if(function.getURI().equals(GeoConstants.difference))
		{
			return geoDifference(leftArg, rightArg);
		}
		else if(function.getURI().equals(GeoConstants.symDifference))
		{
			return geoSymDifference(leftArg, rightArg);
		}
		//XXX GeoSPARQL - Non topological - except distance
		//TODO Must add buffer after deciding how to implement it
		else if(function.getURI().equals(GeoConstants.geoSparqlConvexHull))
		{
			return geoConvexHull(leftArg);
		}
		else if(function.getURI().equals(GeoConstants.geoSparqlIntersection))
		{
			return geoIntersection(leftArg, rightArg);
		}
		else if(function.getURI().equals(GeoConstants.geoSparqlUnion))
		{
			return geoUnion(leftArg, rightArg);
		}
		else if(function.getURI().equals(GeoConstants.geoSparqlDifference))
		{
			return geoDifference(leftArg, rightArg);
		}
		else if(function.getURI().equals(GeoConstants.geoSparqlSymmetricDifference))
		{
			return geoSymDifference(leftArg, rightArg);
		}
		else if(function.getURI().equals(GeoConstants.geoSparqlEnvelope))
		{
			return geoEnvelope(leftArg);
		}
		else if(function.getURI().equals(GeoConstants.geoSparqlBoundary))
		{
			return geoBoundary(leftArg);
		}
		//Should never reach this place
		return null;
	}

	//TODO more to be added here probably
	GeneralDBSqlExpr spatialMetricPicker(Function function,GeneralDBSqlExpr leftArg, GeneralDBSqlExpr rightArg)
	{
		if(function.getURI().equals(GeoConstants.distance))
		{
			return geoDistance(leftArg, rightArg);
		}
		else if(function.getURI().equals(GeoConstants.area))
		{
			return geoArea(leftArg);
		}
		//GeoSPARQL's distance must be added at this place

		//Should never reach this place
		return null;
	}

	GeneralDBSqlExpr spatialPropertyPicker(Function function,GeneralDBSqlExpr arg)
	{
		if(function.getURI().equals(GeoConstants.dimension))
		{
			return dimension(arg);
		}
		else if(function.getURI().equals(GeoConstants.geometryType))
		{
			return geometryType(arg);
		}
		else if(function.getURI().equals(GeoConstants.asText))
		{
			return asText(arg);
		}
		else if(function.getURI().equals(GeoConstants.srid))
		{
			return srid(arg);
		}
		else if(function.getURI().equals(GeoConstants.isEmpty))
		{
			return isEmpty(arg);
		}
		else if(function.getURI().equals(GeoConstants.isSimple))
		{
			return isSimple(arg);
		}

		//Should never reach this place
		return null;
	}

	protected GeneralDBSqlExpr label(ValueExpr arg)
			throws UnsupportedRdbmsOperatorException
			{
		return labelsPeek.createLabelExpr(arg);
			}

	protected GeneralDBSqlExpr uri(ValueExpr arg)
			throws UnsupportedRdbmsOperatorException
			{
		return urisPeek.createUriExpr(arg);
			}

	//	protected GeneralDBSqlExpr numeric(ValueExpr arg)
	//	throws UnsupportedRdbmsOperatorException
	//	{
	//		return sql.createNumericExpr(arg);
	//	}

}