package com.idega.block.form.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Node;

import com.idega.block.form.data.XFormSubmission;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.datastructures.map.MapUtil;
import com.idega.util.xml.XmlUtil;

public class SubmissionDataBean {
	
	public static final String ATTRIBUTE_MAPPING = "mapping";
	
	public static final String VARIABLE_CASE_DESCRIPTION = "string_caseDescription";
	public static final String VARIABLE_OWNER_PERSONAL_ID = "string_ownerKennitala";
	
	public SubmissionDataBean() {}
	
	public SubmissionDataBean(Long formId, String submissionUUID, Date submittedDate, User formAuthor) {
		this.formId = formId;
		this.submissionUUID = submissionUUID;
		this.submittedDate = submittedDate;
		this.formAuthor = formAuthor;
	}
		
	private Long formId;
	
	private String submissionUUID;
	private String localizedTitle;
	
	private User formAuthor;
	
	private Date submittedDate;
	
	private Map<String, String> variables = new TreeMap<String, String>();

	public Long getFormId() {
		return formId;
	}

	public void setFormId(Long formId) {
		this.formId = formId;
	}

	public String getSubmissionUUID() {
		return submissionUUID;
	}

	public void setSubmissionUUID(String submissionUUID) {
		this.submissionUUID = submissionUUID;
	}

	public Date getSubmittedDate() {
		return submittedDate;
	}

	public void setSubmittedDate(Date submittedDate) {
		this.submittedDate = submittedDate;
	}

	public User getFormAuthor() {
		return formAuthor;
	}

	public void setFormAuthor(User formAuthor) {
		this.formAuthor = formAuthor;
	}

	public String getLocalizedTitle() {
		return localizedTitle;
	}

	public void setLocalizedTitle(String localizedTitle) {
		this.localizedTitle = localizedTitle;
	}

	/**
	 * @return the variables
	 */
	public Map<String, String> getVariables() {
		return variables;
	}
	
