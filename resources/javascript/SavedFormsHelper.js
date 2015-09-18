if (SavedFormsHelper == null) var SavedFormsHelper = {};

SavedFormsHelper.doSendMails = function(from) {
	var mails = [];
	jQuery('input.send-author-mail').each(function() {
		var input = jQuery(this);
		if (input.attr('checked') == 'checked') {
			var email = input.val();
			if (!existsElementInArray(mails, email)) {
				mails.push(email);
			}
		}
	});
	if (mails.length == 0) {
		return;
	}
	
	var mailto = 'mailto:?to=' + from + '&bcc=';
	for (var i = 0;  i < mails.length; i++) {
		mailto += mails[i] + ',';
	}
	var id = "id_" + new Date().getTime();
	mailto = '<a style="display: none;" id="' + id + '" href="' + mailto + '"><span>Send it</span></a>';
	jQuery(document.body).append(mailto);
	jQuery('span', jQuery('#' + id)).click();
}