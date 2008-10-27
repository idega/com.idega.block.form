if(FormsEntriesViewer == null) var FormsEntriesViewer = {};

FormsEntriesViewer.loadFormSubmissionView = function(submissionId) {

    changeWindowLocationHref('submissionId='+submissionId);	
}