package com.idega.block.form.presentation;

import java.rmi.RemoteException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.w3c.dom.Node;
import org.w3c.tidy.Attribute;

import com.idega.block.email.presentation.EmailSender;
import com.idega.block.form.IWBundleStarter;
import com.idega.block.form.bean.SubmissionDataBean;
import com.idega.block.form.business.FormAssetsResolver;
import com.idega.block.form.business.SubmissionDataComparator;
import com.idega.block.form.data.XForm;
import com.idega.block.form.data.XFormSubmission;
import com.idega.block.form.data.dao.XFormsDAO;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.JQueryPlugin;
import com.idega.block.web2.business.Web2Business;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.builder.business.BuilderLogic;
import com.idega.builder.business.BuilderLogicWrapper;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.content.business.ContentConstants;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.builder.data.ICPage;
import com.idega.core.contact.data.Email;
import com.idega.core.messaging.MessagingSettings;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.CSSSpacer;
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
import com.idega.presentation.text.Heading3;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.GenericInput;
import com.idega.presentation.ui.IWDatePicker;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SelectDropdown;
import com.idega.presentation.ui.SelectOption;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.presentation.ui.handlers.IWDatePickerHandler;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.ArrayUtil;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.IWTimestamp;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringHandler;
import com.idega.util.StringUtil;
import com.idega.util.URIUtil;
import com.idega.util.datastructures.map.MapUtil;
import com.idega.util.expression.ELUtil;
import com.idega.xformsmanager.business.Document;
import com.idega.xformsmanager.business.DocumentManager;
import com.idega.xformsmanager.business.DocumentManagerFactory;
import com.idega.xformsmanager.component.beans.LocalizedStringBean;

public class SavedForms extends IWBaseComponent {

	public static final String PERSONAL_ID_VARIABLE = SubmissionDataBean.VARIABLE_OWNER_PERSONAL_ID;

	@Autowired
	private XFormsDAO xformsDAO;

	@Autowired
	private DocumentManagerFactory documentManager;

	@Autowired
	private JQuery jQuery;

	@Autowired
	private BuilderLogicWrapper builderLogicWrapper;

	@Autowired
	private Web2Business web2;

	private boolean	showAll = Boolean.TRUE,
					newestOnTop = Boolean.TRUE,
					showTableHeader = Boolean.TRUE,
					showLatestForms = Boolean.FALSE,
					showOnlyCurrentUsersForms = Boolean.FALSE,
					showOnlyAvailableByAccessRights = Boolean.FALSE,
					showOnlySubscribed = Boolean.FALSE,
					showFormTypeFilter = Boolean.FALSE,
					showDateFilter = Boolean.FALSE;

	private Integer userId;
	private ICPage responsePage;

	private String allowedTypes, variablesWithValues, processDefinitionNames = null, dateRange;

	public final String formTypeParameter = "formtype";

	public boolean isShowOnlySubscribed() {
		return showOnlySubscribed;
	}

	public void setShowOnlySubscribed(boolean showOnlySubscribed) {
		this.showOnlySubscribed = showOnlySubscribed;
	}

	public boolean isShowOnlyAvailableByAccessRights() {
		return showOnlyAvailableByAccessRights;
	}

	public void setShowOnlyAvailableByAccessRights(boolean showOnlyAvailableByAccessRights) {
		this.showOnlyAvailableByAccessRights = showOnlyAvailableByAccessRights;
	}


	public boolean isShowDateFilter() {
		return showDateFilter;
	}

	public void setShowDateFilter(boolean showDateFilter) {
		this.showDateFilter = showDateFilter;
	}

	private List<String> procDefNames = null;

	public List<String> getProcDefNames() {
		if (procDefNames != null) {
			return procDefNames;
		}

		procDefNames = new ArrayList<String>();
		List<String> allProcDefNames = null;

		if (!StringUtil.isEmpty(getProcessDefinitionNames())) {
			allProcDefNames = Arrays.asList(getProcessDefinitionNames().split(CoreConstants.COMMA));
		}

		if (!isShowOnlyAvailableByAccessRights()) {
			procDefNames = allProcDefNames == null ? procDefNames : new ArrayList<String>(allProcDefNames);
			return procDefNames;
		}

		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null || !iwc.isLoggedOn()) {
			getLogger().info("Not logged in: proc. def. names: " + procDefNames);
			return procDefNames;
		}

