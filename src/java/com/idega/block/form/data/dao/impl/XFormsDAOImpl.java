package com.idega.block.form.data.dao.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Query;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.form.data.XForm;
import com.idega.block.form.data.XFormSubmission;
import com.idega.block.form.data.dao.XFormsDAO;
import com.idega.core.persistence.Param;
import com.idega.core.persistence.impl.GenericDaoImpl;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.xformsmanager.business.Form;
import com.idega.xformsmanager.business.Submission;
import com.idega.xformsmanager.business.XFormState;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.14 $ Last modified: $Date: 2009/02/12 16:53:52 $ by $Author: donatas $
 */
@Repository
@Transactional(readOnly = true)
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class XFormsDAOImpl extends GenericDaoImpl implements XFormsDAO {

	@Override
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

		return xforms;
	}

	@Override
	public List<Submission> getSubmissionsByTypeAndStorageType(String formType, String formStorageType, long formId) {
		String q = "select sub from " + XForm.class.getName()
		        + " as child inner join child.formParent as parent, "
		        + XFormSubmission.class.getName() + " as sub"
		        + " where sub.xform = child and parent.formId = :"
		        + XForm.formIdProperty;

		return getResultListByInlineQuery(q, Submission.class, new Param(XForm.formIdProperty, formId));
	}

	@Override
	public XForm getXFormByParentVersion(Form parentForm, Integer version, XFormState state) {
		@SuppressWarnings("unchecked")
		List<XForm> xforms = getEntityManager().createNamedQuery(
		    XForm.getByParentVersion).setParameter(XForm.formParentProperty,
		    parentForm).setParameter(XForm.versionProperty, version)
		        .setParameter(XForm.formStateProperty, state).getResultList();

		return xforms.isEmpty() ? null : xforms.iterator().next();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<XForm> getAllVersionsByParentId(Long parentId) {
		Query query = getEntityManager().createQuery("select xf from XForm xf where xf.formParent.id = :parent order by xf.version desc");
		query.setParameter("parent",parentId);
		return query.getResultList();
	}

	@Override
	public XForm getXFormById(Long formId) {
		XForm xform = (XForm) getEntityManager().createNamedQuery(
		    XForm.getByFormId).setParameter(XForm.formIdProperty, formId)
		        .getSingleResult();

		return xform;
	}

	@Override
	public XFormSubmission getSubmissionBySubmissionUUID(String submissionUUID) {

		return getSingleResultByInlineQuery("from "
		        + XFormSubmission.class.getName() + " sub where sub."
		        + XFormSubmission.submissionUUIDProperty + " = :"
		        + XFormSubmission.submissionUUIDProperty,
		    XFormSubmission.class, new Param(
		            XFormSubmission.submissionUUIDProperty, submissionUUID));
	}

	private List<XFormSubmission> getSubmissions(boolean onlyFinal, 
			Integer ownerId) {
		return getSubmissions(onlyFinal, ownerId, null, Boolean.FALSE);
	}
	
	@Override
	public List<XFormSubmission> getAllLatestSubmissionsByUser(String personalID) {
		if (StringUtil.isEmpty(personalID)) {
			return null;
		}
		
		Long value = null;
		try {
			value = Long.valueOf(personalID);
		} catch (NumberFormatException e) {
			return null;
		}
		
		return getSubmissions(Boolean.FALSE, null, value, Boolean.TRUE);
	}
	
	/**
	 * 
	 * <p>Searches for {@link XFormSubmission}s by:</p>
	 * @param onlyFinal - {@value XFormSubmission#isFinalSubmissionProperty};
	 * @param ownerId - {@link User#getPrimaryKey()};
	 * @param personalIDs - {@link User#getPersonalID()} or 
	 * Company#getCEO()#getPersonalID();
	 * @param doSelectLastest - newest by {@link XFormSubmission#dateSubmittedProperty};
	 * @return {@link List} of {@link XFormSubmission} matching one of criteria 
	 * or {@link Collections#emptyList()} on failure.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	private List<XFormSubmission> getSubmissions(boolean onlyFinal, 
			Integer ownerId, Long personalIDs, 
			boolean doSelectLastest) {
		
		StringBuilder query = new StringBuilder();
		query.append("select submissions from ")
		.append(XFormSubmission.class.getName())
		.append(" submissions where submissions.")
		.append(XFormSubmission.isFinalSubmissionProperty)
		.append(" = :").append(XFormSubmission.isFinalSubmissionProperty);
		
		if (!onlyFinal) {
			query.append(" and (submissions.")
			.append(XFormSubmission.isValidSubmissionProperty)
			.append(" = true or submissions.")
			.append(XFormSubmission.isValidSubmissionProperty)
			.append(" is null)");
		}
		
		if (ownerId != null) {
			query.append(" and submissions.")
			.append(XFormSubmission.formSubmitterProperty)
			.append(" = :")
			.append(XFormSubmission.formSubmitterProperty);
		} else if (personalIDs != null) {
			StringBuilder querySQL = new StringBuilder();
			querySQL.append("SELECT DISTINCT i.IC_USER_ID FROM ic_user i, ic_company c ")
			.append("WHERE i.PERSONAL_ID LIKE '%" + personalIDs + "%' ")
			.append("OR (c.CEO_ID = i.IC_USER_ID AND c.PERSONAL_ID LIKE '%")
			.append(personalIDs)
			.append("%') ");
			
			Session session = (Session) getEntityManager().getDelegate();
			SQLQuery sqlQuery = session.createSQLQuery(querySQL.toString());
						
			List<Integer> results = sqlQuery.list();
			if (!ListUtil.isEmpty(results)) {
				query.append(" and submissions.").append(XFormSubmission.formSubmitterProperty).append(" in (");
				
				for (Iterator<Integer> i = results.iterator(); i.hasNext();){
					query.append(i.next());
					if (i.hasNext()) {
						query.append(CoreConstants.COMMA);
					}
				}
						
				query.append(")");
			}
		}
		
		if (doSelectLastest) {
			query.append(" and submissions.")
			.append(XFormSubmission.dateSubmittedProperty)
			.append(" = (SELECT max(s.")
			.append(XFormSubmission.dateSubmittedProperty).append(") FROM ")
			.append(XFormSubmission.class.getName()).append(" s where submissions.")
			.append(XFormSubmission.xformProperty).append(" = s.")
			.append(XFormSubmission.xformProperty).append(")");
		}

		List<XFormSubmission> submissions = null;
		Param finalSubmissionProperty = new Param(XFormSubmission.isFinalSubmissionProperty, onlyFinal);

		if (ownerId == null) {
			submissions = getResultListByInlineQuery(query.toString(), XFormSubmission.class, finalSubmissionProperty);
		}
		else {
			submissions = getResultListByInlineQuery(query.toString(), XFormSubmission.class, finalSubmissionProperty,
					new Param(XFormSubmission.formSubmitterProperty, ownerId));
		}

		return submissions;
	}

	@Override
	public List<XFormSubmission> getAllNotFinalSubmissions() {
		return getSubmissions(Boolean.FALSE, null);
	}

	@Override
	public List<XFormSubmission> getAllNotFinalSubmissionsByUser(Integer userId) {
		return getSubmissions(Boolean.FALSE, userId);
	}
	
	@Override
	public List<XFormSubmission> getAllNotFinalSubmissionsByUser(String personalID) {
		if (StringUtil.isEmpty(personalID)) {
			return null;
		}
		
		Long value = null;
		try {
			value = Long.valueOf(personalID);
		} catch (NumberFormatException e) {
			return null;
		}
		
		return getSubmissions(Boolean.FALSE, null, value, Boolean.FALSE);
	}

	@Override
	public List<XFormSubmission> getAllLatestSubmissionsByUser(Integer userId) {
		return getSubmissions(Boolean.FALSE, userId, null, Boolean.TRUE);
	}

	@Override
	public List<XForm> getXFormsByNameAndStorageIndetifierAndType(String name, String storageIdentifier, String type) {
		if (StringUtil.isEmpty(name) || StringUtil.isEmpty(storageIdentifier) || StringUtil.isEmpty(type))
			return Collections.emptyList();

		String q = "select distinct xf from XForm xf where xf." + XForm.formTypeProperty + " = :"
		        + XForm.formTypeProperty + " and xf."
		        + XForm.formStorageIdentifierProperty + " like :"
		        + XForm.formStorageIdentifierProperty + " and xf."
		        + XForm.displayNameProperty + " = :" + XForm.displayNameProperty;

		List<XForm> xforms = getResultListByInlineQuery(q, XForm.class,
				new Param(XForm.formTypeProperty, type),
				new Param(XForm.formStorageIdentifierProperty, "%" + storageIdentifier + "%"),
				new Param(XForm.displayNameProperty, name)
		);
		return xforms;
	}

}