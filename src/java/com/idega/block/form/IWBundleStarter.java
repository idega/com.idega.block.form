/**
 * $Id: IWBundleStarter.java,v 1.1 2006/10/10 15:29:16 gediminas Exp $
 * Created in 2006 by gediminas
 * 
 * Copyright (C) 2000-2006 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.form;

import org.chiba.xml.xslt.TransformerService;
import org.chiba.xml.xslt.impl.CachingTransformerService;
import org.chiba.xml.xslt.impl.ResourceResolver;
import com.idega.block.form.business.BundleResourceResolver;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.include.GlobalIncludeManager;

/**
 * <p>
 * TODO gediminas Describe Type IWBundleStarter
 * </p>
 * Last modified: $Date: 2006/10/10 15:29:16 $ by $Author: gediminas $
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version $Revision: 1.1 $
 */
public class IWBundleStarter implements IWBundleStartable {

	private static final String STYLE_SHEET_URL = "/style/xforms.css";

	public static final String BUNDLE_IDENTIFIER = "com.idega.block.form";

	public static final String TRANSFORMER_SERVICE = "transformer_service";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.idegaweb.IWBundleStartable#start(com.idega.idegaweb.IWBundle)
	 */
	public void start(IWBundle starterBundle) {
		GlobalIncludeManager.getInstance().addBundleStyleSheet(BUNDLE_IDENTIFIER, STYLE_SHEET_URL);
		
		IWMainApplication application = starterBundle.getApplication();
		ResourceResolver resolver = new BundleResourceResolver(application);
		TransformerService transformerService = new CachingTransformerService(resolver);
		application.setAttribute(TRANSFORMER_SERVICE, transformerService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.idegaweb.IWBundleStartable#stop(com.idega.idegaweb.IWBundle)
	 */
	public void stop(IWBundle starterBundle) {
		starterBundle.getApplication().removeAttribute(TRANSFORMER_SERVICE);
	}
}