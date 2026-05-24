teletype = function() {
	var NEWLINE = '<br>';
	var SPACE = '&nbsp;';
	var MAXSIZE = 3000;
	var CLIPSIZE = 2000;

	var text = "";
	var nextChar = 0;
	var running = false;
	var ttElem;
	var blockNum = 1;
	var holdBuffer;
	var callback;
	var charCount = 0;

	var clip = function() {
		while (charCount > CLIPSIZE && ttElem.firstChild) {
			var node = ttElem.firstChild;
			if (node.nodeType === Node.TEXT_NODE) {
				charCount -= node.nodeValue.length;
			} else {
				charCount -= 1;
			}
			ttElem.removeChild(node);
		}
	};

	var typeCharacter = function() {
		if (nextChar < text.length) {
			var newBit = text.substring(nextChar, nextChar+1).toUpperCase();
			if (newBit == '\n') newBit = NEWLINE;
			if (newBit == ' ') newBit = SPACE;
			render(newBit);
			nextChar++;
		}
		else {
			text = "";
			nextChar = 0;
			if (callback) {
				var theCallback = callback;
				callback = null;
				theCallback();
			}
		}
	};

	var render = function(msg) {
		if (running) {
			if (msg === NEWLINE) {
				$(ttElem).append(document.createElement('br'));
				charCount += 1;
			} else if (msg === SPACE) {
				$(ttElem).append(document.createTextNode(' '));
				charCount += 1;
			} else {
				$(ttElem).append(document.createTextNode(msg));
				charCount += msg.length;
			}
			if (charCount > MAXSIZE) clip();
			ttElem.scrollTop = ttElem.scrollHeight;
		}
		else {
			holdBuffer += msg;
		}
	};

	return {
		print : function(msg) {
			text += msg;
		},
	
		println: function(msg) {
			this.print(msg + "\n");
		},

		start: function(_ttElem) {
			ttElem = _ttElem;
			running = true;
			setInterval(typeCharacter, 33);
		},

		pause: function() {
			running = false;
			holdBuffer = "";
		},

		resume: function() {
			running = true;
			print(holdBuffer);
		},
		
		callWhenDone: function(cb) {
			callback = cb;
		}
	}
}();