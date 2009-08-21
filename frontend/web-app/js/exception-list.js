$(function() {
	$('.withStacktrace .message').click(function() {
		$(this).parents(".entry").toggleClass('selectedEntry')
	})

	$('a.removeEntry').click(function() {
		if ( confirm('Вы уверены что хотите удалить запись?') ) {
			$(this).parents(".entry").hide('medium');
		}
		return false;
	})
})