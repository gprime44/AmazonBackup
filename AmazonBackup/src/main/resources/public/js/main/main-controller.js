app.controller("mainController", function($scope, $rootScope, $http, $timeout, mainService) {
	
	$rootScope.$on('dropEvent', function(evt, dragged, dropped) {
       window.alert(dragged.path + " TO " + dropped.path)
    });
	
	$scope.filesToUpload = [];
	$scope.uploadInProgress = [];
	$scope.uploadedFiles = [];
	
	$http.get("/account").then(function(response) {
		$scope.status = response.data;
	});

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
		$scope.selectedRow = [];
		$scope.display = 'table'
		$scope.images = [];
		
		$http.get("/content?path=" + encodeURIComponent(data.path) + "&withFile=true").then(function(response) {
			$scope.foldercontent = response.data.folders;
			$scope.foldercontent = $scope.foldercontent.concat(response.data.files);
			$scope.folder = response.data;
		});
	}
	
	$scope.order = function (col) {
		$scope.folderOrderBy = col;
	}

	$scope.expand = function(data) {
		$http.get("/content?path=" + encodeURIComponent(data.path) + "&withFile=false").then(function(response) {
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
		if (data.files) {
			// Folder
			return "glyphicon glyphicon-folder-open";
		} else  {
			var pos = data.name.lastIndexOf('.');
			if (pos != -1) {
				var ext = data.name.substring(pos);
				switch (ext) {
				case ".jpg":
				case ".jpeg":
					return "glyphicon glyphicon-picture";
					break;
				case ".mov":
					return "glyphicon glyphicon-film";
					break;
				default:
					return "glyphicon glyphicon-file";
				}
			}
		}
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
	
	$scope.fileAdded = function(e) {
		$scope.$apply(function () {

            for (var i = 0; i < e.files.length; i++) {
            	var file = e.files[i];
            	file.toPath = $scope.folder.path;
                $scope.filesToUpload.push(file)
            }

            manageUpload()
            
        });
	}
	
	function manageUpload() {
		if ($scope.uploadInProgress.length < 5 && $scope.filesToUpload.length > 0) {
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
	
    function updateProgress(e) {
        if (e.lengthComputable) {
			 document.getElementById('pro').setAttribute('value', e.loaded);
			 document.getElementById('pro').setAttribute('max', e.total);
        }
    }

    function transferComplete(e) {
    	$scope.$apply(function () {
	        $scope.uploadedFiles.push($scope.uploadInProgress.pop());
	        $scope.refresh();
	        manageUpload();
    	});
    }
    
    $scope.refresh = function () {
    	$scope.openFolder($scope.folder);
    }
    
    $scope.openContextMenu = function (e) {
         $scope.showPopup = true;
         $scope.popupLeft = e.clientX;
         $scope.popupTop = e.clientY;
    }
});