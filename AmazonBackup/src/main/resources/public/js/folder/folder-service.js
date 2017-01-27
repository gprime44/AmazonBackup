app.service('folderService', function($http) {

	this.getIcon = function(data) {
		if (data.files) {
			// Folder
			return "glyphicon glyphicon-folder-open";

		} else {
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
});