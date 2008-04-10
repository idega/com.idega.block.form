package com.idega.block.form.process;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.chiba.xml.xforms.core.Submission;
import org.w3c.dom.Node;

import com.idega.block.form.presentation.FormViewer;
import com.idega.documentmanager.business.Document;
import com.idega.documentmanager.business.DocumentManager;
import com.idega.documentmanager.business.DocumentManagerFactory;
import com.idega.documentmanager.business.PersistenceManager;
import com.idega.documentmanager.util.FormManagerUtil;
import com.idega.idegaweb.IWMainApplication;
import com.idega.jbpm.def.View;
import com.idega.jbpm.exe.Converter;
import com.idega.util.CoreConstants;
import com.idega.util.URIUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.14 $
 * 
 * Last modified: $Date: 2008/04/10 14:04:51 $ by $Author: civilis $
 */
public class XFormsView implements View {

	public static final String VIEW_TYPE = "xforms";
	public static final String FORM_TYPE = "bpm";

	private String viewId;
	private boolean submitable = true;
	private String displayName;
	private DocumentManagerFactory documentManagerFactory;
	private PersistenceManager persistenceManager;
	private Document form;
	private Converter converter;
	private Map<String, String> parameters;
	private Map<String, Object> variables;

	public Converter getConverter() {
		return converter;
	}

	public void setConverter(Converter converter) {
		this.converter = converter;
	}

	public void setViewId(String viewId) {
		form = null;
		this.viewId = viewId;
	}

	public String getViewId() {
		return viewId;
	}

	public String getViewType() {
		return VIEW_TYPE;
	}

	public void setViewType(String viewType) {
		throw new UnsupportedOperationException(
				"XFormsView view type cannot be changed");
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

		FormViewer formviewer = (FormViewer) application
				.createComponent(FormViewer.COMPONENT_TYPE);
		formviewer.setXFormsDocument(getFormDocument().getXformsDocument());

		return formviewer;
	}

	public void setFormDocument(Document formDocument) {

		form = formDocument;

		if (!isSubmitable())
			form.setReadonly(true);

		viewId = form.getFormId().toString();
	}

	protected Document getFormDocument() {

		if (form != null)
			return form;

		if (getViewId() == null || CoreConstants.EMPTY.equals(getViewId()))
			throw new NullPointerException("View id not set");
		
		Long formId = new Long(getViewId());

		try {
			DocumentManager documentManager = getDocumentManagerFactory()
					.newDocumentManager(IWMainApplication.getIWMainApplication(FacesContext.getCurrentInstance()));
			Document form = documentManager.openForm(formId);
			
			if(form != null) {
			
				setFormDocument(form);
			}

			return form;

		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isSubmitable() {
		return submitable;
	}

	public void setSubmitable(boolean submitable) {

		this.submitable = submitable;
		getFormDocument().setReadonly(!submitable);
	}

	public void populateParameters(Map<String, String> parameters) {
		getFormDocument().getParametersManager().cleanUpdate(parameters);

	}

	public void populateVariables(Map<String, Object> variables) {
		getConverter().revert(variables,
				getFormDocument().getSubmissionInstanceElement());
	}

	public Map<String, String> resolveParameters() {

		if (parameters != null)
			return parameters;

		throw new UnsupportedOperationException(
				"Resolving parameters from form not supported yet.");
	}

	public Map<String, Object> resolveVariables() {

		if (variables != null)
			return variables;

		throw new UnsupportedOperationException(
				"Resolving variables from form not supported yet.");
	}

	public void setSubmission(Submission submission, Node submissionInstance) {

		String action = submission.getElement().getAttribute(
				FormManagerUtil.action_att);

		parameters = new URIUtil(action).getParameters();
		variables = getConverter().convert(submissionInstance);
	}

	public String getDisplayName() {
		
		if(displayName == null) {
			
			try {
				Document document = getFormDocument();
				displayName = document.getFormTitle().getString(new Locale("en"));
				
			} catch (Exception e) {
				displayName = null;
			}
		}
		
		return displayName;
	}

	public PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}

	public void setPersistenceManager(PersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}

	public Date getDateCreated() {
	
//		TODO: implement
		return new Date();
	}
}