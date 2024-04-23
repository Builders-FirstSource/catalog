package com.probuild.retail.web.catalog.datasync.view;

import org.apache.wicket.Page;
import org.apache.wicket.request.target.coding.QueryStringUrlCodingStrategy;
import org.jboss.seam.wicket.SeamWebApplication;
import org.python.util.PythonInterpreter;



public class DashboardApplication extends SeamWebApplication {

    public DashboardApplication() {
        super();
    }

    @Override
    protected void init() {
        mount( new QueryStringUrlCodingStrategy( "dashboard", DashboardHome.class ) );
        mount( new QueryStringUrlCodingStrategy( "manage", ManageJobs.class ) );
        mount( new QueryStringUrlCodingStrategy( "results", JobResult.class ) );
        mount( new QueryStringUrlCodingStrategy( "history", History.class ) );
        
    }
    
    @Override
    protected Class getLoginPage() {
        return DashboardHome.class;
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return DashboardHome.class;
    }

    protected void destory ( ) {
    }
}
