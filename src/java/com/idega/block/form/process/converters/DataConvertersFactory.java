package com.idega.block.form.process.converters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import com.idega.jbpm.def.VariableDataType;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 *
 * Last modified: $Date: 2008/03/27 14:13:11 $ by $Author: civilis $
 */
@Scope("singleton")
@Repository
public class DataConvertersFactory {
	
//	private Map<ConverterDataType, DataConverter> converters = new HashMap<ConverterDataType, DataConverter>();
	final private Map<VariableDataType, DataConverter> dataConverters;
	
	public DataConvertersFactory() {
		dataConverters = new HashMap<VariableDataType, DataConverter>();
	}

	public synchronized DataConverter createConverter(VariableDataType dataType) {

//		ConverterDataType cdt = ConverterDataType.valueOf(dataType.toUpperCase());
//		
//		if(converters.containsKey(cdt))
//			return converters.get(cdt);
//		
//		DataConverter converter = cdt.getConverter();
//		converters.put(cdt, converter);
//		return converter;
		
		return getDataConverters().get(dataType);
	}

	public Map<VariableDataType, DataConverter> getDataConverters() {
		return dataConverters;
	}

	@Autowired
	public void setInjDataConverters(List<DataConverter> injDataConverters) {
		
		final Map<VariableDataType, DataConverter> dataConverters = getDataConverters();
		System.out.println("injecting data converters:"+injDataConverters);
		
		for (DataConverter dataConverter : injDataConverters) {
			
			dataConverters.put(dataConverter.getDataType(), dataConverter);
		}
	}
}