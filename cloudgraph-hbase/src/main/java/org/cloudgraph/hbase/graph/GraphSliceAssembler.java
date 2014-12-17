/**
 *        CloudGraph Community Edition (CE) License
 * 
 * This is a community release of CloudGraph, a dual-license suite of
 * Service Data Object (SDO) 2.1 services designed for relational and 
 * big-table style "cloud" databases, such as HBase and others. 
 * This particular copy of the software is released under the 
 * version 2 of the GNU General Public License. CloudGraph was developed by 
 * TerraMeta Software, Inc.
 * 
 * Copyright (c) 2013, TerraMeta Software, Inc. All rights reserved.
 * 
 * General License information can be found below.
 * 
 * This distribution may include materials developed by third
 * parties. For license and attribution notices for these
 * materials, please refer to the documentation that accompanies
 * this distribution (see the "Licenses for Third-Party Components"
 * appendix) or view the online documentation at 
 * <http://cloudgraph.org/licenses/>. 
 */
package org.cloudgraph.hbase.graph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.util.Bytes;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.DataGraphConfig;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.hbase.filter.GraphFetchColumnFilterAssembler;
import org.cloudgraph.hbase.filter.HBaseFilterAssembler;
import org.cloudgraph.hbase.io.DistributedReader;
import org.cloudgraph.hbase.io.RowReader;
import org.cloudgraph.hbase.io.TableReader;
import org.cloudgraph.query.expr.Expr;
import org.cloudgraph.query.expr.ExprPrinter;
import org.cloudgraph.recognizer.GraphRecognizerContext;
import org.cloudgraph.recognizer.GraphRecognizerSyntaxTreeAssembler;
import org.cloudgraph.state.GraphState;
import org.cloudgraph.state.GraphState.Edge;
import org.plasma.query.collector.Selection;
import org.plasma.query.collector.SelectionCollector;
import org.plasma.query.model.Where;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.Property;
import commonj.sdo.helper.XMLDocument;

/**
 * Constructs a data graph "sliced" using any number of path predicates 
 * starting with a given root SDO type and based on
 * a given selection map of SDO properties and associated predicates.
 * <p>
 * The assembly is triggered by calling the 
 * {@link GraphSliceAssembler#assemble(Result resultRow)} method which
 * recursively reads HBase keys and values incrementally re-constituting the
 * data graph. The assembly traversal is driven by HBase column 
 * values representing the original edges or containment structure 
 * of the graph. 
 * </p>
 * <p>
 * Since every column key in HBase must be unique, and a data graph
 * may contain any number of nodes, a column key factory is used both 
 * to persist as well as re-constitute a graph. A minimal amount of
 * "state" information is therefore stored with each graph which maps
 * user readable sequence numbers (which are used in column keys) to
 * UUID values. The nodes of the resulting data graph are re-created with
 * the original UUID values.       
 * </p>
 * 
 * @see org.cloudgraph.hbase.key.StatefullColumnKeyFactory
 * @see org.cloudgraph.hbase.graphGraphSliceSupport
 *   
 * @author Scott Cinnamond
 * @since 0.5.1
 * 
 */
public class GraphSliceAssembler extends DistributedAssembler {

    private static Log log = LogFactory.getLog(GraphSliceAssembler.class);
	private int scanCount;
	private GraphSliceSupport sliceSupport = new GraphSliceSupport();
	private Charset charset;

	public GraphSliceAssembler(PlasmaType rootType,
			Selection selection,
			DistributedReader distributedReader, 
			Timestamp snapshotDate) {
		super(rootType, selection, distributedReader, snapshotDate);
		this.charset = Charset.forName( CoreConstants.UTF8_ENCODING );
	}

