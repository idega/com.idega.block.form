package com.idega.block.form.process.cases;

import java.io.IOException;

import javax.faces.context.FacesContext;

import com.idega.presentation.IWBaseComponent;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2007/10/16 12:09:20 $ by $Author: civilis $
 */
public class SimpleCasesProcessFormViewer extends IWBaseComponent {
	
	
	public static final String PROCESS_DEFINITION_PROPERTY = "processDefinitionId";
	public static final String PROCESS_INSTANCE_PROPERTY = "processInstanceId";
	
	private String processDefinitionId;
	private String processInstanceId;
    
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