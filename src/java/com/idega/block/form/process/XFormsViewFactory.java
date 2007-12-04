package com.idega.block.form.process;

import com.idega.jbpm.def.View;
import com.idega.jbpm.def.impl.DefaultViewFactoryImpl;
import com.idega.util.CoreConstants;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.3 $
 *
 * Last modified: $Date: 2007/12/04 14:00:37 $ by $Author: civilis $
 */
public class XFormsViewFactory extends DefaultViewFactoryImpl {

	public View createView() {
		return new XFormsView();
	}

	public View getView(String viewIdentifier) {

		if(viewIdentifier == null || CoreConstants.EMPTY.equals(viewIdentifier))
			throw new NullPointerException("View identifier not provided");
		
		XFormsView view = new XFormsView();
		view.setViewId(viewIdentifier);
		view.load();
		
		return view;
	}

	public String getViewType() {
		return XFormsView.VIEW_TYPE;
	}
}