	@Override
	protected void assemble(PlasmaDataObject target, 
			PlasmaDataObject source, PlasmaProperty sourceProperty, 
			RowReader rowReader, int level) throws IOException
    {		 
		Set<Property> props = this.getProperties(target, source, sourceProperty, level);
		if (props.size() == 0) 
			return;
        if (log.isDebugEnabled())
			log.debug("assembling("+level+"): " + target.toString() + ": " + props.toString());
		
		assembleData(target, props, rowReader);
		
		
		TableReader tableReader = rowReader.getTableReader();
		TableConfig tableConfig = tableReader.getTable();

		// reference props
		for (Property p : props) {
			PlasmaProperty prop = (PlasmaProperty)p;
			if (prop.getType().isDataType())
				continue;
			
			byte[] keyValue = getColumnValue(target, prop, 
				tableConfig, rowReader);
			if (keyValue == null || keyValue.length == 0 ) {
				continue; // zero length can happen on modification or delete as we keep cell history
			}
			
			Edge[] edges = rowReader.getGraphState().unmarshalEdges( 
				keyValue);
			
			PlasmaType childType = (PlasmaType)prop.getType();
			
			// NOTE: can we have predicates on singular props? 
			Where where = this.selection.getPredicate(prop);
			
			boolean external = isExternal(edges, rowReader);			
			if (!external) { 								
				Map<Integer, Integer> sequences = null;
				if (prop.isMany() && where != null) {
			    	sequences = this.sliceSupport.fetchSequences((PlasmaType)prop.getType(), 
			    			where, rowReader);
			    	// preload properties for the NEXT level into the current row so we have something to assemble
					Set<Property> childProperies = this.selection.getInheritedProperties(prop.getType(), level+1); 
					this.sliceSupport.loadBySequenceList(sequences.values(), 
							childProperies,
			    		childType, rowReader);
				}
				else {  
			    	// preload properties for the NEXT level into the current row so we have something to assemble
					Set<Property> childProperies = this.selection.getInheritedProperties(prop.getType(), level+1);
				    this.sliceSupport.load(childProperies,
			    			childType, rowReader);
				}			
				
	        	assembleEdges(target, prop, edges, sequences, rowReader, 
	        			rowReader.getTableReader(), 
	        			rowReader, level);			
	        }
			else 
			{
				String childTable = rowReader.getGraphState().getRowKeyTable(edges[0].getUuid());
				TableReader externalTableReader = distributedReader.getTableReader(childTable);
				
				if (log.isDebugEnabled())
					if (!tableConfig.getName().equals(externalTableReader.getTable().getName()))
					    log.debug("switching row context from table: '"
						    + tableConfig.getName() + "' to table: '"
						    + externalTableReader.getTable().getName() + "'");
				Map<String, Result> resultRows = null;
				if (prop.isMany() && where != null) {
					 resultRows = this.filter(childType, edges, 
						where, rowReader, externalTableReader);					
				}
				assembleExternalEdges(target, prop, edges, rowReader,	
					resultRows, externalTableReader, level);
			}			
		}
    }
	
	private void assembleEdges(PlasmaDataObject target, PlasmaProperty prop, 
			Edge[] edges, Map<Integer, Integer> sequences, RowReader rowReader, 
			TableReader childTableReader, RowReader childRowReader,
			int level) throws IOException 
	{
		for (Edge edge : edges) {	
			UUID uuid = UUID.fromString(edge.getUuid());
      	    if (childRowReader.contains(uuid))
        	{            		
        		// we've seen this child before so his data is complete, just link 
        		PlasmaDataObject existingChild = (PlasmaDataObject)childRowReader.getDataObject(uuid);
        		link(existingChild, target, prop);
        		continue; 
        	}
        	if (sequences != null && sequences.get(edge.getId()) == null)
				continue; // screen out edges
			
			if (log.isDebugEnabled())
				log.debug("local edge: " 
			        + target.getType().getURI() + "#" +target.getType().getName()
			        + "->" + prop.getName() + " (" + edge.getUuid() + ")");
         	// create a child object
			PlasmaDataObject child = createChild(target, prop, edge);			
            childRowReader.addDataObject(child);
            
			assembleEdge(target, prop, edge, 
			        child, childRowReader, level);
		}
	}	
	
