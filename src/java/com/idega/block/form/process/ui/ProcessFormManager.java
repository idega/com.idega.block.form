package com.idega.block.form.process.ui;

import javax.faces.context.FacesContext;
import org.w3c.dom.Document;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2007/11/27 16:27:15 $ by $Author: civilis $
 */
public interface ProcessFormManager {

	public Document loadDefinitionForm(FacesContext context, Long processDefinitionId, int initiatorId);
	
	public Document loadInstanceForm(FacesContext context, Long processInstanceId);
	
	public Document loadProcessViewForm(FacesContext context, Long processInstanceId, int viewerId);
}