package org.optimus.amazon.backup.server.dto;

import java.io.Serializable;

import org.optimus.amazon.backup.server.dto.FolderDto.STATE;

public class FileDto implements Serializable {

	private static final long serialVersionUID = 1666220170870197188L;

	private String name;
	
	private String path;

	private long size;

	private STATE state;

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
