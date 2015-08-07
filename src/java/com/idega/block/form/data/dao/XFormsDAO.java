package com.idega.block.form.data.dao;

import java.sql.Date;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.idega.block.form.data.XForm;
import com.idega.block.form.data.XFormSubmission;
import com.idega.core.persistence.GenericDao;
import com.idega.user.data.User;
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

	public abstract List<XFormSubmission> getAllNotFinalSubmissionsByUser(Integer userId, Collection<String> procDefNames, Date from, Date to);

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
	public List<XFormSubmission> getAllLatestSubmissionsByUser(Integer userId, Collection<String> procDefNames, Date from, Date to);

	public List<XForm> getXFormsByNameAndStorageIndetifierAndType(String name, String storageIdentifier, String type);

	/**
	 *
	 * @param personalID - {@link User#getPersonalID()} or
	 * {@link User#getPersonalID()} of Company#getCEO, not <code>null</code>;
	 * @return {@link List} of {@link XFormSubmission}s, where
	 * {@value XFormSubmission#isFinalSubmissionProperty} is <code>false</code>
	 * and not only lastest by date {@link XFormSubmission}s are selected.
	 * {@link Collections#emptyList()} on failure.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public List<XFormSubmission> getAllNotFinalSubmissionsByUser(String personalID, Collection<String> procDefNames, Date from, Date to);

	/**
	 *
	 * @param personalID - {@link User#getPersonalID()} or
	 * {@link User#getPersonalID()} of Company#getCEO, not <code>null</code>;
	 * @return {@link List} of {@link XFormSubmission}s, where
	 * {@value XFormSubmission#isFinalSubmissionProperty} is <code>false</code>
	 * and only lastest by date {@link XFormSubmission}s are selected.
	 * {@link Collections#emptyList()} on failure.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public List<XFormSubmission> getAllLatestSubmissionsByUser(String personalID, Collection<String> procDefNames, Date from, Date to);

	public List<XFormSubmission> getSubmissions(Integer userId, Collection<String> procDefNames, Date from, Date to, boolean onlyLatest);
	
	public List<String> getDistinctXFormNames();
	
	public List<String> getDistinctXFormNamesByStorageIdentifierProperty(List<String> procDefNames);
	
	public List<String> getStorageIdentifierPropertyByNameAndSIP(String name, List<String> procDefNames);

}