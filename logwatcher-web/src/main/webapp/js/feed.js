var app = angular.module('logWatcher', []);

app.filter('fooz', function fooz($data, $postfix) {
	return $data + $postfix;
});

app.config(function($routeProvider) {
	$routeProvider
		.when('/feed/:applicationId', {
			controller: 'FeedController',
			templateUrl: '/partials/view1.html'
		})
		.when('/partial2', {
			controller: 'FeedController',
			templateUrl: '/partials/view2.html'
		})
});

app.controller('FeedController', function ($scope, $http, $routeParams) {
	$scope.applicationId = $routeParams.applicationId;
	$http.get('/rest/feed/' + $routeParams.applicationId).success(function(response) {
		$scope.entries = response;
	});

	$scope.setApplication = function(id) {
		$scope.applicationId = id;
	}
});