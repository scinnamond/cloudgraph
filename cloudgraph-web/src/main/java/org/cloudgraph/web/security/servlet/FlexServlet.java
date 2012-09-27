package org.cloudgraph.web.security.servlet;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class FlexServlet extends HttpServlet
{
	
	private static final long serialVersionUID = 1L;
	
	private static final String[] testList = {"Cost Benefit Summary",
                                              "Hierarchical Chart"};

	private static final String[] enhancedTestList = {"Enhanced RaVis Graph",
                                                      "Hierarchical Graph"};
	
	private static final String hierarchicalGraphXml =
   	   "<Graph>\n" +
       "   <Node id=\"1\" name=\"President\" desc=\"This is a description\" nodeColor=\"0x333333\" nodeSize=\"32\" nodeClass=\"earth\" nodeIcon=\"center\" x=\"10\" y=\"10\" />\n" +
       "   <Node id=\"2\" name=\"Sales\" desc=\"This is a description\" nodeColor=\"0x8F8FFF\" nodeSize=\"12\" nodeClass=\"tree\" nodeIcon=\"2\" x=\"10\" y=\"15\" />\n" +
       "   <Node id=\"3\" name=\"IT\" desc=\"This is a description\" nodeColor=\"0xF00000\" nodeSize=\"36\" nodeClass=\"tree\" nodeIcon=\"3\" x=\"10\" y=\"20\" />\n" +
       "   <Node id=\"4\" name=\"John Doe\" desc=\"This is a description\" nodeColor=\"0x8F8FFF\" nodeSize=\"32\" nodeClass=\"leaf\" nodeIcon=\"8\" x=\"20\" y=\"30\" />\n" +
       "   <Node id=\"5\" name=\"Jane Doe\" desc=\"This is a description\" nodeColor=\"0x8F8FFF\" nodeSize=\"16\" nodeClass=\"leaf\" nodeIcon=\"23\" x=\"20\" y=\"35\" />\n" +
       "   <Node id=\"6\" name=\"John Q. Public\" desc=\"This is a description\" nodeColor=\"0x8F8FFF\" nodeSize=\"16\" nodeClass=\"leaf\" nodeIcon=\"10\" x=\"20\" y=\"35\" />\n" +
       "   <Node id=\"7\" name=\"Mary Jones\" desc=\"This is a description\" nodeColor=\"0x8F8FFF\" nodeSize=\"12\" nodeClass=\"leaf\" nodeIcon=\"14\" x=\"20\" y=\"40\" />\n" +
       "   <Node id=\"8\" name=\"Tom Jones\" desc=\"This is a description\" nodeColor=\"0x8F8FFF\" nodeSize=\"10\" nodeClass=\"leaf\" nodeIcon=\"24\" x=\"20\" y=\"45\" />\n" +
       "   <Edge fromID=\"1\" toID=\"2\" edgeLabel=\"Department\" flow=\"50\" color=\"0x556b2f\" edgeClass=\"sun\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"1\" toID=\"3\" edgeLabel=\"Department\" flow=\"400\" color=\"0xcd5c5c\" edgeClass=\"sun\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"2\" toID=\"4\" edgeLabel=\"Manager\" flow=\"800\" edgeClass=\"rain\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"2\" toID=\"5\" edgeLabel=\"Developer\" flow=\"100\" edgeClass=\"rain\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"2\" toID=\"6\" edgeLabel=\"Tester\" flow=\"200\" edgeClass=\"rain\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"3\" toID=\"7\" edgeLabel=\"Project Lead\" flow=\"120\" edgeClass=\"rain\" edgeIcon=\"Bad\" />\n" +
       "   <Edge fromID=\"3\" toID=\"8\" edgeLabel=\"Contractor\" flow=\"150\" edgeClass=\"rain\" edgeIcon=\"NoChange\" />\n" +
       "</Graph>\n";

	private static final String enhancedRavisGraphXml =
       "<Graph>\n" + 
       "   <Node id=\"1\" name=\"0\" desc=\"This is a description\" nodeColor=\"0x333333\" nodeSize=\"32\" nodeClass=\"earth\" nodeIcon=\"center\" x=\"10\" y=\"10\" />\n" +
       "   <Node id=\"2\" name=\"A\" desc=\"This is a description\" nodeColor=\"0x8F8FFF\" nodeSize=\"12\" nodeClass=\"tree\" nodeIcon=\"2\" x=\"10\" y=\"15\" />\n" +
       "   <Node id=\"3\" name=\"B\" desc=\"This is a description\" nodeColor=\"0xF00000\" nodeSize=\"36\" nodeClass=\"tree\" nodeIcon=\"3\" x=\"10\" y=\"20\" />\n" +
       "   <Node id=\"4\" name=\"C\" desc=\"This is a description\" nodeColor=\"0x00FF00\" nodeSize=\"10\" nodeClass=\"tree\" nodeIcon=\"4\" x=\"10\" y=\"25\" />\n" +
       "   <Node id=\"5\" name=\"D\" desc=\"This is a description\" nodeColor=\"0xFFA500\" nodeSize=\"14\" nodeClass=\"tree\" nodeIcon=\"5\" x=\"10\" y=\"30\" />\n" +
       "   <Node id=\"6\" name=\"E\" desc=\"This is a description\" nodeColor=\"0x191970\" nodeSize=\"10\" nodeClass=\"tree\" nodeIcon=\"6\" x=\"10\" y=\"35\" />\n" +
       "   <Node id=\"7\" name=\"F\" desc=\"This is a description\" nodeColor=\"0x4682b4\" nodeSize=\"18\" nodeClass=\"tree\" nodeIcon=\"7\" x=\"10\" y=\"40\" />\n" +
			  
       "   <Node id=\"8\" name=\"A.1\" desc=\"This is a description\" nodeColor=\"0x8F8FFF\" nodeSize=\"21\" nodeClass=\"leaf\" nodeIcon=\"10\" x=\"20\" y=\"20\" />\n" +
       "   <Node id=\"9\" name=\"A.2\" desc=\"This is a description\" nodeColor=\"0x8F8FFF\" nodeSize=\"15\" nodeClass=\"leaf\" nodeIcon=\"11\" x=\"20\" y=\"25\" />\n" +
       "   <Node id=\"10\" name=\"A.3\" desc=\"This is a description\" nodeColor=\"0x8F8FFF\" nodeSize=\"32\" nodeClass=\"leaf\" nodeIcon=\"12\" x=\"20\" y=\"30\" />\n" +
       "   <Node id=\"11\" name=\"A.4\" desc=\"This is a description\" nodeColor=\"0x8F8FFF\" nodeSize=\"16\" nodeClass=\"leaf\" nodeIcon=\"13\" x=\"20\" y=\"35\" />\n" +
       "   <Node id=\"12\" name=\"A.5\" desc=\"This is a description\" nodeColor=\"0x8F8FFF\" nodeSize=\"12\" nodeClass=\"leaf\" nodeIcon=\"14\" x=\"20\" y=\"40\" />\n" +
       "   <Node id=\"13\" name=\"A.6\" desc=\"This is a description\" nodeColor=\"0x8F8FFF\" nodeSize=\"10\" nodeClass=\"leaf\" nodeIcon=\"15\" x=\"20\" y=\"45\" />\n" +
			  
       "   <Node id=\"14\" name=\"B.1\" desc=\"This is a description\" nodeColor=\"0xF00000\" nodeSize=\"27\" nodeClass=\"leaf\" nodeIcon=\"16\" x=\"30\" y=\"30\" />\n" +
       "   <Node id=\"15\" name=\"B.2\" desc=\"This is a description\" nodeColor=\"0xF00000\" nodeSize=\"10\" nodeClass=\"leaf\" nodeIcon=\"17\" x=\"30\" y=\"35\" />\n" +
       "   <Node id=\"16\" name=\"B.3\" desc=\"This is a description\" nodeColor=\"0xF00000\" nodeSize=\"13\" nodeClass=\"leaf\" nodeIcon=\"18\" x=\"30\" y=\"40\" />\n" +
       "   <Node id=\"17\" name=\"B.4\" desc=\"This is a description\" nodeColor=\"0xF00000\" nodeSize=\"10\" nodeClass=\"leaf\" nodeIcon=\"19\" x=\"30\" y=\"45\" />\n" +
       "   <Node id=\"18\" name=\"B.5\" desc=\"This is a description\" nodeColor=\"0xF00000\" nodeSize=\"10\" nodeClass=\"leaf\" nodeIcon=\"20\" x=\"30\" y=\"50\" />\n" +
       "   <Node id=\"19\" name=\"B.6\" desc=\"This is a description\" nodeColor=\"0xF00000\" nodeSize=\"10\" nodeClass=\"leaf\" nodeIcon=\"21\" x=\"30\" y=\"55\" />\n" +

       "   <Node id=\"20\" name=\"C.1\" desc=\"This is a description\" nodeColor=\"0x00FF00\" nodeSize=\"10\" nodeClass=\"leaf\" nodeIcon=\"22\" x=\"40\" y=\"40\" />\n" +
       "   <Node id=\"21\" name=\"C.2\" desc=\"This is a description\" nodeColor=\"0x00FF00\" nodeSize=\"15\" nodeClass=\"leaf\" nodeIcon=\"23\" x=\"40\" y=\"45\" />\n" +
       "   <Node id=\"22\" name=\"C.3\" desc=\"This is a description\" nodeColor=\"0x00FF00\" nodeSize=\"10\" nodeClass=\"leaf\" nodeIcon=\"24\" x=\"40\" y=\"50\" />\n" +
       "   <Node id=\"23\" name=\"C.4\" desc=\"This is a description\" nodeColor=\"0x00FF00\" nodeSize=\"10\" nodeClass=\"leaf\" nodeIcon=\"25\" x=\"40\" y=\"55\" />\n" +
       "   <Node id=\"24\" name=\"C.5\" desc=\"This is a description\" nodeColor=\"0x00FF00\" nodeSize=\"20\" nodeClass=\"leaf\" nodeIcon=\"26\" x=\"40\" y=\"60\" />\n" +
       "   <Node id=\"25\" name=\"C.6\" desc=\"This is a description\" nodeColor=\"0x00FF00\" nodeSize=\"10\" nodeClass=\"leaf\" nodeIcon=\"27\" x=\"40\" y=\"65\" />\n" +

       "   <Node id=\"26\" name=\"D.1\" desc=\"This is a description\" nodeColor=\"0xFFA500\" nodeSize=\"30\" nodeClass=\"leaf\" nodeIcon=\"28\" x=\"50\" y=\"50\" />\n" +
       "   <Node id=\"27\" name=\"D.2\" desc=\"This is a description\" nodeColor=\"0xFFA500\" nodeSize=\"10\" nodeClass=\"leaf\" nodeIcon=\"29\" x=\"50\" y=\"55\" />\n" +
       "   <Node id=\"28\" name=\"D.3\" desc=\"This is a description\" nodeColor=\"0xFFA500\" nodeSize=\"12\" nodeClass=\"leaf\" nodeIcon=\"30\" x=\"50\" y=\"60\" />\n" +
       "   <Node id=\"29\" name=\"D.4\" desc=\"This is a description\" nodeColor=\"0xFFA500\" nodeSize=\"10\" nodeClass=\"leaf\" nodeIcon=\"31\" x=\"50\" y=\"65\" />\n" +
       "   <Node id=\"30\" name=\"D.5\" desc=\"This is a description\" nodeColor=\"0xFFA500\" nodeSize=\"10\" nodeClass=\"leaf\" nodeIcon=\"32\" x=\"50\" y=\"70\" />\n" +
       "   <Node id=\"31\" name=\"D.6\" desc=\"This is a description\" nodeColor=\"0xFFA500\" nodeSize=\"15\" nodeClass=\"leaf\" nodeIcon=\"33\" x=\"50\" y=\"75\" />\n" +

       "   <Node id=\"32\" name=\"E.1\" desc=\"This is a description\" nodeColor=\"0x191970\" nodeSize=\"26\" nodeClass=\"leaf\" nodeIcon=\"34\" x=\"60\" y=\"60\" />\n" +
       "   <Node id=\"33\" name=\"E.2\" desc=\"This is a description\" nodeColor=\"0x191970\" nodeSize=\"10\" nodeClass=\"leaf\" nodeIcon=\"35\" x=\"60\" y=\"65\" />\n" +
       "   <Node id=\"34\" name=\"E.3\" desc=\"This is a description\" nodeColor=\"0x191970\" nodeSize=\"16\" nodeClass=\"leaf\" nodeIcon=\"36\" x=\"60\" y=\"70\" />\n" +
       "   <Node id=\"35\" name=\"E.4\" desc=\"This is a description\" nodeColor=\"0x191970\" nodeSize=\"10\" nodeClass=\"leaf\" nodeIcon=\"37\" x=\"60\" y=\"75\" />\n" +
       "   <Node id=\"36\" name=\"E.5\" desc=\"This is a description\" nodeColor=\"0x191970\" nodeSize=\"10\" nodeClass=\"leaf\" nodeIcon=\"38\" x=\"60\" y=\"80\" />\n" +
       "   <Node id=\"37\" name=\"E.6\" desc=\"This is a description\" nodeColor=\"0x191970\" nodeSize=\"14\" nodeClass=\"leaf\" nodeIcon=\"39\" x=\"60\" y=\"85\" />\n" +

       "   <Node id=\"38\" name=\"F.1\" desc=\"This is a description\" nodeColor=\"0x4682b4\" nodeSize=\"5\" nodeClass=\"leaf\" nodeIcon=\"40\" x=\"70\" y=\"70\" />\n" +
       "   <Node id=\"39\" name=\"F.2\" desc=\"This is a description\" nodeColor=\"0x4682b4\" nodeSize=\"12\" nodeClass=\"leaf\" nodeIcon=\"41\" x=\"70\" y=\"75\" />\n" +
       "   <Node id=\"40\" name=\"F.3\" desc=\"This is a description\" nodeColor=\"0x4682b4\" nodeSize=\"10\" nodeClass=\"leaf\" nodeIcon=\"42\" x=\"70\" y=\"80\" />\n" +
       "   <Node id=\"41\" name=\"F.4\" desc=\"This is a description\" nodeColor=\"0x4682b4\" nodeSize=\"22\" nodeClass=\"leaf\" nodeIcon=\"43\" x=\"70\" y=\"85\" />\n" +
       "   <Node id=\"42\" name=\"F.5\" desc=\"This is a description\" nodeColor=\"0x4682b4\" nodeSize=\"8\" nodeClass=\"leaf\" nodeIcon=\"44\" x=\"70\" y=\"90\" />\n" +
       "   <Node id=\"43\" name=\"F.6\" desc=\"This is a description\" nodeColor=\"0x4682b4\" nodeSize=\"10\" nodeClass=\"leaf\" nodeIcon=\"45\" x=\"70\" y=\"95\" />\n" +
			  
       "   <Edge fromID=\"1\" toID=\"2\" edgeLabel=\"No Change\" flow=\"50\" color=\"0x556b2f\" edgeClass=\"sun\" edgeIcon=\"NoChange\" />\n" +
       "   <Edge fromID=\"1\" toID=\"3\" edgeLabel=\"Bad\" flow=\"400\" color=\"0xcd5c5c\" edgeClass=\"sun\" edgeIcon=\"Bad\" />\n" +
       "   <Edge fromID=\"1\" toID=\"4\" edgeLabel=\"Good\" flow=\"80\" color=\"0xb22222\" edgeClass=\"sun\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"1\" toID=\"5\" edgeLabel=\"Good\" flow=\"100\" color=\"0x607b8b\" edgeClass=\"sun\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"1\" toID=\"6\" edgeLabel=\"No Change\" flow=\"120\" color=\"0x333333\" edgeClass=\"sun\" edgeIcon=\"NoChange\" />\n" +
       "   <Edge fromID=\"1\" toID=\"7\" edgeLabel=\"Bad\" flow=\"150\" color=\"0x6b8e23\" edgeClass=\"sun\" edgeIcon=\"Bad\" />\n" +
			  
       "   <Edge fromID=\"2\" toID=\"8\" edgeLabel=\"Good\" flow=\"100\" edgeClass=\"rain\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"2\" toID=\"9\" edgeLabel=\"Bad\" flow=\"400\" edgeClass=\"rain\" edgeIcon=\"Bad\" />\n" +
       "   <Edge fromID=\"2\" toID=\"10\" edgeLabel=\"No Change\" flow=\"800\" edgeClass=\"rain\" edgeIcon=\"NoChange\" />\n" +
       "   <Edge fromID=\"2\" toID=\"11\" edgeLabel=\"Good\" flow=\"100\" edgeClass=\"rain\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"2\" toID=\"12\" edgeLabel=\"Bad\" flow=\"120\" edgeClass=\"rain\" edgeIcon=\"Bad\" />\n" +
       "   <Edge fromID=\"2\" toID=\"13\" edgeLabel=\"No Change\" flow=\"150\" edgeClass=\"rain\" edgeIcon=\"NoChange\" />\n" +

       "   <Edge fromID=\"3\" toID=\"14\" edgeLabel=\"Good\" flow=\"1\" edgeClass=\"rain\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"3\" toID=\"15\" edgeLabel=\"No Change\" flow=\"40\" edgeClass=\"rain\" edgeIcon=\"NoChange\" />\n" +
       "   <Edge fromID=\"3\" toID=\"16\" edgeLabel=\"Bad\" flow=\"80\" edgeClass=\"rain\" edgeIcon=\"Bad\" />\n" +
       "   <Edge fromID=\"3\" toID=\"17\" edgeLabel=\"Good\" flow=\"100\" edgeClass=\"rain\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"3\" toID=\"18\" edgeLabel=\"Good\" flow=\"120\" edgeClass=\"rain\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"3\" toID=\"19\" edgeLabel=\"Bad\" flow=\"15\" edgeClass=\"rain\" edgeIcon=\"Bad\" />\n" +

       "   <Edge fromID=\"4\" toID=\"20\" edgeLabel=\"Bad\" flow=\"1\" edgeClass=\"rain\" edgeIcon=\"Bad\" />\n" +
       "   <Edge fromID=\"4\" toID=\"21\" edgeLabel=\"Good\" flow=\"40\" edgeClass=\"rain\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"4\" toID=\"22\" edgeLabel=\"No Change\" flow=\"8\" edgeClass=\"rain\" edgeIcon=\"NoChange\" />\n" +
       "   <Edge fromID=\"4\" toID=\"23\" edgeLabel=\"Good\" flow=\"100\" edgeClass=\"rain\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"4\" toID=\"24\" edgeLabel=\"Good\" flow=\"120\" edgeClass=\"rain\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"4\" toID=\"25\" edgeLabel=\"Bad\" flow=\"150\" edgeClass=\"rain\" edgeIcon=\"Bad\" />\n" +

       "   <Edge fromID=\"5\" toID=\"26\" edgeLabel=\"Bad\" flow=\"1\" edgeClass=\"rain\" edgeIcon=\"Bad\" />\n" +
       "   <Edge fromID=\"5\" toID=\"27\" edgeLabel=\"Good\" flow=\"400\" edgeClass=\"rain\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"5\" toID=\"28\" edgeLabel=\"No Change\" flow=\"8\" edgeClass=\"rain\" edgeIcon=\"NoChange\" />\n" +
       "   <Edge fromID=\"5\" toID=\"29\" edgeLabel=\"Good\" flow=\"100\" edgeClass=\"rain\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"5\" toID=\"30\" edgeLabel=\"Bad\" flow=\"120\" edgeClass=\"rain\" edgeIcon=\"Bad\" />\n" +
       "   <Edge fromID=\"5\" toID=\"31\" edgeLabel=\"No Change\" flow=\"150\" edgeClass=\"rain\" edgeIcon=\"NoChange\" />\n" +

       "   <Edge fromID=\"6\" toID=\"32\" edgeLabel=\"No Change\" flow=\"1\" edgeClass=\"rain\" edgeIcon=\"NoChange\" />\n" +
       "   <Edge fromID=\"6\" toID=\"33\" edgeLabel=\"Good\" flow=\"40\" edgeClass=\"rain\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"6\" toID=\"34\" edgeLabel=\"Bad\" flow=\"800\" edgeClass=\"rain\" edgeIcon=\"Bad\" />\n" +
       "   <Edge fromID=\"6\" toID=\"35\" edgeLabel=\"Good\" flow=\"100\" edgeClass=\"rain\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"6\" toID=\"36\" edgeLabel=\"Good\" flow=\"12\" edgeClass=\"rain\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"6\" toID=\"37\" edgeLabel=\"No Change\" flow=\"150\" edgeClass=\"rain\" edgeIcon=\"NoChange\" />\n" +

       "   <Edge fromID=\"7\" toID=\"38\" edgeLabel=\"Bad\" flow=\"100\" edgeClass=\"rain\" edgeIcon=\"Bad\" />\n" +
       "   <Edge fromID=\"7\" toID=\"39\" edgeLabel=\"Good\" flow=\"40\" edgeClass=\"rain\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"7\" toID=\"40\" edgeLabel=\"No Change\" flow=\"80\" edgeClass=\"rain\" edgeIcon=\"NoChange\" />\n" +
       "   <Edge fromID=\"7\" toID=\"41\" edgeLabel=\"Good\" flow=\"1000\" edgeClass=\"rain\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"7\" toID=\"42\" edgeLabel=\"Good\" flow=\"120\" edgeClass=\"rain\" edgeIcon=\"Good\" />\n" +
       "   <Edge fromID=\"7\" toID=\"43\" edgeLabel=\"Bad\" flow=\"150\" edgeClass=\"rain\" edgeIcon=\"Bad\" />\n" +
			  
       "</Graph>\n";
				
	private static Log log = LogFactory.getLog(FlexServlet.class);

	
    public void init() throws ServletException
    {   	
    	
    	log.debug("init");
    	
    } // init
    
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException
    {
    	
    	log.debug("doGet");
    	
    	String cmd = request.getParameter("cmd");
    	log.debug("cmd: " + cmd);
    	
    	StringBuilder sbXml = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
    	
    	if ("testList".equalsIgnoreCase(cmd))
    	{
    		sbXml.append("<testList>\n");
    		for (String itm : testList)
        		sbXml.append("   <item>" + itm + "</item>\n");
    		sbXml.append("</testList>\n");
    	}
    	else
       	if ("enhancedTestList".equalsIgnoreCase(cmd))
       	{
       		sbXml.append("<enhancedTestList>\n");
       		for (String itm : enhancedTestList)
           		sbXml.append("   <item>" + itm + "</item>\n");
       		sbXml.append("</enhancedTestList>\n");
       	}
    	else
       	if ("testData".equalsIgnoreCase(cmd))
       	{
       		String sel = request.getParameter("sel");
        	log.debug("sel: " + sel);
        	
        	if (testList[0].equalsIgnoreCase(sel))
        	{
        		sbXml.append("<Graph>\n");
           		sbXml.append("   <Node id=\"1\" name=\"" + sel + "\" desc=\"This is a description\" nodeColor=\"0x333333\" nodeSize=\"32\" nodeClass=\"earth\" nodeIcon=\"center\" x=\"10\" y=\"10\" />\n");
           		sbXml.append("   <Node id=\"2\" name=\"Year: 2011\" desc=\"This is a description\" nodeColor=\"0x8F8FFF\" nodeSize=\"12\" nodeClass=\"tree\" nodeIcon=\"14\" x=\"20\" y=\"40\" />\n");
           		sbXml.append("   <Node id=\"3\" name=\"Cost: 123.87\" desc=\"This is a description\" nodeColor=\"0x00FF00\" nodeSize=\"15\" nodeClass=\"tree\" nodeIcon=\"23\" x=\"40\" y=\"45\" />\n");
        		sbXml.append("   <Edge fromID=\"1\" toID=\"2\" edgeLabel=\"Field 1\" flow=\"50\" color=\"0x556b2f\" edgeClass=\"sun\" edgeIcon=\"NoChange\" />\n");
        		sbXml.append("   <Edge fromID=\"1\" toID=\"3\" edgeLabel=\"Field 2\" flow=\"400\" color=\"0xcd5c5c\" edgeClass=\"sun\" edgeIcon=\"Bad\" />\n");
        		sbXml.append("</Graph>\n");
        	}
        	else
           	if (testList[1].equalsIgnoreCase(sel))
           	{
           		sbXml.append(hierarchicalGraphXml);
           	}
           	else
           	{
            	throw new ServletException("Unknown selection: " + sel);
           	}
       	}
       	else
        if ("enhancedTestData".equalsIgnoreCase(cmd))
        {
       		String sel = request.getParameter("sel");
        	log.debug("sel: " + sel);
        	
        	if (enhancedTestList[0].equalsIgnoreCase(sel))
        	{
        		sbXml.append(enhancedRavisGraphXml);
        	}
        	else
           	if (enhancedTestList[1].equalsIgnoreCase(sel))
           	{
        		sbXml.append(hierarchicalGraphXml);
           	}
           	else
           	{
               	throw new ServletException("Unknown selection: " + sel);
           	}
        }
       	else
       	{
        	throw new ServletException("Unknown command: " + cmd);
       	}
    	
    	log.debug("xml: " + sbXml.toString());

        response.setContentType("text/xml");
        PrintWriter out = response.getWriter();
        out.println(sbXml.toString());
        out.close();

    } // doGet
    
    
    public void doPost(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException
    {
    	
    	log.debug("doPost");
    	throw new ServletException("doPost not supported");
        
    } // doPost
	
} // class FlexServlet
