package org.cloudgraph.state;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBException;

public interface NonValidatingDataBinding {

	public abstract String marshal(Object root) throws JAXBException;

	public abstract void marshal(Object root, OutputStream stream)
			throws JAXBException;

	public abstract void marshal(Object root, OutputStream stream,
			boolean formattedOutput) throws JAXBException;

	public abstract Object unmarshal(String xml) throws JAXBException;

	public abstract Object unmarshal(InputStream stream) throws JAXBException;

}