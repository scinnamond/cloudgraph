package org.cloudgraph.web.config;

import java.io.InputStream;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.common.bind.BindingValidationEventHandler;
import org.plasma.common.bind.DataBinding;
import org.plasma.common.bind.ValidatingUnmarshaler;
import org.xml.sax.SAXException;

public class ApplicationConfigDataBinding implements DataBinding {

    private static Log log = LogFactory.getLog(ApplicationConfigDataBinding.class);
    public static String FILENAME_SCHEMA_CHAIN_ROOT = "ApplicationConfig.xsd";

    // just classes in the same package where can find the above respective
    // schema files via Class.getResource*
    public static Class<?> RESOURCE_CLASS = ApplicationConfigDataBinding.class;

    private ValidatingUnmarshaler validatingUnmarshaler;

    public static Class<?>[] FACTORIES = { org.cloudgraph.web.config.web.ObjectFactory.class, };

    @SuppressWarnings("unused")
    private ApplicationConfigDataBinding() {
    }

    public ApplicationConfigDataBinding(BindingValidationEventHandler validationEventHandler)
            throws JAXBException, SAXException {
        log.info("loading schema chain...");
        
        URL url = RESOURCE_CLASS.getResource(FILENAME_SCHEMA_CHAIN_ROOT);
        if (url == null)
        	url = RESOURCE_CLASS.getClassLoader().getResource(FILENAME_SCHEMA_CHAIN_ROOT);
        if (url == null)
        	throw new RuntimeException("could not find resource, '"
        			+ FILENAME_SCHEMA_CHAIN_ROOT + "' on current classpath");
        validatingUnmarshaler = new ValidatingUnmarshaler(url, 
        		JAXBContext.newInstance(FACTORIES),
                validationEventHandler);
    }

    public Class<?>[] getObjectFactories() {
        return FACTORIES;
    }

    public String marshal(Object root) throws JAXBException {
        return validatingUnmarshaler.marshal(root);
    }

    public Object unmarshal(String xml) throws JAXBException {
        return validatingUnmarshaler.unmarshal(xml);
    }

    public Object unmarshal(InputStream stream) throws JAXBException {
        return validatingUnmarshaler.unmarshal(stream);
    }

    public Object validate(String xml) throws JAXBException {
        return validatingUnmarshaler.validate(xml);
    }

    public Object validate(InputStream xml) throws JAXBException, UnmarshalException {
        return validatingUnmarshaler.validate(xml);
    }

    public BindingValidationEventHandler getValidationEventHandler() throws JAXBException {
        return this.validatingUnmarshaler.getValidationEventHandler();
    }

}
