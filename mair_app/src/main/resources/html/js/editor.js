var editor;

function getContent()
{
	return editor.getMarkdown();
}


function setContent(data)
{
	editor.reset()
	editor.setMarkdown(data);
	return true;
}



$(function() {
	$("#privet").text("Привет")
	editor = init();
});


function init()
{
	return  new tui.Editor({
		el: document.querySelector('#content'),
		initialEditType: 'wysiwyg',
		viewer: true,
		previewStyle: 'vertical',
		height: "600px",
		usageStatistics: false,
		initialValue: ""
	});

}




