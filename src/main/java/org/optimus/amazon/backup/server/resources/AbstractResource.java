package org.optimus.amazon.backup.server.resources;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;

public class AbstractResource {

	protected String getUser() throws Exception {
		String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
		if (StringUtils.isEmpty(currentUser)) {
			throw new Exception("User not logged");
		}
		return currentUser;
	}

}
