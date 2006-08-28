<?xml version="1.0"?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
        xmlns:h="http://java.sun.com/jsf/html"
        xmlns:jsf="http://java.sun.com/jsf/core"
        xmlns:ws="http://xmlns.idega.com/com.idega.workspace"
        xmlns:wf="http://xmlns.idega.com/com.idega.webface"
        xmlns:form="http://xmlns.idega.com/com.idega.block.form"
        xmlns:co="http://xmlns.idega.com/com.idega.content"
version="1.2">
<jsp:directive.page contentType="text/html" pageEncoding="UTF-8"/>
	<jsf:view>
		<ws:page>
			<h:form>
				<wf:wfblock id="form_block" title="XForms View">
					<form:formViewer uri="http://localhost:8080/chiba-web-2.0.0rc1/forms/controls.xhtml" />
				</wf:wfblock>
			</h:form>
		</ws:page>
	</jsf:view>
</jsp:root>