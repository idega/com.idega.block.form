package com.idega.block.form.entries.presentation;

import java.io.IOException;
import java.rmi.RemoteException;
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
 * @version $Revision: 1.4 $
 * 
 *          Last modified: $Date: 2009/07/14 16:23:05 $ by $Author: valdas $
 * 
 */
public class UIFormsEntriesViewer extends IWBaseComponent {

	public static final String COMPONENT_TYPE = "com.idega.UIFormsEntriesViewer";

	public static final String entriesFacet = "entries";
	public static final String formSubmissionFacet = "formSubmission";
	public static final String formSubmissionSourceFacet = "formSubmissionSource";
	
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
				.createValueBinding("#{formsEntries.formSubmissionRendered && formsEntries.submissionView}"));

		getFacets().put(formSubmissionFacet, facelet);
		
		facelet = (FaceletComponent) context.getApplication().createComponent(
				FaceletComponent.COMPONENT_TYPE);
		facelet.setFaceletURI(bundle.getFaceletURI("UIFormSubmissionSource.xhtml"));
		facelet.setValueBinding(renderedAtt, context.getApplication()
				.createValueBinding("#{formsEntries.formSubmissionRendered && formsEntries.sourceView}"));

		getFacets().put(formSubmissionSourceFacet, facelet);
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
		UIComponent formSubmissionSource = getFacet(formSubmissionSourceFacet);

		if (formSubmission.isRendered()) {
			renderChild(context, formSubmission);
		} else if (formSubmissionSource.isRendered()) {
			addClientResources(IWContext.getIWContext(context), entries);
			renderChild(context, formSubmissionSource);
		} else if (entries.isRendered()) {
			addClientResources(IWContext.getIWContext(context), entries);
			renderChild(context, entries);
		}
	}

	private void addClientResources(IWContext iwc, UIComponent container) {
		
		Web2Business web2Business = getWeb2Business();
		IWBundle bundle = getBundle((FacesContext)iwc, IWBundleStarter.BUNDLE_IDENTIFIER);
		
		List<String> scripts = new ArrayList<String>(1);
		scripts.add(bundle.getResourcesVirtualPath()+"/javascript/FormsEntriesViewer.js");
		try {
			scripts.add(web2Business.getBundleURIToMootoolsLib());
		} catch (RemoteException e) {
		}
		scripts.add(web2Business.getCodePressScriptFilePath());

		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
	}

	Web2Business getWeb2Business() {
		
		if(web2Business == null)
			ELUtil.getInstance().autowire(this);
		
		return web2Business;
	}
}