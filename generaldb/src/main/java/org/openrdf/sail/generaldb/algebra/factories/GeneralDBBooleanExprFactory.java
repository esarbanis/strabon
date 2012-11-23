/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.algebra.factories;

import static org.openrdf.sail.generaldb.algebra.base.GeneralDBExprSupport.*;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.IsBNode;
import org.openrdf.query.algebra.IsLiteral;
import org.openrdf.query.algebra.IsResource;
import org.openrdf.query.algebra.IsURI;
import org.openrdf.query.algebra.LangMatches;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.query.algebra.evaluation.function.spatial.GeoConstants;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialConstructFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialMetricFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialPropertyFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.SpatialRelationshipFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.GeoSparqlRelateFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.nontopological.GeoSparqlBoundaryFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.nontopological.GeoSparqlConvexHullFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.geosparql.nontopological.GeoSparqlEnvelopeFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.BoundaryFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.ConvexHullFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.EnvelopeFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.construct.UnionFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.metric.AreaFunc;
import org.openrdf.query.algebra.evaluation.function.spatial.stsparql.relation.RelateFunc;
import org.openrdf.query.algebra.evaluation.function.temporal.stsparql.relation.TemporalConstants;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.generaldb.algebra.GeneralDBFalseValue;
import org.openrdf.sail.generaldb.algebra.GeneralDBRefIdColumn;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlCase;
import org.openrdf.sail.generaldb.algebra.GeneralDBSqlNull;
import org.openrdf.sail.generaldb.algebra.GeneralDBTrueValue;
import org.openrdf.sail.generaldb.algebra.base.GeneralDBSqlExpr;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;

