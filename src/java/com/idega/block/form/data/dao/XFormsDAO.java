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
 * @version $Revision: 1.9 $ Last modified: $Date: 2009/02/12 16:53:52 $ by $Author: donatas $
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

	public abstract List<XFormSubmission> getAllNotFinalSubmissions();

	public abstract List<XFormSubmission> getAllNotFinalSubmissionsByUser(Integer userId);

	/**
	 * Get all versions of forms which has a specified parent.
	 *
	 * @param parentId Parent id.
	 * @return List of XForms .
	 */
	public abstract List<XForm> getAllVersionsByParentId(Long parentId);

	/**gets only latest submissions by user
	 *
	 * @param userId
	 * @return
	 */
	public List<XFormSubmission> getAllLatestSubmissionsByUser(Integer userId);
}