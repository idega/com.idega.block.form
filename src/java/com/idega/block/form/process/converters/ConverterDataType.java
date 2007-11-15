package com.idega.block.form.process.converters;


/**
 *  TODO: use data types from VariableDataType 
 *  Last modified: $Date: 2007/11/15 09:23:02 $ by $Author: civilis $
 * 
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 */
public enum ConverterDataType {
	
	DATE {
		public DataConverter getConverter() { 
			return new DateConverter();
		}
	},	
	STRING {
		public DataConverter getConverter() { 
			return new StringConverter();
		}
	},	
	LIST {
		public DataConverter getConverter() { 
			return new CollectionConverter();
		}
	};
	
	public abstract DataConverter getConverter();
}