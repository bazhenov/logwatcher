$(function() {
	$('.withStacktrace .message').click(function() {
		$(this).parents(".entry").toggleClass('selectedEntry')
	})

	$('a.removeEntry').click(function() {
		var entry = $(this).parents(".entry")
		var checksum = entry.attr('checksum')
		if ( confirm('Вы уверены что хотите удалить запись?') ) {
			entry.addClass('removing')
			$.ajax({
				type: "GET",
				url: './remove-entry/' + checksum,
				complete: function() {
					entry.removeClass('removing')
				},
				success: function(msg) {
					entry.hide('medium')
				},
				error: function(request, status, error) {
					alert("Произошла ошибка при удалении записи: "+status+", "+error)
				}
			})
		}

		return false;
	})
})

severity = ['all', 'trace', 'debug', 'info', 'warning', 'error'];

$(document).ready(function() {

	$('#slider').slider({
		step: 1,
		min: 1,
		max: 5,
		slide: function(event, ui) {
			var value = ui.value;
			var className = "slider-" + severity[value]
			$('#slider').addClass(className);

			$('#sliderValue').text(severity[value]);
		},
		change: function(event, ui) {
			var value = severity[ui.value];
			var url = jQuery.param({severity: value});
			document.location = '?' + url;
		}
	});

	for ( var i = 0; i < severity.length; i++ ) {
		if ( severity[i] == severityLevel || severityLevel == i ) {
			$('#sliderValue').text(severity[i]);
			$('#slider').slider('option', 'value', i);
		}
	}

});