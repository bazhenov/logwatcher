severity = ['all', 'trace', 'debug', 'info', 'warning', 'error'];

function changeSeverity(url) {
	$.ajax({
		type: 'GET',
		url: url,
		success: function () {
			window.location = "";
		}
	});
}

$(document).ready(function () {
	$('#applicationId').click(function () {
		showPopup($(this), $('#applicationSelector'));
		return false;
	});

	$('#pieChartLink').click(function () {
		showPopup($(this), $('#pieChart'));
		return false;
	});


	$('#date').click(function () {
		showPopup($(this), $('#dateSelector'));
		return false;
	});

	$('body').click(function (event) {
		if ($(event.target).parents(".popupContainer").length <= 0) {
			hideAllPopups();
		}
	});

	function hideAllPopups() {
		$('.popupContainer').fadeOut(300);
	}

	function showPopup(activator, popup) {
		if (popup.css('display') == 'none') {
			hideAllPopups();
			var position = activator.position();
			popup.css({"left": position.left, "top": position.top + activator.height() + 10});
			popup.find('img.mark').css({ left: (activator.width() / 2 - 8) + "px" });

			//;
			popup.fadeIn(300);
		} else {
			popup.fadeOut(300);
		}
	}

	/* Open In IDE */
	/*$('.entryContent pre').each(function () {
	 var text = $(this).html().replace(/[0-9a-z_A-Z\-\.\/]+:\d+/g, '<a class="openinide-link" href="#$&">$&</a>');
	 $(this).html(text);
	 });

	 $('.logEntry pre').each(function () {
	 var text = $(this).html().replace(/[0-9a-z_A-Z\-\.\/]+:\d+/g, '<a class="openinide-link" href="#$&">$&</a>');
	 $(this).html(text);
	 });

	 $('a.openinide-link').click(function (e) {
	 e.preventDefault();
	 var message = $(this).attr("href").substr(1);
	 $.getJSON('http://localhost:8091/?message=' + message + '&callback=?', function (json) {
	 //do nothing
	 });
	 });*/
});

function Counterina(canvas, maxData) {
	this.canvas = canvas;
	this.maxData = maxData > 1 ? maxData : 2;

	this.formatNumber = function (count) {
		if (count >= 1000000) {
			return Math.round(count / 1000000) + "M";
		} else if (count >= 100000) {
			return Math.round(count / 1000) + "K";
		} else if (count.toString().length == 5) {
			return count.substring(0, 2) + " " + count.substring(2);
		} else if (count.toString().length == 4) {
			return count.substring(0, 1) + " " + count.substring(1);
		}
		return count;
	};

	this.hypLength = function (c1, c2) {
		return Math.sqrt(Math.pow(c1, 2) + Math.pow(c2, 2));
	};

	this.draw = function (count, severity) {
		var context = canvas.getContext('2d');

		var x = canvas.width / 2;
		var y = canvas.height / 2;
		var text = this.formatNumber(count);
		var rMin = 3;
		var rMax = canvas.width / 2;
		var radius = (Math.sqrt(count - 1) * (rMax - rMin)) / (Math.sqrt(this.maxData - 1)) + rMin;

		context.font = 'bold 12pt Helvetica Neue';
		context.textAlign = 'center';
		context.textBaseline = 'middle';

		var metrics = context.measureText(text);

		var width = metrics.width;
		var textRadius = this.hypLength(width / 2, 12 / 2);
		var offset = radius - textRadius;

		switch (severity) {
			case 'warning':
				context.fillStyle = 'rgb(215, 87, 0)';
				break;

			case 'info':
				context.fillStyle = 'rgb(51, 153, 0)';
				break;

			case 'debug':
				context.fillStyle = 'rgb(34, 68, 119)';
				break;

			case 'trace':
				context.fillStyle = 'rgb(170, 170, 170)';
				break;

			default:
				context.fillStyle = 'rgb(153, 0, 0)';
				break;
		}

		if (offset <= 0) {
			// текст не вмещается в круг
			context.beginPath();
			y = (canvas.height - 10) / 2;
			context.arc(x, y, radius, 0, 2 * Math.PI, false);
			context.closePath();
			context.fill();

			context.font = 'bold 10pt Helvetica Neue';
			context.textBaseline = 'bottom';
			context.fillText(text, x, canvas.height);
		} else {
			// текст вмещается в круг
			context.beginPath();
			context.arc(x, y, radius, 0, 2 * Math.PI, false);
			context.closePath();
			context.fill();

			if (offset > 5) {
				context.fillStyle = 'white';
				context.fillText(text, x, y);
			} else {
				context.font = 'bold 10pt Helvetica Neue';
				context.fillStyle = 'white';
				context.fillText(text, x, y);
			}
		}
	}
}