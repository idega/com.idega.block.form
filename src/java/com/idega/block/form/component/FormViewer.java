/*
 * $Id: FormViewer.java,v 1.1 2006/08/28 16:39:04 gediminas Exp $
 * Created on Aug 17, 2006
 *
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.form.component;

import java.io.IOException;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import com.idega.presentation.IWBaseComponent;


/**
 * 
 *  Last modified: $Date: 2006/08/28 16:39:04 $ by $Author: gediminas $
 * 
 * @author <a href="mailto:gediminas@idega.com">gediminas</a>
 * @version $Revision: 1.1 $
 */
public class FormViewer extends IWBaseComponent {

	public static final String FORM_BLOCK_ID = "form_block";
	
	public FormViewer() {
		super();
	}
	
	protected void initializeComponent(FacesContext context) {
		setId(FORM_BLOCK_ID);
		
		// init chiba
	}

	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		super.encodeBegin(context);
		ResponseWriter out = context.getResponseWriter();
		out.startElement("div", this);
		out.write("XForms document");
	}

	@Override
	public void encodeEnd(FacesContext context) throws IOException {
		ResponseWriter out = context.getResponseWriter();
		out.endElement("div");
		super.encodeEnd(context);
	}

	/*
	public FormBean getFormBean(){
		return (FormBean) WFUtil.getBeanInstance(FORM_BEAN_ID);
	}
	*/

}
