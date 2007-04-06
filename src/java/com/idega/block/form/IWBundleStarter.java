/**
 * $Id: IWBundleStarter.java,v 1.12 2007/04/06 20:13:00 civilis Exp $
 * Created in 2006 by gediminas
 * 
 * Copyright (C) 2000-2006 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.form;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.idega.block.form.business.FormsSlideChangeListener;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;
import com.idega.idegaweb.IWMainApplication;
import com.idega.slide.business.IWSlideService;

/**
 * Last modified: $Date: 2007/04/06 20:13:00 $ by $Author: civilis $
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version $Revision: 1.12 $
 */
public class IWBundleStarter implements IWBundleStartable {
	
	private static final Logger log = Logger.getLogger(IWBundleStarter.class.getName());

	public static final String BUNDLE_IDENTIFIER = "com.idega.block.form";

	public void start(IWBundle starterBundle) {
		
		IWMainApplication application = starterBundle.getApplication();
		
		IWApplicationContext iwac = application.getIWApplicationContext();
	    try {
	    	IWSlideService service = (IWSlideService) IBOLookup.getServiceInstance(iwac, IWSlideService.class);
	        service.addIWSlideChangeListeners(new FormsSlideChangeListener());
	    } catch (IBOLookupException e) {
			log.log(Level.WARNING, "Could not get FormsService", e);
	    } catch (RemoteException e) {
			log.log(Level.WARNING, "Error adding FormsService as slide change listener", e);
	    }
	}

	public void stop(IWBundle starterBundle) { }
}
