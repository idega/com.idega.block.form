if(FormsEntriesViewer == null) var FormsEntriesViewer = {};

FormsEntriesViewer.loadFormSubmissionView = function(submissionId) {

    var submissionInput = document.getElementById("submissionId");
    submissionInput.value = submissionId;
    submissionInput.form.submit();
}