package com.idega.block.form.process.cases.beans;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jbpm.JbpmConfiguration;

import com.idega.jbpm.data.CasesJbpmBind;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2007/10/20 20:13:59 $ by $Author: civilis $
 */
public class SimpleCasesProcessProcessingBean {

	private JbpmConfiguration jbpmConfiguration;
	private SessionFactory sessionFactory;
	
	public void loadDefinitionForm(Long processDefinitionId) {
		
		System.out.println("looading definition form for: "+processDefinitionId);
		
		Session session = getSessionFactory().getCurrentSession();

		CasesJbpmBind bind = (CasesJbpmBind)session.load(CasesJbpmBind.class, processDefinitionId);
		System.out.println("bind");
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
}