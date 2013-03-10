package org.cloudgraph.web.etl;


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;
import org.cloudgraph.web.common.xslt.XSLTUtils;
import org.cloudgraph.web.config.ImportExportConfig;
import org.cloudgraph.web.config.imex.DataEntity;
import org.cloudgraph.web.config.imex.DataImport;
import org.cloudgraph.web.config.imex.ImportLoader;
import org.cloudgraph.web.config.imex.Transformation;
import org.cloudgraph.web.etl.loader.Loader;


/**
 */
public class DataLoad
{
	
    private static Logger log = Logger.getLogger(DataLoad.class);


    private static final String LINE_SEP = System.getProperty("line.separator");
    private static final String FILE_SEP = System.getProperty("file.separator");
	
    
	public DataLoad()
	{
	} 
	
	public static void main(String[] args) throws TransformerConfigurationException, 
	    MalformedURLException, IOException, TransformerException, SQLException 
	{	
		if (args == null || args.length != 3)
			throw new IllegalArgumentException("expected 3 arguments: [command, source-dir, target-dir]");
		
		File baseSourceDir = new File(args[1]);
		File baseTargetDir = new File(args[2]);
		
		if (!baseSourceDir.exists())
			if (!baseSourceDir.mkdirs()) 
				throw new IllegalArgumentException("could not create source directory '"
						+ args[1] + "'");

			
		if (!baseTargetDir.exists())
			if (!baseTargetDir.mkdirs())
				throw new IllegalArgumentException("could not create target directory '"
						+ args[2] + "'");
		
		ETLAction command = ETLAction.valueOf(ETLAction.class, args[0]);

		for (org.cloudgraph.web.config.imex.DataImport dataImport : ImportExportConfig.getInstance().getConfig().getDataImport()) 
		{
			log.info("processing data import: " + dataImport.getName());
			
			processTransformations(
					dataImport.getTransformation(),
					command, 
					baseSourceDir,  
					baseTargetDir);
			
			processLoaders(
					dataImport,
					dataImport.getImportLoader(),
					command, 
					baseSourceDir, 
					baseTargetDir);
		}
	}

	private static void processTransformations(
			List<Transformation> xformations,
			ETLAction command, 
			File baseSourceDir,  
			File baseTargetDir) throws TransformerConfigurationException, MalformedURLException, IOException, TransformerException, SQLException {

		for (Transformation xform : xformations) {
	    	
	    	URL templateURL = DataLoad.class.getClassLoader().getResource(xform.getTemplate());
	    	
	    	if (templateURL == null)
				throw new IllegalArgumentException("template '"
						+ xform.getTemplate() + "' does not exist");
	    	for (DataEntity entity : xform.getDataEntity()) {
		    	File sourceFile = null;
		    	if (entity.getSourceDir() == null || entity.getSourceDir().trim().length() == 0)
		    		sourceFile = new File(baseSourceDir, entity.getSource());
		    	else
		    		sourceFile = new File(new File(baseSourceDir, entity.getSourceDir()), entity.getSource());
		    	if (!sourceFile.exists()) {
                    if (entity.getSource().contains("*")) {
                    	if (!entity.getTarget().contains("*"))
						    throw new IllegalArgumentException("expected wildcard within target file spec '"
									+ entity.getTarget() + "'");
                    		
                    	int srcIdx = entity.getSource().indexOf("*");
                    	String sourcePrefix = entity.getSource().substring(0, srcIdx);
                    	String sourceSuffix = entity.getSource().substring(srcIdx+1, entity.getSource().length());
                    	                                                  	
		    		    FilenameFilter filter = new WildcardFileFilter(entity.getSource());
		    		    File[] files = sourceFile.getParentFile().listFiles(filter);
		    		    for (int i = 0; i < files.length; i++) {
		    		    	String srcToken = files[i].getName().substring(sourcePrefix.length(), 
		    		    			files[i].getName().indexOf(sourceSuffix));
		    		    	
		    		    	String targetName = entity.getTarget().replaceAll(
		    		    			"\\*", srcToken);
		    		    	File targetFile = new File(baseTargetDir, 
		    		    			targetName);
		    		    	if (command.ordinal() == ETLAction.transform.ordinal())
		    				{
		    			    	XSLTUtils ut = new XSLTUtils();
		    			    	ut.transform(targetFile, files[i], templateURL);
		    				}
		    			    else if (command.ordinal() == ETLAction.load.ordinal()) {
		    				    process(targetFile, files[i], templateURL);
		    		    	}
		    			    else
		    					throw new IllegalArgumentException("expected one of '"
		    							+ ETLAction.values().toString() + "' as first arg");
		    		    }
		    		    //jdbc.commit();
                    }
                    else
					    throw new IllegalArgumentException("source file '"
							+ sourceFile.getAbsolutePath() + "' does not exist");
		    	} 
		    	else {
				    File targetFile = new File(baseTargetDir, entity.getTarget());
    		    	if (command.ordinal() == ETLAction.transform.ordinal())
    				{
    			    	XSLTUtils ut = new XSLTUtils();
    			    	ut.transform(targetFile, sourceFile, templateURL);
   				    }
    			    else if (command.ordinal() == ETLAction.load.ordinal()) {
					    process(targetFile, sourceFile, templateURL);
					    //jdbc.commit();
    		    	}
    			    else
    					throw new IllegalArgumentException("expected one of '"
    							+ ETLAction.values().toString() + "' as first arg");
 		    	}
	    	}
	    }		
	}

