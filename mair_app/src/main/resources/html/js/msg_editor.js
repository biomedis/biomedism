var editor;

function getMessageFromEditor() {
  return editor.getMarkdown();
}

function setContent(data) {
  editor.reset()
  editor.setMarkdown(data);
  return true;
}

function initEditor(height) {
  return new tui.Editor({
    el: document.querySelector('#content'),
    initialEditType: 'wysiwyg',
    hideModeSwitch:true,
    viewer: true,
    previewStyle: 'vertical',
    height: height + "px",
    usageStatistics: false,
    initialValue: ""
  });
}


$(document).ready(function () {

 editor = initEditor(document.documentElement.clientHeight-15)



})

$(window).resize(function() {
  $("#content").css("height", (window.innerHeight-20)+"px")
});




