severity = ['all', 'trace', 'debug', 'info', 'warning', 'error'];

;(function($) {
	$.fn.extend({
		autocomplete: function(el) {

		}
	});
})(jQuery);

$(document).ready(function() {

	function toggleEntry(entry) {
		entry.toggleClass('selectedEntry');
		var checksum = entry.attr("checksum");
		var attributes = entry.find('.attributes');
		if ( attributes.attr("loaded") == "false" ) {
			attributes.attr("loaded", "true");
			entry.addClass("loadingAttributes");
			attributes.load("/service/attributes", {'checksum': checksum, 'date': date}, function(response, code) {
				if ( code != "success" ) {
					attributes.html("Error while loading attributes");
				}
				entry.removeClass("loadingAttributes");
			});
		}

	}

	$('#searchInput').autocomplete($('#suggest'));

	var suggestPane = $('#suggest');
	var searchInput = $('#searchInput');
	searchInput.focus();

	searchInput.bind('focus', function() {
		suggestPane.show();
	});

	searchInput.bind('blur', function() {
		suggestPane.hide();
	});

	$('.entryHeader').click(function(target) {
		if ( $(target.target).parents(".noBubble").length <= 0 ) {
			toggleEntry($(this).parents(".entry"));

		}
	});

	$('#legend #question').click(function() {
		$(this).parents("#legend").toggleClass('selected');
	});

	$('#legend a').click(function() {
		var input = $('#searchInput');
		var value = $(this).text();
		if ( input.val().length > 0 && input.val().substr(-1, 1) != ' ' ) {
			value = " " + value;
		}
		input.val(input.val() + value);
		input.focus();
	});



	$('a.removeEntry').click(function() {
		var entry = $(this).parents(".entry")
		var checksum = entry.attr('checksum')
		if ( confirm('Вы уверены что хотите удалить запись?') ) {
			entry.addClass('removing');
			$.ajax({
				type: "GET",
				url: './entry/remove?checksum=' + checksum,
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

	$("#sortBox span").each(function() {
		var el = $(this);
		if ( el.attr("value") == entrySortOrder ) {
			el.addClass("selected");
		}else{
			el.click(function() {
				var value = $(this).attr("value");
				$.ajax({
					type: "GET",
					url: './session?sortOrder=' + value,
					complete: function() {
						window.location = window.location;
					}
				});
			});
		}
	});
});
