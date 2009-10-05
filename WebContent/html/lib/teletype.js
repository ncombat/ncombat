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

	var clip = function(markup) {
		var br = "<br>";
		while (markup.length > CLIPSIZE) {
			var i = markup.indexOf(br);
			if (i > -1) markup = markup.substring(i + br.length);
		}
		return markup;
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
			var markup = ttElem.innerHTML + msg;
			if (markup.length > MAXSIZE) markup = clip(markup);
			ttElem.innerHTML = markup;
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