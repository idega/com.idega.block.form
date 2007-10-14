package com.idega.block.form.process.converters;

import org.w3c.dom.Element;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2007/10/14 10:51:07 $ by $Author: civilis $
 */
public interface DataConverter {

	public Object convert(Element o);
	public Element revert(Object o, Element e);
}