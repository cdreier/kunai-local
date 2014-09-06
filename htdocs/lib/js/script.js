$(function(){
	
	var ready = false;
	var mode = "bulk";
	var activeLiveLI = false;
	
	if(typeof io == undefined || typeof io == "undefined"){
		error("No Server-Connection. We apologize and hope we can fix this soon!");
		return;
	}
	var socket = io.connect(LOCAL_SOCKET, {
		'reconnect' : true,
		'reconnection delay' : 1000,
		'max reconnection attempts' : 10
	});

	socket.on("lError", function(msg){
		error(msg);
	});
	
	socket.on("successfulJoined", function(){
		joined();
	});
	
	socket.on("roomUpdate", function(count){
		if(count == 2){
			status(count + " Clients connected! Im ready!");
			ready = true;
		}else{
			status(count + " Client connected, waiting for second...");
			ready = false;
		}
	});
	
	
	//check if hash is present, join room automaticly
	if(window.location.hash != ""){
		var room = window.location.hash.substr(1);
		$("#roomCode").val(room);
		joinRoom(room);
	}
	
	function joinRoom(room){
		socket.emit("joinRoom", {
			r: room,
			t : "b"
		});
	}
	
	$("#loginForm").on("submit", function(){
		var room = $("#roomCode").val();
		var slug = slugify(room);
		window.location.hash = slug;
		$("#roomCode").val(slug);
		joinRoom(slug);
		return false;
	});
	
	$("#txtForm").on("submit", function(){
		if(!ready)return false;
		
		var txt = $("#txt").val();
		socket.emit("submitFullText", txt);
		$("#txt").val("");
		return false;
	});
	
	$("#txt").on("input propertychange", function(){
		if(!ready)return;
				
		if(mode == "live"){
			
			if(!activeLiveLI){
				addLiveModeLine();
			}
			
			var c = $(this).val();
			socket.emit("submitSingleChar", c);
			activeLiveLI.append(c);
			
			$(this).val("");
		}	
	});
	$("#txt").on("keydown", function(e){
		
		if(mode == "bulk"){
			
			//enter
			if(e.keyCode == 13 && ready){
				if(e.ctrlKey){
					$("#txtForm").submit();
				}else if(e.altKey){
					socket.emit("triggerEnter");
				}
			}
			
		}else if(mode == "live"){
			if(!ready)return false;

			//enter
			if(e.keyCode == 13){
				socket.emit("triggerEnter");
				addLiveModeLine();
				return false;
			}
			
			//backspace
			if(e.keyCode == 8){
				socket.emit("triggerBack");
				if(activeLiveLI){
					var text = activeLiveLI.text();
					text = text.slice(0, -1);
					activeLiveLI.text(text);
				}
				return false;
			}
		}
	});
	
	$(".toggleMode").on("click", function(){
		if($(this).hasClass("inactive")){
			$("#txt").val("");
			$(this).removeClass("inactive");
			var id = "btn-send";
			var hint = "You activated the LIVE - Mode! Every keypress is now transfered immediatly. Just type!";
			mode = "live";
			if($(this).attr("id") == "btn-send"){
				id = "btn-live-mode";
				mode = "bulk";
				hint = "You can trigger the \"Enter-Key\" with ALT-Enter. e.g. to send a chat-message";
				setTimeout(function(){
					$(".shortcut").fadeIn();
				}, 100);
				
				$("#history").animate({
					height: 0
				}, 300);
				$("#txt").animate({
					height: 200
				}, 300);
				
			}else{
				$(".shortcut").hide();
				
				$("#txt").focus();
				
				$("#history ul").empty();
				
				$("#history").animate({
					height: 150
				}, 300);
				$("#txt").animate({
					height: 50
				}, 300);
			}
			$(".hint").text(hint);
			$("#"+id).addClass("inactive");
		}
	});
	
	
	
	
	function addLiveModeLine(){
		activeLiveLI = $("<li></li>");
		$("#history ul").append(activeLiveLI);
	}
	
	function error(msg){
		$("#status-bar .inner").addClass("error");
		$("#status-bar .inner").text(msg);
	}
	
	function status(msg){
		$("#status-bar .inner").addClass("status");
		$("#status-bar .inner").text(msg);
	}
	
	function joined(){
		$('#txtForm').animate({
			top: 0
		}, {duration: 1000, easing: 'easeOutBounce'});
		$("#info-btn-wrap").animate({
			marginTop: 30
		}, 300);
	}
	
	function slugify(str){
		return str.replace(/\s+/g, '-').replace(/[^a-zA-Z0-9-]/g, '');
	}
	
	
	//http://gsgd.co.uk/sandbox/jquery/easing/jquery.easing.1.3.js
	jQuery.easing['jswing'] = jQuery.easing['swing'];
	jQuery.extend( jQuery.easing,
	{
		def: 'jswing',
		swing: function (x, t, b, c, d) {
			//alert(jQuery.easing.default);
			return jQuery.easing[jQuery.easing.def](x, t, b, c, d);
		},
		easeOutBounce: function (x, t, b, c, d) {
			if ((t/=d) < (1/2.75)) {
				return c*(7.5625*t*t) + b;
			} else if (t < (2/2.75)) {
				return c*(7.5625*(t-=(1.5/2.75))*t + .75) + b;
			} else if (t < (2.5/2.75)) {
				return c*(7.5625*(t-=(2.25/2.75))*t + .9375) + b;
			} else {
				return c*(7.5625*(t-=(2.625/2.75))*t + .984375) + b;
			}
		}
	});
	
});

	
