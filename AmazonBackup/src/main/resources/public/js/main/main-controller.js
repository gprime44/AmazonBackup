app.controller("mainController", function($scope, $rootScope, $http, $timeout,
		mainService) {

	$scope.filesToUpload = [];
	$scope.uploadInProgress = [];
	$scope.uploadedFiles = [];

	$http.get("/account").then(function(response) {
		$scope.status = response.data;
	});

	$rootScope.$on('dropEvent', function(evt, dragged, dropped) {
		if (dropped.files) {
			var data = new FormData();
			data.append("from", dragged);
			data.append("to", dropped);

			$http.post("/content/move", data).then(function(response) {
				$rootScope.$broadcast('refreshData', dragged.parent);
				$rootScope.$broadcast('refreshData', dropped.path);
			});
		}
	});

	$scope.fileAdded = function(e) {
		$scope.$apply(function() {

			for (var i = 0; i < e.files.length; i++) {
				var file = e.files[i];
				file.toPath = $scope.folder.path;
				$scope.filesToUpload.push(file)
			}

			folderService.manageUpload($scope)

		});
	}
});