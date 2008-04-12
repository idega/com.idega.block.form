package com.idega.block.form.process;

/**
 * Just kinda hack for spring proxies (can't cast directly to implementation)
 * 
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/04/12 21:58:59 $ by $Author: civilis $
 */
public interface IXFormViewFactory {

	public abstract XFormsView getXFormsView();
}