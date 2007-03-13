package com.idega.block.form.business;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Logger;

import org.chiba.xml.xforms.connector.AbstractConnector;
import org.chiba.xml.xforms.connector.SubmissionHandler;
import org.chiba.xml.xforms.core.Submission;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Node;

import com.idega.block.form.business.util.BlockFormUtil;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWMainApplication;

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
 * @version $Id: WebdavSubmissionHandler.java,v 1.12 2007/03/13 21:02:00 civilis Exp $
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
            if (!submission.getReplace().equals("none") && !submission.getReplace().equals("all")) {
                throw new XFormsException("submission mode '" + submission.getReplace() + "' not supported");
            }

            try {
                String form_id = BlockFormUtil.getFormIdFromSubmissionInstance(instance);
                
                if(form_id != null) {
                	ByteArrayOutputStream out = new ByteArrayOutputStream();
    				serialize(submission, instance, out);
    				InputStream is = new ByteArrayInputStream(out.toByteArray());
    				getFormsService().saveSubmittedData(form_id, is);
                } else {
                	LOGGER.severe("Form id not found in the submitted data instance");
                }
            }
            catch (Exception e) {
                throw new XFormsException(e);
            }

            return null;
        }

        throw new XFormsException("submission method '" + submission.getMethod() + "' not supported");
    }

	private FormsService getFormsService() {
		FormsService service = null;
		try {
			IWApplicationContext iwc = IWMainApplication.getDefaultIWApplicationContext();
			service = (FormsService) IBOLookup.getServiceInstance(iwc, FormsService.class);
		}
		catch (IBOLookupException e) {
			LOGGER.severe("Could not find FormsService");
		}
		return service;
	}
	
}

