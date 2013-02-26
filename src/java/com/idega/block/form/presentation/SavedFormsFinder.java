/**
 * @(#)SavedFormFinder.java    1.0.0 9:02:40 AM
 *
 * Idega Software hf. Source Code Licence Agreement x
 *
 * This agreement, made this 10th of February 2006 by and between 
 * Idega Software hf., a business formed and operating under laws 
 * of Iceland, having its principal place of business in Reykjavik, 
 * Iceland, hereinafter after referred to as "Manufacturer" and Agura 
 * IT hereinafter referred to as "Licensee".
 * 1.  License Grant: Upon completion of this agreement, the source 
 *     code that may be made available according to the documentation for 
 *     a particular software product (Software) from Manufacturer 
 *     (Source Code) shall be provided to Licensee, provided that 
 *     (1) funds have been received for payment of the License for Software and 
 *     (2) the appropriate License has been purchased as stated in the 
 *     documentation for Software. As used in this License Agreement, 
 *     Licensee shall also mean the individual using or installing 
 *     the source code together with any individual or entity, including 
 *     but not limited to your employer, on whose behalf you are acting 
 *     in using or installing the Source Code. By completing this agreement, 
 *     Licensee agrees to be bound by the terms and conditions of this Source 
 *     Code License Agreement. This Source Code License Agreement shall 
 *     be an extension of the Software License Agreement for the associated 
 *     product. No additional amendment or modification shall be made 
 *     to this Agreement except in writing signed by Licensee and 
 *     Manufacturer. This Agreement is effective indefinitely and once
 *     completed, cannot be terminated. Manufacturer hereby grants to 
 *     Licensee a non-transferable, worldwide license during the term of 
 *     this Agreement to use the Source Code for the associated product 
 *     purchased. In the event the Software License Agreement to the 
 *     associated product is terminated; (1) Licensee's rights to use 
 *     the Source Code are revoked and (2) Licensee shall destroy all 
 *     copies of the Source Code including any Source Code used in 
 *     Licensee's applications.
 * 2.  License Limitations
 *     2.1 Licensee may not resell, rent, lease or distribute the 
 *         Source Code alone, it shall only be distributed as a 
 *         compiled component of an application.
 *     2.2 Licensee shall protect and keep secure all Source Code 
 *         provided by this this Source Code License Agreement. 
 *         All Source Code provided by this Agreement that is used 
 *         with an application that is distributed or accessible outside
 *         Licensee's organization (including use from the Internet), 
 *         must be protected to the extent that it cannot be easily 
 *         extracted or decompiled.
 *     2.3 The Licensee shall not resell, rent, lease or distribute 
 *         the products created from the Source Code in any way that 
 *         would compete with Idega Software.
 *     2.4 Manufacturer's copyright notices may not be removed from 
 *         the Source Code.
 *     2.5 All modifications on the source code by Licencee must 
 *         be submitted to or provided to Manufacturer.
 * 3.  Copyright: Manufacturer's source code is copyrighted and contains 
 *     proprietary information. Licensee shall not distribute or 
 *     reveal the Source Code to anyone other than the software 
 *     developers of Licensee's organization. Licensee may be held 
 *     legally responsible for any infringement of intellectual property 
 *     rights that is caused or encouraged by Licensee's failure to abide 
 *     by the terms of this Agreement. Licensee may make copies of the 
 *     Source Code provided the copyright and trademark notices are 
 *     reproduced in their entirety on the copy. Manufacturer reserves 
 *     all rights not specifically granted to Licensee.
 *
 * 4.  Warranty & Risks: Although efforts have been made to assure that the 
 *     Source Code is correct, reliable, date compliant, and technically 
 *     accurate, the Source Code is licensed to Licensee as is and without 
 *     warranties as to performance of merchantability, fitness for a 
 *     particular purpose or use, or any other warranties whether 
 *     expressed or implied. Licensee's organization and all users 
 *     of the source code assume all risks when using it. The manufacturers, 
 *     distributors and resellers of the Source Code shall not be liable 
 *     for any consequential, incidental, punitive or special damages 
 *     arising out of the use of or inability to use the source code or 
 *     the provision of or failure to provide support services, even if we 
 *     have been advised of the possibility of such damages. In any case, 
 *     the entire liability under any provision of this agreement shall be 
 *     limited to the greater of the amount actually paid by Licensee for the 
 *     Software or 5.00 USD. No returns will be provided for the associated 
 *     License that was purchased to become eligible to receive the Source 
 *     Code after Licensee receives the source code. 
 */
package com.idega.block.form.presentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;

import com.idega.block.form.IWBundleStarter;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.TextInput;
import com.idega.user.business.UserBusiness;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;

/**
 * <p>Searcher for user saved and submitted forms.</p>
 * <p>You can report about problems to: 
 * <a href="mailto:martynas@idega.is">Martynas Stakė</a></p>
 *
 * @version 1.0.0 Jan 21, 2013
 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
 */
