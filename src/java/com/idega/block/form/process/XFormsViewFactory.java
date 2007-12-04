package com.idega.block.form.process;

import com.idega.jbpm.def.View;
import com.idega.jbpm.def.impl.DefaultViewFactoryImpl;
import com.idega.util.CoreConstants;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.4 $
 *
 * Last modified: $Date: 2007/12/04 18:48:49 $ by $Author: civilis $
 */
public class XFormsViewFactory extends DefaultViewFactoryImpl {

//	TODO: is this called?
	@Deprecated
	public View createView() {
		return new XFormsView();
	}

	public View getView(String viewIdentifier, boolean submitable) {

		if(viewIdentifier == null || CoreConstants.EMPTY.equals(viewIdentifier))
			throw new NullPointerException("View identifier not provided");
		
		XFormsView view = new XFormsView();
		view.setViewId(viewIdentifier);
		view.setSubmitable(submitable);
		view.load();
		
		return view;
	}

	public String getViewType() {
		return XFormsView.VIEW_TYPE;
	}
}