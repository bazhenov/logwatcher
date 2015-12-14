$(document).ready(function () {
	var el = $("h2");
	var checksum = el.attr("data-checksum");
	var application = el.attr("data-application");

	function loadDayData() {
		var date = $("#datePicker").val();
		location.hash = date;
		var data = {'date': date, 'application': application, 'checksum': checksum};
		$.ajax({ url: "/service/content", data: data }).done(function (result) {
			$("#attributesContainer").html(result);
		}).fail(function() {
			$("#attributesContainer").html("Failed to load data");
		});
		$.ajax({ url: "/service/log", data: data }).done(function (result) {
			$("#logSamples").html(result);
		}).fail(function() {
			$("#logSamples").html("Failed to load data");
		});
	}

	function showStatistics() {
		$.ajax({
			dataType: "json",
			url: "/service/stat/by-minute.json",
			data: {application: application, checksum: checksum, minutes: 60},
			success: function (data) {
				$('#minuteStatistics').highcharts({
					chart: {
						type: 'column'
					},
					title: {
						text: 'Event frequency (last hour)'
					},
					xAxis: {
						categories: data['labels'],
						labels: {
                            maxStaggerLines: 1,
							step: 6
						}
					},
					yAxis: {
						title: {
							text: 'Frequency'
						},
						min: 0
					},
					plotOptions: {
						column: {
							pointPadding: 0.2,
							borderWidth: 0,
							groupPadding: 0,
							shadow: false
						}
					},
					legend: {
						enabled: false
					},
					series: [
						{data: data['data']}
					]
				});
			}
		});

		$.ajax({
			dataType: "json",
			url: "/service/stat/by-day.json",
			data: {application: application, checksum: checksum, days: 30},
			success: function (data) {
				$('#dayStatistics').highcharts({
					chart: {
						type: 'column'
					},
					title: {
						text: 'Event frequency (last month)'
					},
					xAxis: {
						categories: data['labels'],
						labels: {
                            maxStaggerLines: 1,
							step: 6
						}
					},
					yAxis: {
						title: {
							text: 'Frequency'
						},
						min: 0
					},
					plotOptions: {
						column: {
							pointPadding: 0.2,
							borderWidth: 0,
							groupPadding: 0,
							shadow: false
						}
					},
					legend: {
						enabled: false
					},
					series: [
						{data: data['data']}
					]
				});
			}
		});
	}

	var datePicker = $('#datePicker').datepicker({
		onRender: function (date) {
			return date.valueOf() > new Date().valueOf() ? 'disabled' : '';
		}
	});
	if(location.hash) {
		datePicker.val(location.hash.replace('#', ''));
	}

	$("#showLog").click(loadDayData);

	showStatistics();
	loadDayData();

	$("#clusterSave").click(function () {
		var title = $("#clusterTitle").val();
		var issueKey = $("#clusterIssueKey").val();
		var description = $("#clusterDescription").val();

		$.ajax({
			url: "/cluster/" + application + "/" + checksum,
			type: "POST",
			data: {'title': title, 'issueKey': issueKey, 'description': description}
		}).success(function () {
				window.location = window.location;
			}).error(function (req, text, error) {
				alert("Operation failed. Server says: " + error);
			});
	})
});