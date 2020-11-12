var converter;
$(document).ready(function () {
  $(window).resize(function() {
    $("#content").css("height", (window.innerHeight-20)+"px")
  });

})



function addMessage(htmlMsg, msgId){
  $("#content").append("<div style='padding: 10px;' id='msg_id_"+msgId+"'>"+htmlMsg+"</div>")
 }

function updateMessage(htmlMsg, msgId){
  var msgContainer= $("#content div[id*='msg_id_'"+msgId+"]");
  if(msgContainer) {
    msgContainer.empty().html(htmlMsg);
    return true;
  }else return false;
}

function removeMessage(msgId){
  var msgContainer= $("#content div[id*='msg_id_'"+msgId+"]");
  if(msgContainer) {
    msgContainer.remove();
    return true;
  }else return false;
}






