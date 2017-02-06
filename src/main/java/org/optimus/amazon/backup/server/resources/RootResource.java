package org.optimus.amazon.backup.server.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class RootResource extends AbstractResource {

	private final static Logger LOGGER = LoggerFactory.getLogger(RootResource.class);

	@RequestMapping(method = RequestMethod.GET)
	public String getContainerStats() throws Exception {
		LOGGER.info("User {} logged", getUser());
		return getUser();
	}

}
