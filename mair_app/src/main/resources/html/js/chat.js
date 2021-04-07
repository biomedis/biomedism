var converter;
var out_msg_tpl="";
var in_msg_tpl="";
var lastId=-1;
var hasNotMessages=false;
var inLoadingProcess=false;

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

var jwindow = $(window);
  $('body').bind('mousewheel', function(e){
    if(e.originalEvent.wheelDelta /120 > 0) {

      if(jwindow.scrollTop() == 0 && !hasNotMessages) {
        $("#content").prepend("<p class='loaded_msg' style='text-align: center;margin:10px;'>Загрузка...</p>")
        inLoadingProcess=true;
        javaConnector.loadMessages(lastId);

      }
    }

  });

})


function addMessages(messages){
  for (var m of messages) {
    if (m.out) addMessageOutcome(m.msg, m.msgId, false);
    else addMessageIncoming(m.msg, m.msgId, false);
  }
  if(messages.length!=0) {

    lastId= messages[0].msgId;
  }else hasNotMessages=true;

  scrollBottom();
}

function addPrevMessages(messages){
  if(messages.length!=0){
    for (var i=messages.length-1;i>=0;i--) {
      if (messages[i].out) addMessageOutcomePre(messages[i].msg, messages[i].msgId);
      else addMessageIncomingPre(messages[i].msg, messages[i].msgId);
    }
    lastId= messages[0].msgId;
  }else hasNotMessages=true;

  inLoadingProcess=false;
  $("#content .loaded_msg").remove();
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

function addMessageOutcomePre(htmlMsg, msgId){
  $("#content")
  .prepend(fillTemplateString(out_msg_tpl, {htmlMsg:htmlMsg, msgId:msgId}));
}

function addMessageIncomingPre(htmlMsg, msgId){
  $("#content")
  .prepend(fillTemplateString(in_msg_tpl, {htmlMsg:htmlMsg, msgId:msgId}));

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





