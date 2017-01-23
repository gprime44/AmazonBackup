package org.optimus.amazon.backup.server.exception;

import java.text.MessageFormat;

import org.slf4j.helpers.MessageFormatter;

public class ServiceException extends Exception {

	public ServiceException(String message, Object... params) {
		super(MessageFormatter.arrayFormat(message, params).getMessage());
	}
}
