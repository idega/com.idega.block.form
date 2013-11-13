package com.idega.block.form.event;

import org.springframework.context.ApplicationEvent;

public class FormSavedEvent extends ApplicationEvent {

	private static final long serialVersionUID = 2502092843201340504L;

	private Long xformSubmissionId;

	public FormSavedEvent(Object source, Long xformSubmissionId) {
		super(source);

		this.xformSubmissionId = xformSubmissionId;
	}

	public Long getSubmissionId() {
		return xformSubmissionId;
	}

}