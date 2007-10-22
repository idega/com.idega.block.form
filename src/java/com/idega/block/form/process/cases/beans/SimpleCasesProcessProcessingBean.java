package com.idega.block.form.process.cases.beans;

import javax.faces.context.FacesContext;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.taskmgmt.def.Task;

import com.idega.documentmanager.business.Document;
import com.idega.documentmanager.business.DocumentManager;
import com.idega.documentmanager.business.DocumentManagerFactory;
import com.idega.documentmanager.business.ext.SimpleCaseFormCreateDMIManager;
import com.idega.documentmanager.business.ext.SimpleCaseFormCreateMetaInf;
import com.idega.jbpm.data.CasesJbpmBind;
import com.idega.jbpm.def.View;
import com.idega.jbpm.def.ViewToTask;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 *
 * Last modified: $Date: 2007/10/22 15:36:25 $ by $Author: civilis $
 */
public class SimpleCasesProcessProcessingBean {

	private JbpmConfiguration jbpmConfiguration;
	private SessionFactory sessionFactory;
	private DocumentManagerFactory documentManagerFactory;
	private ViewToTask viewToTaskBinder;
	
	public org.w3c.dom.Document loadDefinitionForm(FacesContext context, Long processDefinitionId, int initiatorId) {
		
		System.out.println("looading definition form for: "+processDefinitionId+" & "+initiatorId);
		
		Session session = getSessionFactory().getCurrentSession();
		
		Transaction transaction = session.getTransaction();
		boolean transactionWasActive = transaction.isActive();
		
		if(!transactionWasActive)
			transaction.begin();
		
		JbpmContext ctx = getJbpmConfiguration().createJbpmContext();
		ctx.setSession(session);
		
		try {
			CasesJbpmBind bind = (CasesJbpmBind)session.load(CasesJbpmBind.class, processDefinitionId);
			
			String initTaskName = bind.getInitTaskName();
			
			if(initTaskName == null || "".equals(initTaskName))
				throw new NullPointerException("Init task name not found on CasesJbpmBind.");
			
			ProcessDefinition pd = ctx.getGraphSession().getProcessDefinition(processDefinitionId);
			Task initTask = pd.getTaskMgmtDefinition().getTask(initTaskName);
			
			View view = getViewToTaskBinder().getView(initTask.getId());
			String formId = view.getViewId();

			DocumentManager documentManager = getDocumentManagerFactory().newDocumentManager(context);
			Document form = documentManager.openForm(formId);

			SimpleCaseFormCreateDMIManager metaInfManager = new SimpleCaseFormCreateDMIManager();
			form.setMetaInformationManager(metaInfManager);
			
			SimpleCaseFormCreateMetaInf metaInf = new SimpleCaseFormCreateMetaInf();
			metaInf.setInitiatorId(String.valueOf(initiatorId));
			metaInf.setProcessDefinitionId(String.valueOf(processDefinitionId));
			metaInf.setCaseCategoryId(String.valueOf(bind.getCasesCategoryId()));
			metaInf.setCaseTypeId(String.valueOf(bind.getCasesTypeId()));
			metaInfManager.update(metaInf);
			
			return form.getXformsDocument();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
			
		} finally {
			
			ctx.close();
			
			if(!transactionWasActive)
				transaction.commit();
		}
	}
	
	public void loadInstanceForm(Long processInstanceId) {
		
		System.out.println("looading instance form for: "+processInstanceId);
	}

	public JbpmConfiguration getJbpmConfiguration() {
		return jbpmConfiguration;
	}

	public void setJbpmConfiguration(JbpmConfiguration jbpmConfiguration) {
		this.jbpmConfiguration = jbpmConfiguration;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public DocumentManagerFactory getDocumentManagerFactory() {
		return documentManagerFactory;
	}

	public void setDocumentManagerFactory(
			DocumentManagerFactory documentManagerFactory) {
		this.documentManagerFactory = documentManagerFactory;
	}

	public ViewToTask getViewToTaskBinder() {
		return viewToTaskBinder;
	}

	public void setViewToTaskBinder(ViewToTask viewToTaskBinder) {
		this.viewToTaskBinder = viewToTaskBinder;
	}
}