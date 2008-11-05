package com.idega.block.form.entries.presentation.beans;

import com.idega.xformsmanager.business.Form;
import com.idega.xformsmanager.business.Submission;

/**
 * 
 * @author <a href="civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.3 $
 * 
 *          Last modified: $Date: 2008/11/05 08:50:24 $ by $Author: civilis $
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