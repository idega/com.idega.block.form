package com.idega.block.form.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpException;
import org.apache.webdav.lib.WebdavResource;

import com.idega.block.form.presentation.FormViewer;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.SpringBeanLookup;
import com.idega.core.file.util.MimeTypeUtil;
import com.idega.graphics.generator.business.PDFGenerator;
import com.idega.idegaweb.IWMainApplication;
import com.idega.slide.business.IWSlideService;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.FileUtil;

/**
 * Downloads PDF for provided XForm
 * @author <a href="mailto:valdas@idega.com>Valdas Žemaitis</a>
 * Created: 2008.04.30
 * @version
 */
public class XFormDownloadServlet extends HttpServlet {

	private static final long serialVersionUID = 7189590074220492929L;

	private String pathInSlide = CoreConstants.CONTENT_PATH + "/xforms/pdf/";
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String formId = request.getParameter("XFormIdToDownload");
		//	TODO: path in slide: make available as parameter also

		byte[] formInPdfAndBytes = getRequestedForm(formId);
		
		if (formInPdfAndBytes == null) {
			//	TODO: add error message
		}
		else {
			response.setContentType(MimeTypeUtil.MIME_TYPE_PDF_1);
			response.setContentLength(formInPdfAndBytes.length);
			response.setHeader("Content-Disposition", "filename=XForm_"+formId+".pdf");
			
			ServletOutputStream stream = response.getOutputStream();
			stream.write(formInPdfAndBytes);
			stream.flush();
			stream.close();
		}
	}
	
	private byte[] getRequestedForm(String formId) {
		if (formId == null) {
			return null;
		}
		
		IWSlideService slide = null;
		try {
			slide = (IWSlideService) IBOLookup.getServiceInstance(IWMainApplication.getDefaultIWApplicationContext(), IWSlideService.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}
		if (slide == null) {
			return null;
		}
		
		String pdfName = formId + ".pdf";
		String pathToForm = pathInSlide + pdfName;
		WebdavResource formInPdf = null;
		try {
			formInPdf = slide.getWebdavResourceAuthenticatedAsRoot(CoreConstants.WEBDAV_SERVLET_URI + pathToForm);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		InputStream streamToPdf = null;
		if (formInPdf != null) {
			try {
				streamToPdf = formInPdf.getMethodData();
			} catch (HttpException e) {
			} catch (IOException e) {
			}
		}
		if (streamToPdf == null) {
			FormViewer viewer = new FormViewer();
			viewer.setFormId(formId);
	
			PDFGenerator generator = (PDFGenerator) SpringBeanLookup.getInstance().getSpringBean(IWMainApplication.getDefaultIWMainApplication().getServletContext(), CoreConstants.SPRING_BEAN_NAME_PDF_GENERATOR);
			if (generator.generatePDF(CoreUtil.getIWContext(), viewer, pdfName, pathInSlide, true)) {
				try {
					streamToPdf = slide.getInputStream(CoreConstants.WEBDAV_SERVLET_URI + pathToForm);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		if (streamToPdf == null) {
			return null;
		}
		
		ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
		stream(streamToPdf, streamOut);
		
		try {
			return streamOut == null ? null : streamOut.toByteArray();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			closeOutputStream(streamOut);
		}
		
		return null;
	}
	
	private void stream(InputStream streamIn, OutputStream streamOut) {
		try {
			FileUtil.streamToOutputStream(streamIn, streamOut);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			closeInputStream(streamIn);
		}
	}
	
	private void closeInputStream(InputStream stream) {
		if (stream == null) {
			return;
		}
		
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void closeOutputStream(OutputStream stream) {
		if (stream == null) {
			return;
		}
		
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.doPost(request, response);
	}
}
