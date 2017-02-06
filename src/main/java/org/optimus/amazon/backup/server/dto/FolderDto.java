package org.optimus.amazon.backup.server.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FolderDto extends FileDto implements Serializable {

	private static final long serialVersionUID = 1150490729774025729L;

	private List<FileDto> files = new ArrayList<>();

	private List<FolderDto> folders = new ArrayList<>();

	public List<FileDto> getFiles() {
		return files;
	}

	public void setFiles(List<FileDto> files) {
		this.files = files;
	}

	public List<FolderDto> getFolders() {
		return folders;
	}

	public void setFolders(List<FolderDto> folders) {
		this.folders = folders;
	}
}
