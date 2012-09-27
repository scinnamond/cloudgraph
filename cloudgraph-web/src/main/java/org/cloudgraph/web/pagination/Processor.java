package org.cloudgraph.web.pagination;

import java.util.List;

import org.plasma.query.model.Property;

import commonj.sdo.DataGraph;

public interface Processor {

	/** gets table view specific column to query field translation.
	 * 
	 * @param column - string of column being sorted.
	 * @return - string of JDO based column to use on query sorting.
	 */
	public String getSortColumnString(String column);

	/** gets table view specific column to query field translation.
	 * 
	 * @param column - string of column being sorted.
	 * @return - Property of QoM based column to use on query sorting.
	 */
	public Property getSortColumnProperty(String column);

	/** Processes results set returned from database. It returns java beans
	 *  used for view table.
	 *  
	 * @param results - array of DPS Faces Beans
	 * @return - List of java beans to be used in view table.
	 */
	public List processResults(DataGraph[] results);
	
	/** set the paged list object. It is mainly to facilitate the 
	 *  row id of a given result.
	 *  
	 * @param pagedList - paged list get useful info.
	 */
	public void setPagedList(PagedList pagedList);

}
