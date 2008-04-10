package com.idega.block.form.business;

import javax.faces.model.SelectItem;
import org.apache.slide.event.ContentEvent;
import com.idega.block.form.business.util.BlockFormUtil;
import com.idega.slide.business.IWContentEvent;
import com.idega.slide.business.IWSlideChangeListener;

/**
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version 1.0
 * 
 */
public class FormsSlideChangeListener implements IWSlideChangeListener {
	
	private FormsSlidePersistence forms_slide_persistence;

	public void onSlideChange(IWContentEvent contentEvent) {

		if(!contentEvent.getMethod().equals(ContentEvent.REMOVE))
			return;
		
		String uri = contentEvent.getContentEvent().getUri();
		if (uri.startsWith(FormsSlidePersistence.SUBMITTED_DATA_PATH) || !uri.startsWith(FormsSlidePersistence.FORMS_PATH))
			return;
		
		String remainder = uri.substring(FormsSlidePersistence.FORMS_PATH.length() + 1);
		int slashIndex = remainder.indexOf(BlockFormUtil.slash);
		if (slashIndex == -1) {
			return;
		}
		String formId = remainder.substring(0, slashIndex);
		if (!remainder.equals(
				new StringBuilder(formId)
				.append(BlockFormUtil.slash)
				.append(formId)
				.append(FormsSlidePersistence.FORMS_FILE_EXTENSION)
				.toString()
			))
			return;
		
//		TODO:
//		SelectItem form_name = getFormsSlidePersistence().findFormName(formId);
//		if(form_name != null)
//			getFormsSlidePersistence().getForms().remove(form_name);
	}
	
	private FormsSlidePersistence getFormsSlidePersistence() {
		
//		TODO: make this a spring managed bean and inject PersistenceManager
		
		if(forms_slide_persistence == null)
			forms_slide_persistence = new FormsSlidePersistence();

		return forms_slide_persistence;
	}
}