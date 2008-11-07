package com.idega.block.form.data.dao;

import java.util.List;

import com.idega.block.form.data.XForm;
import com.idega.core.persistence.GenericDao;
import com.idega.xformsmanager.business.Form;
import com.idega.xformsmanager.business.XFormState;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.5 $
 * 
 *          Last modified: $Date: 2008/11/07 10:33:48 $ by $Author: civilis $
 */
public interface XFormsDAO extends GenericDao {

	public abstract List<Form> getAllXFormsByTypeAndStorageType(
			String formType, String formStorageType, XFormState state);

	public abstract XForm getXFormByParentVersion(Long parentFormId,
			Integer version, XFormState state);

	public abstract XForm getXFormById(Long formId);
}