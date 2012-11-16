package org.cloudgraph.state.model;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.state.StateException;
import org.plasma.common.bind.BindingValidationEventHandler;
import org.plasma.common.bind.DataBinding;
import org.plasma.common.bind.NonValidatingUnmarshaler;
import org.plasma.common.bind.ValidatingUnmarshaler;
import org.xml.sax.SAXException;

/**
 * State JAXB non-validating Binding delegate. 
 *  
 * @author Scott Cinnamond
 * @since 0.5.2
 */
public class StateModeNonValidatinglDataBinding {

    private static Log log = LogFactory.getLog(StateModeNonValidatinglDataBinding.class);
    private NonValidatingUnmarshaler unmarshaler;

    public static Class<?>[] FACTORIES = { org.cloudgraph.state.model.ObjectFactory.class, };
        
    public StateModeNonValidatinglDataBinding()
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
