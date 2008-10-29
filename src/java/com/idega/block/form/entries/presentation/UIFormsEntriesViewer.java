package com.idega.block.form.entries.presentation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.form.IWBundleStarter;
import com.idega.block.web2.business.Web2Business;
import com.idega.facelets.ui.FaceletComponent;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;

/**
 * 
 * @author <a href="civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 * 
 *          Last modified: $Date: 2008/10/29 10:08:03 $ by $Author: alexis $
 * 
 */
public class UIFormsEntriesViewer extends IWBaseComponent {

	public static final String COMPONENT_TYPE = "com.idega.UIFormsEntriesViewer";

	public static final String entriesFacet = "entries";
	public static final String formSubmissionFacet = "formSubmission";
	public static final String formIdParam 	= "formId";
	
	private String formId;
	
	@Autowired private Web2Business web2Business;

	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);

		IWBundle bundle = getBundle(context, IWBundleStarter.BUNDLE_IDENTIFIER);
		FaceletComponent facelet = (FaceletComponent) context.getApplication()
				.createComponent(FaceletComponent.COMPONENT_TYPE);
		facelet.setFaceletURI(bundle.getFaceletURI("UIFormsEntries.xhtml"));
		facelet.setValueBinding(renderedAtt, context.getApplication()
				.createValueBinding("#{formsEntries.entriesRendered}"));

		getFacets().put(entriesFacet, facelet);

		facelet = (FaceletComponent) context.getApplication().createComponent(
				FaceletComponent.COMPONENT_TYPE);
		facelet.setFaceletURI(bundle.getFaceletURI("UIFormSubmission.xhtml"));
		facelet.setValueBinding(renderedAtt, context.getApplication()
				.createValueBinding("#{formsEntries.formSubmissionRendered}"));

		getFacets().put(formSubmissionFacet, facelet);
	}

	@Override
	public boolean getRendersChildren() {
		return true;
	}

	@Override
	public void encodeChildren(FacesContext context) throws IOException {
		super.encodeChildren(context);

		UIComponent entries = getFacet(entriesFacet);
		UIComponent formSubmission = getFacet(formSubmissionFacet);

		if (formSubmission.isRendered()) {
			renderChild(context, formSubmission);
		} else if (entries.isRendered()) {

			addClientResources(IWContext.getIWContext(context), entries);
			renderChild(context, entries);
		}
	}

	private void addClientResources(IWContext iwc, UIComponent container) {
		
//		Web2Business web2Business = getWeb2Business();
		
		IWBundle bundle = getBundle((FacesContext)iwc, IWBundleStarter.BUNDLE_IDENTIFIER);
		
		List<String> scripts = new ArrayList<String>(1);
		scripts.add(bundle.getResourcesVirtualPath()+"/javascript/FormsEntriesViewer.js");
		
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
		
		/*
		 * Web2Business web2Business =
		 * getBeanInstance(Web2Business.SPRING_BEAN_IDENTIFIER); IWBundle bundle
		 * = getBundle((FacesContext)iwc, IWBundleStarter.IW_BUNDLE_IDENTIFIER);
		 * 
		 * // CSS sources List<String> cssFiles = new ArrayList<String>();
		 * cssFiles.add(web2Business.getBundleURIToJQGridStyles());
		 * cssFiles.add(
		 * web2Business.getBundleUriToHumanizedMessagesStyleSheet()); if
		 * (isAllowPDFSigning()) {
		 * cssFiles.add(web2Business.getBundleUtiToGreyBoxStyleSheet()); }
		 * PresentationUtil.addStyleSheetsToHeader(iwc, cssFiles);
		 * 
		 * boolean isSingle = CoreUtil.isSingleComponentRenderingProcess(iwc);
		 * 
		 * // JS sources List<String> scripts = new ArrayList<String>(); if
		 * (!isSingle) { scripts.add(web2Business.getBundleURIToJQueryLib()); }
		 * scripts.add(web2Business.getBundleURIToJQGrid());
		 * scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		 * scripts.add(CoreConstants.DWR_UTIL_SCRIPT);
		 * scripts.add("/dwr/interface/BPMProcessAssets.js"); if
		 * (isAllowPDFSigning()) {
		 * scripts.add(web2Business.getBundleUtiToGreyBoxScript());
		 * scripts.add("/dwr/interface/PDFGeneratorFromProcess.js"); }
		 * scripts.add(web2Business.getBundleUriToHumanizedMessagesScript());
		 * scripts
		 * .add(bundle.getResourcesVirtualPath()+"/javascript/CasesBPMAssets.js"
		 * ); PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
		 * 
		 * // JS actions IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		 * String gridLocalization = new StringBuilder(
		 * "if(CasesBPMAssets.Loc == null || !CasesBPMAssets.Loc.inited) { \nif(CasesBPMAssets.Loc == null) { CasesBPMAssets.Loc = { inited: false }; }\n"
		 * )
		 * 
		 * .append("CasesBPMAssets.Loc.CASE_GRID_STRING_CONTACT_NAME = '")
		 * .append(iwrb.getLocalizedString("cases_bpm.human_name",
		 * "Name")).append("';\n")
		 * .append("CasesBPMAssets.Loc.CASE_GRID_STRING_TASK_NAME = '")
		 * .append(iwrb.getLocalizedString("cases_bpm.task_name",
		 * "Task name")).append("';\n")
		 * .append("CasesBPMAssets.Loc.CASE_GRID_STRING_FORM_NAME = '")
		 * .append(iwrb.getLocalizedString("cases_bpm.document_name",
		 * "Document name")).append("';\n")
		 * .append("CasesBPMAssets.Loc.CASE_GRID_STRING_SENDER = '")
		 * .append(iwrb.getLocalizedString("sender", "Sender")).append("';\n")
		 * .append("CasesBPMAssets.Loc.CASE_GRID_STRING_DATE = '")
		 * .append(iwrb.getLocalizedString("date", "Date")).append("';\n")
		 * .append("CasesBPMAssets.Loc.CASE_GRID_STRING_TAKEN_BY = '")
		 * .append(iwrb.getLocalizedString("cases_bpm.assigned_to",
		 * "Taken by")).append("';\n")
		 * .append("CasesBPMAssets.Loc.CASE_GRID_STRING_EMAIL_ADDRESS = '")
		 * .append(iwrb.getLocalizedString("email_address",
		 * "E-mail address")).append("';\n")
		 * .append("CasesBPMAssets.Loc.CASE_GRID_STRING_PHONE_NUMBER = '")
		 * .append(iwrb.getLocalizedString("phone_number",
		 * "Phone number")).append("';\n")
		 * .append("CasesBPMAssets.Loc.CASE_GRID_STRING_ADDRESS = '")
		 * .append(iwrb.getLocalizedString("address", "Address")).append("';\n")
		 * .append("CasesBPMAssets.Loc.CASE_GRID_STRING_SUBJECT = '")
		 * .append(iwrb.getLocalizedString("cases_bpm.subject",
		 * "Subject")).append("';\n")
		 * .append("CasesBPMAssets.Loc.CASE_GRID_STRING_FILE_DESCRIPTION = '")
		 * .append(iwrb.getLocalizedString("cases_bpm.file_description",
		 * "Descriptive name")).append("';\n")
		 * .append("CasesBPMAssets.Loc.CASE_GRID_STRING_FILE_NAME = '")
		 * .append(iwrb.getLocalizedString("cases_bpm.file_name",
		 * "File name")).append("';\n")
		 * .append("CasesBPMAssets.Loc.CASE_GRID_STRING_CHANGE_ACCESS_RIGHTS = '"
		 * ) .append(iwrb.getLocalizedString("cases_bpm.change_access_rights",
		 * "Change access rights")).append("';\n").append(
		 * "CasesBPMAssets.Loc.CASE_GRID_STRING_DOWNLOAD_DOCUMENT_AS_PDF = '")
		 * .append(iwrb.getLocalizedString("cases_bpm.get_document_as_pdf",
		 * "Download document")).append("';\n")
		 * .append("CasesBPMAssets.Loc.CASE_GRID_STRING_FILE_SIZE = '")
		 * .append(iwrb.getLocalizedString("cases_bpm.file_size",
		 * "File size")).append("';\n")
		 * .append("CasesBPMAssets.Loc.CASE_GRID_STRING_SUBMITTED_BY = '")
		 * .append(iwrb.getLocalizedString("cases_bpm.submitted_by",
		 * "Submitted by")).append("';\n")
		 * 
		 * .append("CasesBPMAssets.Loc.inited = true; }\n")
		 * 
		 * .toString();
		 * 
		 * String clientId = container.getClientId(iwc); if (clientId == null) {
		 * container.setId(iwc.getViewRoot().createUniqueId()); clientId =
		 * container.getClientId(iwc); }
		 * 
		 * CasesBPMAssetsState stateBean =
		 * getBeanInstance(CasesBPMAssetsState.beanIdentifier); Long
		 * processInstanceId = stateBean.getProcessInstanceId(); Integer caseId
		 * = stateBean.getCaseId();
		 * 
		 * String mainAction = newStringBuffer(gridLocalization).append(
		 * "\n CasesBPMAssets.initGrid(jQuery('div."
		 * ).append(clientId).append("')[0], ")
		 * .append(processInstanceId.toString
		 * ()).append(", ").append(caseId.toString
		 * ()).append(", ").append(isUsePdfDownloadColumn()).append(", ")
		 * .append(isAllowPDFSigning()).append(");").toString();
		 * 
		 * if (!isSingle) { mainAction = new
		 * StringBuffer("jQuery(document).ready(function() {\n"
		 * ).append(mainAction).append("\n});").toString(); }
		 * 
		 * PresentationUtil.addJavaScriptActionToBody(iwc, mainAction);
		 */
	}

	Web2Business getWeb2Business() {
		
		if(web2Business == null)
			ELUtil.getInstance().autowire(this);
		
		return web2Business;
	}
	
	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}
}