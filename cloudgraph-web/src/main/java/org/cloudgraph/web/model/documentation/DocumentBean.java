package org.cloudgraph.web.model.documentation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.cloudgraph.web.ErrorHandlerBean;
import org.cloudgraph.web.config.web.AppActions;
import org.cloudgraph.web.model.ModelBean;

public class DocumentBean extends ModelBean 
{
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(DocumentBean.class);	
	
	private String url = "overview/Section-Overview.htm";
	private int width = 700;
	private int height = 1000;
		
	public DocumentBean() {
		log.debug("created DocumentBean");
	}
	
	public String view() {
       return null;
    }

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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

	public String getContent() {
		String result = "";
		FacesContext context = FacesContext.getCurrentInstance();
		String tcp = context.getExternalContext().getRequestContextPath();
		String classesDirPath;
		try {
			String rootStr = context.getExternalContext().getResource("/").toString();
			classesDirPath = context.getExternalContext().getResource("/").getPath();
			File classesDir = new File(classesDirPath);
		} catch (MalformedURLException e1) {
			log.error(e1.getMessage(), e1);
		}
		String resuestURL = getRequestURL();
		
		File file = new File(this.url);
		FileInputStream is = null;
		ByteArrayOutputStream os = null;
		try {
			is = new FileInputStream(file);
			os = new ByteArrayOutputStream();
			copy(is, os);
			os.flush();
			result = new String(os.toByteArray());
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
				}
		}
		return result;		
	}
	
	private String getRequestURL()
	{
	    Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();
	    if(request instanceof HttpServletRequest)
	    {
	            return ((HttpServletRequest) request).getRequestURL().toString();
	    }else
	    {
	        return "";
	    }
	}
	
    private void copy(InputStream in, OutputStream out) throws IOException {
        
        // do not allow other threads to read from the
        // input or write to the output while copying is
        // taking place
        
        synchronized (in) {
            synchronized (out) {
                
                byte[] buffer = new byte[256];
                while (true) {
                    int bytesRead = in.read(buffer);
                    if (bytesRead == -1) break;
                    out.write(buffer, 0, bytesRead);
                }
            }
        }
    }

}
