<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	
	<title>NCOMBAT</title>
	
	<script type="text/javascript" src="html/lib/jquery-1.3.2.js" ></script>
	<script type="text/javascript" src="html/lib/ncombat_base.js"></script>
	<script type="text/javascript" src="html/lib/teletype.js"></script>
	
	<link type="text/css" rel="stylesheet" href="html/styles/ncombat_screen.css">
	   
	<script>
		// Our configuration object, where we put our data to avoid cluttering
		// up the global namespace.
		ncombat = {};
		
		function processResponse(xhr, status) {
			if (!status || (status != 'success')) return;
			//alert("AJAX SUCCESS: status=" + status +", response=" + xhr.responseText);
			eval('var data = ' + xhr.responseText + '.data');
			var messages = data.messages;
			if (messages) {
				$('#commandConsole').css('display', 'none');
				for (var i = 0 ; i < messages.length ; i++) {
					teletype.println(messages[i]);
				}
				teletype.callWhenDone( function() {
					if (data.alive) {
						$('#commandConsole').css('display', 'inline');
						$('#cmds').val('').focus();
					}
					else {
						$('#restartPane').css('display', 'inline-block');
					}
				});
			}
			if (data.prompt) {
				$('#prompt').html(data.prompt);
			}
			if (data.url) {
				ncombat.nextUrl = data.url;
			}
		}

		function processError(xhr, status, error) {
			alert("AJAX ERROR: status='" + status + "', error='" + error + "'");	
		}

		function sendRequest() {
			if (ncombat.nextUrl) {
				$.ajax({
					complete: processResponse,
					data: { text: $('#cmds').attr('value') },
					error: processError,
					type: 'post',
					url: ncombat.nextUrl
				});
			}		
		}

		$(document).ready( function() {
		
			setInterval( function() {
				$.getJSON("gamePing.json");
			}, 30000);

			$("#dd").load("html/content/docs/COMINFO.txt");
			// allow click to expand dd tag
			$("dt").click(function(){
				$(this).next().toggle();
				
			});
			
			// start interface	
			teletype.start(statusScreen);
			$.ajax({
				complete: processResponse,
				error: processError,
				type: 'post',
				url: 'gameJoin.json'
			});		
		});
	</script>
</head>
<body>
	<div id="header"><span id="ncombatTitle">N * C * O * M * B * A * T</span></div>
	<div id="content">
		<div id="gameConsole">
			<div id="statusScreen"></div>
			<div id="commandConsole">
				<form id="cmdString" action="#" method="post" onsubmit="sendRequest(); return false"> 
				   <span id="prompt"></span>
				   <input id="cmds" type="text" name="cmds" size="69" maxlength="69"/> 
				   <input id="submit" type="submit" value="send""/> 
				</form>
			</div>
			<div id="restartPane">
				Thank you for playing N * C * O * M * B * A * T.<br>
				<div id="restartLink"><a href="game.do">Play Again</a></div>
			</div>
		</div>
	</div>
	<div id="docPane">
		<dl>
			<dt>Our starting point, the original player documentation (click to hide/unhide)</dt>
			<dd><pre><span id="originalDocs"></span></pre></dd>
		</dl></div>
	<div id="footer"><span id="about">about</span> <span id="contact">contact</span> <span id="help">help</span> <span id="license">license</span> </div>
	<div id="googleanalytics">
		<script type="text/javascript">
			var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
			document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
		</script>
		<script type="text/javascript">
			try{
				var pageTracker = _gat._getTracker("UA-10593313-2");
				pageTracker._trackPageview();
			} catch(err) {}
		</script>
	</div>
</body>
</html>
