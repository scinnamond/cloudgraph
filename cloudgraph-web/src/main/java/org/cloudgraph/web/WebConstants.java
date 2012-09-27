package org.cloudgraph.web;


/**
 */
public interface WebConstants
{
    public static final String BUNDLE_BASENAME = "bundles.AppResources";
    
    public static final int RECORDS_TO_FETCH = 25;
    public static boolean LOCAL = false;
    public static final String DEFAULT_SELECTION = "--not selected--";
    public static final String ANY_SELECTION = "--any--";
    
    
    public static String RESOURCE_DASHBOARD_SATUS = "aplsDashboardStatus";
    
    public static final String URL_ERROR_FORWARD = "/apls/ErrorHandler.faces";

    public static final String URL_DASHBOARD = "Dashboard.faces";
    
    public static final String URL_APP_FUNDING_FROM_DASHBOARD = "AppFundingQueue.faces";
    public static final String URL_JOB_CODE_FUNDING_FROM_DASHBOARD = "JobCodeFundingQueue.faces";
    public static final String URL_PROJECT_FUNDING_FROM_DASHBOARD = "ProjectFundingQueue.faces";
    
    public static String DATE_FORMAT_CHART = "MM/dd/yyyy"; 
    
    public static String RESOURCE_DASHBOARD_SATUS_PREFIX = "appDashboardStatus_";
    
    
    public static String TAXONOMY_NAME_BRM = "FEA Business Reference Model (BRM)";
    public static String TAXONOMY_NAME_SRM = "FEA Service Reference Model (SRM)";
   
    
}
