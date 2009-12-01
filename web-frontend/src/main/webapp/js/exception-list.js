severity = ['all', 'trace', 'debug', 'info', 'warning', 'error'];

$(document).ready(function() {

	$('.message').click(function() {
		$(this).parents(".entry").toggleClass('selectedEntry')
	});

	$('#searchInput').focus();

	$('a.removeEntry').click(function() {
		var entry = $(this).parents(".entry")
		var checksum = entry.attr('checksum')
		if ( confirm('Вы уверены что хотите удалить запись?') ) {
			entry.addClass('removing');
			$.ajax({
				type: "GET",
				url: './remove-entry/' + checksum,
				complete: function() {
					entry.removeClass('removing');
				},
				success: function(msg) {
					entry.hide('normal');
				},
				error: function(request, status, error) {
					alert("Произошла ошибка при удалении записи: "+status+", "+error)
				}
			})
		}

		return false;
	})

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
			$.ajax({
				type: "GET",
				url: './session?severity=' + value,
				complete: function() {
					window.location = window.location;
				}
			});
		}
	});

	for ( var i = 0; i < severity.length; i++ ) {
		if ( severity[i] == severityLevel || severityLevel == i ) {
			$('#sliderValue').text(severity[i]);
			$('#slider').slider('option', 'value', i);
		}
	}

	var checksum = location.hash.substring(1);
	if ( checksum.length > 0 ) {
		$('.entry').each(function() {
			if ( $(this).attr('checksum') == checksum ) {
				$(this).toggleClass('selectedEntry');
			}
		});
	}
});