	// Target is a different row, within this table or another.
	// Since we are assembling a graph, each edge requires
	// a new row reader. 
	// each edge is a new root in the target table
	// so need a new row reader for each
	private void assembleExternalEdges(PlasmaDataObject target, PlasmaProperty prop, 
			Edge[] edges, RowReader rowReader, Map<String, Result> resultRows,
			TableReader childTableReader, int level) throws IOException 
	{
		RowReader childRowReader = null;
		for (Edge edge : edges) {	
			// need to look up an existing row reader based on the root UUID of the external graph
			// or the row key, and the row key is all we have in the local graph state. The edge UUID
			// is a local graph UUID. 
			byte[] childRowKey = rowReader.getGraphState().getRowKey(edge.getUuid()); // use local edge UUID
			RowReader existingChildRowReader = childTableReader.getRowReader(childRowKey);
        	if (existingChildRowReader != null)
        	{            		
        		// we've seen this child before so his data is complete, just link 
        		PlasmaDataObject existingChild = (PlasmaDataObject)existingChildRowReader.getRootDataObject();
        		link(existingChild, target, prop);
        		continue; 
        	}
			String childRowKeyStr = new String(childRowKey, this.charset);
			if (resultRows != null && resultRows.get(childRowKeyStr) == null)
				continue; //not found in predicate
			
			// create a row reader for every external edge
			Result childResult = fetchGraph(childRowKey, childTableReader, edge.getType());
	    	if (childResult.containsColumn(rootTableReader.getTable().getDataColumnFamilyNameBytes(), 
	    			GraphState.TOUMBSTONE_COLUMN_NAME_BYTES)) {
	    		log.warn("ignoring toubstone result row '" + 
	    			Bytes.toString(childRowKey) + "'");
				continue; // ignore toumbstone edge
	    	}
	    	
	        // need to reconstruct the original graph, so need original UUID
			byte[] rootUuid = childResult.getValue(Bytes.toBytes(
					childTableReader.getTable().getDataColumnFamilyName()), 
	                Bytes.toBytes(GraphState.ROOT_UUID_COLUMN_NAME));
			if (rootUuid == null)
				throw new GraphServiceException("expected column: "
					+ childTableReader.getTable().getDataColumnFamilyName() + ":"
					+ GraphState.ROOT_UUID_COLUMN_NAME);
			String uuidStr = null;
			uuidStr = new String(rootUuid, 
					childTableReader.getTable().getCharset());
			UUID uuid = UUID.fromString(uuidStr);	    	
			if (log.isDebugEnabled())
				log.debug("external edge: " 
			        + target.getType().getURI() + "#" +target.getType().getName()
			        + "->" + prop.getName() + " (" + uuid.toString() + ")");			
	    	
        	// create a child object
			PlasmaDataObject child = createChild(target, prop, edge, uuid);
			childRowReader = childTableReader.createRowReader(
				child, childResult);
			
			assembleEdge(target, prop, edge, 
		        child, childRowReader, level);
		}
	}
	 
