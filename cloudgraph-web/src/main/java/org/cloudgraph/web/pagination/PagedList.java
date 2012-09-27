package org.cloudgraph.web.pagination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.query.bind.PlasmaQueryDataBinding;
import org.plasma.query.model.Clause;
import org.plasma.query.model.OrderBy;
import org.plasma.query.model.Property;
import org.plasma.query.model.Query;
import org.plasma.query.model.SortDirectionValues;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.xml.sax.SAXException;

import commonj.sdo.DataGraph;

/**
 * 
 * The Faces pagination is server side component to facilitate viewing of large
 * and huge data result sets from database queries. <p/> The default
 * implementation of viewing query results is to load all the relevant data from
 * query results into memory in java bean form and to iterate through them as
 * needed from memory. This has some critical drawbacks. <p/> The first draw
 * back is that database has to return entire result set of the query, thus
 * making database to do more work then needed. The results are only displayed
 * in certain given viewable size at a time. <p/> The second, more serious draw
 * back is for each user, such large memory is required, making memory usage to
 * be extremely inefficient to the point where making web application might be
 * unable to support required number of current users. <p/> The third draw back
 * is requiring web applications to iterate through entire result set in order
 * to prepare for result set to work with view table. <p/> The proposed solution
 * is to implement a way to load only displaying data or partial result set from
 * the database query. And load the each displaying set as needed on demand.
 * Such scheme is known as lazy loading or �on demand loading algorithms.
 * <p/> The pagination component is designed to be a �drop in solution for
 * MyFaces Tomahawk DataTable component. However, no actual dependency to
 * MyFaces package exists. Thus, allowing possibility to be used with other
 * comparable package to MyFaces <p/> A significant consideration was given to
 * produce correct sort results based on user column selection of view table.
 * With lazy loading, majority of actual data set will not be available in
 * memory for sort operation but still provide seem-less operations as default
 * implementation <p/> Third consideration was given to speed performance aspect
 * of pagination. The operations must be reasonably fast enough not to degrade
 * user experience compare to default implementation.
 */
public class PagedList implements List {

	private static Log log = LogFactory.getLog(PagedList.class);

	/**
	 * QoM based query to be used for data loading.
	 * 
	 */
	private Query query = null;

	/**
	 * Total count of entire result set.
	 * 
	 */
	private int totalCount = 0;

	/**
	 * starting index range of cached data.
	 * 
	 */
	private int startRange = 0;

	/**
	 * number of rows to be cached at a time. <p/> It is highly recommanded that
	 * this number match the viewable number of rows in the view table.
	 * 
	 */
	private int fetchCount = 20;

	/**
	 * Cache of displayed data
	 * 
	 */
	private ArrayList cache = new ArrayList();

	/**
	 * column sort directional marker.
	 * 
	 */
	private boolean _ascending = true;

	/**
	 * sorting column name
	 * 
	 */
	private String _sort = null;

	/**
	 * interface to handle the result set after query.
	 * 
	 */
	private Processor processor = null;

	private String prevQuery = null;

	private boolean sortDirty = false;

	private Object first = null;

	/**
	 * returns the object implementing the Processor interface.
	 * 
	 * @return Processor
	 */
	public Processor getProcessor() {
		return processor;
	}

	/**
	 * sets the object implementing the Processor interface.
	 * 
	 * @param processor
	 */
	public void setProcessor(Processor processor) {
		this.processor = processor;
		if (processor != null)
			processor.setPagedList(this);
	}

	/**
	 * gets sort direction.
	 * 
	 * @return true for acending, false for descending.
	 */
	public boolean isAscending() {
		return _ascending;
	}

	/**
	 * sets sort direction.
	 * 
	 * @param ascending -
	 *            true for acending, false for descending.
	 */
	public void setAscending(boolean ascending) {
		if (this._ascending != ascending) {
			sortDirty = true;
			this._ascending = ascending;
		}
	}

	/**
	 * gets sort column name string
	 * 
	 * @return sorting column name
	 */
	public String getSort() {
		return _sort;
	}

	/**
	 * sets sort column name string
	 * 
	 * @param sort -
	 *            column name
	 */
	public void setSort(String sort) {
		if (this._sort == null && sort == null)
			return;

		if (this._sort == null || sort == null
				|| (!this._sort.equalsIgnoreCase(sort))) {
			sortDirty = true;
			this._sort = sort;
		}
	}

	/**
	 * gets QoM based database query.
	 * 
	 * @return QoM query used to retrieve data.
	 */
	public Query getQuery() {
		return query;
	}

	/**
	 * sets Plasma based database query.
	 * 
	 * @param query -
	 *            Plasma Query used to retrieve data.
	 */
	public void setQuery(Query query) {
		if (log.isDebugEnabled())
			log.debug("*** setQuery");

		String q1 = prevQuery;
		String q2 = marshallQuery(query);

		// this check for same/dup query assignment.
		if (log.isDebugEnabled()) {
			log.debug("q1: " + q1);
			log.debug("q2: " + q2);
		}
		if (q1 != null && q2 != null && q1.equalsIgnoreCase(q2)) {
			if (log.isDebugEnabled())
				log.debug("*** same query set.");
			return;
		} else {
			if (log.isDebugEnabled())
				log.debug("*** diff query set.");
		}
		prevQuery = q2;
		this.query = query;
		first = null;
		if (this.query != null) {
			SDODataAccessClient serviceProxy = new SDODataAccessClient();
			totalCount = serviceProxy.count(this.query);
			startRange = 0;
			if (log.isDebugEnabled())
				log.debug("*** total count " + totalCount);
		}
		cache.clear();
	}

