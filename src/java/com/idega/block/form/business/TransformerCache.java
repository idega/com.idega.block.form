package com.idega.block.form.business;

import javax.faces.context.FacesContext;
import org.chiba.xml.xslt.impl.CachingTransformerService;
import org.chiba.xml.xslt.impl.ResourceResolver;
import com.idega.idegaweb.IWMainApplication;
import com.idega.repository.data.Instantiator;
import com.idega.repository.data.Singleton;
import com.idega.repository.data.SingletonRepository;

/**
 * An extension of <code>CachingTransformerService</code> which makes it a Singleton
 * 
 * @author gediminas
 */
public class TransformerCache extends CachingTransformerService implements Singleton {

	private static Instantiator instantiator = new Instantiator() {

		public Object getInstance(Object parameter) {
			IWMainApplication iwma = null;
			if (parameter instanceof FacesContext) {
				iwma = IWMainApplication.getIWMainApplication((FacesContext) parameter);
			}
			else {
				iwma = (IWMainApplication) parameter;
			}
			ResourceResolver resolver = new BundleResourceResolver(iwma);
			return new CachingTransformerService(resolver);
		}
	};

	public static TransformerCache getInstance(IWMainApplication iwma) {
		return (TransformerCache) SingletonRepository.getRepository().getInstance(TransformerCache.class, instantiator,
				iwma);
	}

	public static TransformerCache getInstance(FacesContext context) {
		return (TransformerCache) SingletonRepository.getRepository().getInstance(TransformerCache.class, instantiator,
				context);
	}
}
