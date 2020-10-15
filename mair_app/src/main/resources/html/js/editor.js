var editor;

function getContent() {
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




