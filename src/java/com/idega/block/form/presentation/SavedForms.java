package com.idega.block.form.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

import org.apache.myfaces.custom.htmlTag.HtmlTag;
import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.form.IWBundleStarter;
import com.idega.block.form.bean.SubmissionDataBean;
import com.idega.block.form.business.SubmissionDataComparator;
import com.idega.block.form.data.XFormSubmission;
import com.idega.block.form.data.dao.XFormsDAO;
import com.idega.block.web2.business.JQueryPlugin;
import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.contact.data.Email;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.TableBodyRowGroup;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableHeaderCell;
import com.idega.presentation.TableHeaderRowGroup;
import com.idega.presentation.TableRow;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.GenericInput;
import com.idega.presentation.ui.SelectOption;
import com.idega.presentation.ui.TextInput;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.IWTimestamp;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.URIUtil;
import com.idega.util.expression.ELUtil;
import com.idega.xformsmanager.business.DocumentManagerFactory;
import com.idega.xformsmanager.component.beans.LocalizedStringBean;

public class SavedForms extends IWBaseComponent {

	@Autowired
	private XFormsDAO xformsDAO;
	
	@Autowired
	private DocumentManagerFactory documentManager;
	
	@Autowired
	private Web2Business web2;
	
	private boolean showAll = Boolean.TRUE;
	private boolean newestOnTop = Boolean.TRUE;
	private boolean showTableHeader = Boolean.TRUE;
	
	private Integer userId;

	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		
		IWContext iwc = IWContext.getIWContext(context);
		PresentationUtil.addStyleSheetToHeader(iwc, getBundle(context, IWBundleStarter.BUNDLE_IDENTIFIER)
				.getVirtualPathWithFileNameString("style/formsEntries.css"));
		
