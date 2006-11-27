package com.idega.block.form.business;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import org.chiba.xml.xforms.connector.AbstractConnector;
import org.chiba.xml.xforms.connector.URIResolver;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Document;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.WebdavExtendedResource;

/**
 * This class resolves <code>webdav</code> URIs. It treats the denoted
 * <code>webdav</code> resource as XML and returns the parsed response.
 * <p/>
 * If the specified URI contains a fragment part, the specified element is
 * looked up via the <code>getElementById</code>. Thus, the parsed response must
 * have an internal DTD subset specifiyng all ID attributes. Otherwise the
 * element would not be found.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: WebdavURIResolver.java,v 1.1 2006/11/27 14:32:22 gediminas Exp $
 */
public class WebdavURIResolver extends AbstractConnector implements URIResolver {

    /**
     * The logger.
     */
    private static Logger LOGGER = Logger.getLogger(WebdavURIResolver.class.getName());

    /**
     * Performs link traversal of the <code>webdav</code> URI and returns the
     * result as a DOM document.
     *
     * @return a DOM node parsed from the <code>webdav</code> URI.
     * @throws XFormsException if any error occurred during link traversal.
     */
    public Object resolve() throws XFormsException {
        try {
            // create uri
            URI uri = new URI(getURI());

            String path = uri.getPath();

    		IWUserContext iwuc = IWContext.getInstance();
            IWSlideSession session = getIWSlideSession(iwuc);
            
            WebdavExtendedResource resource = session.getWebdavResource(path);
            resource.setProperties();

			if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("loading webdav resource '" + path + "'");
            }

            // parse file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            Document document = factory.newDocumentBuilder().parse(resource.getMethodData());

            // check for fragment identifier
            if (uri.getFragment() != null) {
                return document.getElementById(uri.getFragment());
            }

            return document;
        }
        catch (Exception e) {
            throw new XFormsException(e);
        }
    }

	protected static IWSlideSession getIWSlideSession(IWUserContext iwuc) {
		IWSlideSession session = null;
		try {
			session = (IWSlideSession) IBOLookup.getSessionInstance(iwuc, IWSlideSession.class);
		}
		catch (IBOLookupException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return session;
	}
   
}
