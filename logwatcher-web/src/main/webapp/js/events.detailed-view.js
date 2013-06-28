$(document).ready(function () {
	function loadAttributes() {

		var el = $("#attributesContainer");
		var checksum = el.attr("data-checksum");
		var lastOccurredDate = el.attr("data-date");
		el.attr("loaded", "true");
		el.load("/service/content", {'checksum': checksum, 'date': lastOccurredDate}, function (response, code) {
			if (code != "success") {
				el.html("");
			}
		});
	}

	loadAttributes()
});