package com.idega.block.form.data.dao;

import java.util.List;

import com.idega.block.form.data.XForm;
import com.idega.core.persistence.GenericDao;
import com.idega.documentmanager.business.XFormState;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/04/10 01:06:12 $ by $Author: civilis $
 */
public interface XFormsDAO extends GenericDao {

	public abstract List<XForm> getAllXFormsByTypeAndStorageType(String formType, String formStorageType);
	
	public abstract XForm getXFormByParentVersion(Long parentFormId, Integer version, XFormState state);
}