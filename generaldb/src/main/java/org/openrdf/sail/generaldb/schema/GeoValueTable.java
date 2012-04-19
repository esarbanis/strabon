
package org.openrdf.sail.generaldb.schema;


import org.openrdf.sail.generaldb.GeneralDBSqlTable;
import org.openrdf.sail.rdbms.schema.RdbmsTable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;


/**
 * Modified to be used with a "geographic info" table
 *
 *
 * Manages the rows in a value table. These tables have two columns: an internal
 * id column and a value column. PLUS: strdfgeo column, constraint column
 * 
 * @author James Leigh
 * 
 */
public class GeoValueTable {

	public static int BATCH_SIZE = 8 * 1024;

	public static final long NIL_ID = 0; // TODO

	private static final String[] PKEY = { "id" };

	//used to be named "value"
	//private static final String[] VALUE_INDEX = { "original" };

	//private static final String[] MINIMUM_BOUNDING_BOX = { "value" };

	private static final String[] CONSTRAINT = { "constr" };

	private int length = -1;

	private int sqlType;

	private int idType;

	private String INSERT;

	private String INSERT_SELECT;

	private String EXPUNGE;

	private RdbmsTable table;

	private RdbmsTable temporary;

	private ValueBatch batch;

	private BlockingQueue<Batch> queue;

	private boolean indexingValues;

	private PreparedStatement insertSelect;

	public void setQueue(BlockingQueue<Batch> queue) {
		this.queue = queue;
	}

	public boolean isIndexingValues() {
		return indexingValues;
	}

	public void setIndexingValues(boolean indexingValues) {
		this.indexingValues = indexingValues;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getSqlType() {
		return sqlType;
	}

	public void setSqlType(int sqlType) {
		this.sqlType = sqlType;
	}

	public int getIdType() {
		return idType;
	}

	public void setIdType(int sqlType) {
		this.idType = sqlType;
	}

	public RdbmsTable getRdbmsTable() {
		return table;
	}

	public void setRdbmsTable(RdbmsTable table) {
		this.table = table;
	}

	public RdbmsTable getTemporaryTable() {
		return temporary;
	}

	public void setTemporaryTable(RdbmsTable temporary) {
		this.temporary = temporary;
	}

	public String getName() {
		return table.getName();
	}

	public long size() {
		return table.size();
	}

	public int getBatchSize() {
		return BATCH_SIZE;
	}

	//FIXME temporal additions
	public void initialize()
	throws SQLException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(getInsertTable().getName());
//		Integer srid=  StrabonPolyhedron.defaultSRID;
//		sb.append(" (id, strdfgeo,srid) VALUES (?,ST_Transform(ST_GeomFromWKB(?,?),").append(srid).append("),?)");
		sb.append(((GeneralDBSqlTable)table).buildInsertGeometryValue());
		INSERT = sb.toString();
		sb.delete(0, sb.length());
		sb.append("DELETE FROM ").append(table.getName()).append("\n");
		sb.append(((GeneralDBSqlTable)table).buildWhere());
		EXPUNGE = sb.toString();
		if (temporary != null) {
			sb.delete(0, sb.length());
			sb.append("INSERT INTO ").append(table.getName());
			//sb.append(" (id, value, interval_start, interval_end, strdfgeo) SELECT DISTINCT id, value, intervalStart, intervalEnd,strdfgeo FROM ");
			//sb.append(" (id, interval_start, interval_end, strdfgeo) SELECT DISTINCT id, intervalStart, intervalEnd,strdfgeo FROM ");
			sb.append(" (id, strdfgeo) SELECT DISTINCT id, strdfgeo FROM ");
			sb.append(temporary.getName()).append(" tmp\n");
			sb.append("WHERE NOT EXISTS (SELECT id FROM ").append(table.getName());
			sb.append(" val WHERE val.id = tmp.id)");
			INSERT_SELECT = sb.toString();

		}
		if (!table.isCreated()) {
			createTable(table);
			table.primaryIndex(PKEY);
			//the field being indexed was 'original' --> Removed
			//	if (isIndexingValues()) {
			//		table.index(VALUE_INDEX);
			//	}
		}
		else {
			table.count();
		}
		if (temporary != null && !temporary.isCreated()) {
			createTemporaryTable(temporary);
		}
	}