	private String marshallQuery(final Query query) {
		if (query == null)
			return null;
        try {
            PlasmaQueryDataBinding binding = new PlasmaQueryDataBinding(
                    new DefaultValidationEventHandler());
            if (log.isDebugEnabled())
                log.debug("marshaling query");
            return binding.marshal(query);
        } catch (JAXBException e) {
            log.error(e.getMessage(), e);
            return null;
        } catch (SAXException e) {
            log.error(e.getMessage(), e);
            return null;
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
	}

	/**
	 * gets FetchCount used for data cache size.
	 * 
	 * @return fetchCount attribute
	 */
	public int getFetchCount() {
		return fetchCount;
	}

	/**
	 * sets FetchCount used for data cache size.
	 * 
	 * @param fetchCount
	 */
	public void setFetchCount(int fetchCount) {
		this.fetchCount = fetchCount;
	}

	/**
	 * gets start range index of cache data.
	 * 
	 * @return start range of cache index
	 */
	public int getStartRange() {
		return startRange;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#get(int)
	 */
	public Object get(int index) {
		if (log.isDebugEnabled()) {
			log.debug("*** GET total count " + totalCount);
			log.debug("start: " + startRange);
			log.debug("index: " + index);
		}
		if (index == 0 && first != null && sortDirty == false)
			return first;
		if (!isCached(index)) {
			if (log.isDebugEnabled())
				log.debug("flushing cache.");
			OrderBy orderbyOrig = null;

			// flush cache.
			cache.clear();
			sortDirty = false;

			if (log.isDebugEnabled())
				log.debug("new start: " + index);
			startRange = index;
			// set the new range
			query.setStartRange(index);
			query.setEndRange(index + fetchCount);

			// set sort option
			if (getSortColumn()) {
				orderbyOrig = query.findOrderByClause();
				query.clearOrderByClause();
				OrderBy newOrder = getSortOrderBy();
				if (newOrder != null)
					query.getClauses().add(new Clause(newOrder));
			}

			SDODataAccessClient serviceProxy = new SDODataAccessClient();
			DataGraph[] results = serviceProxy.find(query);

			// restore orderby if any.
			if (getSortColumn()) {
				query.clearOrderByClause();
				if (orderbyOrig != null)
					query.getClauses().add(new Clause(orderbyOrig));
			}

			// reset range to set count straight.
			query.setEndRange(-1);
			query.setStartRange(-1);

			if (log.isDebugEnabled())
				log.debug("**** results count: " + results.length);
			cache.addAll(processResults(results));
		}
		// double check for data after load to see if in cache.
		if (isCached(index)) {
			try {
				if (startRange == 0)
					first = cache.get(0);
				return cache.get(index - startRange);
			} catch (java.lang.IndexOutOfBoundsException e) {
				log.error("total count " + totalCount + " start: " + startRange
						+ "index: " + index + "cache size: " + cache.size(), e);
				throw e;
			}
		} else {
			log.warn("Can't load index. totalcount " + totalCount + " start: "
					+ startRange + "index: " + index + "cache size: "
					+ cache.size());
			return null;
		}
	}

	private boolean isCached(int index) {
		int cachesize = cache.size();
		if (sortDirty == false
				&& cachesize > 0
				&& ((index - startRange) >= 0 && index < (startRange + cachesize)))
			return true;
		else
			return false;
	}

	/**
	 * get status of sort column no sort column by default
	 * 
	 * @return true if sort column is specified, false if not.
	 */
	private boolean getSortColumn() {
		boolean sortOn = false;

		if (processor != null) {
			sortOn = (processor.getSortColumnString(_sort) != null || processor
					.getSortColumnProperty(_sort) != null);
		}
		return sortOn;
	}

	/**
	 * get OrderBy based on sort column.
	 * 
	 * @return QoM OrderBy clause based on sort column.
	 */
	private OrderBy getSortOrderBy() {
		if (processor == null)
			return null;

		String colString = processor.getSortColumnString(_sort);
		if (colString != null) {
			if (_ascending)
				colString += " ascending ";
			else
				colString += " descending ";
			return new OrderBy(colString);
		}

		Property colProperty = processor.getSortColumnProperty(_sort);
		if (colProperty != null) {
			// origDirection = colProperty.getDirection();
			if (_ascending)
				colProperty.setDirection(SortDirectionValues.ASC);
			else
				colProperty.setDirection(SortDirectionValues.DESC);
			return new OrderBy(colProperty);
		}
		return null;
	}

	/**
	 * enable and handles processing of faces results beans. This can be
	 * over-ridded or Processor interface is used or basicProcessor can be
	 * extends to accomplish same thing.
	 * 
	 * @param results -
	 *            faces results beans.
	 * @return List of java beans to be used for table view.
	 */
	protected List processResults(DataGraph[] results) {
		if (processor != null)
			return processor.processResults(results);

		List l = new ArrayList();
		for (int i = 0; i < results.length; i++)
			l.add(results[i]);
		return l;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#size()
	 */
	public int size() {
		int size = 0;
		if (query != null)
			size = totalCount;
		if (log.isDebugEnabled())
			log.debug("**** size: " + totalCount);
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#clear()
	 */
	public void clear() {
		this.cache.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#isEmpty()
	 */
	public boolean isEmpty() {
		return this.cache.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray()
	 */
	public Object[] toArray() {
		return new Object[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#remove(int)
	 */
	public Object remove(int index) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	public void add(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	public boolean add(Object o) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection c) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#iterator()
	 */
	public Iterator iterator() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#subList(int, int)
	 */
	public List subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#listIterator()
	 */
	public ListIterator listIterator() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator listIterator(int index) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	public Object set(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray(java.lang.Object[])
	 */
	public Object[] toArray(Object[] a) {
		throw new UnsupportedOperationException();
	}

}
