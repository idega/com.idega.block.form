package com.idega.block.form.entries.presentation.beans;

import com.idega.documentmanager.business.Form;
import com.idega.documentmanager.business.Submission;

/**
 * 
 * @author <a href="civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 * 
 *          Last modified: $Date: 2008/10/28 13:01:08 $ by $Author: civilis $
 * 
 */
public class FormSubmissionEntry {

	private Submission submission;
	private Form form;

	public FormSubmissionEntry(Submission submission, Form form) {
		this.submission = submission;
		this.form = form;
	}

	public Submission getSubmission() {
		return submission;
	}

	public void setSubmission(Submission submission) {
		this.submission = submission;
	}

	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}
}