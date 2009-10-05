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
					$('#commandConsole').css('display', 'inline');
					$('#cmds').val('').focus();
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
	<div id="header"><h1>N * C * O * M * B * A * T</h1></div>

	<div id="content">
		<div id="gameConsole">
			<div id="statusScreen"><!--  Server status updates appear here. --></div>
			
			<div id="commandConsole">
				<form id="cmdString" action="#" method="post" onsubmit="sendRequest(); return false"> 
				   <span id="prompt"></span>
				   <input id="cmds" type="text" name="cmds" size="60" /> 
				   <input id="submit" type="submit" value="send""/> 
				</form>
			</div>
		</div>
	</div>
	
	<div id="docPane">
		<dl>
			<dt>Our starting point, the original player documentation (click to view)</dt>
			<dd><pre><jsp:include page="/html/content/docs/COMINFO.txt"/></pre></dd>
		</dl></div>
		<script type="text/javascript">
		$(document).ready(function(){
			$("dd").hide();
		});
		
		$("dt").click(function(){
			$(this).next().toggle();
		});
		</script>
	<div id="footer">InfoBar - about, contact, help, copyright, license</div>
</body>
</html>
