package org.cloudgraph.cassandra;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.NotFoundException;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class ThriftClient
{
    public static void main(String[] args)
    throws TException, InvalidRequestException, UnavailableException, UnsupportedEncodingException, NotFoundException, TimedOutException
    {
    	//CREATE KEYSPACE keyspace_thrift
    	//WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
    	
    	//CREATE TABLE Standard1 (
    	//		  name text PRIMARY KEY,
    	//		  age int
    	//		);
    	
    	//CREATE COLUMNFAMILY CF1( name varchar, age int, PRIMARY KEY (name));
    	
        TTransport tr = new TFramedTransport(new TSocket("u16551142.onlinehome-server.com", 9160));
        TProtocol proto = new TBinaryProtocol(tr);
        Cassandra.Client client = new Cassandra.Client(proto);
        tr.open();

        String key_user_id = "1111";

        // insert data
        long timestamp = System.currentTimeMillis();
        client.set_keyspace("keyspace_thrift");      
        ColumnParent parent = new ColumnParent("cf1");

        Column nameColumn = new Column(toByteBuffer("name"));
        nameColumn.setValue(toByteBuffer("Chris Goffinet"));
        nameColumn.setTimestamp(timestamp);
        client.insert(toByteBuffer(key_user_id), parent, nameColumn, ConsistencyLevel.ONE);
        
        Column ageColumn = new Column(toByteBuffer("age"));
        ageColumn.setValue(toByteBuffer("24"));
        ageColumn.setTimestamp(timestamp);
        client.insert(toByteBuffer(key_user_id), parent, ageColumn, ConsistencyLevel.ONE);

        /*
        ColumnPath path = new ColumnPath("cf1");

        // read single column
        path.setColumn(toByteBuffer("name"));
        System.out.println(client.get(toByteBuffer(key_user_id), path, ConsistencyLevel.ONE));

        // read entire row
        SlicePredicate predicate = new SlicePredicate();
        SliceRange sliceRange = new SliceRange(toByteBuffer(""), toByteBuffer(""), false, 10);
        predicate.setSlice_range(sliceRange);
        
        List<ColumnOrSuperColumn> results = client.get_slice(toByteBuffer(key_user_id), parent, predicate, ConsistencyLevel.ONE);
        for (ColumnOrSuperColumn result : results)
        {
            Column column = result.column;
            System.out.println(toString(column.name) + " -> " + toString(column.value));
        }
        */

        tr.close();
    }
    
    public static ByteBuffer toByteBuffer(String value) 
    throws UnsupportedEncodingException
    {
        return ByteBuffer.wrap(value.getBytes("UTF-8"));
    }
        
    public static String toString(ByteBuffer buffer) 
    throws UnsupportedEncodingException
    {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes, "UTF-8");
    }
}