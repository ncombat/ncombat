// javascript for basic tasks

var expDays = 30;
var exp = new Date();
exp.setTime(exp.getTime() + (expDays*24*60*60*1000));
var ShowCount = 0;
var SwapColour;
var who = null;
var since = null;

function init() {
   // probably check for js disable here too
   who = GetCookie('VisitorName');
   if (who == null) {
        set();
    }
    var since =    GetCookie ('WWhenH', 0, exp);
    if (since == null) {
        since = new Date().getTime();
    }
   
}
function set() {
    VisitorName = prompt("Who are you?");
    SetCookie ('VisitorName', VisitorName, exp);
    SetCookie ('WWHCount', 0, exp);
    SetCookie ('WWhenH', 0, exp);
}
function getCookieVal (offset) {
    var endstr = document.cookie.indexOf (";", offset);
    if (endstr == -1)
    endstr = document.cookie.length;
    return unescape(document.cookie.substring(offset, endstr));
}
function GetCookie (name) {
    var arg = name + "=";
    var alen = arg.length;
    var clen = document.cookie.length;
    var i = 0;
    while (i < clen) {
    var j = i + alen;
    if (document.cookie.substring(i, j) == arg)
    return getCookieVal (j);
    i = document.cookie.indexOf(" ", i) + 1;
    if (i == 0) break;
}
return null;
}
function SetCookie (name, value) {
var argv = SetCookie.arguments;
var argc = SetCookie.arguments.length;
var expires = (argc > 2) ? argv[2] : null;
var path = (argc > 3) ? argv[3] : null;
var domain = (argc > 4) ? argv[4] : null;
var secure = (argc > 5) ? argv[5] : false;
document.cookie = name + "=" + escape (value) +
((expires == null) ? "" : ("; expires=" + expires.toGMTString())) +
((path == null) ? "" : ("; path=" + path)) +
((domain == null) ? "" : ("; domain=" + domain)) +
((secure == true) ? "; secure" : "");
}
function DeleteCookie (name) {
var exp = new Date();
exp.setTime (exp.getTime() - 1);
var cval = GetCookie (name);
document.cookie = name + "=" + cval + "; expires=" + exp.toGMTString();
}

function RemoveAllCookies() {
    DeleteCookie ('VisitorName');
    DeleteCookie ('WWHCount');
    DeleteCookie ('WWhenH');   
    var NumToDoItems = GetCookie('PT_NumToDoList');
    var i;
    var ToDoItem;
    if (NumToDoItems == null) {
        NumToDoItems = 0;
    }
    ShowCount = 0; SwapColour = 0;
    for (i=1; i <= NumToDoItems; i++) {
        DeleteCookie('PT_ToDoItem'+i);
    }
    alert("all cookies deleted - tasks and identity");
    window.location = window.location;   
}


function diceRoll(numDice, numSides){

    var result=0;
    
    if ((numDice!=0) && (numSides!=0)) {
        for (var x = 1; x <= numDice; x++)
       {
          result = result + Math.round(numSides*Math.random());
       }
     }
        
    return result;
    
}


function replaceText(el, text) {
  if (el != null) {
    clearText(el);
    var newNode = document.createTextNode(text);
    el.appendChild(newNode);
  }
}

function clearText(el) {
  if (el != null) {
    if (el.childNodes) {
      for (var i = 0; i < el.childNodes.length; i++) {
        var childNode = el.childNodes[i];
        el.removeChild(childNode);
      }
    }
  }
}

function getText(el) {
  var text = "";
  if (el != null) {
    if (el.childNodes) {
      for (var i = 0; i < el.childNodes.length; i++) {
        var childNode = el.childNodes[i];
        if (childNode.nodeValue != null) {
          text = text + childNode.nodeValue;
        }
      }
    }
  }
  return text;
}