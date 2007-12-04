package com.idega.block.form.process;

import java.util.Map;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.idega.block.form.process.ui.ProcessFormViewer;
import com.idega.documentmanager.business.Document;
import com.idega.documentmanager.business.DocumentManager;
import com.idega.documentmanager.business.DocumentManagerFactory;
import com.idega.jbpm.def.View;
import com.idega.jbpm.exe.Converter;
import com.idega.util.CoreConstants;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.4 $
 *
 * Last modified: $Date: 2007/12/04 14:00:37 $ by $Author: civilis $
 */
public class XFormsView implements View {

	public static final String VIEW_TYPE = "xforms";
	
	private String viewType;
	private DocumentManagerFactory documentManagerFactory;
	private Document form;
	private Converter converter;
	
	public Converter getConverter() {
		return converter;
	}

	public void setConverter(Converter converter) {
		this.converter = converter;
	}

	public void setViewId(String viewId) {
		throw new UnsupportedOperationException("XFormsView view type cannot be changed");
	}
	
	public String getViewId() {
		return VIEW_TYPE;
	}

	public String getViewType() {
		return viewType;
	}

	public void setViewType(String viewType) {
		this.viewType = viewType;
	}
	
	public DocumentManagerFactory getDocumentManagerFactory() {
		return documentManagerFactory;
	}

	public void setDocumentManagerFactory(
			DocumentManagerFactory documentManagerFactory) {
		this.documentManagerFactory = documentManagerFactory;
	}
	
	public UIComponent getViewForDisplay() {

		FacesContext context = FacesContext.getCurrentInstance();
		Application application = context.getApplication();
		
		ProcessFormViewer viewer = (ProcessFormViewer)application.createComponent(ProcessFormViewer.COMPONENT_TYPE);
		
		//FIXME: viewer.setFormManager((ProcessFormManager)WFUtil.getBeanInstance("casesJbpmFormManager"));
		//viewer.setTaskInstanceId(String.valueOf(taskInstanceId));
		
		return viewer;
	}
	
	public void load() {
		
		String formId = getViewId();
		
		if(formId == null || CoreConstants.EMPTY.equals(formId))
			throw new NullPointerException("View id not set");
		
		try {
			DocumentManager documentManager = getDocumentManagerFactory().newDocumentManager(FacesContext.getCurrentInstance());
			form = documentManager.openForm(formId);
		
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void addParameters(Map<String, String> parameters) {
		
//		FIXME: remove those custom 'managers', add possibility to add parameters to submit action 
		
		form.getParametersManager().cleanUpdate(parameters);
	}
	
	public void populate(Map<String, Object> variables) {
		
		if(form == null)
			throw new NullPointerException("Form not loaded (null)");
	
		getConverter().revert(variables, form.getSubmissionInstanceElement());
	}
}