	public void close()
	throws SQLException
	{
		if (insertSelect != null) {
			insertSelect.close();
		}
		if (temporary != null) {
			temporary.close();
		}
		table.close();
	}


	/**
	 * FIXME careful here !!!!!!!!!!!!
	 * this is the one I am using
	 */

	/**
	 * 
	 * @param constraint the field contained in "value" --> currently string
	 * @param geom the field contained in "strdfgeo"
	 */
	//	public synchronized void insert(Number id, /*String constraint,*/ Timestamp interval_start, Timestamp interval_end, byte[] geom)
	//		throws SQLException, InterruptedException
	//	{
	//		/*ValueBatch batch = getValueBatch();
	//		if (isExpired(batch)) {
	//			batch = newValueBatch();
	//			initBatch(batch);
	//		}
	//		batch.setObject(1, id);
	//		batch.setObject(2, value);
	//		batch.setObject(3, constraint);
	//		batch.setString(4, mbb);
	//		
	//		batch.addBatch();
	//		queue(batch);
	//		*/
	//		//tests
	//		
	////		if (geom.equals("")) {
	////			throw new SQLException("The formula does not describe a finite point set.");
	////		}
	//		
	////		if (geom.equals(ConvexPolyhedron.INCONSISTENT)) {
	//		if (geom.length==0) {
	//			String psqlInsertLine =  "INSERT INTO geo_values (id,  interval_start, interval_end,strdfgeo) VALUES (?, ? , ?, NULL)";
	//			Connection conn = table.getConnection();
	//			PreparedStatement insertStmt = conn.prepareStatement(psqlInsertLine);
	//			
	//			System.out.println("//////////////////////// INCONSISTENT" + geom);
	//			
	//			[B@62f4739
	//			insertStmt.setLong(1, id.longValue());
	//			//insertStmt.setString(2, constraint);
	//			//insertStmt.setBytes(2, constraint);
	//			insertStmt.setTimestamp(2,interval_start);
	//			insertStmt.setTimestamp(3,interval_end);
	//			//insertStmt.setString(4, geom);
	//	
	//			insertStmt.executeUpdate();
	//		} else {	
	//			//String psqlInsertLine =  "INSERT INTO geo_values (id, value,interval_start, interval_end, strdfgeo) VALUES (?, ?, ?, ?, ST_GeometryFromText(?,-1))";
	//			String psqlInsertLine =  "INSERT INTO geo_values (id, interval_start, interval_end, strdfgeo) VALUES (?, ?, ?, ST_GeomFromWKB(?,-1))";
	//			
	//			Connection conn = table.getConnection();
	//			PreparedStatement insertStmt = conn.prepareStatement(psqlInsertLine);
	//			
	//			//System.out.println("//////////////////////// STR  " + value);
	//			//System.out.println("//////////////////////// GEOM " + geom);
	//			
	//			insertStmt.setLong(1, id.longValue());
	//			//insertStmt.setString(2, value);
	//			//insertStmt.setString(2, constraint);
	//			//insertStmt.setBytes(2, constraint);
	//			
	//			insertStmt.setTimestamp(2,interval_start);
	//			insertStmt.setTimestamp(3,interval_end);
	//			
	//			
	//			//insertStmt.setString(5, geom);//not using wkt any more -switched to WKB
	//			insertStmt.setBytes(4, geom);
	//			
	//			insertStmt.executeUpdate();
	//		}
	//	}


