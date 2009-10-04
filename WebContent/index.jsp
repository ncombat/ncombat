<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>N * C * O * M * B * A * T Home Page</title>
	<!-- Javascript library -->
	<script src="html/lib/jquery-1.3.2.js" content="text/javascript" ></script>
	<script src="html/lib/ncombat_base.js" content="text/javascript" ></script>
    <!-- Framework CSS -->
    <!--<link rel="stylesheet" href="html/styles/blueprint/screen.css" type="text/css" media="screen, projection"> -->
    <!--<link rel="stylesheet" href="html/styles/blueprint/print.css" type="text/css" media="print"> -->
    <!--[if lt IE 8]><link rel="stylesheet" href="./styles/blueprint/ie.css" type="text/css" media="screen, projection"><![endif]-->
    <link rel="stylesheet" href="html/styles/ncombat_screen.css" type="text/css" media="screen">
    <!-- setup methods -->
    <script>
	var someText = "Four score and seven years ago our fathers brought forth on this continent, a new nation, "
					+ "conceived in liberty, and dedicated to the proposition that all men are created equal. "
					+ "\n"
					+ "Now we are engaged in a great civil war, testing whether that nation, or any nation so "
					+ "conceived and so dedicated, can long endure. We are met on a great battlefield of that "
					+ "war. We have come to dedicate a portion of that field, as a final resting place for those "
					+ "who here gave their lives that that nation might live. It is altogether fitting and proper "
					+ "that we should do this."
					+ "\n"
					+ "But, in a larger sense, we can not dedicate -- we can not consecrate -- we can not hallow -- "
					+ "this ground. The brave men, living and dead, who struggled here, have consecrated it, far "
					+ "above our poor power to add or detract. The world will little note, nor long remember what "
					+ "we say here, but it can never forget what they did here. It is for us the living, rather, "
					+ "to be dedicated here to the unfinished work which they who fought here have thus far so "
					+ "nobly advanced. It is rather for us to be here dedicated to the great task remaining before "
					+ "us -- that from these honored dead we take increased devotion to that cause for which they "
					+ "gave the last full measure of devotion -- that we here highly resolve that these dead shall "
					+ "not have died in vain -- that this nation, under God, shall have a new birth of freedom -- "
					+ "and that government of the people, by the people, for the people, shall not perish from the "
					+ "earth.";

	var teletype = function() {
		var NEWLINE = '<br>&nbsp;<br>';
		var MAXSIZE = 3000;
		var CLIPSIZE = 2000;
		
		var text = someText;
		var textLen = someText.length;
		var nextChar = 0;
		var running = false;
		var ttElem;
		var blockNum = 1;
		var holdBuffer;

		var print = function(msg) {
			if (running) {
				var markup = ttElem.innerHTML + msg;
				if (markup.length > MAXSIZE) markup = clip(markup);
				ttElem.innerHTML = markup;
				ttElem.scrollTop = ttElem.scrollHeight;
			}
			else {
				holdBuffer += msg;
			}
		};

		var clip = function(markup) {
			var br = "<br>";
			while (markup.length > CLIPSIZE) {
				var i = markup.indexOf(br);
				if (i > -1) markup = markup.substring(i + br.length);
			}
			return markup;
		};

		var typeCharacter = function() {
			if (nextChar < textLen) {
				var newBit = text.substring(nextChar, nextChar+1).toUpperCase();
				if (newBit == '\n') newBit = NEWLINE;
				print(newBit);
				
				nextChar++;
			}
			else {
				var newBit = NEWLINE + "<b>Block #" + (++blockNum) 
								+ " begins here.</b>" + NEWLINE;
				print(newBit);
				nextChar = 0;
			} 
		};

		return {
			start: function(_ttElem) {
				ttElem = _ttElem;
				running = true;
				print("<b>Block #1 begins here.</b>" + NEWLINE);
				setInterval(typeCharacter, 33);
			},
	
			pause: function() {
				running = false;
				holdBuffer = "";
			},
	
			resume: function() {
				running = true;
				print(holdBuffer);
			}
		}
	}();
								
</script>
  <script type="text/javascript" src="text-utils.js"> </script>
  <script language="javascript" type="text/javascript">
   var request = null;
   function createRequest() {
     try {
       request = new XMLHttpRequest();
     } catch (trymicrosoft) {
       try {
         request = new ActiveXObject("Msxml2.XMLHTTP");
       } catch (othermicrosoft) {
         try {
           request = new ActiveXObject("Microsoft.XMLHTTP");
         } catch (failed) {
           request = null;
         }
       }
     }

     if (request == null)
       alert("Error creating request object!");
   }
   function sendCommandStack() {
      createRequest();
      var url = "ncombat/GameStatus";
      request.open("POST", url, true);
      request.onreadystatechange = updatePage;
      request.send(null);
      }
   function updateStatus() {
   if (request.readyState == 4) {
         var newStatus = request.responseText;
         var statusEl = document.getElementById("statusScreen");
         replaceText(statusEl, newStatus);
         }
         }

   
 </script>
 	<script>teletype.start(statusScreen); start.disabled=true; pause.disabled=false; resume.disabled=true</script>
	<!-- <script type="text/javascript">init();</script> -->
    <div id="header"><h1>N * C * O * M * B * A * T</h1></div>
</head>
<body>
	<div id="content"><p><jsp:include page="html/content/docs/ncombatIntro.html"/></p>
		<div id="playerInfo">Player information - handle, game, game time</div>
		<div id="gameConsole">
			<div id="statusScreen">Server status updates appear here.</div>
			<div id="commandConsole">
				<form id="cmdString" action="servlet/gameManager/" method="post"> 
				   CMDS? <input type="text" name="cmds" /> 
				   <input type="submit" value="send" /> 
				</form>
			</div>
		</div>
	</div>
	<div id="docPane"><p>Our starting point, the original player documentation:</p><pre><jsp:include page="html/content/docs/COMINFO.txt"/></pre></div>
	<div id="footer">InfoBar - about, contact, help, copyright, license</div>
</body>
</html>
