package com.idega.block.form.business;

import java.util.Comparator;
import java.util.Date;

import com.idega.block.form.bean.SubmissionDataBean;

public class SubmissionDataComparator implements Comparator<SubmissionDataBean> {

	private boolean newestOnTop;
	
	public SubmissionDataComparator(boolean newestOnTop) {
		this.newestOnTop = newestOnTop;
	}
	
	public int compare(SubmissionDataBean bean1, SubmissionDataBean bean2) {
		Date time1 = bean1.getSubmittedDate();
		Date time2 = bean2.getSubmittedDate();
		
		if (time1 == null || time2 == null) {
			return 0;
		}

		return newestOnTop ? -time1.compareTo(time2) : time1.compareTo(time2);
	}

}
