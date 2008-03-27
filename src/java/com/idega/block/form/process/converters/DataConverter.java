package com.idega.block.form.process.converters;

import org.w3c.dom.Element;

import com.idega.jbpm.def.VariableDataType;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 *
 * Last modified: $Date: 2008/03/27 14:13:11 $ by $Author: civilis $
 */
public interface DataConverter {

	public abstract Object convert(Element o);
	public abstract Element revert(Object o, Element e);
	public abstract VariableDataType getDataType();
}