package com.idega.block.form.xml.xforms.connector.context.sandbox;

import java.util.Date;

/**
 * 
 * @author <a href="mailto:civilis@idega.com">Vytautas ‰ivilis</a>
 * @version 1.0
 * 
 */
public class KeyContextResolverTestBean {

	public String getSomeValue() {
		
		return new Date().toString();
	}
}