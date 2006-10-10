/*
 * $Id: FormListViewer.java,v 1.1 2006/10/10 15:58:11 gediminas Exp $ Created on
 * 24.1.2005
 * 
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.form.presentation;

import com.idega.content.presentation.ContentItemListViewer;

/**
 * <p>
 * Displays a list of available XForms
 * </p>
 * 
 * Last modified: $Date: 2006/10/10 15:58:11 $ by $Author: gediminas $
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version $Revision: 1.1 $
 */
public class FormListViewer extends ContentItemListViewer {

	final static String FORM_LIST_BEAN = "formListBean";

	public FormListViewer() {
		super();
		setBeanIdentifier(FORM_LIST_BEAN);
		setBaseFolderPath("/files/xforms");
	}
	
	protected String[] getToolbarActions(){
		return new String[0];
	}

}
