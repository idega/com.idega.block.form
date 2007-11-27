package com.idega.block.form.process;

import com.idega.jbpm.data.ViewTaskBind;
import com.idega.jbpm.def.View;
import com.idega.jbpm.def.impl.DefaultViewFactoryImpl;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 *
 * Last modified: $Date: 2007/11/27 16:27:15 $ by $Author: civilis $
 */
public class XFormsViewFactory extends DefaultViewFactoryImpl {

	public View createView() {
		return new XFormsView();
	}

	public View createView(ViewTaskBind viewTaskBind) {

		if(!XFormsView.VIEW_TYPE.equals(viewTaskBind.getViewType()))
			throw new IllegalArgumentException("Wrong view type viewTaskBind passed. View type: "+viewTaskBind.getViewType());
		
		XFormsView view = new XFormsView();
		view.setViewId(viewTaskBind.getViewIdentifier());
		
		return view;
	}

	public String getViewType() {
		return XFormsView.VIEW_TYPE;
	}
}