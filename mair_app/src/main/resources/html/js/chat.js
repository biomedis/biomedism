var converter;
$(document).ready(function () {
  $(window).resize(function() {
    $("#content").css("height", (window.innerHeight-20)+"px")
  });

  $("#content").on("click","i.editBtn", function (e) {
    e.stopPropagation()
    e.preventDefault()
    javaConnector.editMsg(parseInt($(this).data("msg_id")));
    //updateMessage("111111111 ", $(this).data("msg_id"))
  })

  $("#content").on("click","i.deleteBtn", function (e) {
    e.stopPropagation()
    e.preventDefault()
    javaConnector.deleteMsg(parseInt($(this).data("msg_id")));
    //removeMessage($(this).data("msg_id"))
  });

 javaConnector.loadInitMessages();

})

function addMessageOutcome(htmlMsg, msgId){
  $("#content")
  .append(fillTemplate("msg_item_tpl_outcome", {htmlMsg:htmlMsg, msgId:msgId}))
 }

function addMessageIncoming(htmlMsg, msgId){
  $("#content")
  .append(fillTemplate("msg_item_tpl_incoming", {htmlMsg:htmlMsg, msgId:msgId}))
}

function editMessage(msgId, htmlMsg ){
  var msgContainer= $("#content .msg_content[data-msg_id='"+msgId+"']");
  if(msgContainer) {
    msgContainer.empty().html(htmlMsg);
    return true;
  }else return false;
}

function removeMessage(msgId){
  var msgContainer= $("#content .message[data-msg_id='"+msgId+"']");
  if(msgContainer) {
    msgContainer.remove();
    return true;
  }else return false;
}


/**
 * Использует mustache. Переменные щаблона обрамляются {{ переменная }}
 * @param templateID
 * @param templateVars объект типа {var:val,..., } в качестве значения может быть функция
 * @returns {void | string | *}
 */
function fillTemplate(templateID = "", templateVars = {}) {
  let template = $('#' + templateID).text();
  return Mustache.render(template, templateVars);
}


/**
 * Использует mustache. Переменные щаблона обрамляются {{ переменная }}
 * @param template строка шаблона
 * @param templateVars объект типа {var:val,..., } в качестве значения может быть функция
 * @returns {void | string | *}
 */
function fillTemplateString(template = "", templateVars = {}) {
  return Mustache.render(template, templateVars);
}





