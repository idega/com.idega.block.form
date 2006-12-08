package com.idega.block.form.business;


import javax.ejb.CreateException;
import com.idega.business.IBOHome;
import java.rmi.RemoteException;

public interface FormsServiceHome extends IBOHome {

	public FormsService create() throws CreateException, RemoteException;
}