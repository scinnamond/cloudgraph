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
import org.plasma.common.bind.ValidatingUnmarshaler;
import org.xml.sax.SAXException;

/**
 * State JAXB Binding delegate. It is crucial that this binding be cached by
 * service implementations at the appropriate level to 1.) guarantee
 * thread safety (this class is NOT thread safe) and 2.) re-use the
 * underlying JAXB context and parsed schema instance(s) across as many
 * requests as possible.   
 *  
 * @author Scott Cinnamond
 * @since 0.5.2
 */
public class StateModelDataBinding implements DataBinding {

    private static Log log = LogFactory.getLog(StateModelDataBinding.class);
    public static String FILENAME_SCHEMA_CHAIN_ROOT = "cloudgraph-state.xsd";

    public static Class<?> RESOURCE_CLASS = StateModelDataBinding.class;

    private ValidatingUnmarshaler unmarshaler;

    public static Class<?>[] FACTORIES = { org.cloudgraph.state.model.ObjectFactory.class, };
        
    public StateModelDataBinding()
            throws JAXBException, SAXException {
        log.info("loading schema chain...(note: this is expensive - cache this binding where possible)");
        InputStream stream = RESOURCE_CLASS.getResourceAsStream(FILENAME_SCHEMA_CHAIN_ROOT);
        if (stream == null)
            stream = RESOURCE_CLASS.getClassLoader().getResourceAsStream(FILENAME_SCHEMA_CHAIN_ROOT);
        if (stream == null)
            throw new StateException("could not find configuration file schema resource '" 
                    + FILENAME_SCHEMA_CHAIN_ROOT 
                    + "' on the current classpath");        
        this.unmarshaler = new ValidatingUnmarshaler( 
        		stream, JAXBContext.newInstance(FACTORIES),
                new StateModelValidationEventHandler());
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

    public Object validate(String xml) throws JAXBException {
        return unmarshaler.validate(xml);
    }

    public Object validate(InputStream xml) throws JAXBException, UnmarshalException {
        return unmarshaler.validate(xml);
    }

    public BindingValidationEventHandler getValidationEventHandler() throws JAXBException {
        return unmarshaler.getValidationEventHandler();
    }

}
