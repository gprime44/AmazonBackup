package org.optimus.amazon.backup.server.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FolderDto implements Serializable {

	private static final long serialVersionUID = 1150490729774025729L;

	public enum STATE {
		LOCAL, REMOTE, BOTH
	};

	private String name;
	
	private String path;

	private long size;

	private STATE state;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public STATE getState() {
		return state;
	}

	public void setState(STATE state) {
		this.state = state;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
