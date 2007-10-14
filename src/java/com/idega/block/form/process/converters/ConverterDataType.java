package com.idega.block.form.process.converters;


/**
 * 
 *  Last modified: $Date: 2007/10/14 10:51:07 $ by $Author: civilis $
 * 
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
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