$(document).ready(function() {

	$.plot($("#pieChartPlaceHolder"), JSON.parse($("#pieChartData").text()), {
		series: {
			pie: {
				show: true,
				radius: 1,
				label: {
					show: true,
					radius: 3 / 4,
					formatter: function(label, series) {
						return '<div style="font-size:10pt;font-weight: bold;text-align:center;padding:2px;color:white;">' + Math.round(series.percent) + '%</div>';
					},
					background: { opacity: 0.5 }
				}
			}
		},
		grid: {
			hoverable: true,
			clickable: true
		},
		legend: {
			show: true,
			labelFormatter: function(label, series) {
				var newLabel = label.substring(0, 70).trim();
				if (label.length > 70) newLabel += "...";
				return '<span class="pieChartLabel">' + newLabel + '</span>'
			}
		}
	});


	$("#pieChartPlaceHolder").bind("plothover", pieHover);

	$("#pieChartWindow").css("display", "none");
	$("#pieChartLink").click(function (e) {
		e.preventDefault();
		$("#pieChartWindow").slideToggle('fast')
	});

});

function pieHover(event, pos, obj) {
	if (!obj)	return;
	$(".pieChartLabel").css("text-decoration", "none");
	$($(".pieChartLabel").get(obj.seriesIndex)).css("text-decoration", "underline");
}