	/**
	 * Creates and executes a "sub-graph" filter based on the given state-edges and path predicate
	 * and then excludes appropriate results based on a {@link GraphRecognizerSyntaxTreeAssembler binary syntax tree} assembled 
	 * from the same path predicate. Each sub-graph must first be assembled to do any evaluation, but
	 * a single syntax tree instance evaluates every sub-graph 
	 * (potentially thousands/millions) resulting
	 * from the given edge collection. The graph {@link Selection selection criteria} is based not on the 
	 * primary graph selection but only on the properties found in the given path predicate, so the
	 * assembly is only/exactly as extensive as required by the predicate.  
	 * Any sub-graphs assembled may themselves be "distributed" graphs.   
	 *  
	 * @param contextType the current type
	 * @param edges the state edge set
	 * @param where the path predicate
	 * @param rowReader the row reader
	 * @param tableReader the table reader
	 * @return the results filtered results
	 * @throws IOException
	 * @see GraphRecognizerSyntaxTreeAssembler
	 * @see SelectionCollector
	 * @see Selection
	 */
	private Map<String, Result> filter(  
			PlasmaType contextType, Edge[] edges, 
			Where where, RowReader rowReader, TableReader tableReader) throws IOException
	{
		Map<String, Result> results = new HashMap<String, Result>();
		     	
        SelectionCollector selectionCollector = new SelectionCollector(
               where, contextType);

        HBaseGraphAssembler graphAssembler = new GraphAssembler(contextType,
        		selectionCollector, (DistributedReader)tableReader.getFederatedOperation(), 
       			snapshotDate);
   	
        GraphRecognizerSyntaxTreeAssembler recognizerAssembler = new GraphRecognizerSyntaxTreeAssembler(
        		where, contextType);
        Expr graphRecognizerRootExpr = recognizerAssembler.getResult();
        if (log.isDebugEnabled()) {
            ExprPrinter printer = new ExprPrinter();
            graphRecognizerRootExpr.accept(printer);
            log.debug("Graph Recognizer: " + printer.toString());
        }
        
        // column filter
        HBaseFilterAssembler columnFilterAssembler = 
     		new GraphFetchColumnFilterAssembler(
     				this.selection, contextType);
        Filter columnFilter = columnFilterAssembler.getFilter();       
        
        List<Get> gets = new ArrayList<Get>();
		for (Edge edge : edges) {	
			byte[] childRowKey = rowReader.getGraphState().getRowKey(edge.getUuid()); // use local edge UUID
			Get get = new Get(childRowKey);
			get.setFilter(columnFilter);
			gets.add(get);		
		}
		DataGraphConfig graphConfig = CloudGraphConfig.getInstance().getDataGraph(
				contextType.getQualifiedName());
        Result[] rows = this.sliceSupport.fetchResult(gets, tableReader, 
    			graphConfig);
        
    	GraphRecognizerContext recognizerContext = new GraphRecognizerContext();
        int rowIndex = 0;
        for (Result resultRow : rows) {
        	if (resultRow == null || resultRow.isEmpty()) {
        		Get get = gets.get(rowIndex);
        		String rowStr = new String(get.getRow(), charset);
        		if (resultRow == null)
        		    throw new IllegalStateException("got null result row for '" + rowStr + "' for mulit-get operation - indicates failure with retries");
        		else
        		    throw new IllegalStateException("got no result for row for '" + rowStr + "' for mulit-get operation - indicates row noes not exist");
        	}
        	
      	    graphAssembler.assemble(resultRow);            	
        	PlasmaDataGraph assembledGraph = graphAssembler.getDataGraph();
            graphAssembler.clear();
        	
        	recognizerContext.setGraph(assembledGraph);
        	if (!graphRecognizerRootExpr.evaluate(recognizerContext)) {
        		if (log.isDebugEnabled())
        			log.debug("recognizer excluded: " + Bytes.toString(
        					resultRow.getRow()));
        		if (log.isDebugEnabled())
        			log.debug(serializeGraph(assembledGraph));
        		
        		continue;
        	}

        	String rowKey = new String(resultRow.getRow(), charset);
        	results.put(rowKey, resultRow);
        	rowIndex++;
        }
		
		return results;
	}
		
    protected String serializeGraph(commonj.sdo.DataGraph graph) throws IOException
    {
        DefaultOptions options = new DefaultOptions(
        		graph.getRootObject().getType().getURI());
        options.setRootNamespacePrefix("debug");
        
        XMLDocument doc = PlasmaXMLHelper.INSTANCE.createDocument(graph.getRootObject(), 
        		graph.getRootObject().getType().getURI(), 
        		null);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
	    PlasmaXMLHelper.INSTANCE.save(doc, os, options);        
        os.flush();
        os.close(); 
        String xml = new String(os.toByteArray());
        return xml;
    }
}
