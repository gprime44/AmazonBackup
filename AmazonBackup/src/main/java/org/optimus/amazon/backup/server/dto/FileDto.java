package org.optimus.amazon.backup.server.dto;

import java.io.Serializable;
import java.util.Date;

public class FileDto implements Serializable {

	private static final long serialVersionUID = 1666220170870197188L;

	public enum STATE {
		LOCAL, REMOTE, BOTH
	};

	private String name;

	private String path;

	private long size;

	private Date dateCreate;

	private Date dateUpdate;

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

	public Date getDateCreate() {
		return dateCreate;
	}

	public void setDateCreate(Date dateCreate) {
		this.dateCreate = dateCreate;
	}

	public Date getDateUpdate() {
		return dateUpdate;
	}

	public void setDateUpdate(Date dateUpdate) {
		this.dateUpdate = dateUpdate;
	}

}
