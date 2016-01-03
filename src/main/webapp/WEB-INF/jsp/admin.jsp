<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	
	<title>NCOMBAT Administration</title>
	
	<script type="text/javascript" src="html/lib/jquery-1.3.2.js" ></script>
	
	<link type="text/css" rel="stylesheet" href="html/styles/ncombat_screen.css">
	   
	<script>
		// Our configuration object, where we put our data to avoid cluttering
		// up the global namespace.
		ncombat = ncombat || {};
		
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
		});
	</script>
</head>
<body>
	<div id="ncombatInfo">
		<table>
			<tr>
				<td>Up since:</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td>Up time:</td>
				<td>&nbsp;</td>
			</tr>
		</table>
	</div>
	
	<div id="playerInfo">
	</div>
	
	<div id="gameInfo">
	</div>
	
	<div id="footer"><span id="about">about</span> <span id="contact">contact</span> <span id="help">help</span> <span id="license">license</span> </div>
</body>
</html>