	public synchronized void insert(Number id, Integer srid,/*String constraint, Timestamp interval_start, Timestamp interval_end,*/ byte[] geom)
		throws SQLException, InterruptedException, NullPointerException
	{

	
		ValueBatch batch = getValueBatch();
		if (isExpired(batch)) {
			batch = newValueBatch();
			initBatch(batch);
		}
		batch.setObject(1, id);
		//batch.setObject(2, interval_start);
		//batch.setObject(3, interval_end);


		if(geom.length==0)
		{
			batch.setObject(2,null); 
		}
		else
		{
			batch.setBytes(2,geom);
		}
		batch.setObject(3, srid); //adding original srid-constant
		batch.setObject(4, srid);
		
		batch.addBatch();
		queue(batch);

	}
	
	/**
	 * FIXME careful here 
	 * Haven't used it yet
	 */
	/*public synchronized void insert(Number id, Number value, String strdfgeo, byte[] constraint)
		throws SQLException, InterruptedException
	{
		ValueBatch batch = getValueBatch();
		if (isExpired(batch)) {
			batch = newValueBatch();
			initBatch(batch);
		}
		batch.setObject(1, id);
		//batch.setObject(2, value);
		batch.setObject(2, constraint);
		batch.setString(3, strdfgeo);
		//batch.setString(4, constraint);

		batch.addBatch();
		queue(batch);


	}*/

	public ValueBatch getValueBatch() {
		return this.batch;
	}

	public boolean isExpired(ValueBatch batch) {
		if (batch == null || batch.isFull())
			return true;
		return queue == null || !queue.remove(batch);
	}

	public ValueBatch newValueBatch() {
		return new ValueBatch();
	}

	public void initBatch(ValueBatch batch)
	throws SQLException
	{
		batch.setTable(table);
		batch.setBatchStatement(prepareInsert(INSERT));
		batch.setMaxBatchSize(getBatchSize());
		if (temporary != null) {
			batch.setTemporary(temporary);
			if (insertSelect == null) {
				insertSelect = prepareInsertSelect(INSERT_SELECT);
			}
			batch.setInsertStatement(insertSelect);
		}
	}

	public void queue(ValueBatch batch)
	throws SQLException, InterruptedException
	{
		this.batch = batch;
		if (queue == null) {
			batch.flush();
		}
		else {
			queue.put(batch);
		}
	}

	public void optimize()
	throws SQLException
	{
		table.optimize();
	}

	public boolean expunge(String condition)
	throws SQLException
	{
		synchronized (table) {
			int count = table.executeUpdate(EXPUNGE + condition);
			if (count < 1)
				return false;
			table.modified(0, count);
			return true;
		}
	}

	public List<Long> maxIds(int shift, int mod)
	throws SQLException
	{
		String column = "id";
		StringBuilder expr = new StringBuilder();
		expr.append("MOD((").append(column);
		expr.append(" >> ").append(shift);
		expr.append(") + ").append(mod).append(", ");
		expr.append(mod);
		expr.append(")");
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append("MAX(");
		sb.append(column);
		sb.append("), ").append(expr).append(" AS grp");
		sb.append("\nFROM ").append(getName());
		sb.append("\nGROUP BY grp");
		String query = sb.toString();
		PreparedStatement st = table.prepareStatement(query);
		try {
			ResultSet rs = st.executeQuery();
			try {
				List<Long> result = new ArrayList<Long>();
				while (rs.next()) {
					result.add(rs.getLong(1));
				}
				return result;
			}
			finally {
				rs.close();
			}
		}
		finally {
			st.close();
		}
	}

