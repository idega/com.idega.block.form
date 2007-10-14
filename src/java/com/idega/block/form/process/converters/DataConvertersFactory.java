package com.idega.block.form.process.converters;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2007/10/14 10:51:07 $ by $Author: civilis $
 */
public class DataConvertersFactory {
	
	private Map<ConverterDataType, DataConverter> converters = new HashMap<ConverterDataType, DataConverter>();

	public synchronized DataConverter createConverter(String dataType) {

		ConverterDataType cdt = ConverterDataType.valueOf(dataType.toUpperCase());
		
		if(converters.containsKey(cdt))
			return converters.get(cdt);
		
		DataConverter converter = cdt.getConverter();
		converters.put(cdt, converter);
		return converter;
	}
}