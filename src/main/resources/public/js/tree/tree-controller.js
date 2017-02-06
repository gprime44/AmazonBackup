app.controller("treeController", function($scope, $rootScope, $http, $timeout,
		treeService) {

	$http.get("/content?withFile=false").then(function(response) {
		$scope.treeData = response.data;
	});

	$scope.canExpand = function(data) {
		return data.folders && !data.loaded;
	}

	$scope.canCollapse = function(data) {
		return data.folders && data.loaded;
	}

	$scope.getChildren = function(data) {
		if (data) {
			return data.folders;
		} else {
			if ($scope.treeData) {
				return $scope.treeData.folders;
			} else {
				return [];
			}
		}
	}

	$scope.openFolder = function(data) {
		$rootScope.$broadcast('openFolder', data);
	}

	$scope.expand = function(data) {
		$http.get(
				"/content?path=" + encodeURIComponent(data.path)
						+ "&withFile=false").then(function(response) {
			data.files = response.data.files;
			data.folders = response.data.folders;
			data.loaded = true;
		});
	}

	$scope.collapse = function(data) {
		data.files = [];
		data.folders = [];
		data.loaded = false;
	}

	$rootScope.$on('refreshData', function(evt, path) {

	});
});