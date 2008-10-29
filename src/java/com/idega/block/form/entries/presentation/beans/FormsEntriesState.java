package com.idega.block.form.entries.presentation.beans;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import com.idega.block.form.entries.presentation.UIFormsEntriesViewer;
import com.idega.documentmanager.business.PersistenceManager;
import com.idega.documentmanager.business.Submission;
import com.idega.documentmanager.business.XFormPersistenceType;
import com.idega.documentmanager.util.FormManagerUtil;
import com.idega.util.CoreConstants;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

/**
 * 
 * @author <a href="civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.3 $
 * 
 *          Last modified: $Date: 2008/10/29 11:22:56 $ by $Author: civilis $
 * 
 */
@Scope("request")
@Service("formsEntries")
public class FormsEntriesState implements Serializable {

	private static final long serialVersionUID = 7398164884900011980L;
	private String facetDisplayed;
	private String submissionFacetDisplayed;
	private String submissionId;
	private static final String submissionIdParam = "submissionId";

	@Autowired
	@XFormPersistenceType("slide")
	private transient PersistenceManager persistenceManager;

	public List<FormSubmissionEntry> getFormsEntries() {

		List<Submission> submissions = getPersistenceManager()
				.getAllStandaloneFormsSubmissions();

		ArrayList<FormSubmissionEntry> entries = new ArrayList<FormSubmissionEntry>(
				submissions.size());

		for (Submission submission : submissions) {

			entries.add(new FormSubmissionEntry(submission, submission
					.getXform()));
		}

		return entries;
	}

	public boolean isEntriesRendered() {

		return UIFormsEntriesViewer.entriesFacet.equals(getFacetDisplayed());
	}
	
	public boolean isSubmissionView() {

		return UIFormsEntriesViewer.formSubmissionFacet.equals(getSubmissionFacetDisplayed());
	}
	
	public boolean isSourceView() {

		return UIFormsEntriesViewer.formSubmissionSourceFacet.equals(getSubmissionFacetDisplayed());
	}

	public boolean isFormSubmissionRendered() {

		return UIFormsEntriesViewer.formSubmissionFacet
				.equals(getFacetDisplayed());
	}

	String getFacetDisplayed() {

		String submissionIdStr = (String) FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get(
						submissionIdParam);

		if (!StringUtil.isEmpty(submissionIdStr)) {
			facetDisplayed = UIFormsEntriesViewer.formSubmissionFacet;
			submissionFacetDisplayed = UIFormsEntriesViewer.formSubmissionFacet;
		} else if (facetDisplayed == null) {
			facetDisplayed = UIFormsEntriesViewer.entriesFacet;
		}

		return facetDisplayed;
	}

	void setFacetDisplayed(String facetDisplayed) {
		this.facetDisplayed = facetDisplayed;
	}

	public void showEntries() {
		setFacetDisplayed(UIFormsEntriesViewer.entriesFacet);
	}
	
	public void viewSource() {
		setSubmissionFacetDisplayed(UIFormsEntriesViewer.formSubmissionSourceFacet);
	}
	
	public void viewSubmission() {
		setSubmissionFacetDisplayed(UIFormsEntriesViewer.formSubmissionFacet);
	}

	PersistenceManager getPersistenceManager() {

		if (persistenceManager == null)
			ELUtil.getInstance().autowire(this);

		return persistenceManager;
	}

	void setPersistenceManager(PersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}

	public String getSubmissionFacetDisplayed() {
		return submissionFacetDisplayed;
	}

	public void setSubmissionFacetDisplayed(String submissionFacetDisplayed) {
		this.submissionFacetDisplayed = submissionFacetDisplayed;
	}
	
	public String getSubmissionSource() {
		
		Long submissionId = new Long(getSubmissionId());
		
		Submission submission = getPersistenceManager().getSubmission(submissionId);
		
		Document submissionDocument = submission.getSubmissionDocument();
		
		String serialized;
		
		try {
			serialized = FormManagerUtil.serializeDocument(submissionDocument);
			
		} catch (IOException e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Exception while serializing submission document. submission id = "+submission.getSubmissionId(), e);
			serialized = CoreConstants.EMPTY;
		}
		
		return serialized;
	}

	public String getSubmissionId() {

		String submissionIdStr = (String) FacesContext.getCurrentInstance()
		.getExternalContext().getRequestParameterMap().get(
				submissionIdParam);
		
		if(!StringUtil.isEmpty(submissionIdStr)) {
			setSubmissionId(submissionIdStr);
		}
		
		return submissionId;
	}

	public void setSubmissionId(String submissionId) {
		this.submissionId = submissionId;
	}
}