public class SavedFormsFinder extends Block {
	
	public static final String CLASS_USER_INPUT = "userInput";
	public static final String CLASS_PROJECT_NAME_INPUT = "projectInput";
	public static final String CLASS_VALUE = "value";
	public static final String CLASS_LABEL = "label";
	public static final String CLASS_SEARCH_BY_DROPDOWN = "searchByDropdown";
	public static final String CLASS_BPM_VARIABLE_INPUT = "bpm_input";
	public static final String CLASS_FORM_FINDER = "formsFinder";
	public static final String CLASS_INPUT_LAYER = "inputLayer";
	public static final String CLASS_PARAMETERS_LAYER = "parametersLayer";
	public static final String CLASS_BUTTONS_LAYER = "buttonsLayer";
	public static final String PARAMETER_BPM_IPNUT_VARIABLE = "prm_";
	public static final String PARAMETER_USER_IDENTIFIER = "prm_user_identifier";
	public static final String PARAMETER_PROJECT_NAME = "prm_project_name";
	public static final String PARAMETER_ACTION = "prm_action";
	public static final String ACTION_SEARCH = "submit";
	public static final String BPM_VARIABLE_LOCALIZATION_PREFIX = "bpm_variable.";
	
	private Collection<String> searchFields = new ArrayList<String>();
	
	/**
	 * @return the searchFields
	 */
	public Collection<String> getSearchFields() {
		return searchFields;
	}

	/**
	 * @param searchFields the searchFields to set
	 */
	public void setSearchFields(Collection<String> searchFields) {
		this.searchFields = searchFields;
	}

	private String searchFieldsString = null;
	
	/**
	 * @return the searchFieldsString
	 */
	public String getSearchFieldsString() {
		return searchFieldsString;
	}

	/**
	 * @param searchFieldsString the searchFieldsString to set
	 */
	public void setSearchFieldsString(String searchFieldsString) {
		this.searchFieldsString = searchFieldsString;
		
		if (!StringUtil.isEmpty(this.searchFieldsString)) {
			getSearchFields().addAll(Arrays.asList(
					this.searchFieldsString.split(CoreConstants.COMMA)
					));
		}
	}
	
	/* (non-Javadoc)
	 * @see com.idega.presentation.Block#getBundleIdentifier()
	 */
	@Override
	public String getBundleIdentifier() {
		return IWBundleStarter.BUNDLE_IDENTIFIER;
	}

	@Override
	public void main(IWContext iwc) throws Exception {
		PresentationUtil.addStyleSheetToHeader(
				iwc, 
				getBundle(iwc)
					.getVirtualPathWithFileNameString("style/formFinder.css"));
		
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, Arrays.asList(
				getBundle(iwc).getVirtualPathWithFileNameString("javascript/SavedFormsFilter.js")
		));
		
		PresentationUtil.addJavaScriptActionOnLoad(iwc, 
				"SavedFormsFilter.LOADING_MESSAGE = '" + 
						getBundle(iwc).getLocalizedString("loading", "Loading...") + 
						"';");
		
		Form searcherFrom = new Form();
		searcherFrom.add(getInputsLayer(iwc));
		
		Layer container = new Layer();
		container.setStyleClass(CLASS_FORM_FINDER);
		container.add(searcherFrom);
		
		add(container);
		
