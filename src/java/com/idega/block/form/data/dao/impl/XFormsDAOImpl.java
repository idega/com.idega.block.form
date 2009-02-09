package com.idega.block.form.data.dao.impl;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.form.data.XForm;
import com.idega.block.form.data.XFormSubmission;
import com.idega.block.form.data.dao.XFormsDAO;
import com.idega.core.persistence.Param;
import com.idega.core.persistence.impl.GenericDaoImpl;
import com.idega.xformsmanager.business.Form;
import com.idega.xformsmanager.business.Submission;
import com.idega.xformsmanager.business.XFormState;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.13 $ Last modified: $Date: 2009/02/09 15:02:04 $ by $Author: valdas $
 */
@Scope("singleton")
@Repository
@Transactional(readOnly = true)
public class XFormsDAOImpl extends GenericDaoImpl implements XFormsDAO {
	
	public List<Form> getAllXFormsByTypeAndStorageType(String formType,
	        String formStorageType, XFormState state) {
		
		String q = "from XForm xf where xf." + XForm.formTypeProperty + " = :"
		        + XForm.formTypeProperty + " and xf."
		        + XForm.formStorageTypeProperty + " = :"
		        + XForm.formStorageTypeProperty + " and xf."
		        + XForm.formStateProperty + " = :" + XForm.formStateProperty;
		
		@SuppressWarnings("unchecked")
		List<Form> xforms = getEntityManager().createQuery(q).setParameter(
		    XForm.formTypeProperty, formType).setParameter(
		    XForm.formStorageTypeProperty, formStorageType).setParameter(
		    XForm.formStateProperty, state).getResultList();
		
		// @SuppressWarnings("unchecked")
		// List<Form> xforms =
		// getEntityManager().createNamedQuery(XForm.getAllByTypeAndStorageType)
		// .setParameter(XForm.formTypeProperty, formType)
		// .setParameter(XForm.formStorageTypeProperty, formStorageType)
		// .getResultList();
		
		return xforms;
	}
	
	public List<Submission> getSubmissionsByTypeAndStorageType(String formType,
	        String formStorageType, long formId) {
		
		String q = "select sub from " + XForm.class.getName()
		        + " as child inner join child.formParent as parent, "
		        + XFormSubmission.class.getName() + " as sub"
		        + " where sub.xform = child and parent.formId = :"
		        + XForm.formIdProperty;
		
		List<Submission> submissions = getEntityManager().createQuery(q)
		        .setParameter(XForm.formIdProperty, formId).getResultList();
		
		return submissions;
	}
	
	public XForm getXFormByParentVersion(Form parentForm, Integer version,
	        XFormState state) {
		
		@SuppressWarnings("unchecked")
		List<XForm> xforms = getEntityManager().createNamedQuery(
		    XForm.getByParentVersion).setParameter(XForm.formParentProperty,
		    parentForm).setParameter(XForm.versionProperty, version)
		        .setParameter(XForm.formStateProperty, state).getResultList();
		
		return xforms.isEmpty() ? null : xforms.iterator().next();
	}
	
	public XForm getXFormById(Long formId) {
		XForm xform = (XForm) getEntityManager().createNamedQuery(
		    XForm.getByFormId).setParameter(XForm.formIdProperty, formId)
		        .getSingleResult();
		
		return xform;
	}
	
	public XFormSubmission getSubmissionBySubmissionUUID(String submissionUUID) {
		
		return getSingleResultByInlineQuery("from "
		        + XFormSubmission.class.getName() + " sub where sub."
		        + XFormSubmission.submissionUUIDProperty + " = :"
		        + XFormSubmission.submissionUUIDProperty,
		    XFormSubmission.class, new Param(
		            XFormSubmission.submissionUUIDProperty, submissionUUID));
	}

	private List<XFormSubmission> getSubmissions(boolean onlyFinal, Integer ownerId) {
		String query = "select submissions from " + XFormSubmission.class.getName() + " submissions where submissions." +
			XFormSubmission.isFinalSubmissionProperty + " = :" + XFormSubmission.isFinalSubmissionProperty;
		if (!onlyFinal) {
			query += " and (submissions." + XFormSubmission.isValidSubmissionProperty + " = true or submissions." + XFormSubmission.isValidSubmissionProperty +
			" is null)";
		}
		if (ownerId != null) {
			query += " and submissions." + XFormSubmission.formSubmitterProperty + " = :" + XFormSubmission.formSubmitterProperty;
		}
		
		List<XFormSubmission> submissions = null;
		Param finalSubmissionProperty = new Param(XFormSubmission.isFinalSubmissionProperty, onlyFinal);
		
		if (ownerId == null) {
			submissions = getResultListByInlineQuery(query, XFormSubmission.class, finalSubmissionProperty);
		}
		else {
			submissions = getResultListByInlineQuery(query, XFormSubmission.class, finalSubmissionProperty,
					new Param(XFormSubmission.formSubmitterProperty, ownerId));
		}
		
		return submissions;
	}
	
	public List<XFormSubmission> getAllNotFinalSubmissions() {
		return getSubmissions(Boolean.FALSE, null);
	}

	public List<XFormSubmission> getAllNotFinalSubmissionsByUser(Integer userId) {
		return getSubmissions(Boolean.FALSE, userId);
	}
}