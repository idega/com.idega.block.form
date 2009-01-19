package com.idega.block.form.data.dao;

import java.util.List;

import com.idega.block.form.data.XForm;
import com.idega.block.form.data.XFormSubmission;
import com.idega.core.persistence.GenericDao;
import com.idega.xformsmanager.business.Form;
import com.idega.xformsmanager.business.Submission;
import com.idega.xformsmanager.business.XFormState;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.7 $ Last modified: $Date: 2009/01/19 21:48:53 $ by $Author: civilis $
 */
public interface XFormsDAO extends GenericDao {
	
	public abstract List<Form> getAllXFormsByTypeAndStorageType(
	        String formType, String formStorageType, XFormState state);
	
	public abstract List<Submission> getSubmissionsByTypeAndStorageType(
	        String formType, String formStorageType, long formId);
	
	public abstract XForm getXFormByParentVersion(Form parentForm,
	        Integer version, XFormState state);
	
	public abstract XForm getXFormById(Long formId);
	
	public abstract XFormSubmission getSubmissionBySubmissionUUID(
	        String submissionUUID);
}