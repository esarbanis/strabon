package org.openrdf.query.resultio.sparqljson;

import info.aduna.io.IndentingWriter;
import info.aduna.text.StringUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;

/**
 * A TupleQueryResultWriter that writes query results in the <a
 * href="http://www.geojson.org/geojson-spec.html/">GeoJSON Format</a>.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 */
public class stSPARQLResultsGeoJSONWriter implements TupleQueryResultWriter {

	private IndentingWriter writer;
	
	public stSPARQLResultsGeoJSONWriter(OutputStream out) {
		Writer w = new OutputStreamWriter(out, Charset.forName("UTF-8"));
		w = new BufferedWriter(w, 1024);
		writer = new IndentingWriter(w);
	}

	@Override
	public void startQueryResult(List<String> bindingNames) throws TupleQueryResultHandlerException {
		try {
			openBraces();
			
		} catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void endQueryResult() throws TupleQueryResultHandlerException {
	}

	@Override
	public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
	}

	@Override
	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return TupleQueryResultFormat.JSON;
	}
	

	protected void writeKeyValue(String key, String value)
		throws IOException
	{
		writeKey(key);
		writeString(value);
	}

	protected void writeKeyValue(String key, Value value)
		throws IOException, TupleQueryResultHandlerException
	{
		writeKey(key);
		writeValue(value);
	}

	protected void writeKeyValue(String key, Iterable<String> array)
		throws IOException
	{
		writeKey(key);
		writeArray(array);
	}

	protected void writeKey(String key)
		throws IOException
	{
		writeString(key);
		writer.write(": ");
	}

	protected void writeValue(Value value)
		throws IOException, TupleQueryResultHandlerException
	{
		writer.write("{ ");

		if (value instanceof URI) {
			writeKeyValue("type", "uri");
			writer.write(", ");
			writeKeyValue("value", ((URI)value).toString());
		}
		else if (value instanceof BNode) {
			writeKeyValue("type", "bnode");
			writer.write(", ");
			writeKeyValue("value", ((BNode)value).getID());
		}
		else if (value instanceof Literal) {
			Literal lit = (Literal)value;

			if (lit.getDatatype() != null) {
				writeKeyValue("type", "typed-literal");
				writer.write(", ");
				writeKeyValue("datatype", lit.getDatatype().toString());
			}
			else {
				writeKeyValue("type", "literal");
				if (lit.getLanguage() != null) {
					writer.write(", ");
					writeKeyValue("xml:lang", lit.getLanguage());
				}
			}

			writer.write(", ");
			writeKeyValue("value", lit.getLabel());
		}
		else {
			throw new TupleQueryResultHandlerException("Unknown Value object type: " + value.getClass());
		}

		writer.write(" }");
	}

	protected void writeString(String value) throws IOException
	{
		// Escape special characters
		value = StringUtil.gsub("\\", "\\\\", value);
		value = StringUtil.gsub("\"", "\\\"", value);
		value = StringUtil.gsub("/", "\\/", value);
		value = StringUtil.gsub("\b", "\\b", value);
		value = StringUtil.gsub("\f", "\\f", value);
		value = StringUtil.gsub("\n", "\\n", value);
		value = StringUtil.gsub("\r", "\\r", value);
		value = StringUtil.gsub("\t", "\\t", value);

		writer.write("\"");
		writer.write(value);
		writer.write("\"");
	}

	protected void writeArray(Iterable<String> array)
		throws IOException
	{
		writer.write("[ ");

		Iterator<String> iter = array.iterator();
		while (iter.hasNext()) {
			String value = iter.next();

			writeString(value);

			if (iter.hasNext()) {
				writer.write(", ");
			}
		}

		writer.write(" ]");
	}

	protected void openArray() throws IOException
	{
		writer.write("[");
		writer.writeEOL();
		writer.increaseIndentation();
	}

	protected void closeArray() throws IOException
	{
		writer.writeEOL();
		writer.decreaseIndentation();
		writer.write("]");
	}

	protected void openBraces()
		throws IOException
	{
		writer.write("{");
		writer.writeEOL();
		writer.increaseIndentation();
	}

	protected void closeBraces()
		throws IOException
	{
		writer.writeEOL();
		writer.decreaseIndentation();
		writer.write("}");
	}

	protected void writeComma()
		throws IOException
	{
		writer.write(", ");
		writer.writeEOL();
	}

}
