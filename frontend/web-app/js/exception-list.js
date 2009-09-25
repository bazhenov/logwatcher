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