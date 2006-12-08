package com.idega.block.form.business;


import javax.ejb.CreateException;
import com.idega.business.IBOHomeImpl;

public class FormsServiceHomeImpl extends IBOHomeImpl implements FormsServiceHome {

	public Class getBeanInterfaceClass() {
		return FormsService.class;
	}

	public FormsService create() throws CreateException {
		return (FormsService) super.createIBO();
	}
}