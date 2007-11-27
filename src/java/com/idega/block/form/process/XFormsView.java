package com.idega.block.form.process;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.idega.block.form.process.ui.ProcessFormManager;
import com.idega.block.form.process.ui.ProcessFormViewer;
import com.idega.jbpm.def.impl.DefaultViewImpl;
import com.idega.webface.WFUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.3 $
 *
 * Last modified: $Date: 2007/11/27 20:35:33 $ by $Author: civilis $
 */
public class XFormsView extends DefaultViewImpl {

	public static final String VIEW_TYPE = "xforms";
	
	public XFormsView() {
		setViewType(VIEW_TYPE);
	}

	public UIComponent getViewForDisplay(Long taskInstanceId) {

		FacesContext context = FacesContext.getCurrentInstance();
		Application application = context.getApplication();
		
		ProcessFormViewer viewer = (ProcessFormViewer)application.createComponent(ProcessFormViewer.COMPONENT_TYPE);
		
		viewer.setFormManager((ProcessFormManager)WFUtil.getBeanInstance("casesJbpmFormManager"));
		viewer.setTaskInstanceId(String.valueOf(taskInstanceId));
		
		return viewer;
	}
}