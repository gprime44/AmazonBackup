app.service('mainService', function() {

	this.manageUpload = function($scope) {
		if ($scope.uploadInProgress.length < 5
				&& $scope.filesToUpload.length > 0) {

			var fileToUpload = $scope.filesToUpload.pop();
			fileToUpload.idProgress = $scope.uploadInProgress.length;
			$scope.uploadInProgress.push(fileToUpload);

			var data = new FormData();
			data.append("file", fileToUpload);
			data.append("path", fileToUpload.toPath);

			// ADD LISTENERS.
			var objXhr = new XMLHttpRequest();
			objXhr.addEventListener("progress", updateProgress, false);
			objXhr.addEventListener("load", transferComplete, false);

			// SEND FILE DETAILS TO THE API.
			objXhr.open("POST", "/content");
			objXhr.send(data);

			manageUpload();
		}
	}

	this.updateProgress = function(e) {
		if (e.lengthComputable) {
			document.getElementById('pro').setAttribute('value', e.loaded);
			document.getElementById('pro').setAttribute('max', e.total);
		}
	}

	this.transferComplete = function(e) {
		$scope.$apply(function() {
			$scope.uploadedFiles.push($scope.uploadInProgress.pop());
			$scope.refresh();
			manageUpload();
		});
	}

});