		Map<String, FormAssetsResolver> resolvers = WebApplicationContextUtils.getRequiredWebApplicationContext(iwc.getServletContext()).getBeansOfType(FormAssetsResolver.class);
		if (MapUtil.isEmpty(resolvers)) {
			getLogger().info("No resolvers: proc. def. names: " + procDefNames);
			return procDefNames;
		}

		User user = iwc.getCurrentUser();
		for (FormAssetsResolver resolver: resolvers.values()) {
			List<String> processes = resolver.getNamesOfAvailableProcesses(user, allProcDefNames);
			if (!ListUtil.isEmpty(processes)) {
				procDefNames.addAll(processes);
			}
		}

		return procDefNames;
	}

	/**
	 *
	 * <p>Property for filtering saved forms by process definition</p>
	 * @return process definitions names, separated by comma;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	public String getProcessDefinitionNames() {
		return processDefinitionNames;
	}
	public void setProcessDefinitionNames(String processDefinitionNames) {
		this.processDefinitionNames = processDefinitionNames;
	}

	/**
	 * @return the variablesWithValues
	 */
	public String getVariablesWithValues() {
		return variablesWithValues;
	}

	/**
	 * @param variablesWithValues the variablesWithValues to set
	 */
	public void setVariablesWithValues(String variablesWithValues) {
		this.variablesWithValues = variablesWithValues;
	}

	/**
	 *
	 * <p>Splits {@link String} of BPM variables to {@link Collection}
	 * of {@link String}, where each {@link String} has single variable. </p>
	 * @param variablesWithValues to split, not <code>null</code>.
	 * @return split {@link Collection} of separate variables or
	 * {@link Collections#emptyList()} on failure.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	protected Collection<String> getSplittedVariablesWithValues(String variablesWithValues) {
		Collection<String> splittedVariablesWithValues = new ArrayList<String>();

		if (StringUtil.isEmpty(variablesWithValues)) {
			return splittedVariablesWithValues;
		}

		String[] tmpArray = variablesWithValues.split(CoreConstants.COMMA);
		splittedVariablesWithValues.addAll(Arrays.asList(tmpArray));
		return splittedVariablesWithValues;
	}

	/**
	 *
	 * @param data to be shown or not, not <code>null</code>.
	 * @return <code>true</code> if data matches search criteria,
	 * <code>false</code> otherwise.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public boolean doMatchCriteria(SubmissionDataBean data) {
		if (data == null) {
			return Boolean.FALSE;
		}

		return data.containsAll(getSplittedVariablesWithValues(getVariablesWithValues()));
	}

	protected String getValueFromVariables(String variableName) {
		if (StringUtil.isEmpty(getVariablesWithValues())) {
			return null;
		}

		if (!getVariablesWithValues().contains(variableName)) {
			return null;
		}

		int index = getVariablesWithValues().indexOf(variableName);
		if (index < 0 || index > getVariablesWithValues().length()) {
			return null;
		}

		String substring = getVariablesWithValues().substring(index);
		if (StringUtil.isEmpty(substring)) {
			return null;
		}

		if (substring.contains(CoreConstants.COMMA)) {
			index = substring.indexOf(CoreConstants.COMMA);
			if (index < 0 || index > substring.length()) {
				return null;
			}

			substring = substring.substring(0, index);
			if (StringUtil.isEmpty(substring)) {
				return null;
			}
		}

		String[] variable = substring.split(CoreConstants.EQ);
		if (variable.length != 2) {
			return null;
		}

		return variable[1];
	}

	public List<XFormSubmission> getFilteredOutForms(IWContext iwc, List<XFormSubmission> submissions) {
		if (ListUtil.isEmpty(submissions) || !isShowOnlySubscribed() || iwc.isSuperAdmin()) {
			return submissions;
		}

		Map<?, ?> beans = null;
		try {
			beans = WebApplicationContextUtils.getWebApplicationContext(iwc.getServletContext()).getBeansOfType(FormAssetsResolver.class);
		} catch (Exception e) {}
		if (MapUtil.isEmpty(beans)) {
			return submissions;
		}

		List<String> procDefNames = getProcDefNames();
		for (Object bean: beans.values()) {
			if (bean instanceof FormAssetsResolver) {
				List<XFormSubmission> filteredOut = ((FormAssetsResolver) bean).getFilteredOutForms(iwc, submissions, procDefNames);
				if (filteredOut != null) {
					return filteredOut;
				}
			}
		}

		return submissions;
	}

	private IWDatePicker getDateRange(IWContext iwc, String name, Date from, Date to) {
		IWDatePicker datePicker = new IWDatePicker(name);
		datePicker.setVersion(IWDatePicker.VERSION_1_8_17);

		if (from != null)
			datePicker.setDate(from);
		if (to != null)
			datePicker.setDateTo(to);
		datePicker.setDateRange(true);
		datePicker.setUseCurrentDateIfNotSet(false);

		return datePicker;
	}

	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);

		IWContext iwc = IWContext.getIWContext(context);
		IWBundle bundle = iwc.getIWMainApplication().getBundle(IWBundleStarter.BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);

		PresentationUtil.addStyleSheetToHeader(iwc, bundle.getVirtualPathWithFileNameString("style/formsEntries.css"));
		IWBundle processBundle = iwc.getIWMainApplication().getBundle(com.idega.block.process.IWBundleStarter.IW_BUNDLE_IDENTIFIER);
		PresentationUtil.addStyleSheetToHeader(iwc, processBundle.getVirtualPathWithFileNameString("style/process.css"));

		ELUtil.getInstance().autowire(this);

		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, jQuery.getBundleURIToJQueryLib());
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, jQuery.getBundleURIToJQueryPlugin(JQueryPlugin.TABLE_SORTER));
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, web2.getBundleURIsToFancyBoxScriptFiles());
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, bundle.getVirtualPathWithFileNameString("javascript/SavedFormsHelper.js"));
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, iwc.getIWMainApplication().getBundle(ContentConstants.IW_BUNDLE_IDENTIFIER).getVirtualPathWithFileNameString("javascript/FileUploadHelper.js"));

		PresentationUtil.addStyleSheetToHeader(iwc, web2.getBundleURIToFancyBoxStyleFile());

		Form form = new Form();
		add(form);

		Layer container = new Layer();
		container.setStyleClass("savedFormsViewer");
		String uuid = BuilderLogic.getInstance().getInstanceId(this);
		if (!StringUtil.isEmpty(uuid))
			form.setId("id_".concat(uuid));
		
		if (showDateFilter) {
			form.add(container);
		}

		if (!iwc.isLoggedOn()) {
			form.add(new Heading3(iwrb.getLocalizedString("please_login_to_see_forms", "Please login to see saved forms")));
			return;
		}

		//	Dates from and to
		boolean userSavedForms = this instanceof UserSavedForms;
		String dateRangeParameter = "dateRange";
		Date from = null, to = null;
		if (iwc.isParameterSet(dateRangeParameter)) {
			String dateRangeValue = iwc.getParameter(dateRangeParameter);
			String[] dates = dateRangeValue.split(CoreConstants.MINUS);
			if (!ArrayUtil.isEmpty(dates) && dates.length == 2) {
				Locale locale = iwc.getCurrentLocale();
				java.util.Date tmp = IWDatePickerHandler.getParsedDate(dates[0].trim(), locale);
				if (tmp != null) {
					IWTimestamp iwFrom = new IWTimestamp(tmp);
					iwFrom.setHour(0);
					iwFrom.setMinute(0);
					iwFrom.setSecond(0);
					iwFrom.setMilliSecond(0);
					from = iwFrom.getDate();
				}
				tmp = IWDatePickerHandler.getParsedDate(dates[1].trim(), locale);
				if (tmp != null) {
					IWTimestamp iwTo = new IWTimestamp(tmp);
					iwTo.setHour(23);
					iwTo.setMinute(59);
					iwTo.setSecond(59);
					iwTo.setMilliSecond(999);
					to = iwTo.getDate();
				}
			}
		}
		if (from == null) {
			IWTimestamp now = IWTimestamp.RightNow();
			if (userSavedForms) {
				now.setYear(now.getYear() - 5);
			}
			now.setDay(1);
			now.setHour(0);
			now.setMinute(0);
			now.setSecond(0);
			now.setMilliSecond(0);
			from = now.getDate();
		}
		if (to == null) {
			IWTimestamp now = IWTimestamp.RightNow();
			now.setMonth(now.getMonth() + 1);
			now.setDay(1);
			now.setDay(now.getDay() - 1);
			now.setHour(23);
			now.setMinute(59);
			now.setSecond(59);
			now.setMilliSecond(999);
			to = now.getDate();
		}

		IWDatePicker dateRange = getDateRange(iwc, dateRangeParameter, from, to);
		Layer element = new Layer(Layer.DIV);
		container.add(element);
		element.setStyleClass("formItem shortFormItem");
		Label label = null;
		label = new Label(iwrb.getLocalizedString("date_range", "Date range") + CoreConstants.COLON, dateRange);
		element.add(label);
		element.add(dateRange);

		boolean filterError = false;
		if (showFormTypeFilter) {
			List<String> processDefNames = null;
			if (getProcessDefinitionNames() != null) {
				processDefNames = Arrays.asList(getProcessDefinitionNames()
						.split(CoreConstants.COMMA));
			}

			SelectDropdown formTypeSelect = new SelectDropdown(formTypeParameter);
			formTypeSelect.setStyleClass("formTypeFilter");
			formTypeSelect.addOption(new SelectOption(iwrb.getLocalizedString("filter_forms_by_type", "Filter forms by type"), ""));
			if (getProcessDefinitionNames() != null) {
				List<String> formList = getXformsDAO().getDistinctXFormNamesByStorageIdentifierProperty(processDefNames);
				for (String formType : formList) {
					formTypeSelect.addOption(new SelectOption(formType, StringEscapeUtils.escapeHtml(formType)));
				}
			} else {
				List<String> formList = getXformsDAO().getDistinctXFormNames();
				for (String formType : formList) {
					formTypeSelect.addOption(new SelectOption(formType, StringEscapeUtils.escapeHtml(formType)));
				}
			}

			if (iwc.isParameterSet(formTypeParameter)) {
				String formTypeValue = StringEscapeUtils.unescapeHtml(iwc.getParameter(formTypeParameter));
				if (!StringUtil.isEmpty(formTypeValue)) {
					if (getProcessDefinitionNames() != null) {
						List<String> defFilter = getXformsDAO().getStorageIdentifierPropertyByNameAndSIP(formTypeValue, processDefNames);
						if (ListUtil.isEmpty(defFilter)){
							filterError = true;
							getLogger().warning("Couldn't find form types for provided type, this should not happen!");
						} else {
							setProcessDefinitionNames(StringUtils.join(defFilter, CoreConstants.COMMA));
						}
					} else {
						List<String> defFilter = getXformsDAO().getStorageIdentifierPropertyByNameAndSIP(formTypeValue, null);
						if (ListUtil.isEmpty(defFilter)){
							filterError = true;
							getLogger().warning("Couldn't find form types for provided type, this should not happen!");
						} else {
							setProcessDefinitionNames(StringUtils.join(defFilter, CoreConstants.COMMA));
						}
					}
				}
			}

			element.add(formTypeSelect);
		}

		Layer button = new Layer();
		button.setStyleClass("buttonLayer date-range-filter-button");
		SubmitButton show = new SubmitButton(iwrb.getLocalizedString("show", "Show"));
		show.setStyleClass("savedFormsFilterButton");
		button.add(show);
		container.add(button);
		container.add(new CSSSpacer());

		if (filterError){
			form.add(new Heading3(iwrb.getLocalizedString("no_forms_found", "There are no forms available")));
			return;
		}

		String rangeLimit = userSavedForms ? null : iwc.getIWMainApplication().getSettings().getProperty("forms.date_range_limit", String.valueOf(31));
		if (StringHandler.isNumeric(rangeLimit)) {
			if ((to.getTime() - from.getTime()) / 86400000 > Integer.valueOf(rangeLimit)) {
				form.add(new Heading3(iwrb.getLocalizedString("date_range_limit_is", "Date range limit is") + ": " + rangeLimit + " " + iwrb.getLocalizedString("days", "days")));
				return;
			}
		}

		Integer currentUserId = null;
		String ownerPersonalId = null;
		if (isShowOnlyCurrentUsersForms()) {
			currentUserId = Integer.valueOf(iwc.getCurrentUser().getId());
			getLogger().info("Showing forms only of current user");
		} else {
			ownerPersonalId = getValueFromVariables(PERSONAL_ID_VARIABLE);
			getLogger().info("Forms' owner personal ID: " + ownerPersonalId);
		}
		List<XFormSubmission> submissions = getSubmissions(context, ownerPersonalId, currentUserId, from, to);
		submissions = getFilteredOutForms(iwc, submissions);
		if (ListUtil.isEmpty(submissions)) {
			form.add(new Heading3(iwrb.getLocalizedString("no_forms_found", "There are no forms available")));
			return;
		}

		Locale locale = iwc.getCurrentLocale();

		Map<String, Boolean> addedSubmissions = new HashMap<String, Boolean>();
		List<SubmissionDataBean> submissionsData = new ArrayList<SubmissionDataBean>();
		for (XFormSubmission submission: submissions) {
			try {
				Long formId = submission.getXform().getFormId();
				String submissionUUID = submission.getSubmissionUUID();

				if (formId != null && !StringUtil.isEmpty(submissionUUID) && !addedSubmissions.containsKey(submissionUUID)) {
					User user = getUser(iwc, getUserId() == null ?
							isShowAll() ?
									submission.getFormSubmitter() :
									null :
							getUserId()
					);
					SubmissionDataBean data = new SubmissionDataBean(
							formId,
							submissionUUID,
							submission.getDateSubmitted(),
							user
					);

					if (!StringUtil.isEmpty(variablesWithValues)) {
						data.doLoadVariables(submission);
					}

					LocalizedStringBean localizedTitle = getLocalizedTitle(submission, locale);
					data.setLocalizedTitle(localizedTitle.getString(locale));

					String allowedTypes = getAllowedTypes();
					String englishLocalization = localizedTitle == null ? null : localizedTitle.getString(Locale.ENGLISH);
					if (allowedTypes != null && englishLocalization != null && allowedTypes.indexOf(englishLocalization) == -1) {
						getLogger().info("Not adding submission by UUID: " + submissionUUID + ". Allowed types: " + allowedTypes + ", English localization: " + englishLocalization);
						continue;
					}

					if (!doMatchCriteria(data)) {
						getLogger().info("Not adding submission by UUID: " + submissionUUID + ". Did not match criteria: " + data);
						continue;
					}

					submissionsData.add(data);
					addedSubmissions.put(submissionUUID, Boolean.TRUE);
				}
			} catch(Exception e) {
				getLogger().log(Level.WARNING, "Error getting submission by: " + submission.getSubmissionUUID(), e);
			}
		}

		if (ListUtil.isEmpty(submissionsData)) {
			return;
		}

		Collections.sort(submissionsData, new SubmissionDataComparator(isNewestOnTop()));

		BuilderService bs = null;
		try {
			bs = BuilderServiceFactory.getBuilderService(iwc);
		} catch (RemoteException e) {
			Logger.getLogger(SavedForms.class.getName()).log(Level.SEVERE, "Error getting " + BuilderService.class, e);
		}
		if (bs == null) {
			return;
		}
		String url = bs.getFullPageUrlByPageType(iwc, FormViewer.formviewerPageType, true);

		Table2 table = new Table2();
		form.add(table);
		table.setStyleClass("savedFormsViewerTable");
		TableHeaderRowGroup header = table.createHeaderRowGroup();
		header.setStyleClass("savedFormsViewerHeaderRow");
		if (!isShowTableHeader()) {
			header.setStyleAttribute("display", "none");
		}
		TableRow headerRow = header.createRow();
		headerRow.setStyleClass("header");

		if (isShowAll()) {
			TableHeaderCell checkboxCell = headerRow.createHeaderCell();
			checkboxCell.add(new Text(CoreConstants.EMPTY));
		}

		TableHeaderCell emailCell = headerRow.createHeaderCell();
		emailCell.add(new Text(CoreConstants.EMPTY));

		TableHeaderCell formCell = headerRow.createHeaderCell();
		formCell.add(new Text(iwrb.getLocalizedString("saved_forms.form", "Form")));
		String sortTitle = iwrb.getLocalizedString("saved_forms.click_to_sort", "Click cell to sort data");
		formCell.setTitle(sortTitle);
		formCell.setStyleClass("savedFormsViewerFormHeaderRow");

		TableHeaderCell savedAtCell = headerRow.createHeaderCell();
		savedAtCell.add(new Text(iwrb.getLocalizedString("saved_forms.saved_at", "Date")));
		savedAtCell.setTitle(sortTitle);
		savedAtCell.setStyleClass("savedFormsViewerSavedAtHeaderRow");

		if (isShowAll()) {
			TableHeaderCell authorCell = headerRow.createHeaderCell();
			authorCell.add(new Text(iwrb.getLocalizedString("saved_forms.submission_author", "Author")));
			authorCell.setTitle(sortTitle);
			authorCell.setStyleClass("savedFormsViewerAuthorHeaderRow");

			TableHeaderCell applicantEmailCell = headerRow.createHeaderCell();
			applicantEmailCell.add(new Text(iwrb.getLocalizedString("saved_forms.applicant_email", "E-mail")));
			applicantEmailCell.setTitle(sortTitle);
			applicantEmailCell.setStyleClass("savedFormsViewerApplicantEmailHeaderRow");
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

			Object[] mailData = getLinkToSendEmail(iwc, data, bundle, iwrb, linkToForm);

			if (isShowAll() ) {
				TableCell2 sendMessageCell = bodyRow.createCell();

				Object email = mailData[0];
				if (email == null) {
					sendMessageCell.add(new Text(CoreConstants.EMPTY));
				} else {
					CheckBox checkbox = new CheckBox("author_email", email.toString());
					checkbox.setMarkupAttribute("data-form-id", data.getFormId().toString());
					checkbox.setStyleClass("send-author-mail");
					sendMessageCell.add(checkbox);
				}
			}

			//	Email link
			bodyRow.createCell().add((UIComponent) mailData[1]);

			//	Link
			Link linkToSavedForm = new Link(data.getLocalizedTitle());
			if (getResponsePage() != null) {
				linkToSavedForm.setPage(getResponsePage());
				linkToSavedForm.addParameter(FormViewer.submissionIdParam, data.getSubmissionUUID());
			} else {
				linkToSavedForm.setURL(linkToForm);
			}
			bodyRow.createCell().add(linkToSavedForm);

			//	Date
			TableCell2 dataCell = bodyRow.createCell();
			String date = getSubmissionDate(data, locale);
			dataCell.add(new Text(StringUtil.isEmpty(date) ? CoreConstants.EMPTY : date));

			if (isShowAll()) {
				//	Author
				TableCell2 authorCell = bodyRow.createCell();
				authorCell.add(new Text(data.getFormAuthor() == null ? CoreConstants.EMPTY : data.getFormAuthor().getName()));

				//	E-mail
				TableCell2 applicantMailCell = bodyRow.createCell();
				UIComponent content = null;
				if (mailData[0] == null) {
					content = new Text(CoreConstants.EMPTY);
				} else {
					HtmlOutputLink sendMessage = (HtmlOutputLink) iwc.getApplication().createComponent(HtmlOutputLink.COMPONENT_TYPE);
					sendMessage.setValue("mailto:" + mailData[0].toString());
					sendMessage.getChildren().add(new Text( mailData[0].toString()));
					content = sendMessage;
				}
				applicantMailCell.add(content);
			}

			index++;
		}

		if (isShowAll()) {
			Layer buttons = new Layer();
			buttons.setStyleClass("buttonLayer");
			form.add(buttons);

			GenericButton sendMessage = new GenericButton(iwrb.getLocalizedString("send_message", "Send message"));
			sendMessage.setInputType("button");
			String fromEmail = iwc.getApplicationSettings().getProperty(MessagingSettings.PROP_MESSAGEBOX_FROM_ADDRESS, "no-reply@idega.com");
			sendMessage.setOnClick("SavedFormsHelper.doSendMails('" + from + "', '" +
					getBuilderLogicWrapper().getBuilderService(iwc).getUriToObject(EmailSender.class, Arrays.asList(
							new AdvancedProperty(EmailSender.FROM_PARAMETER, fromEmail),
							new AdvancedProperty(EmailSender.RECIPIENT_TO_PARAMETER, fromEmail),
							new AdvancedProperty(EmailSender.USE_RICH_TEXT_EDITOR, Boolean.TRUE.toString()),
							new AdvancedProperty(EmailSender.ALLOW_MULTIPLE_FILES, Boolean.TRUE.toString())
						)
					) +
			"', true, '" + form.getId() + "');");
			buttons.add(sendMessage);
		}

		boolean addPager = submissionsData.size() > 10;
		Layer pagerContainer = null;
		if (addPager) {
			PresentationUtil.addJavaScriptSourceLineToHeader(iwc, jQuery.getBundleURIToJQueryPlugin(JQueryPlugin.TABLE_SORTER_PAGER));

			//	Pager
			pagerContainer = new Layer();
			form.add(pagerContainer);
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

	/**
	 *
	 * <p>Checks for {@link Node} with {@link Attribute}
	 * "mapping=string_caseDescription", if none found, then
	 * searches for {@link com.idega.xformsmanager.business.Document#getFormTitle()}.</p>
	 * @param submission - where to search; not <code>null</code>;
	 * @param locale - {@link Locale} of text to be returned;
	 * @return Localized text or <code>null</code> on failure.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	protected LocalizedStringBean getLocalizedTitle(XFormSubmission submission, Locale locale) {
		if (submission == null) {
			return null;
		}

		if (locale == null) {
			locale = CoreUtil.getCurrentLocale();
		}

		String value = submission.getVariableValue("string_caseDescription");
		if (StringUtil.isEmpty(value)) {
			return getLocalizedTitle(submission.getXform());
		}

		LocalizedStringBean lsb = new LocalizedStringBean();
		lsb.setString(locale, value);
		return lsb;
	}

	/**
	 *
	 * @param xform which title to get, not <code>null</code>;
	 * @return {@link com.idega.xformsmanager.business.Document#getFormTitle()}
	 * or <code>null</code> on failure.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	protected LocalizedStringBean getLocalizedTitle(XForm xform) {
		if (xform == null) {
			return null;
		}

		DocumentManager documentmanagerLocal = getDocumentManager()
				.newDocumentManager(getIWMainApplication(getFacesContext()));
		if (documentmanagerLocal == null) {
			return null;
		}

		Document document = documentmanagerLocal.openFormLazy(xform.getFormId());
		if (document == null) {
			return null;
		}

		return document.getFormTitle();
	}

	private Object[] getLinkToSendEmail(IWContext iwc, SubmissionDataBean data, IWBundle bundle, IWResourceBundle iwrb, String linkToForm) {
		HtmlOutputLink sendEmailJSF = (HtmlOutputLink) iwc.getApplication().createComponent(HtmlOutputLink.COMPONENT_TYPE);
		AdvancedProperty email = getEmailAddressMailtoFormattedWithSubject(iwc, data.getFormAuthor(), iwrb.getLocalizedString("saved_forms.link_to_a_saved_form", "Link to a saved form"), linkToForm);
		sendEmailJSF.setValue(email.getValue());
		sendEmailJSF.getChildren().add(bundle.getImage("images/email.png"));

		Object[] results = new Object[] {email.getId(), sendEmailJSF};
		return results;

	}

	private AdvancedProperty getEmailAddressMailtoFormattedWithSubject(IWApplicationContext iwac, User formAuthor, String subject, String body) {
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
			return new AdvancedProperty(emailAddress, body);
		}

		return new AdvancedProperty(emailAddress, new StringBuilder("mailto:").append(emailAddress).append("?subject=").append(subject).append("&body=").append(body).toString());
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
			Logger.getLogger(SavedForms.class.getName()).log(Level.SEVERE, "Error getting user by id: " + id, e);
		}

		return null;
	}

	private UserBusiness getUserBusiness(IWApplicationContext iwac) {
		try {
			return IBOLookup.getServiceInstance(iwac, UserBusiness.class);
		} catch (IBOLookupException e) {
			Logger.getLogger(SavedForms.class.getName()).log(Level.SEVERE, "Error getting " + UserBusiness.class, e);
		}
		return null;
	}

	private List<XFormSubmission> getSubmissions(FacesContext context, String personalID, Integer userId, Date from, Date to) {
		if (userId != null) {
			return getXformsDAO().getSubmissions(userId, getProcDefNames(), from, to, isShowLatestForms());
		}

		if (StringUtil.isEmpty(personalID)) {
			return getSubmissions(context, from, to);
		}

		IWContext iwc = IWContext.getIWContext(context);
		if (!iwc.isLoggedOn()) {
			return null;
		}

		if (!isShowAll()) {
			return null;
		}

		if (this.showLatestForms) {
			return getXformsDAO().getAllLatestSubmissionsByUser(personalID, getProcDefNames(), from, to);
		}
		return getXformsDAO().getAllNotFinalSubmissionsByUser(personalID, getProcDefNames(), from, to);
	}

	private List<XFormSubmission> getSubmissions(FacesContext context, Date from, Date to) {
		Integer currentUserId = null;
		if (!isShowAll()) {
			IWContext iwc = IWContext.getIWContext(context);
			if (iwc.isLoggedOn()) {
				currentUserId = iwc.getCurrentUserId();
			}
		}

		if (this.showLatestForms) {
			return getXformsDAO().getAllLatestSubmissionsByUser(currentUserId, getProcDefNames(), from, to);
		}
		return getXformsDAO().getAllNotFinalSubmissionsByUser(currentUserId, getProcDefNames(), from, to);
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

	public boolean isShowLatestForms() {
		return showLatestForms;
	}

	public void setShowLatestForms(boolean showLatestForms) {
		this.showLatestForms = showLatestForms;
	}

	public ICPage getResponsePage() {
		return responsePage;
	}

	public void setResponsePage(ICPage responsePage) {
		this.responsePage = responsePage;
	}

	public String getAllowedTypes() {
		return allowedTypes;
	}

	public void setAllowedTypes(String allowedTypes) {
		this.allowedTypes = allowedTypes;
	}

	public boolean isShowOnlyCurrentUsersForms() {
		return showOnlyCurrentUsersForms;
	}

	public void setShowOnlyCurrentUsersForms(boolean showOnlyCurrentUsersForms) {
		this.showOnlyCurrentUsersForms = showOnlyCurrentUsersForms;
	}

	public String getDateRange() {
		return dateRange;
	}

	public void setDateRange(String dateRange) {
		this.dateRange = dateRange;
	}

	public boolean isShowFormTypeFilter() {
		return showFormTypeFilter;
	}

	public void setShowFormTypeFilter(boolean showFormTypeFilter) {
		this.showFormTypeFilter = showFormTypeFilter;
	}

	private BuilderLogicWrapper getBuilderLogicWrapper() {
		if (builderLogicWrapper == null) {
			ELUtil.getInstance().autowire(this);
		}
		return builderLogicWrapper;
	}

}