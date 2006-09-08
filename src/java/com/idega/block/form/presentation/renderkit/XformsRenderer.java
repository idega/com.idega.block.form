/*
 * $Id: XformsRenderer.java,v 1.1 2006/09/08 14:04:13 gediminas Exp $
 * Created on Sep 6, 2006
 *
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.form.presentation.renderkit;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.render.Renderer;
import com.idega.block.form.presentation.FormViewer;

public class XformsRenderer extends Renderer {

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		FormViewer form = (FormViewer) component;
		
		// TODO: move stuff from FormViewer
		
		super.encodeEnd(context, component);
	}
	
}
