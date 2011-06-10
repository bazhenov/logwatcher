$(document).ready(function() {

	/**
	 * Flot не умеет рисовать графики в невидимых блоках,
	 * поэтому перед рисованием блок чудесным образом показывается,
	 * а после отрисовки снова скрывается
	 */
	$('#pieChart').css("display", "block");
	$.plot($("#pieChartPlaceHolder"), JSON.parse($("#pieChartData").text()), {
		series: {
			pie: {
				show: true,
				radius: 1,
				label: {
					show: true,
					radius: 3 / 4,
					formatter: function(label, series) {
						return '<div style="font-size:10pt;font-weight: bold;text-align:center;padding:2px;color:black;">' + Math.round(series.percent) + '%</div>';
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
			show: false
		}
	});
	$('#pieChart').css("display", "none");

	$("#pieChartPlaceHolder").bind("plothover", pieHover);
});


function showTooltip(x, y, contents) {
	$('<div id="chartTooltip">' + contents + '</div>').css({
		position: 'absolute',
		display: 'none',
		top: y + 5,
		left: x + 5,
		border: '1px solid #fdd',
		padding: '2px',
		'background-color': '#fee',
		zIndex: 1,
		opacity: 0.80
	}).appendTo("body").show();
}

function pieHover(event, pos, item) {
	if (item) {
		$("#chartTooltip").remove();
		showTooltip(pos.pageX, pos.pageY, "<span style='font-weight:bold;'>" + Math.round(item.series.percent) + "%</span><br/>" + item.series.label);
	} else {
		$("#chartTooltip").remove();
	}

}