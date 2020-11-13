var converter;
$(document).ready(function () {
  $(window).resize(function() {
    $("#content").css("height", (window.innerHeight-20)+"px")
  });

  $("#content").on("click","a.editBtn", function (e) {
    e.stopPropagation()
    e.preventDefault()
    javaConnector.editMsg(parseInt($(this).data("msg_id")));
   // updateMessage("111111111 ", $(this).data("msg_id"))
  })

  $("#content").on("click","a.deleteBtn", function (e) {
    e.stopPropagation()
    e.preventDefault()
    javaConnector.deleteMsg(parseInt($(this).data("msg_id")));
    //removeMessage($(this).data("msg_id"))
  })
})



function addMessage(htmlMsg, msgId){
  $("#content")
  .append(fillTemplate("msg_item_tpl", {htmlMsg:htmlMsg, msgId:msgId}))
 }

function updateMessage(htmlMsg, msgId){
  var msgContainer= $("#content tr[id*='msg_id_"+msgId+"'] td.msg_content");
  if(msgContainer) {
    msgContainer.empty().html(htmlMsg);
    return true;
  }else return false;
}

function removeMessage(msgId){
  var msgContainer= $("#content tr[id*='msg_id_"+msgId+"']");
  var spaceContainer= $("#content tr[data-msg_id_*='"+msgId+"']");
  if(msgContainer) {
    msgContainer.remove();
    spaceContainer.remove();
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