	private static void processLoaders(
			DataImport dataImport,
			List<ImportLoader> loaderList,
			ETLAction command, 
			File baseSourceDir, 
			File baseTargetDir) 
	    throws TransformerConfigurationException, MalformedURLException, 
	        IOException, TransformerException, SQLException 
	{
	    for (ImportLoader importLoader : loaderList) {
	    	Loader loader = createLoader(importLoader.getClassName(),
	    			dataImport);
	    	File queryFile = null;
	    	if (importLoader.getQueryFileName() != null) {
	    		queryFile = new File(baseSourceDir, 
	    				importLoader.getQueryFileName());
	    		if (!queryFile.exists())
				    throw new IllegalArgumentException("query file '"
							+ queryFile.getAbsolutePath() + "' does not exist");
	    	    loader.define(queryFile);
	    	}	    	
	    	
	    	for (DataEntity entity : importLoader.getDataEntity()) {
		    	File sourceFile = null;
		    	if (entity.getSourceDir() == null || entity.getSourceDir().trim().length() == 0)
		    		sourceFile = new File(baseSourceDir, entity.getSource());
		    	else
		    		sourceFile = new File(new File(baseSourceDir, entity.getSourceDir()), entity.getSource());
		    	if (!sourceFile.exists()) {
                    if (entity.getSource().contains("*")) {
		    		    FilenameFilter filter = new WildcardFileFilter(entity.getSource());
		    		    if (sourceFile.getParentFile() == null)
						    throw new IllegalArgumentException("parent dir for '"
									+ sourceFile.getAbsolutePath() + "' does not exist");

		    		    File[] files = sourceFile.getParentFile().listFiles(filter);
					    if (files == null)
		    		        throw new IllegalArgumentException("no files found for filter '" + entity.getSource() + "'");
		    		    for (int i = 0; i < files.length; i++) {

		    		    	if (command.ordinal() == ETLAction.transform.ordinal())
		    				{
		    		    		// ignored
		    				}
		    			    else if (command.ordinal() == ETLAction.load.ordinal()) {
		    			    	loader.load(files[i]);
		    		    	}
		    			    else
		    					throw new IllegalArgumentException("expected one of '"
		    							+ ETLAction.values().toString() + "' as first arg");
		    		    }
                    }
                    else
					    throw new IllegalArgumentException("source file '"
							+ sourceFile.getAbsolutePath() + "' does not exist");
		    	} 
		    	else {
    		    	if (command.ordinal() == ETLAction.transform.ordinal())
    				{
    		    		// ignored
   				    }
    			    else if (command.ordinal() == ETLAction.load.ordinal()) {
    			    	loader.load(sourceFile);
    		    	}
    			    else
    					throw new IllegalArgumentException("expected one of '"
    							+ ETLAction.values().toString() + "' as first arg");
 		    	}
	    	}
	    }
		
	}
	
	private static Loader createLoader(String className, DataImport dataImport) {
		
		try {
			Class<?> c = Class.forName(className);
			Class<?>[] params = { dataImport.getClass() };
			Constructor<?> constructor = c.getConstructor(params);
			Object[] args = { dataImport };
			return (Loader)constructor.newInstance(args);			
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void process(File targetFile, File sourceFile, URL templateURL) 
	    throws TransformerConfigurationException, MalformedURLException, 
	        IOException, TransformerException, SQLException 
	{
    	XSLTUtils xslt = new XSLTUtils();
    	xslt.transform(targetFile, sourceFile, templateURL);
    	//if (jdbc == null)
    	//    jdbc = new JDBCUtils();
    	//jdbc.executeBatchNoCommit(targetFile);		
	}
	
} // class DataLoader