		ELUtil.getInstance().autowire(this);
		
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, web2.getBundleURIToJQueryLib());
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, web2.getBundleURIToJQueryPlugin(JQueryPlugin.TABLE_SORTER));
		
		HtmlTag container = (HtmlTag) context.getApplication().createComponent(HtmlTag.COMPONENT_TYPE);
		container.setValue("div");
		container.setStyleClass("savedFormsViewer");
		this.getChildren().add(container);
		
		List<XFormSubmission> submissions = getAllSubmissions(context);
		if (ListUtil.isEmpty(submissions)) {
			return;
		}
		
		List<String> addedSubmissions = new ArrayList<String>();
		List<SubmissionDataBean> submissionsData = new ArrayList<SubmissionDataBean>();
		for (XFormSubmission submission: submissions) {
			try {
				Long formId = submission.getXform().getFormId();
				String submissionUUID = submission.getSubmissionUUID();
				
				if (formId != null && !StringUtil.isEmpty(submissionUUID) && !addedSubmissions.contains(submissionUUID)) {
					addedSubmissions.add(submissionUUID);
					submissionsData.add(new SubmissionDataBean(formId, submissionUUID, submission.getDateSubmitted(),
							getUser(iwc, getUserId() == null ? isShowAll() ? submission.getFormSubmitter() : null : getUserId())));
				}
			} catch(Exception e) {
				Logger.getLogger(SavedForms.class.getName()).log(Level.SEVERE, "Error getting submission data", e);
			}
		}
		
		if (ListUtil.isEmpty(submissionsData)) {
			return;
		}
		
		Collections.sort(submissionsData, new SubmissionDataComparator(isNewestOnTop()));
		
		IWMainApplication iwma = iwc.getIWMainApplication();
		Locale locale = iwc.getCurrentLocale();
		
		BuilderService bs = null;
		try {
			bs = BuilderServiceFactory.getBuilderService(iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		String url = bs.getFullPageUrlByPageType(iwc, FormViewer.formviewerPageType, true);
		
		IWBundle bundle = iwc.getIWMainApplication().getBundle(IWBundleStarter.BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		
		Table2 table = new Table2();
		container.getChildren().add(table);
		table.setStyleClass("savedFormsViewerTable");
		TableHeaderRowGroup header = table.createHeaderRowGroup();
		header.setStyleClass("savedFormsViewerHeaderRow");
		if (!isShowTableHeader()) {
			header.setStyleAttribute("display", "none");
		}
		TableRow headerRow = header.createRow();
		headerRow.setStyleClass("header");
		TableHeaderCell emailCell = headerRow.createHeaderCell();
		emailCell.add(new Text(CoreConstants.EMPTY));
		
		TableHeaderCell formCell = headerRow.createHeaderCell();
		formCell.add(new Text(iwrb.getLocalizedString("saved_forms.form", "Form")));
		String sortTitle = iwrb.getLocalizedString("saved_forms.click_to_sort", "Click cell to sort data");
		formCell.setTitle(sortTitle);
		
		TableHeaderCell savedAtCell = headerRow.createHeaderCell();
		savedAtCell.add(new Text(iwrb.getLocalizedString("saved_forms.saved_at", "Date")));
		savedAtCell.setTitle(sortTitle);
		
		if (isShowAll()) {
			TableHeaderCell authorCell = headerRow.createHeaderCell();
			authorCell.add(new Text(iwrb.getLocalizedString("saved_forms.submission_author", "Author")));
			authorCell.setTitle(sortTitle);
		}
		
		int index = 0;
		String linkToForm = null;
		TableBodyRowGroup body = table.createBodyRowGroup();
		body.setStyleClass("savedFormsViewerBodyRows");
		for (SubmissionDataBean data: submissionsData) {
			TableRow bodyRow = body.createRow();
			bodyRow.setStyleClass(index % 2 == 0 ? "even" : "odd");
			
			URIUtil uriUtil = new URIUtil(url);
			uriUtil.setParameter(FormViewer.submissionIdParam, data.getSubmissionUUID());
			linkToForm = uriUtil.getUri();
			
			//	Email link
			bodyRow.createCell().add(getLinkToSendEmail(iwc, data, bundle, iwrb, linkToForm));
			
			//	Link
			LocalizedStringBean localizedTitle = getDocumentManager().newDocumentManager(iwma).openFormLazy(data.getFormId()).getFormTitle();
			Link linkToSavedForm = new Link(localizedTitle.getString(locale));
			
			linkToSavedForm.setURL(linkToForm);
			bodyRow.createCell().add(linkToSavedForm);
			
			//	Date
			TableCell2 dataCell = bodyRow.createCell();
			String date = getSubmissionDate(data, locale);
			dataCell.add(new Text(StringUtil.isEmpty(date) ? CoreConstants.EMPTY : date));
			
			if (isShowAll()) {
				//	Author
				TableCell2 authorCell = bodyRow.createCell();
				authorCell.add(new Text(data.getFormAuthor() == null ? CoreConstants.EMPTY : data.getFormAuthor().getName()));
			}
			
			index++;
		}
		
		boolean addPager = submissionsData.size() > 10;
		Layer pagerContainer = null;
		if (addPager) {
			PresentationUtil.addJavaScriptSourceLineToHeader(iwc, web2.getBundleURIToJQueryPlugin(JQueryPlugin.TABLE_SORTER_PAGER));
			
			//	Pager
			pagerContainer = new Layer();
			container.getChildren().add(pagerContainer);
			pagerContainer.setStyleClass("savedFormsViewerPager");
			
			Image first = bundle.getImage("images/pager_first.png");
			first.setStyleClass("first");
			pagerContainer.add(first);
			
			Image previous = bundle.getImage("images/pager_prev.png");
			previous.setStyleClass("prev");
			pagerContainer.add(previous);
			
			GenericInput pageDisplay = new TextInput(); 
			pageDisplay.setStyleClass("pagedisplay");
			pagerContainer.add(pageDisplay);
			
			Image next = bundle.getImage("images/pager_next.png");
			next.setStyleClass("next");
			pagerContainer.add(next);
			
			Image last = bundle.getImage("images/pager_last.png");
			last.setStyleClass("last");
			pagerContainer.add(last);
			
			DropdownMenu pageSizeSelector = new DropdownMenu();
			pageSizeSelector.setStyleClass("pagesize");
			pageSizeSelector.add(new SelectOption(String.valueOf(10), String.valueOf(10)));
			pageSizeSelector.add(new SelectOption(String.valueOf(20), String.valueOf(20)));
			pageSizeSelector.add(new SelectOption(String.valueOf(30), String.valueOf(30)));
			pageSizeSelector.add(new SelectOption(String.valueOf(40), String.valueOf(40)));
			pageSizeSelector.add(new SelectOption(String.valueOf(50), String.valueOf(50)));
			pagerContainer.add(pageSizeSelector);
		}
		
		String initAction = new StringBuilder("jQuery('#").append(table.getId()).append("').tablesorter({")
			.append(addPager ? "widthFixed: true, " : CoreConstants.EMPTY).append("headers: { 0: {sorter: false}}})").toString();
		if (addPager) {
			initAction = new StringBuilder(initAction).append(".tablesorterPager({container: jQuery('#").append(pagerContainer.getId()).append("')})").toString();
		}
		if (!CoreUtil.isSingleComponentRenderingProcess(iwc)) {
			initAction = new StringBuilder("jQuery(window).load(function() {").append(initAction).append("});").toString();
		}
		PresentationUtil.addJavaScriptActionToBody(iwc, initAction);
	}
	
	private Link getLinkToSendEmail(IWContext iwc, SubmissionDataBean data, IWBundle bundle, IWResourceBundle iwrb, String linkToForm) {
		Link sendEmail = new Link(bundle.getImage("images/email.png", iwrb.getLocalizedString("saved_forms.send_email", "Send e-mail")));
		
		sendEmail.setForceToReplaceAfterEncoding(true);
		if (iwc.isWindows()) {
			sendEmail.setCharEncoding("ISO-8859-1");	//	TODO
		}
		
		sendEmail.setURL(getEmailAddressMailtoFormattedWithSubject(iwc, data.getFormAuthor(),
				iwrb.getLocalizedString("saved_forms.link_to_a_saved_form", "Link to a saved form"), linkToForm));
		
		return sendEmail;
	}
	
	private String getEmailAddressMailtoFormattedWithSubject(IWApplicationContext iwac, User formAuthor, String subject, String body) {
		String emailAddress = null;
		
		if (formAuthor != null) {
			Email email = null;
			try {
				email = getUserBusiness(iwac).getUserMail(formAuthor);
			} catch (RemoteException e) {}
			if (email != null) {
				emailAddress = email.getEmailAddress();
			}
		}
		if (StringUtil.isEmpty(emailAddress)) {
			emailAddress = iwac.getApplicationSettings().getProperty(CoreConstants.PROP_SYSTEM_ACCOUNT);
		}
		
		if (StringUtil.isEmpty(emailAddress)) {
			return body;
		}
		
		return new StringBuilder("mailto:").append(emailAddress).append("?subject=").append(subject).append("&body=").append(body).toString();
	}
	
	private String getSubmissionDate(SubmissionDataBean data, Locale locale) {
		if (data.getSubmittedDate() == null) {
			return null;
		}
		
		IWTimestamp date = new IWTimestamp(data.getSubmittedDate());
		return date.getLocaleDateAndTime(locale, IWTimestamp.SHORT, IWTimestamp.SHORT);
	}

	private User getUser(IWApplicationContext iwac, Integer id) {
		if (id == null) {
			return null;
		}
		
		UserBusiness userBusiness = getUserBusiness(iwac);
		if (userBusiness == null) {
			return null;
		}

		try {
			return userBusiness.getUser(id);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private UserBusiness getUserBusiness(IWApplicationContext iwac) {
		try {
			return (UserBusiness) IBOLookup.getServiceInstance(iwac, UserBusiness.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}
		return null;
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
		this.newestOnTop = (values[2] instanceof Boolean) ? (Boolean) values[2] : Boolean.TRUE;
		this.showTableHeader = (values[3] instanceof Boolean) ? (Boolean) values[3] : Boolean.TRUE;
		this.userId = (values[4] instanceof Integer) ? (Integer) values[4] : null;
	}

	@Override
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[5];
		values[0] = super.saveState(ctx);
		values[1] = isShowAll();
		values[2] = isNewestOnTop();
		values[3] = isShowTableHeader();
		values[4] = getUserId();
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

	public boolean isNewestOnTop() {
		return newestOnTop;
	}

	public void setNewestOnTop(boolean newestOnTop) {
		this.newestOnTop = newestOnTop;
	}

	public boolean isShowTableHeader() {
		return showTableHeader;
	}

	public void setShowTableHeader(boolean showTableHeader) {
		this.showTableHeader = showTableHeader;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
}
