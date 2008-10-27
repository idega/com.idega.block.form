package com.idega.block.form.entries.presentation.beans;


/**
 * 
 * @author <a href="civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 * 
 *          Last modified: $Date: 2008/10/27 20:18:49 $ by $Author: civilis $
 * 
 */
public class FormSubmissionEntry {

	public FormSubmissionEntry(Long submissionId, String formName) {
		this.submissionId = submissionId;
		this.formName = formName;
	}
	private Long submissionId;
	private String formName;
	
	public Long getSubmissionId() {
		return submissionId;
	}
	public void setSubmissionId(Long submissionId) {
		this.submissionId = submissionId;
	}
	public String getFormName() {
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}
}