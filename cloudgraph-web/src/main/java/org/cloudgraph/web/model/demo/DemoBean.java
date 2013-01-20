package org.cloudgraph.web.model.demo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.config.CloudGraphConfig;
import org.cloudgraph.config.CloudGraphConfigDataBinding;
import org.cloudgraph.config.TableConfig;
import org.cloudgraph.web.model.ModelBean;
import org.plasma.common.bind.DefaultValidationEventHandler;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.helper.PlasmaTypeHelper;
import org.xml.sax.SAXException;

import commonj.sdo.Type;

public class DemoBean extends ModelBean 
{
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(DemoBean.class);	
	
	private String defaultUrl = "overview/Section-Overview.htm";
	
	private String modelDisplayName;
	private String modelDescription;
	private String modelUrl;
	private String javaDocUrl;
	private String modelRootURI;
	private String modelRootType;
	private String queryCodeSamplesURL;
	private String createCodeSamplesURL;
	private String updateCodeSamplesURL;
	private String deleteCodeSamplesURL;
	
	private int width = 700;
	private int height = 1000;
	
	private String selectedTab;
	private String selectedTable;
		
	public DemoBean() {
		log.debug("created");
	}
		
	public boolean getHasModel() {
		return this.modelUrl != null && 
				!this.modelUrl.equals(this.defaultUrl);
	}
	
	public String getDefaultUrl() {
		return defaultUrl;
	}

	public String view() {
       return null;
    }	

	public String getSelectedTable() {
		return selectedTable;
	}

	public void setSelectedTable(String selectedTable) {
		this.selectedTable = selectedTable;
	}

	public String getCreateCodeSamplesURL() {
		return createCodeSamplesURL;
	}

	public void setCreateCodeSamplesURL(String createCodeSamplesURL) {
		this.createCodeSamplesURL = createCodeSamplesURL;
	}

	public String getQueryCodeSamplesURL() {
		return queryCodeSamplesURL;
	}

	public void setQueryCodeSamplesURL(String queryCodeSamplesURL) {
		this.queryCodeSamplesURL = queryCodeSamplesURL;
	}

	public String getUpdateCodeSamplesURL() {
		return updateCodeSamplesURL;
	}

	public void setUpdateCodeSamplesURL(String updateCodeSamplesURL) {
		this.updateCodeSamplesURL = updateCodeSamplesURL;
	}

	public String getDeleteCodeSamplesURL() {
		return deleteCodeSamplesURL;
	}

	public void setDeleteCodeSamplesURL(String deleteCodeSamplesURL) {
		this.deleteCodeSamplesURL = deleteCodeSamplesURL;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getModelDisplayName() {
		return modelDisplayName;
	}

	public void setModelDisplayName(String modelDisplayName) {
		this.modelDisplayName = modelDisplayName;
	}

	public String getModelDescription() {
		return modelDescription;
	}

	public void setModelDescription(String modelDescription) {
		this.modelDescription = modelDescription;
	}

	public String getModelUrl() {
		return modelUrl;
	}

	public void setModelUrl(String modelUrl) {
		this.modelUrl = modelUrl;
	}

	public String getJavaDocUrl() {
		return javaDocUrl;
	}

	public void setJavaDocUrl(String javaDocUrl) {
		this.javaDocUrl = javaDocUrl;
	}

	public String getSelectedTab() {
		return selectedTab;
	}

	public void setSelectedTab(String selectedTab) {
		this.selectedTab = selectedTab;
	}

	public String getModelRootURI() {
		return modelRootURI;
	}

	public void setModelRootURI(String modelRootURI) {
		this.modelRootURI = modelRootURI;
	}

	public String getModelRootType() {
		return modelRootType;
	}

	public void setModelRootType(String modelRootType) {
		this.modelRootType = modelRootType;
	}
	
	public List<TableConfigInfo> getTables()
	{
		List<TableConfigInfo> result = new ArrayList<TableConfigInfo>();
		if (this.getHasModel()) {
		    List<Type> types = PlasmaTypeHelper.INSTANCE.getTypes(this.modelRootURI);
		    for (Type type : types) {
		    	PlasmaType plasmaType = (PlasmaType)type;
				TableConfig tableConfig = CloudGraphConfig.getInstance().findTable(plasmaType.getQualifiedName());
		        if (tableConfig != null)
				try {
		        	ByteArrayOutputStream stream = new ByteArrayOutputStream();
		        	CloudGraphConfigDataBinding configBinding = new CloudGraphConfigDataBinding(
		                    new DefaultValidationEventHandler());
		            configBinding.marshal(tableConfig.getTable(), stream);
		            stream.flush();
		            String xml = stream.toString();
		            result.add(new TableConfigInfo(tableConfig, xml)); 
		        } catch (JAXBException e1) {
		            log.error(e1.getMessage(), e1);
		        } catch (SAXException e1) {
		        	log.error(e1.getMessage(), e1);
		        } catch (UnsupportedEncodingException e) {
		        	log.error(e.getMessage(), e);
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}		    	
		    }
		}
		return result;
	}
	
	public String getConfigurationXML() {
		String result = "";
		if (this.getHasModel()) {
			
			
			PlasmaType rootType = (PlasmaType)PlasmaTypeHelper.INSTANCE.getType(
					this.modelRootURI, 
					this.modelRootType); 
			
			TableConfig tableConfig = CloudGraphConfig.getInstance().getTable(rootType.getQualifiedName());
	        try {
	        	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	        	CloudGraphConfigDataBinding configBinding = new CloudGraphConfigDataBinding(
	                    new DefaultValidationEventHandler());
	            configBinding.marshal(tableConfig.getTable(), stream);
	            stream.flush();
	            result = stream.toString();
	            log.info(result);
	        } catch (JAXBException e1) {
	            log.error(e1.getMessage(), e1);
	        } catch (SAXException e1) {
	        	log.error(e1.getMessage(), e1);
	        } catch (UnsupportedEncodingException e) {
	        	log.error(e.getMessage(), e);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
        }
		return result;
	}
	
}
