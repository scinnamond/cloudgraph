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
package org.cloudgraph.state;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.common.bind.NonValidatingUnmarshaler;
import org.xml.sax.SAXException;

/**
 * State JAXB non-validating Binding delegate. 
 *  
 * @author Scott Cinnamond
 * @since 0.5.2
 */
public class StatelNonValidatinglDataBinding {

    private NonValidatingUnmarshaler unmarshaler;

    public static Class<?>[] FACTORIES = { org.cloudgraph.state.ObjectFactory.class, };
        
    public StatelNonValidatinglDataBinding()
            throws JAXBException, SAXException {
        this.unmarshaler = new NonValidatingUnmarshaler( 
        	JAXBContext.newInstance(FACTORIES));
    }

    public Class<?>[] getObjectFactories() {
        return FACTORIES;
    }

    public String marshal(Object root) throws JAXBException {
    	
        return unmarshaler.marshal(root);
    }

    public void marshal(Object root, OutputStream stream) throws JAXBException {
        unmarshaler.marshal(root, stream);
    }
    
    public void marshal(Object root, OutputStream stream, boolean formattedOutput) throws JAXBException
    {
    	unmarshaler.marshal(root, stream, formattedOutput);
    }
    
    public Object unmarshal(String xml) throws JAXBException {
        return unmarshaler.unmarshal(xml);
    }

    public Object unmarshal(InputStream stream) throws JAXBException {
        return unmarshaler.unmarshal(stream);
    }
}