		super.main(iwc);
	}
	
	protected Layer getInputsLayer(IWContext iwc) {	
		Layer inputLayer = new Layer(Layer.DIV);
		inputLayer.setStyleClass(CLASS_INPUT_LAYER);
		
		inputLayer.add(getParametersLayer(iwc));
		inputLayer.add(getButtonsLayer(iwc));
		
		return inputLayer;
	}
	
	protected Layer getParametersLayer(IWContext iwc) {
		Layer parametersLayer = new Layer(Layer.DIV);
		parametersLayer.setStyleClass(CLASS_PARAMETERS_LAYER);
		for (String variableName: getSearchFields()) {
			parametersLayer.add(getBPMVariableInput(iwc, variableName));
		}
		
		return parametersLayer;
	}
	
	
	protected Layer getButtonsLayer(IWContext iwc) {
		Layer buttonsLayer = new Layer(Layer.DIV);
		buttonsLayer.setStyleClass(CLASS_BUTTONS_LAYER);
		buttonsLayer.add(getClearButton(iwc));
		buttonsLayer.add(getSearchButton(iwc));
		
		return buttonsLayer;
	}
	
	protected Layer getInputLayer(IWContext iwc, String name, String labelName, String styleClassName) {
		TextInput textInput = new TextInput(name);
		textInput.setStyleClass(CLASS_VALUE);
		textInput.setOnChange("SavedFormsFilter.updateVariable('" + 
				name.substring(PARAMETER_BPM_IPNUT_VARIABLE.length()) + "')");
		
		String parameterValue = iwc.getParameter(name);
		if (!StringUtil.isEmpty(parameterValue)) {
			textInput.setContent(parameterValue);
		}
		
		Label label = new Label(labelName, textInput);
		label.setStyleClass(CLASS_LABEL);
		
		Layer inputLayer = new Layer(Layer.DIV);
		inputLayer.setStyleClass(styleClassName);
		inputLayer.add(label);
		inputLayer.add(textInput);
		return inputLayer;
	}
	
	/**
	 * 
	 * <p>Creates {@link Layer} wiith {@link Label} and {@link TextInput}
	 * for chosen bpm variable.</p>
	 * @param iwc - execution context;
	 * @param bpmVariable - bpm variable name for example "string_someName";
	 * @return {@link Layer} with {@link TextInput} or <code>null</code> on failure.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 * @deprecated <p>Has a hack at 
	 * ...getBundle("is.idega.idegaweb.egov.bpm"); BPM module depends on this 
	 * module but not vice versa.</p>
	 */
	@Deprecated
	protected Layer getBPMVariableInput(IWContext iwc, String bpmVariable) {
		if (StringUtil.isEmpty(bpmVariable)) {
			return null;
		}
		
		IWMainApplication iwma = iwc.getIWMainApplication();
		if (iwma == null) {
			getLogger().log(Level.WARNING, "Unable to get: " + 
					IWMainApplication.class);
			return null;
		}
		
		IWBundle bundle = iwma.getBundle("is.idega.idegaweb.egov.bpm");
		if (bundle == null) {
			getLogger().log(Level.WARNING, "Unable to get: " + IWBundle.class);
			return null;
		}
		
		return getInputLayer(iwc, 
				PARAMETER_BPM_IPNUT_VARIABLE + bpmVariable, 
				bundle.getLocalizedString(
						BPM_VARIABLE_LOCALIZATION_PREFIX + bpmVariable, 
						bpmVariable), 
				CLASS_BPM_VARIABLE_INPUT);
	}
	
	protected Layer getUserNameInput(IWContext iwc) {
		return getInputLayer(
				iwc,
				PARAMETER_USER_IDENTIFIER,
				getResourceBundle(iwc).getLocalizedString("user_name", "User name:"), 
				CLASS_USER_INPUT);
	}
	
	protected Layer getProjectNameInput(IWContext iwc) {
		return getInputLayer(
				iwc,
				PARAMETER_PROJECT_NAME,
				getResourceBundle(iwc).getLocalizedString("project_name", "Project name:"), 
				CLASS_PROJECT_NAME_INPUT);
	}
	
	protected DropdownMenu getSearchChooser(IWContext iwc) {
		DropdownMenu searchBy = new DropdownMenu();
		searchBy.setStyleClass(CLASS_SEARCH_BY_DROPDOWN);
		searchBy.addMenuElement(-1, getResourceBundle(iwc)
				.getLocalizedString("search_by", "Search by:"));
		
		return searchBy;
	}
	
	protected GenericButton getClearButton(IWContext iwc) {
		GenericButton search = new GenericButton(
				getResourceBundle(iwc).getLocalizedString("clear", "Clear"));
		search.setStyleClass("button");
		
		search.setOnClick("SavedFormsFilter.clear();");
		return search;
	}
	
	protected GenericButton getSearchButton(IWContext iwc) {
		GenericButton search = new GenericButton(
				getResourceBundle(iwc).getLocalizedString("search", "Search"));
		search.setStyleClass("button");
		search.setOnClick("SavedFormsFilter.loadFilteredForms(SavedFormsFilter.variables);");
		
		return search;
	}
	
	/**
	 * 
	 * <p>Takes all values from inputs and etc., places them into 
	 * {@link Collection} of bpm variable name = bpm variable value,
	 * for example string_myName=Martynas.</p>
	 * @param iwc - execution context;
	 * @return	{@link Collection} of  variable = value on 
	 * {@link Collections#emptyList()} on failure.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	protected Collection<String> getSubmittedParameters(IWContext iwc) {
		Collection<String> submittedParameters = new ArrayList<String>();
		
		if (ListUtil.isEmpty(getSearchFields())) {
			return submittedParameters;
		}
		
		for (String searchField: getSearchFields()) {
			String parameterValue = iwc.getParameter(
					PARAMETER_BPM_IPNUT_VARIABLE + searchField);
			if (StringUtil.isEmpty(parameterValue)) {
				continue;
			}
			
			submittedParameters.add(searchField + CoreConstants.EQ + parameterValue);
		}
		
		return submittedParameters;
	}
	
	private UserBusiness userBusiness = null;
	
	protected UserBusiness getUserBusiness(IWApplicationContext iwac) {
		if (this.userBusiness == null) {
			try {
				this.userBusiness = (UserBusiness) IBOLookup.getServiceInstance(
						iwac, UserBusiness.class);
			} catch (IBOLookupException e) {
				getLogger().log(Level.SEVERE, "Error getting " + UserBusiness.class, e);
			}
		}
		
		
		return this.userBusiness;
	}
}
