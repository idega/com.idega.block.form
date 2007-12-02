package com.idega.block.form.process.ui;

import javax.faces.context.FacesContext;
import org.w3c.dom.Document;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 *
 * Last modified: $Date: 2007/12/02 11:50:25 $ by $Author: civilis $
 */
public interface ProcessFormManager {

	public abstract Document loadDefinitionForm(FacesContext context, Long processDefinitionId, int initiatorId);
	
	public abstract Document loadInstanceForm(FacesContext context, Long processInstanceId);
	
	public abstract Document loadTaskInstanceForm(FacesContext context, Long taskInstanceId);
	
	public abstract Document loadProcessViewForm(FacesContext context, Long processInstanceId, int viewerId);
}