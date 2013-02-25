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
package org.cloudgraph.hbase.io;

import java.util.List;

import org.apache.hadoop.hbase.client.Result;

import commonj.sdo.DataObject;


/**
 * Provides access to the operational, configuration and other state information
 * required for read operations on a single graph table. 
 * <p>
 * Acts as a container for one or more {@link RowReader} elements
 * and encapsulates the HBase client <a target="#" href="http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/client/Get.html">Get</a> 
 * operations for use in read operations across one or more graph rows. 
 * </p>
 * 
 * @see org.cloudgraph.hbase.io.RowReader
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public interface TableReader extends TableOperation {

	/**
	 * Returns the row reader context for the given UUID string
	 * @param uuid the UUID string
	 * @return the row reader context for the given UUID string
	 */
	public RowReader getRowReader(String uuid);
	
	/**
	 * Returns the row reader context for the given data object
	 * @param dataObject the data object
	 * @return the row reader context for the given data object
	 */
	public RowReader getRowReader(DataObject dataObject);
	
	/**
	 * Adds the given row reader context mapping it to the
	 * given UUID string.
	 * @param uuid the UUID string
	 * @param rowContext the row reader context
	 */
	public void addRowReader(String uuid, 
			RowReader rowContext);

	/**
	 * Creates and adds a row reader based on the given
	 * data object. If a reader else already mapped for the
	 * given data object, the existing reader is returned. 
	 * @param dataObject
	 * @return the row reader
	 */
	public RowReader createRowReader(DataObject dataObject,
			Result resultRow);
	
	/**
	 * Returns all row reader context values for this table context.
	 * @return all row reader context values for this table context.
	 */
	public List<RowReader> getAllRowReaders();

	
	/**
     * Frees resources associated with this reader and any
     * component readers. 
     */
    public void clear();
}
