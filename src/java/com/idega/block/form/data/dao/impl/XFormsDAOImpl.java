package com.idega.block.form.data.dao.impl;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.form.data.XForm;
import com.idega.block.form.data.dao.XFormsDAO;
import com.idega.core.persistence.impl.GenericDaoImpl;
import com.idega.xformsmanager.business.Form;
import com.idega.xformsmanager.business.XFormState;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.9 $
 *
 * Last modified: $Date: 2008/11/05 08:51:03 $ by $Author: civilis $
 */
@Scope("singleton")
@Repository
@Transactional(readOnly=true)
public class XFormsDAOImpl extends GenericDaoImpl implements XFormsDAO {

	public List<Form> getAllXFormsByTypeAndStorageType(String formType, String formStorageType) {
		
		@SuppressWarnings("unchecked")
		List<Form> xforms = getEntityManager().createNamedQuery(XForm.getAllByTypeAndStorageType)
		.setParameter(XForm.formTypeProperty, formType)
		.setParameter(XForm.formStorageTypeProperty, formStorageType)
		.getResultList();
		
		return xforms;
	}
	
	public XForm getXFormByParentVersion(Long parentFormId, Integer version, XFormState state) {
		
		@SuppressWarnings("unchecked")
		List<XForm> xforms = getEntityManager().createNamedQuery(XForm.getByParentVersion)
		.setParameter(XForm.formParentProperty, parentFormId)
		.setParameter(XForm.versionProperty, version)
		.setParameter(XForm.formStateProperty, state)
		.getResultList();
		
		return xforms.isEmpty() ? null : xforms.iterator().next();
	}

	public XForm getXFormById(Long formId) {
		XForm xform = (XForm) getEntityManager().createNamedQuery(XForm.getByFormId)
		.setParameter(XForm.formIdProperty, formId)
		.getSingleResult();
		
		return xform;
	}
}