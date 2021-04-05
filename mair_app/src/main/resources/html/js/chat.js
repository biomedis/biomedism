var converter;
var out_msg_tpl="";
var in_msg_tpl="";

$(document).ready(function () {
  out_msg_tpl = $('#msg_item_tpl_outcome').text();
  in_msg_tpl = $('#msg_item_tpl_incoming').text();

  $(window).resize(function() {
    $("#content").css("height", (window.innerHeight-20)+"px")
  });

  $("#content").on("click","i.editBtn", function (e) {
    e.stopPropagation();
    e.preventDefault();
    javaConnector.editMsg(parseInt($(this).data("msg_id")));
  })

  $("#content").on("click","i.deleteBtn", function (e) {
    e.stopPropagation();
    e.preventDefault();
    javaConnector.deleteMsg(parseInt($(this).data("msg_id")));

  });

  $("body").on("click","a", function (e){
    e.stopPropagation();
    e.preventDefault();
    javaConnector.onLinkClick($(this).attr("href"));
  });

 javaConnector.loadInitMessages();

})


function addMessages(messages){
  for (var m of messages) {
    if (m.out) addMessageOutcome(m.msg, m.msgId, false);
    else addMessageIncoming(m.msg, m.msgId, false);
  }

  scrollBottom();
}

function addMessageOutcome(htmlMsg, msgId, scroll){
  $("#content")
  .append(fillTemplateString(out_msg_tpl, {htmlMsg:htmlMsg, msgId:msgId}));
  if(scroll)scrollBottom();
 }

function addMessageIncoming(htmlMsg, msgId, scroll){
  $("#content")
  .append(fillTemplateString(in_msg_tpl, {htmlMsg:htmlMsg, msgId:msgId}));
  if(scroll)scrollBottom();
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




function scrollBottom(){
  var curPos=$(document).scrollTop();
  var height=$("body").height();
  var scrollTime=(height-curPos)/1.73;
  $("body,html").animate({"scrollTop":height},scrollTime);
}





