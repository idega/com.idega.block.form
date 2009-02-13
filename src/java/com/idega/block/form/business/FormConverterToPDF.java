package com.idega.block.form.business;

public interface FormConverterToPDF {

	public static final String STRING_BEAN_IDENTIFIER = "processTaskInstanceConverterToPDF";
	
	public String getGeneratedPDFFromXForm(String taskInstanceId, String formId, String formSubmissionId, String uploadPath, String pdfName, boolean checkExistence);
	
	public String getHashValueForGeneratedPDFFromXForm(String taskInstanceId, boolean checkExistence);
	
	public String getHashValueForGeneratedPDFFromXForm(String taskInstanceId, boolean checkExistence, String pdfName);
	
}
