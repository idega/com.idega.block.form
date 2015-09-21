if (SavedFormsHelper == null) var SavedFormsHelper = {};

SavedFormsHelper.doSendMails = function(from, uri, checkCheckboxes, formId) {
	var linkClass = "send-mail-message-to-forms-authors";
	if (jQuery('a.' + linkClass).length == 0) {
		var link = '<a style="display: none;" href="#" class="' + linkClass + '"><span>Send it</span></a>';
		jQuery(document.body).append(link);
	}
	
	var mailto = '';
	if (checkCheckboxes) {
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
		
		mailto = '&recipientBcc=';
		for (var i = 0;  i < mails.length; i++) {
			mailto += mails[i] + ',';
		}
		
		mailto = mailto.substring(0, mailto.length - 1);
	}
	
	jQuery('a.' + linkClass).attr('href', uri + mailto);
	
	var fancyboxInitialized = 'fancy-box-initialized';
	if (jQuery('a.' + fancyboxInitialized).length == 0) {
		jQuery('a.' + linkClass).fancybox({
			type: 'ajax',
			autoScale: false,
			autoDimensions: false,
			minWidth:	950,
			minHeight:	650,
			hideOnContentClick: false,
			afterShow: function() {
				jQuery('input.email-form-input').each(function() {
					jQuery(this).attr('style', 'width: 78.7% !important;');
				});
				if (EmailSenderHelper.settings == null) {
					EmailSenderHelper.settings = {};
				}
				EmailSenderHelper.settings.validateBCCMail = false;
			},
			beforeClose: function() {
				tinymce.get('emailSenderMessage').destroy();
				tinymce.EditorManager.execCommand('mceRemoveEditor', true, 'emailSenderMessage');
			}
		});
		jQuery('a.' + linkClass).addClass(fancyboxInitialized);
	}
	
	jQuery('span', jQuery('a.' + linkClass)).click();
}