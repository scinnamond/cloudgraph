package org.cloudgraph.common.concurrent;


/**
 * A concurrent task which processes a portion of a data graph or sub-graph.  
 * @author Scott Cinnamond
 * @since 0.6.2
 */
public interface SubgraphTask
{
    public void start();
    public void join();
}
