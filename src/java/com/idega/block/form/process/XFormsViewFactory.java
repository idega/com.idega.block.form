package com.idega.block.form.process;

import com.idega.jbpm.def.View;
import com.idega.jbpm.def.ViewFactory;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2007/10/15 05:02:44 $ by $Author: civilis $
 */
public class XFormsViewFactory implements ViewFactory {

	public View createView() {
		return new XFormsView();
	}
}