	/**
	 * 
	 * <p>Appends bpm variable name and value.</p>
	 * @param variableName - BPM variable name to append;
	 * @param variableValue - variable value to append;
	 * @return <code>true</code> if successfully appended, false otherwise; 
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public boolean addVariable(String variableName, String variableValue) {
		if (StringUtil.isEmpty(variableName)) {
			return Boolean.FALSE;
		}
		
		this.variables.put(variableName, variableValue);
		return Boolean.TRUE;
	}
		
	/**
	 * 
	 * <p>Appends data from XForms submission document.</p>
	 * @param submission document to append;
	 * @return <code>true</code> if successfully appended, <code>false</code>
	 * otherwise.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public boolean addVariables(XFormSubmission submission) {
		if (submission == null) {
			return Boolean.FALSE;
		}
		
		org.w3c.dom.Document submissionDocument = submission.getSubmissionDocument();
		if (submissionDocument == null) {
			return Boolean.FALSE;
		}	
		
		List<Node> nodes = XmlUtil.getChildNodes(
				submissionDocument.getDocumentElement(), 
				null, null, ATTRIBUTE_MAPPING, null);
		
		if (ListUtil.isEmpty(nodes)) {
			return Boolean.FALSE;
		}
		
		for (Iterator<Node> i = nodes.iterator(); i.hasNext();) {
			Node node = i.next();

			org.w3c.dom.Element element = null;
			if (node instanceof org.w3c.dom.Element) {
				element = (org.w3c.dom.Element) node;
			} else {
				continue;
			}
			
			addVariable(
					element.getAttribute(ATTRIBUTE_MAPPING), 
					node.getTextContent());
		}
	
		return Boolean.TRUE;
	}
	
	/**
	 * 
	 * @return Contained names of BPM variables.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public Collection<String> getVariablesNames() {
		return new ArrayList<String>(this.variables.keySet());
	}
	
	/**
	 * 
	 * @param variableName - name of BPM variable, for example string_someValue;
	 * @return value of BPM variable of <code>null</code> on failure.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public String getVariableValue(String variableName) {
		if (StringUtil.isEmpty(variableName)) {
			return null;
		}
		
		return this.variables.get(variableName);
	}
	
	/**
	 * 
	 * <p>Checks if this submission data contains such variable value
	 * by given variable name.</p>
	 * @param bpmVariableName to search by, not <code>null</code>;
	 * @param bpmVariableValue to search for, not <code>null</code>;
	 * @return <code>true</code> if such variable and it's value found, 
	 * <code>false</code> otherwise.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public boolean contains(String bpmVariableName, String bpmVariableValue) {
		if (StringUtil.isEmpty(bpmVariableValue) || StringUtil.isEmpty(bpmVariableName)) {
			return Boolean.TRUE;
		}
		
		String lowerCaseVariableValue = bpmVariableValue.toLowerCase();
		String lowerCaseComparable = getVariableValue(bpmVariableName).toLowerCase();
		
		if (lowerCaseComparable.contains(lowerCaseVariableValue) 
				&& !StringUtil.isEmpty(lowerCaseVariableValue)) {
			return Boolean.TRUE;
		}
		
		String lowerCaseTitle = getLocalizedTitle().toLowerCase();
		if (bpmVariableName.equals(VARIABLE_CASE_DESCRIPTION) && 
				lowerCaseTitle.contains(lowerCaseVariableValue) && 
				!StringUtil.isEmpty(lowerCaseVariableValue)) {
			return Boolean.TRUE;
		}
		
		String submitterPersonalId = getFormAuthor().getPersonalID().toLowerCase();
		if (bpmVariableName.equals(VARIABLE_OWNER_PERSONAL_ID) && 
				submitterPersonalId.contains(lowerCaseVariableValue) && 
				!StringUtil.isEmpty(lowerCaseVariableValue)) {
			return Boolean.TRUE;
		}
		
		return Boolean.FALSE;
	}
	
	/**
	 * 
	 * <p>Same as {@link SubmissionDataBean#contains(String, String)}, 
	 * but here argument should be passed as: "string_someName=someValue" or
	 * "long_someLong=someLongValue" or etc.</p>
	 * @param variableNameAndValue in form of "string_someName=someValue" or
	 * "long_someLong=someLongValue" or etc., not <code>null</code>.
	 * @return <code>true</code> if this object contains value, false otherwise. 
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public boolean contains(String variableNameAndValue) {
		if (StringUtil.isEmpty(variableNameAndValue)) {
			return Boolean.TRUE;
		}
		
		String[] variable = variableNameAndValue.split(CoreConstants.EQ);
		if (variable.length != 2 ) {
			return Boolean.TRUE;
		}
		
		return contains(variable[0], variable[1]);
	}
	
	/**
	 * 
	 * <p>Same as {@link SubmissionDataBean#contains(String), but 
	 * multiple variables can be passed.</p>
	 * @param namesOfVariablesAndValues to be processed;
	 * @return <code>true</code> if all variables are contained, 
	 * <code>false</code> otherwise.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public boolean containsAll(Collection<String> namesOfVariablesAndValues) {
		if (ListUtil.isEmpty(namesOfVariablesAndValues)) {
			return Boolean.TRUE;
		}
		
		for (String variableNameAndValue: namesOfVariablesAndValues) {
			if (!contains(variableNameAndValue)) {
				return Boolean.FALSE;
			}
		}
		
		return Boolean.TRUE;
	}
	
	/**
	 * 
	 * <p>Checks if current form has all of given variables with given values.</p>
	 * @param variables is {@link Map} of (BPM variable name, BPM variable value),
	 * not <code>null</code>.
	 * @return <code>true</code> if all properties found in this 
	 * {@link SubmissionDataBean}, <code>false</code> otherwise.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public boolean containsAll(Map<String, String> variables) {
		if (MapUtil.isEmpty(variables)) {
			return Boolean.TRUE;
		}
		
		for (String variableName: variables.keySet()) {
			if (!contains(variableName, variables.get(variableName))) {
				return Boolean.FALSE;
			}
		}
		
		return Boolean.TRUE;
	}
}
