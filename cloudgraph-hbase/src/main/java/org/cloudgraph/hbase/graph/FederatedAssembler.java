package org.cloudgraph.hbase.graph;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Result;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.hbase.io.FederatedReader;
import org.cloudgraph.hbase.io.RowReader;
import org.cloudgraph.state.GraphState.Edge;
import org.plasma.query.collector.PropertySelectionCollector;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.core.TraversalDirection;

/**
 * Supports the assembly of a directed data graph which may span multiple
 * HBase tables and/or rows by maintaining a stack of
 * row readers annotated with graph "level" and other information. 
 * This allows a specific row reader to be determined for any data object within a graph
 * based entirely or in part on its level within the graph. 
 * This is essential for data object types which are not 
 * configured as root types within any table, and yet a specific
 * row reader must be determined. 
 * 
 * @see org.cloudgraph.hbase.io.FederatedReader
 * @see org.cloudgraph.hbase.io.TableReader
 * @see org.cloudgraph.hbase.io.RowReader
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public abstract class FederatedAssembler extends DefaultAssembler 
    implements HBaseGraphAssembler
{
    private static Log log = LogFactory.getLog(FederatedAssembler.class);

	protected FederatedReader federatedReader;
	protected Stack<ReaderFrame> stack = new Stack<ReaderFrame>();

	/**
	 * Constructor. 
	 * @param rootType the federated graph root type
	 * @param collector the selection collector
	 * @param federatedReader the federated reader
	 * @param snapshotDate the query snapshot date
	 */
	public FederatedAssembler(PlasmaType rootType,
			PropertySelectionCollector collector,
			FederatedReader federatedReader,
			Timestamp snapshotDate) {
		super(rootType, collector, 
			federatedReader.getRootTableReader(),
			snapshotDate);
		this.federatedReader = federatedReader;
	}

	/**
     * Recursively re-constitutes a data graph federated across multiple
     * HBase tables and/or rows, starting with the given HBase client result row. 
     * <p>
     * To retrieve the graph use {@link FederatedGraphAssembler#getDataGraph()}.
     * a map of selected SDO properties. Properties are mapped by 
     * selected types required in the result graph.
     * </p>
	 * @param resultRow the HBase client
     * result (row).
	 */
	@Override
	public void assemble(Result resultRow) {
		
	    this.root = createRoot(resultRow);
		
		RowReader rowReader = this.rootTableReader.createRowReader(
			this.root, resultRow);
		this.federatedReader.mapRowReader(this.root, 
				rowReader);					
		ReaderFrame node = new ReaderFrame(rowReader, 0);
		stack.push(node);					
		
		try {
			assemble(this.root, null, null, rowReader, 0);
		} catch (IOException e) {
			throw new GraphServiceException(e);
		}
	}
	
	/**
	 * Populates the given data object target, recursively fetching
	 * data for and linking related data objects which make up the 
	 * resulting directed graph. 
	 * @param target the current data object target
	 * @param source the source or parent data object
	 * @param sourceProperty the source (reference) property
	 * @param rowReader the current row reader
	 * @param level the current graph level
     * @throws IOException if a remote or network exception occurs.
	 */
	protected abstract void assemble(PlasmaDataObject target, 
		PlasmaDataObject source, PlasmaProperty sourceProperty, 
		RowReader rowReader, int level) throws IOException;

	/**
	 * Returns a row reader based entirely on the given graph
	 * (traversal) level. 
	 * @param level the graph traversal level
	 * @return the row reader
	 */
	protected RowReader getRowReader(int level)
	{
    	ReaderFrame top = this.stack.peek();
    	while (top.getLevel() > level) {
    		this.stack.pop();
    		top = this.stack.peek();
			if (log.isDebugEnabled())
				log.debug("removed reader stack to level "
					+ top.getLevel() + " table: " 
					+ top.getRowReader().getTableReader().getTable().getName());
    	}
		return top.getRowReader();
	}
		
	
	protected void assembleEdge(PlasmaDataObject target, PlasmaProperty prop,
		Edge edge, PlasmaDataObject child, RowReader childRowReader, int level) throws IOException {
		ReaderFrame node = new ReaderFrame(childRowReader, level+1);
		stack.push(node);
    	this.federatedReader.mapRowReader(child, 
				childRowReader);					
        
		if (log.isDebugEnabled())
			log.debug("traverse: (" + prop.getName() + ") " + String.valueOf(edge.getId()));					
		
		// FIXME: if left, then create a link only??
		if (edge.getDirection().ordinal() == TraversalDirection.RIGHT.ordinal())
	        assemble(child, target, prop, childRowReader, level+1);		
	}	
	
	/**
	 * Resets the assembler.
	 */
	@Override
	public void clear() {
		this.root = null;
		this.stack.clear();
		this.federatedReader.clear();
	}	
}
