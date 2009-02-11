package com.idega.block.form.bean;

import java.util.Date;

import com.idega.user.data.User;

public class SubmissionDataBean {
	
	public SubmissionDataBean() {}
	
	public SubmissionDataBean(Long formId, String submissionUUID, Date submittedDate, User formAuthor) {
		this.formId = formId;
		this.submissionUUID = submissionUUID;
		this.submittedDate = submittedDate;
		this.formAuthor = formAuthor;
	}
	
	private Long formId;
	
	private String submissionUUID;
	private String localizedTitle;
	
	private User formAuthor;
	
	private Date submittedDate;

	public Long getFormId() {
		return formId;
	}

	public void setFormId(Long formId) {
		this.formId = formId;
	}

	public String getSubmissionUUID() {
		return submissionUUID;
	}

	public void setSubmissionUUID(String submissionUUID) {
		this.submissionUUID = submissionUUID;
	}

	public Date getSubmittedDate() {
		return submittedDate;
	}

	public void setSubmittedDate(Date submittedDate) {
		this.submittedDate = submittedDate;
	}

	public User getFormAuthor() {
		return formAuthor;
	}

	public void setFormAuthor(User formAuthor) {
		this.formAuthor = formAuthor;
	}

	public String getLocalizedTitle() {
		return localizedTitle;
	}

	public void setLocalizedTitle(String localizedTitle) {
		this.localizedTitle = localizedTitle;
	}

}
