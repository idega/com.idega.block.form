package com.idega.block.form.presentation;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Locale;

import javax.faces.context.FacesContext;

import org.apache.myfaces.custom.htmlTag.HtmlTag;
import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.form.IWBundleStarter;
import com.idega.block.form.data.XFormSubmission;
import com.idega.block.form.data.dao.XFormsDAO;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.text.Link;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.URIUtil;
import com.idega.util.expression.ELUtil;
import com.idega.xformsmanager.business.DocumentManagerFactory;
import com.idega.xformsmanager.component.beans.LocalizedStringBean;

public class SavedForms extends IWBaseComponent {

	@Autowired
	private XFormsDAO xformsDAO;
	
	@Autowired
	private DocumentManagerFactory documentManager;
	
	private boolean showAll = Boolean.TRUE;

	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		
		IWContext iwc = IWContext.getIWContext(context);
		PresentationUtil.addStyleSheetToHeader(iwc, getBundle(context, IWBundleStarter.BUNDLE_IDENTIFIER)
				.getVirtualPathWithFileNameString("style/formsEntries.css"));
		
		ELUtil.getInstance().autowire(this);
		
		HtmlTag container = (HtmlTag) context.getApplication().createComponent(HtmlTag.COMPONENT_TYPE);
		container.setValue("div");
		container.setStyleClass("savedFormsViewer");
		this.getChildren().add(container);
		
		List<XFormSubmission> submissions = getAllSubmissions(context);
		if (ListUtil.isEmpty(submissions)) {
			return;
		}
		
		IWMainApplication iwma = iwc.getIWMainApplication();
		Locale locale = iwc.getCurrentLocale();
		
		BuilderService bs = null;
		try {
			bs = BuilderServiceFactory.getBuilderService(iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		String url = bs.getFullPageUrlByPageType(iwc, FormViewer.formviewerPageType, true);
				
		HtmlTag list = (HtmlTag) context.getApplication().createComponent(HtmlTag.COMPONENT_TYPE);
		list.setValue("ul");
		container.getChildren().add(list);
		for (XFormSubmission submission: submissions) {
			HtmlTag listItem = (HtmlTag) context.getApplication().createComponent(HtmlTag.COMPONENT_TYPE);
			listItem.setValue("li");
			list.getChildren().add(listItem);
			
			LocalizedStringBean localizedTitle = getDocumentManager().newDocumentManager(iwma).openFormLazy(submission.getXform().getFormId()).getFormTitle();
			Link linkToSavedForm = new Link(localizedTitle.getString(locale));
			URIUtil uriUtil = new URIUtil(url);
			uriUtil.setParameter(FormViewer.submissionIdParam, submission.getSubmissionUUID());
			linkToSavedForm.setURL(uriUtil.getUri());
			
			listItem.getChildren().add(linkToSavedForm);
		}
	}

	private List<XFormSubmission> getAllSubmissions(FacesContext context) {
		Integer currentUserId = null;
		if (!isShowAll()) {
			IWContext iwc = IWContext.getIWContext(context);
			if (iwc.isLoggedOn()) {
				currentUserId = iwc.getCurrentUserId();
			}
		}
		
		return getXformsDAO().getAllNotFinalSubmissionsByUser(currentUserId);
	}
	
	@Override
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		this.showAll = (values[1] instanceof Boolean) ? (Boolean) values[1] : Boolean.TRUE;
	}

	@Override
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[2];
		values[0] = super.saveState(ctx);
		values[1] = isShowAll();
		return values;
	}

	public boolean isShowAll() {
		return showAll;
	}

	public void setShowAll(boolean showAll) {
		this.showAll = showAll;
	}

	public XFormsDAO getXformsDAO() {
		return xformsDAO;
	}

	public void setXformsDAO(XFormsDAO xformsDAO) {
		this.xformsDAO = xformsDAO;
	}

	public DocumentManagerFactory getDocumentManager() {
		return documentManager;
	}

	public void setDocumentManager(DocumentManagerFactory documentManager) {
		this.documentManager = documentManager;
	}
	
}