/**
 * Boolean SQL expression factory. This factory can convert a number of core
 * algebra nodes into an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public class GeneralDBBooleanExprFactory extends QueryModelVisitorBase<UnsupportedRdbmsOperatorException> {

	private static final double HR14 = 14 * 60 * 60 * 1000;

	protected GeneralDBSqlExpr result;

	private GeneralDBSqlExprFactory sql;

	public GeneralDBSqlExpr createBooleanExpr(ValueExpr expr)
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
	public void meet(And node)
			throws UnsupportedRdbmsOperatorException
			{
		result = and(bool(node.getLeftArg()), bool(node.getRightArg()));
			}

	@Override
	public void meet(Bound node)
			throws UnsupportedRdbmsOperatorException
			{
		result = not(isNull(new GeneralDBRefIdColumn(node.getArg())));
			}


	/**
	 * XXX
	 * Additions here to support metrics
	 */
	@Override
	public void meet(Compare compare)
			throws UnsupportedRdbmsOperatorException
			{
		ValueExpr left = compare.getLeftArg();
		ValueExpr right = compare.getRightArg();
		CompareOp op = compare.getOperator();

		/**
		 * 
		 */
		boolean leftIsSpatial = false;
		boolean rightIsSpatial = false;
		GeneralDBSqlExpr leftSql = null;
		GeneralDBSqlExpr rightSql = null;

		if(left instanceof FunctionCall)
		{
			Function function = FunctionRegistry.getInstance().get(((FunctionCall)left).getURI());
			if(function instanceof SpatialMetricFunc)
			{
				leftSql = spatialMetricFunction((FunctionCall) left, function);
			}
			else //spatial property
			{
				leftSql = spatialPropertyFunction((FunctionCall) left, function);
			}
			leftIsSpatial = true;
		}
		else if(left instanceof MathExpr)
		{
			//some recursive function plainly to find out whether there is some nested metric
			leftIsSpatial = containsMetric((MathExpr)left);
			if(leftIsSpatial)
			{
				leftSql = numeric(left);
			}
		}

		if(right instanceof FunctionCall)
		{
			Function function = FunctionRegistry.getInstance().get(((FunctionCall)right).getURI());
			if(function instanceof SpatialMetricFunc)
			{
				rightSql = spatialMetricFunction((FunctionCall) right, function);
			}
			else //spatial property
			{
				rightSql = spatialPropertyFunction((FunctionCall) right, function);
			}
			rightIsSpatial = true;
		}
		else if(right instanceof MathExpr)
		{
			//some recursive function plainly to find out whether there is some nested metric
			rightIsSpatial = containsMetric((MathExpr)left);
			if(rightIsSpatial)
			{
				rightSql = numeric(right);
			}
		}

		/**
		 * 
		 */

		switch (op) {
		case EQ:
			if(!rightIsSpatial&&!leftIsSpatial)
			{
				//default cases
				if (isTerm(left) && isTerm(right)) {
					result = termsEqual(left, right);

				}
				else {
					result = equal(left, right);
				}
			}
			else
			{
				//more complicated cases
				if(!rightIsSpatial)
				{
					rightSql = numeric(right);
				}

				if(!leftIsSpatial)
				{
					leftSql = numeric(right);
				}
				result = eq(leftSql,rightSql);
			}
			break;
		case NE:
			if(!rightIsSpatial&&!leftIsSpatial)
			{
				//default cases
				if (isTerm(left) && isTerm(right)) {
					result = not(termsEqual(left, right));

				}
				else {
					result = not(equal(left, right));
				}
			}
			else
			{
				//more complicated cases
				if(!rightIsSpatial)
				{
					rightSql = numeric(right);
				}

				if(!leftIsSpatial)
				{
					leftSql = numeric(right);
				}
				result = neq(leftSql,rightSql);
			}
			break;
		case GE:
		case GT:
		case LE:
		case LT:

			if(!rightIsSpatial&&!leftIsSpatial)
			{
				//default cases
				GeneralDBSqlExpr simple = and(simple(type(left)), simple(type(right)));
				GeneralDBSqlExpr labels = and(cmp(label(left), op, label(right)), simple);
				GeneralDBSqlExpr time = cmp(time(left), op, time(right));
				GeneralDBSqlExpr within = cmp(time(left), op, sub(time(right), num(HR14)));
				GeneralDBSqlExpr comp = or(eq(zoned(left), zoned(right)), within);
				GeneralDBSqlExpr dateTime = and(eq(type(left), type(right)), and(comp, time));
				/**
				 * In case you need to remove the operations involving MOD,
				 * use the following line for datetime instead:
				 */
//				GeneralDBSqlExpr dateTime = and(eq(type(left), type(right)), time);
				result = or(cmp(numeric(left), op, numeric(right)), or(dateTime, labels));

			}
			else
			{
				//more complicated cases
				if(!rightIsSpatial)
				{
					rightSql = numeric(right);
				}

				if(!leftIsSpatial)
				{
					leftSql = numeric(right);
				}
				result = cmp(leftSql,op,rightSql);
			}

			break;
		}
			}

	@Override
	public void meet(IsBNode node)
			throws UnsupportedRdbmsOperatorException
			{
		result = isNotNull(sql.createBNodeExpr(node.getArg()));
			}

	@Override
	public void meet(IsLiteral node)
			throws UnsupportedRdbmsOperatorException
			{
		result = isNotNull(sql.createLabelExpr(node.getArg()));
			}

	@Override
	public void meet(IsResource node)
			throws UnsupportedRdbmsOperatorException
			{
		GeneralDBSqlExpr isBNode = isNotNull(sql.createBNodeExpr(node.getArg()));
		result = or(isBNode, isNotNull(sql.createUriExpr(node.getArg())));
			}

	@Override
	public void meet(IsURI node)
			throws UnsupportedRdbmsOperatorException
			{
		result = isNotNull(sql.createUriExpr(node.getArg()));
			}

	@Override
	public void meet(LangMatches node)
			throws UnsupportedRdbmsOperatorException
			{
		ValueExpr left = node.getLeftArg();
		ValueExpr right = node.getRightArg();
		GeneralDBSqlCase sqlCase = new GeneralDBSqlCase();
		sqlCase.when(eq(label(right), str("*")), neq(label(left), str("")));
		GeneralDBSqlExpr pattern = concat(lowercase(label(right)), str("%"));
		sqlCase.when(new GeneralDBTrueValue(), like(label(left), pattern));
		result = sqlCase;
			}

	@Override
	public void meet(Not node)
			throws UnsupportedRdbmsOperatorException
			{
		result = not(bool(node.getArg()));
			}

	@Override
	public void meet(Or node)
			throws UnsupportedRdbmsOperatorException
			{
		result = or(bool(node.getLeftArg()), bool(node.getRightArg()));
			}

	@Override
	public void meet(Regex node)
			throws UnsupportedRdbmsOperatorException
			{
		result = regex(label(node.getArg()), label(node.getPatternArg()), label(node.getFlagsArg()));
			}

	@Override
	public void meet(SameTerm node)
			throws UnsupportedRdbmsOperatorException
			{
		ValueExpr left = node.getLeftArg();
		ValueExpr right = node.getRightArg();
		boolean leftIsVar = left instanceof Var;
		boolean rightIsVar = right instanceof Var;
		boolean leftIsConst = left instanceof ValueConstant;
		boolean rightIsConst = right instanceof ValueConstant;
		if (leftIsVar && rightIsVar) {
			result = eq(new GeneralDBRefIdColumn((Var)left), new GeneralDBRefIdColumn((Var)right));
		}
		else if ((leftIsVar || leftIsConst) && (rightIsVar || rightIsConst)) {
			result = eq(hash(left), hash(right));
		}
		else {
			GeneralDBSqlExpr bnodes = eqComparingNull(bNode(left), bNode(right));
			GeneralDBSqlExpr uris = eqComparingNull(uri(left), uri(right));
			GeneralDBSqlExpr langs = eqComparingNull(lang(left), lang(right));
			GeneralDBSqlExpr datatype = eqComparingNull(type(left), type(right));
			GeneralDBSqlExpr labels = eqComparingNull(label(left), label(right));

			GeneralDBSqlExpr literals = and(langs, and(datatype, labels));
			result = and(bnodes, and(uris, literals));
		}
			}

	@Override
	public void meet(ValueConstant vc)
			throws UnsupportedRdbmsOperatorException
			{
		result = valueOf(vc.getValue());
			}

	@Override
	public void meet(Var var)
			throws UnsupportedRdbmsOperatorException
			{
		if (var.getValue() == null) {
			result = effectiveBooleanValue(var);
		}
		else {
			result = valueOf(var.getValue());
		}
			}

	public void setSqlExprFactory(GeneralDBSqlExprFactory sql) {
		this.sql = sql;
	}

	protected GeneralDBSqlExpr bNode(ValueExpr arg)
			throws UnsupportedRdbmsOperatorException
			{
		return sql.createBNodeExpr(arg);
			}

	protected GeneralDBSqlExpr bool(ValueExpr arg)
			throws UnsupportedRdbmsOperatorException
			{
		return sql.createBooleanExpr(arg);
			}

	protected GeneralDBSqlExpr label(ValueExpr arg)
			throws UnsupportedRdbmsOperatorException
			{
		return sql.createLabelExpr(arg);
			}

	protected GeneralDBSqlExpr lang(ValueExpr arg)
			throws UnsupportedRdbmsOperatorException
			{
		return sql.createLanguageExpr(arg);
			}

	protected GeneralDBSqlExpr hash(ValueExpr arg)
			throws UnsupportedRdbmsOperatorException
			{
		return sql.createHashExpr(arg);
			}

	@Override
	protected void meetNode(QueryModelNode arg)
			throws UnsupportedRdbmsOperatorException
			{
		if (arg instanceof ValueExpr) {
			result = effectiveBooleanValue((ValueExpr)arg);
		}
		else {
			throw unsupported(arg);
		}
			}

	protected GeneralDBSqlExpr numeric(ValueExpr arg)
			throws UnsupportedRdbmsOperatorException
			{
		return sql.createNumericExpr(arg);
			}

	protected GeneralDBSqlExpr time(ValueExpr arg)
			throws UnsupportedRdbmsOperatorException
			{
		return sql.createTimeExpr(arg);
			}

	protected GeneralDBSqlExpr type(ValueExpr arg)
			throws UnsupportedRdbmsOperatorException
			{
		return sql.createDatatypeExpr(arg);
			}

	protected GeneralDBSqlExpr uri(ValueExpr arg)
			throws UnsupportedRdbmsOperatorException
			{
		return sql.createUriExpr(arg);
			}

	protected GeneralDBSqlExpr zoned(ValueExpr arg)
			throws UnsupportedRdbmsOperatorException
			{
		return sql.createZonedExpr(arg);
			}

	private GeneralDBSqlExpr effectiveBooleanValue(ValueExpr v)
			throws UnsupportedRdbmsOperatorException
			{
		String bool = XMLSchema.BOOLEAN.stringValue();
		GeneralDBSqlCase sqlCase = new GeneralDBSqlCase();
		sqlCase.when(eq(type(v), str(bool)), eq(label(v), str("true")));
		sqlCase.when(simple(type(v)), not(eq(label(v), str(""))));
		sqlCase.when(isNotNull(numeric(v)), not(eq(numeric(v), num(0))));
		return sqlCase;
			}

	private GeneralDBSqlExpr equal(ValueExpr left, ValueExpr right)
			throws UnsupportedRdbmsOperatorException
			{
		GeneralDBSqlExpr bnodes = eq(bNode(left), bNode(right));
		GeneralDBSqlExpr uris = eq(uri(left), uri(right));
		GeneralDBSqlCase scase = new GeneralDBSqlCase();
		scase.when(or(isNotNull(bNode(left)), isNotNull(bNode(right))), bnodes);
		scase.when(or(isNotNull(uri(left)), isNotNull(uri(right))), uris);
		return literalEqual(left, right, scase);
			}

	private boolean isTerm(ValueExpr node) {
		return node instanceof Var || node instanceof ValueConstant;
	}

	private GeneralDBSqlExpr literalEqual(ValueExpr left, ValueExpr right, GeneralDBSqlCase scase)
			throws UnsupportedRdbmsOperatorException
			{
		GeneralDBSqlExpr labels = eq(label(left), label(right));
		GeneralDBSqlExpr langs = and(eqIfNotNull(lang(left), lang(right)), labels.clone());
		GeneralDBSqlExpr numeric = eq(numeric(left), numeric(right));
		GeneralDBSqlExpr time = eq(time(left), time(right));

		GeneralDBSqlExpr bothCalendar = and(isNotNull(time(left)), isNotNull(time(right)));
		GeneralDBSqlExpr over14 = gt(abs(sub(time(left), time(right))), num(HR14 / 2));
		GeneralDBSqlExpr comparable = and(bothCalendar, or(eq(zoned(left), zoned(right)), over14));

		scase.when(or(isNotNull(lang(left)), isNotNull(lang(right))), langs);
		scase.when(and(simple(type(left)), simple(type(right))), labels.clone());
		scase.when(and(isNotNull(numeric(left)), isNotNull(numeric(right))), numeric);
		scase.when(comparable, time);
		scase.when(and(eq(type(left), type(right)), labels.clone()), new GeneralDBTrueValue());
		return scase;
			}

	private GeneralDBSqlExpr termsEqual(ValueExpr left, ValueExpr right)
			throws UnsupportedRdbmsOperatorException
			{
		GeneralDBSqlExpr bnodes = eqIfNotNull(bNode(left), bNode(right));
		GeneralDBSqlExpr uris = eqIfNotNull(uri(left), uri(right));
		GeneralDBSqlCase scase = new GeneralDBSqlCase();
		scase.when(or(isNotNull(bNode(left)), isNotNull(bNode(right))), bnodes);
		scase.when(or(isNotNull(uri(left)), isNotNull(uri(right))), uris);
		return literalEqual(left, right, scase);
			}

	private GeneralDBSqlExpr valueOf(Value value) {
		if (value instanceof Literal) {
			if (((Literal)value).booleanValue()) {
				return new GeneralDBTrueValue();
			}
			return new GeneralDBFalseValue();
		}
		return sqlNull();
	}

	/**
	 * FIXME spatials
	 */
	@Override
	public void meet(FunctionCall functionCall)
			throws UnsupportedRdbmsOperatorException
			{
		Function function = FunctionRegistry.getInstance().get(functionCall.getURI());

		if(function instanceof SpatialConstructFunc)
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

			//These three (actually six) functions only have one argument!!
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
						rightArg = numeric(right);
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

			result = spatialConstructPicker(function, leftArg, rightArg);

		}
		else if(function instanceof SpatialRelationshipFunc)
		{
			ValueExpr left = functionCall.getArgs().get(0);
			ValueExpr right = functionCall.getArgs().get(1);


			GeneralDBSqlExpr leftArg = null;
			GeneralDBSqlExpr rightArg = null;
			GeneralDBSqlExpr thirdArg = null;

			if(left instanceof FunctionCall)
			{
				leftArg = spatialFunction((FunctionCall) left);
			}
			else
			{
				leftArg = label(left);
			}

			if(right instanceof FunctionCall)
			{
				rightArg = spatialFunction((FunctionCall) right);
			}
			else
			{
				rightArg = label(right);
			}

			if(function instanceof RelateFunc || function instanceof GeoSparqlRelateFunc)
			{
				//for st_relate!!
				ValueExpr third = functionCall.getArgs().get(2);
				//For now, I assume that I am only dealing with st_relate
				thirdArg = label(third);
			}

			result = spatialRelationshipPicker(function, leftArg, rightArg, thirdArg);
		}
		else if(function instanceof SpatialPropertyFunc) //1 argument
		{
			//TODO not ended yet -> no result has been set
			ValueExpr arg = functionCall.getArgs().get(0);

			GeneralDBSqlExpr sqlArg = null;

			if(arg instanceof FunctionCall)
			{
				arg.visit(this);
			}
			else
			{
				sqlArg = label(arg);
			}

			result = spatialPropertyPicker(function, sqlArg);
		}
		else if(function instanceof SpatialMetricFunc) 
			//Argument # depending on the function selected
		{
			//TODO
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

			//These two functions only have one argument!!
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

			result = spatialMetricPicker(function, leftArg, rightArg);
		}
		else //default case
		{
			meetNode(functionCall);
		}

			}

	public GeneralDBSqlExpr spatialFunction(FunctionCall functionCall) throws UnsupportedRdbmsOperatorException
	{
		Function function = FunctionRegistry.getInstance().get(functionCall.getURI());
		if(function instanceof SpatialConstructFunc)
		{
			return spatialConstructFunction(functionCall,function);	
		}
		else if(function instanceof SpatialRelationshipFunc)
		{
			return spatialRelationshipFunction(functionCall,function);	
		}
		else if(function instanceof SpatialPropertyFunc) //1 argument
		{
			return spatialPropertyFunction(functionCall,function);
		}
		else if(function instanceof SpatialMetricFunc) //1 argument
		{
			return spatialMetricFunction(functionCall,function);	
		}
		return null;
	}


	GeneralDBSqlExpr spatialRelationshipFunction(FunctionCall functionCall, Function function) throws UnsupportedRdbmsOperatorException
	{
		ValueExpr left = functionCall.getArgs().get(0);
		ValueExpr right = functionCall.getArgs().get(1);


		GeneralDBSqlExpr leftArg = null;
		GeneralDBSqlExpr rightArg = null;
		GeneralDBSqlExpr thirdArg = null;

		if(left instanceof FunctionCall)
		{
			leftArg = spatialFunction((FunctionCall) left);
		}
		else
		{
			leftArg = label(left);
		}

		if(right instanceof FunctionCall)
		{
			rightArg = spatialFunction((FunctionCall) right);
		}
		else
		{
			rightArg = label(right);
		}


		if(function instanceof RelateFunc || function instanceof GeoSparqlRelateFunc)
		{
			//for st_relate!!
			ValueExpr third = functionCall.getArgs().get(2);
			//For now, I assume that I am only dealing with st_relate
			thirdArg = label(third);
		}

		return spatialRelationshipPicker(function, leftArg, rightArg, thirdArg);
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
				&& !(function instanceof GeoSparqlEnvelopeFunc)
				&& !(function instanceof UnionFunc && functionCall.getArgs().size()==1))
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
					rightArg = numeric(right);
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

	GeneralDBSqlExpr spatialRelationshipPicker(Function function,GeneralDBSqlExpr leftArg, GeneralDBSqlExpr rightArg, 
			GeneralDBSqlExpr thirdArg)
	{
		//XXX stSPARQL
		if(function.getURI().equals(GeoConstants.anyInteract))
		{
			return anyInteract(leftArg,rightArg);
		}
		if(function.getURI().equals(GeoConstants.intersects))
		{
			return intersects(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.contains))
		{
			return contains(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.coveredBy))
		{
			return coveredBy(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.covers))
		{
			return covers(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.disjoint))
		{
			return disjoint(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.equals))
		{
			return equalsGeo(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.inside))
		{
			return inside(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.overlap))
		{
			return overlap(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.touch))
		{
			return touch(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.relate))
		{
			return relate(leftArg,rightArg,thirdArg);
		}
		else if(function.getURI().equals(GeoConstants.left))
		{
			return left(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.right))
		{
			return right(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.above))
		{
			return above(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.below))
		{
			return below(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.touch))
		{
			return touch(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.mbbOverlaps))
		{
			return mbbIntersects(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.mbbInside))
		{
			return mbbInside(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.mbbContains))
		{
			return ContainsMBB(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.mbbEquals))
		{
			return mbbEqualsGeo(leftArg,rightArg);
		}
		//stSPARQL temporal functions
		else if(function.getURI().equals(TemporalConstants.afterPeriod))
		{
			return afterPeriod(leftArg,rightArg);
		}
		else if(function.getURI().equals(TemporalConstants.beforePeriod))
		{
			return beforePeriod(leftArg,rightArg);
		}
		else if(function.getURI().equals(TemporalConstants.periodContains))
		{
			return periodContains(leftArg,rightArg);
		}
		else if(function.getURI().equals(TemporalConstants.periodOverlaps))
		{
			return periodOverlaps(leftArg,rightArg);
		}
		//XXX GeoSPARQL
		//Simple Features
		else if(function.getURI().equals(GeoConstants.sfContains))
		{
			return sfContains(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.sfCrosses))
		{
			return sfCrosses(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.sfDisjoint))
		{
			return sfDisjoint(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.sfEquals))
		{
			return sfEquals(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.sfIntersects))
		{
			return sfIntersects(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.sfOverlaps))
		{
			return sfOverlaps(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.sfTouches))
		{
			return sfTouches(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.sfWithin))
		{
			return sfWithin(leftArg,rightArg);
		}
		//RCC8
		else if(function.getURI().equals(GeoConstants.rccDisconnected))
		{
			return rccDisconnected(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.rccEquals))
		{
			return rccEquals(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.rccExternallyConnected))
		{
			return rccExternallyConnected(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.rccNonTangentialProperPart))
		{
			return rccNonTangentialProperPart(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.rccNonTangentialProperPartInverse))
		{
			return rccNonTangentialProperPartInverse(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.rccPartiallyOverlapping))
		{
			return rccPartiallyOverlapping(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.rccTangentialProperPart))
		{
			return rccTangentialProperPart(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.rccTangentialProperPartInverse))
		{
			return rccTangentialProperPartInverse(leftArg,rightArg);
		}
		//Egenhofer
		else if(function.getURI().equals(GeoConstants.ehContains))
		{
			return ehContains(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.ehCoveredBy))
		{
			return ehCoveredBy(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.ehCovers))
		{
			return ehCovers(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.ehDisjoint))
		{
			return ehDisjoint(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.ehEquals))
		{
			return ehEquals(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.ehInside))
		{
			return ehInside(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.ehMeet))
		{
			return ehMeet(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.ehOverlap))
		{
			return ehOverlap(leftArg,rightArg);
		}
		else if(function.getURI().equals(GeoConstants.geoSparqlRelate))
		{
			return relate(leftArg,rightArg,thirdArg);
		}
		//Should never reach this place
		return null;
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

		} else if (function.getURI().equals(GeoConstants.asGML)) {
			return asGML(arg);
		}

		//Should never reach this place
		return null;
	}

	private boolean containsMetric(MathExpr expr)
	{
		boolean leftChildMetric = false;
		boolean rightChildMetric = false;

		ValueExpr left = expr.getLeftArg();
		ValueExpr right = expr.getRightArg();

		if(left instanceof MathExpr)
		{
			leftChildMetric = containsMetric(((MathExpr)left));
		}
		else if(left instanceof FunctionCall)
		{
			return true;
		}

		if(right instanceof MathExpr)
		{
			rightChildMetric = containsMetric(((MathExpr)right));
		}
		else if(right instanceof FunctionCall)
		{
			return true;
		}

		return leftChildMetric||rightChildMetric;

	}
}
