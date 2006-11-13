package com.idega.block.form.business;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.chiba.xml.xforms.connector.AbstractConnector;
import org.chiba.xml.xforms.connector.SubmissionHandler;
import org.chiba.xml.xforms.core.Submission;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Node;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.WebdavExtendedResource;

/**
 * The file submission driver serializes and submits instance data to a file.
 * <p/>
 * When using the <code>put</code> submission method, the driver only supports
 * the replace mode <code>none</code>. It simply serializes the instance data to
 * the file denoted by the connector URI. When this file exists, it will be
 * overwritten silently, otherwise it will be created.
 * <p/>
 * 
 * @author Gediminas Paulauskas
 * @version $Id: WebdavSubmissionHandler.java,v 1.1 2006/11/13 22:33:04 gediminas Exp $
 */
public class WebdavSubmissionHandler extends AbstractConnector implements SubmissionHandler {
    
	private static final Logger LOGGER = Logger.getLogger(WebdavSubmissionHandler.class.getName());

    /**
     * Serializes and submits the specified instance data over the
     * <code>webdav</code> protocol.
     *
     * @param submission the submission issuing the request.
     * @param instance the instance data to be serialized and submitted.
     * @return <code>null</code>.
     * @throws XFormsException if any error occurred during submission.
     */
    public Map submit(Submission submission, Node instance) throws XFormsException {
        if (submission.getMethod().equalsIgnoreCase("put")) {
            if (!submission.getReplace().equals("none")) {
                throw new XFormsException("submission mode '" + submission.getReplace() + "' not supported");
            }

            try {
                // create uri
                URI uri = new URI(getURI());

                String path = uri.getPath();
                
                LOGGER.info("Trying to save instance to webdav path: " + path);
                
        		IWUserContext iwuc = IWContext.getInstance();
                IWSlideSession session = getIWSlideSession(iwuc);
                
                WebdavExtendedResource resource = session.getWebdavResource(path);
				
				if(resource.exists())
					resource.setProperties();
				
				InputStream is = null;
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				
				try {
					serialize(submission, instance, out);
					is = new ByteArrayInputStream(out.toByteArray());
					resource.putMethod(is);
				}
				catch (Exception e) {
					LOGGER.log(Level.WARNING, "Error while saving instance to webdav", e);
				}
				finally {
					if (is != null) {
						try {
							is.close();
						}
						catch (Exception e) {
						}
					}
					out.close();
				}

            }
            catch (Exception e) {
                throw new XFormsException(e);
            }

            return new HashMap();
        }

        throw new XFormsException("submission method '" + submission.getMethod() + "' not supported");
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

// end of class
