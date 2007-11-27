package com.idega.block.form.process;

import javax.faces.component.UIComponent;

import com.idega.jbpm.def.impl.DefaultViewImpl;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 *
 * Last modified: $Date: 2007/11/27 16:27:15 $ by $Author: civilis $
 */
public class XFormsView extends DefaultViewImpl {

	public static final String VIEW_TYPE = "xforms";
	
	public XFormsView() {
		setViewType(VIEW_TYPE);
	}

	public UIComponent getViewForDisplay(Long taskInstanceId) {

		
		return null;
	}
}