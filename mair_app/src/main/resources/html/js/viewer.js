

function initEditor(content) {
  var editor =  new toastui.Editor({
    el: document.querySelector('#content'),
    viewer: true,
    previewStyle: 'vertical',
    height: (document.documentElement.clientHeight-15) + "px",
    usageStatistics: false,
    initialValue: content
  });
}


$(document).ready(function () {

})

$(window).resize(function() {
  $("#content").css("height", (window.innerHeight-20)+"px")
});




