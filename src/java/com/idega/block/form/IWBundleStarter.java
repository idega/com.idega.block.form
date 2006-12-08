/**
 * $Id: IWBundleStarter.java,v 1.4 2006/12/08 15:45:33 gediminas Exp $
 * Created in 2006 by gediminas
 * 
 * Copyright (C) 2000-2006 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.form;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import org.chiba.web.session.impl.DefaultXFormsSessionManagerImpl;
import org.chiba.xml.xforms.config.Config;
import org.chiba.xml.xforms.config.XFormsConfigException;
import org.chiba.xml.xslt.TransformerService;
import org.chiba.xml.xslt.impl.CachingTransformerService;
import org.chiba.xml.xslt.impl.ResourceResolver;
import com.idega.block.form.business.BundleResourceResolver;
import com.idega.block.form.business.FormsService;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.include.GlobalIncludeManager;
import com.idega.slide.business.IWSlideService;

/**
 * <p>
 * TODO gediminas Describe Type IWBundleStarter
 * </p>
 * Last modified: $Date: 2006/12/08 15:45:33 $ by $Author: gediminas $
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version $Revision: 1.4 $
 */
public class IWBundleStarter implements IWBundleStartable {
	
	private static final Logger log = Logger.getLogger(IWBundleStarter.class.getName());

	private static final String STYLE_SHEET_URL = "/style/xforms.css";

	public static final String BUNDLE_IDENTIFIER = "com.idega.block.form";

	public static final String TRANSFORMER_SERVICE = TransformerService.class.getName();

	public static final URI XSLT_URI = URI.create("bundle://" + BUNDLE_IDENTIFIER + "/resources/xslt/html4.xsl");
	private static final URI CHIBA_CONFIG_URI = URI.create("bundle://" + BUNDLE_IDENTIFIER + "/resources/chiba-config.xml");

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.idegaweb.IWBundleStartable#start(com.idega.idegaweb.IWBundle)
	 */
	public void start(IWBundle starterBundle) {
		GlobalIncludeManager.getInstance().addBundleStyleSheet(BUNDLE_IDENTIFIER, STYLE_SHEET_URL);
		
		IWMainApplication application = starterBundle.getApplication();

		// create transformer service
		ResourceResolver resolver = new BundleResourceResolver(application);
		TransformerService transformerService = new CachingTransformerService(resolver);
		application.setAttribute(TRANSFORMER_SERVICE, transformerService);

    	// cache default stylesheet
        try {
			transformerService.getTransformer(XSLT_URI);
		}
		catch (TransformerException e) {
			log.log(Level.SEVERE, "Cannot load XForms transformer stylesheet", e);
		}
		
		// start managing xforms sessions
		createXFormsSessionManager(0, 0);
		
		// read chiba config
		try {
			InputStream inputStream = resolver.resolve(CHIBA_CONFIG_URI).getInputStream();
			Config.getInstance(inputStream);
		}
		catch (IOException e) {
			log.log(Level.SEVERE, "Error reading chiba config", e);
		}
		catch (XFormsConfigException e) {
			log.log(Level.SEVERE, "Error initializing chiba config", e);
		}
		
		// add forms service as slide listener
		IWApplicationContext iwac = application.getIWApplicationContext();
	    try {
	    	IWSlideService service = (IWSlideService) IBOLookup.getServiceInstance(iwac,IWSlideService.class);
	           
	        FormsService formsService = (FormsService) IBOLookup.getServiceInstance(iwac, FormsService.class);
	        service.addIWSlideChangeListeners(formsService);
	    } catch (IBOLookupException e) {
			log.log(Level.WARNING, "Could not get FormsService", e);
	    } catch (RemoteException e) {
			log.log(Level.WARNING, "Error adding FormsService as slide change listener", e);
	    }

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.idegaweb.IWBundleStartable#stop(com.idega.idegaweb.IWBundle)
	 */
	public void stop(IWBundle starterBundle) {
		starterBundle.getApplication().removeAttribute(TRANSFORMER_SERVICE);
		DefaultXFormsSessionManagerImpl manager = DefaultXFormsSessionManagerImpl.getInstance();
		manager.kill();
		manager.interrupt();
	}
	
    /**
     * factory method to create and setup an XFormsSessionManager. Overwrite this to provide your own implementation.
     *
     * @param wipingInterval
     * @param timeout
     */
    protected void createXFormsSessionManager(int wipingInterval, int timeout) {
        DefaultXFormsSessionManagerImpl manager = DefaultXFormsSessionManagerImpl.getInstance();
        if(wipingInterval != 0){
            manager.setInterval(wipingInterval);
        }else{
            manager.setInterval(1000 * 30);// every 30 secs as default
        }

        if (timeout != 0){
            manager.setTimeout(timeout);
        }else{
            manager.setTimeout(1000 * 30); // 1 minute session lifetime
        }

        //start running the session cleanup

        manager.start();
    }

}
