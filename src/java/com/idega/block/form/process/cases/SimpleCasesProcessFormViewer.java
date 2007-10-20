package com.idega.block.form.process.cases;

import java.io.IOException;

import javax.faces.context.FacesContext;

import com.idega.block.form.process.cases.beans.SimpleCasesProcessProcessingBean;
import com.idega.presentation.IWBaseComponent;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 *
 * Last modified: $Date: 2007/10/20 20:13:59 $ by $Author: civilis $
 */
public class SimpleCasesProcessFormViewer extends IWBaseComponent {
	
	
	public static final String PROCESS_DEFINITION_PROPERTY = "processDefinitionId";
	public static final String PROCESS_INSTANCE_PROPERTY = "processInstanceId";
	public static final String PROCESSING_BEAN_PROPERTY = "processingBean";
	
	private String processDefinitionId;
	private String processInstanceId;
	private SimpleCasesProcessProcessingBean processingBean;
    
	public SimpleCasesProcessProcessingBean getProcessingBean() {
		
		return processingBean;
	}
	
	public SimpleCasesProcessProcessingBean getProcessingBean(FacesContext context) {
		
		SimpleCasesProcessProcessingBean processingBean = getProcessingBean();
		
		if(processingBean == null) {
			
			processingBean = getValueBinding(PROCESSING_BEAN_PROPERTY) != null ? (SimpleCasesProcessProcessingBean)getValueBinding(PROCESSING_BEAN_PROPERTY).getValue(context) : null;
			setProcessingBean(processingBean);
		}
		
		return processingBean;
	}

	public void setProcessingBean(SimpleCasesProcessProcessingBean processingBean) {
		this.processingBean = processingBean;
	}

	public String getProcessDefinitionId() {
		
		return processDefinitionId;
	}
	
	public String getProcessDefinitionId(FacesContext context) {

		String processDefinitionId = getProcessDefinitionId();
		
		if(processDefinitionId == null) {
			
			processDefinitionId = getValueBinding(PROCESS_DEFINITION_PROPERTY) != null ? (String)getValueBinding(PROCESS_DEFINITION_PROPERTY).getValue(context) : (String)context.getExternalContext().getRequestParameterMap().get(PROCESS_DEFINITION_PROPERTY);
			setProcessDefinitionId(processDefinitionId);
		}
		
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		
		this.processDefinitionId = processDefinitionId;
	}

	public String getProcessInstanceId() {
		
		return processInstanceId;
	}
	
	public String getProcessInstanceId(FacesContext context) {

		String processInstanceId = getProcessInstanceId();
		
		if(processInstanceId == null) {
			
			processInstanceId = getValueBinding(PROCESS_INSTANCE_PROPERTY) != null ? (String)getValueBinding(PROCESS_INSTANCE_PROPERTY).getValue(context) : (String)context.getExternalContext().getRequestParameterMap().get(PROCESS_INSTANCE_PROPERTY);
			setProcessInstanceId(processInstanceId);
		}
		
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		
		this.processInstanceId = processInstanceId;
	}

	public SimpleCasesProcessFormViewer() {
		
		super();
		setRendererType(null);
	}
	
	@Override
	protected void initializeComponent(FacesContext context) {
		
		
		SimpleCasesProcessProcessingBean processingBean = getProcessingBean(context);
		
		String processDefinitionId = getProcessDefinitionId();
		
		if(processDefinitionId != null) {
			
			/*form = */processingBean.loadDefinitionForm(Long.parseLong(processDefinitionId));
			
			return;
		}
		
		String processInstanceId = getProcessInstanceId();
		
		if(processInstanceId != null) {
			/*form = */processingBean.loadInstanceForm(Long.parseLong(processInstanceId));
			
			return;
		}
	}
	
	@Override
	public boolean getRendersChildren() {
		return true;
	}
	
	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		// TODO Auto-generated method stub
		super.encodeBegin(context);
	}
	
	@Override
	public void encodeChildren(FacesContext context) throws IOException {
		
		super.encodeChildren(context);

	}
	
	@Override
	public void encodeEnd(FacesContext context) throws IOException {
		// TODO Auto-generated method stub
		super.encodeEnd(context);
	}
}