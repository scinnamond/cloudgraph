package org.cloudgraph.hbase.graph;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Result;
import org.cloudgraph.common.service.GraphServiceException;
import org.cloudgraph.hbase.io.FederatedReader;
import org.cloudgraph.hbase.io.RowReader;
import org.cloudgraph.state.GraphState.Edge;
import org.plasma.query.collector.PropertySelection;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.core.CoreNode;

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

	/**
	 * Constructor. 
	 * @param rootType the federated graph root type
	 * @param selection the selection collector
	 * @param federatedReader the federated reader
	 * @param snapshotDate the query snapshot date
	 */
	public FederatedAssembler(PlasmaType rootType,
			PropertySelection selection,
			FederatedReader federatedReader,
			Timestamp snapshotDate) {
		super(rootType, selection, 
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
	
	protected void assembleEdge(PlasmaDataObject target, PlasmaProperty prop,
		Edge edge, PlasmaDataObject child, RowReader childRowReader, int level) throws IOException {
    	this.federatedReader.mapRowReader(child, 
				childRowReader);					
        
		if (log.isDebugEnabled())
			log.debug("traverse: (" + prop.getName() + ") " + String.valueOf(edge.getId()));					
		
        assemble(child, target, prop, childRowReader, level+1);		
	}	
	
	/**
	 * Creates contained child data object with the same type
	 * as the given containment property or of a specific sub-type
	 * as determined by querying the graph state.
	 * 
	 * @param target the container data object
	 * @param prop the containment property
	 * @param edge the edge
	 * @param rowReader the row reader
	 * @return the new child data object
	 * @throws IOException 
	 */
	protected PlasmaDataObject createChild(PlasmaDataObject target, PlasmaProperty prop,
			Edge edge, RowReader rowReader) throws IOException {
		PlasmaType edgeType = edge.getType();
		if (log.isDebugEnabled())
			log.debug("creating data object ("
		        + edge.getUuid()+ ") type:  "
		        + edgeType.toString());
		PlasmaDataObject child = (PlasmaDataObject)target.createDataObject(prop, edge.getType());								
		CoreNode childDataNode = (CoreNode)child;
		childDataNode.setValue(CoreConstants.PROPERTY_NAME_UUID, 
			UUID.fromString(edge.getUuid()));
		return child;		
	}
	
	/**
	 * Peeks at the first edge and determines whether 
	 * an external edge collection
	 * @param edges the state edges
	 * @param rowReader the reader
	 * @return whether 
	 * an external edge collection
	 * @throws IOException
	 */
	protected boolean isExternal(Edge[] edges, RowReader rowReader) throws IOException {
		if (edges.length > 0) {
		    return rowReader.getGraphState().findRowKey(
				edges[0].getUuid()) != null;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Resets the assembler.
	 */
	@Override
	public void clear() {
		this.root = null;
		this.federatedReader.clear();
	}	
}
