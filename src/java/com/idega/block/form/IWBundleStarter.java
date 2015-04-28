/**
 * $Id: IWBundleStarter.java,v 1.13 2008/11/20 16:20:54 civilis Exp $
 * Created in 2006 by gediminas
 *
 * Copyright (C) 2000-2006 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.form;

import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;

/**
 * Last modified: $Date: 2008/11/20 16:20:54 $ by $Author: civilis $
 *
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version $Revision: 1.13 $
 */
public class IWBundleStarter implements IWBundleStartable {

	public static final String BUNDLE_IDENTIFIER = "com.idega.block.form";

	@Override
	public void start(IWBundle starterBundle) {}

	@Override
	public void stop(IWBundle starterBundle) {}
}
