$(document).ready(function () {
	var el = $("h2");
	var checksum = el.attr("data-checksum");
	var application = el.attr("data-application");

	function loadAttributes() {
		var el = $("#attributesContainer");
		el.load("/service/content", {'checksum': checksum}, function (response, code) {
			if (code != "success") {
				el.html("Error while loading data...");
			}
		});
	}

	function showMinuteStatistics() {
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

	$('#datePicker').datepicker({
		onRender: function (date) {
			return date.valueOf() > new Date().valueOf() ? 'disabled' : '';
		}
	});

	var showLogButton = $("#showLog");
	showLogButton.click(function () {
		var date = $("#datePicker").val();
		$.ajax({
			url: "/service/log",
			data: {'date': date, 'application': application, 'checksum': checksum}
		}).done(function (data) {
				$("#logSamples").html(data);
			});
	});

	showLogButton.click();
	showMinuteStatistics();
	loadAttributes();
});