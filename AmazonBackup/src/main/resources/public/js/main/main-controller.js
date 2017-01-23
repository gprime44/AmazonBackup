app.controller("mainController", function($scope, $http) {
	$http.get("/account").then(function(response) {
		$scope.status = response.data;
	});

	$scope.hasChildren = function(data) {
		return false;
	}

	$scope.getChildren = function(data) {
		if (!data) {
			if (data == 'root') {
				$http.get("/content/folder/root?withfile=false").then(
						function(response) {
							return response.data;
						});
			} else {
				if (!data.expand) {
					return [];
				} else {
					$http.get(
							"/content/folder/root" + data.path
									+ "?withfile=false").then(
							function(response) {
								return response.data;
							});
				}
			}
		}
	}

	$scope.expand = function(data) {
		data.expand = true;
	}

	$scope.collapse = function(data) {
		data.expand = false;
	}
});