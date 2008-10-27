package com.idega.block.form.entries.presentation.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.form.entries.presentation.UIFormsEntriesViewer;
import com.idega.documentmanager.business.PersistenceManager;
import com.idega.documentmanager.business.Submission;
import com.idega.documentmanager.business.XFormPersistenceType;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

/**
 * 
 * @author <a href="civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 * 
 *          Last modified: $Date: 2008/10/27 20:18:49 $ by $Author: civilis $
 * 
 */
@Scope("request")
@Service("formsEntries")
public class FormsEntriesState implements Serializable {

	private static final long serialVersionUID = 7398164884900011980L;
	private String facetDisplayed;
	private static final String submissionIdParam = "submissionId";

	@Autowired
	@XFormPersistenceType("slide")
	private transient PersistenceManager persistenceManager;

	public List<FormSubmissionEntry> getFormsEntries() {

		List<Submission> submissions = getPersistenceManager()
				.getAllStandaloneFormsSubmissions();

		ArrayList<FormSubmissionEntry> entries = new ArrayList<FormSubmissionEntry>(
				!submissions.isEmpty() ? submissions.size() : 2);

		if (!submissions.isEmpty()) {

			for (Submission submission : submissions) {

				entries.add(new FormSubmissionEntry(submission
						.getSubmissionId(), submission.getXform()
						.getDisplayName()));
			}

		} else {

			FormSubmissionEntry entry = new FormSubmissionEntry(1L,
					"test submission entry 1");
			entries.add(entry);
			entry = new FormSubmissionEntry(2L, "test submission entry 2");
			entries.add(entry);
		}

		return entries;
	}

	public boolean isEntriesRendered() {

		return UIFormsEntriesViewer.entriesFacet.equals(getFacetDisplayed());
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

	PersistenceManager getPersistenceManager() {

		if (persistenceManager == null)
			ELUtil.getInstance().autowire(this);

		return persistenceManager;
	}

	void setPersistenceManager(PersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}
}