	public String sql(int type, int length) {
		switch (type) {
		case Types.VARCHAR:
			if (length > 0)
				return "VARCHAR(" + length + ")";
			return "TEXT";
		case Types.LONGVARCHAR:
			if (length > 0)
				return "LONGVARCHAR(" + length + ")";
			return "TEXT";
		case Types.BIGINT:
			return "BIGINT";
		case Types.INTEGER:
			return "INTEGER";
		case Types.SMALLINT:
			return "SMALLINT";
		case Types.FLOAT:
			return "FLOAT";
		case Types.DOUBLE:
			return "DOUBLE";
		case Types.DECIMAL:
			return "DECIMAL";
		case Types.BOOLEAN:
			return "BOOLEAN";
		case Types.TIMESTAMP:
			return "TIMESTAMP";
			//FIXME my addition here	
		case Types.VARBINARY:
			//System.out.println("BYTE ARRAY NEEDED");
			return "BYTEA";
			//case 2112:
			//	System.out.println("Geometry NEEDED");
			//	return "GEOMETRY";
		default:
			throw new AssertionError("Unsupported SQL Type: " + type);
		}
	}

	@Override
	public String toString() {
		return getName();
	}

	protected RdbmsTable getInsertTable() {
		RdbmsTable tmp = getTemporaryTable();
		if (tmp == null) {
			tmp = getRdbmsTable();
		}
		return tmp;
	}

	protected PreparedStatement prepareInsert(String sql)
	throws SQLException
	{
		return table.prepareStatement(sql);
	}

	protected PreparedStatement prepareInsertSelect(String sql)
	throws SQLException
	{
		return table.prepareStatement(sql);
	}


	/**
	 * FIXME careful here 
	 * CURRENT FIELDS: id, value (byte[] form of Polyhedron, strdfgeo(wkt version of Polyhedron)
	 */
	protected void createTable(RdbmsTable table)
		throws SQLException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("  id ").append(sql(idType, -1)).append(" NOT NULL,");
		sb.append("  srid ").append(sql(Types.INTEGER, 20)).append(" NOT NULL\n"); //constant-SRID
		//sb.append("  original ").append(sql(sqlType, length)).append(" NOT NULL,\n");

		//FIXME very careful about these changes!!
		//sb.append("  value ").append(sql(Types.VARCHAR, length));
		//sb.append("  value ").append(sql(Types.VARBINARY, length));
		//sb.append(" NOT NULL\n,");

		//FIXME used to accommodate temporals
		//sb.append("  interval_start ").append("TIMESTAMP DEFAULT NULL").append(",\n");
		//sb.append("  interval_end ").append("TIMESTAMP DEFAULT NULL").append("\n");

		//sb.append("  value ").append(sql(sqlType, length)).append(" NOT NULL;\n");
		//sb.append("  constr ").append(sql(sqlType, length));

		//sb.append("  constr ").append("BYTEA");
		table.createTable(sb);
		
		String extension = ((GeneralDBSqlTable)table).buildGeometryCollumn();
		table.execute(extension);
		
		String index = ((GeneralDBSqlTable)table).buildIndexOnGeometryCollumn();
		table.execute(index);
	}

	/**
	 * FIXME careful here - has not been tested
	 */
	protected void createTemporaryTable(RdbmsTable table)
	throws SQLException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("  id ").append(sql(idType, -1)).append(" NOT NULL,");
		sb.append("  srid ").append(sql(Types.INTEGER, 20)).append(" NOT NULL\n"); //constant-SRID
		//sb.append("  original ").append(sql(sqlType, length));
		//sb.append(" NOT NULL,\n");
		//sb.append("  value ").append(sql(Types.VARCHAR, length));
		//sb.append("  value ").append(sql(Types.VARBINARY, length));
		//sb.append(" NOT NULL,\n");

		//FIXME used to accommodate temporals
		//sb.append("  interval_start ").append("TIMESTAMP DEFAULT NULL").append(",\n");
		//sb.append("  interval_end ").append("TIMESTAMP DEFAULT NULL").append("\n");
		//sb.append("  value ").append(sql(sqlType, length));
		//sb.append(" NOT NULL\n");
		//sb.append("  constr ").append(sql(sqlType, length));


		table.createTemporaryTable(sb);
//		String extension = "SELECT AddGeometryColumn('','geo_values','value',32630,'GEOMETRY',2)";
		String extension = ((GeneralDBSqlTable)table).buildGeometryCollumn();
		table.execute(extension);
	}
}

