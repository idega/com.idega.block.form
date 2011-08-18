package com.idega.block.form.action;

import org.chiba.xml.xforms.core.Model;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Element;

import com.idega.block.form.save.SaveFormAndGeneratePdfAction;
import com.idega.chiba.web.xml.xforms.functions.IdegaExtensionFunctions;
import com.idega.presentation.IWContext;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.StringUtil;

public class ExecuteAction extends SaveFormAndGeneratePdfAction {

	public static final String XFORM_SUBMISSION_UUID_PARAM = "xform_submission_id";
	
	private String expression;
	
	public ExecuteAction(Element element, Model model) {
		super(element, model);
	}

	@Override
	public void perform() throws XFormsException {
		super.saveForm();
		String submissionUUId = getSubmissionUUID();
		if (!StringUtil.isEmpty(submissionUUId)) {
			IWContext iwc = CoreUtil.getIWContext();
			iwc.getSession().setAttribute(XFORM_SUBMISSION_UUID_PARAM, submissionUUId);
		}
		
		if (StringUtil.isEmpty(expression))
			return;
		
		String beanExpEnd = "',";
		String beanExp = expression.substring(0, expression.indexOf(beanExpEnd) + (beanExpEnd.length() - 1));
		beanExp = beanExp.replaceAll("'", CoreConstants.EMPTY);
		
		String params = null;
		int separator = expression.indexOf(beanExpEnd);
		if (separator != -1) {
			params = expression.substring(separator + beanExpEnd.length());
		}
		
		IdegaExtensionFunctions.getValueFromExpression(null, getInstance(), beanExp, params);
	}

	@Override
	public void init() throws XFormsException {
		super.init();
		
		expression = getXFormsAttribute("expression");
	}
	
}