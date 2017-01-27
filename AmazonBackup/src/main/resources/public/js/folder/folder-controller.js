app.controller("folderController", function($scope, $rootScope, $http, $timeout, folderService) {
	
	$scope.display = "table";
	$scope.images = [];
	$scope.folder = false;
	
	$rootScope.$on("openFolder", function(event, data) {
		$scope.display = 'table';
		$scope.images = [];
		$scope.folder = true;
		
		$http.get("/content?path=" + encodeURIComponent(data.path) + "&withFile=true").then(function(response) {
			$scope.foldercontent = response.data.folders;
			$scope.foldercontent = $scope.foldercontent.concat(response.data.files);
			$scope.folder = response.data;
		});
	});
	
	$scope.order = function (col) {
		$scope.folderOrderBy = col;
	}
	
	$scope.delete = function (data) {
		$http.delete("/content?path=" + encodeURIComponent(data.path)).then(function(response) {
			$scope.refresh();
		});
	}
	
	$scope.canDelete = function (data) {
		return !data.files;
	}
	
	$scope.canDownload = function (data) {
		return !data.files;
	}
	
	$scope.getIcon = function (data) {
		folderService.getIcon(data);
	}
	
	$scope.isFolder = function (data) {
		if (data) {
			return data.files;
		}
	}
	
	$scope.canPreview = function (data) {
		if (data) {
			return !data.files;
		}
	}
	
	$scope.getUrl = function (data) {
		if (data) {
			return "/content/get?path=" + encodeURIComponent(data.path);
		}
	}
	
	$scope.downloadUrl = function (data) {
		if (data) {
			return "/content/download?path=" + encodeURIComponent(data.path);
		}
	}
	
	$scope.preview = function (data) {
		$scope.selectedRow = data;
	}
	
	$scope.changeDisplay = function (newDisplay) {
		$scope.display = newDisplay;
		var images = []
		var files = $scope.folder.files;
		for (i=0;i<files.length;i++) {
			if (files[i].name.endsWith('jpg')) {
				images.push(files[i]);
			}
		}
		$scope.images = images;
		
		$timeout(function() {
	          $('.carousel-indicators li').first().addClass('active');
	          $('.carousel-inner .item').first().addClass('active');
	        });
	}
	
    $scope.refresh = function () {
    	$scope.openFolder($scope.folder);
    }
    
    $rootScope.$on('refreshData', function(evt, path) {
		
	});
});