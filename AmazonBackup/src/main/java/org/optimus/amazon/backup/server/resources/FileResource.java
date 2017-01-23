package org.optimus.amazon.backup.server.resources;

import org.apache.commons.lang3.StringUtils;
import org.optimus.amazon.backup.server.dto.FolderDto;
import org.optimus.amazon.backup.server.exception.ServiceException;
import org.optimus.amazon.backup.server.services.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/content")
public class FileResource extends AbstractResource {

	private final static Logger LOGGER = LoggerFactory.getLogger(FileResource.class);

	@Autowired
	private FileService fileService;

	@RequestMapping(method = RequestMethod.GET, value = "/folder/{FOLDER}")
	public FolderDto getFolderContent(@PathVariable("FOLDER") String folder, //
			@RequestParam("withfile") boolean withFile) throws Exception {

		LOGGER.info("User {} get folder content {}", getUser(), folder);

		if (!StringUtils.startsWith(folder, "root")) {
			throw new ServiceException("Param FOLDER must start with 'root'");
		}

		return fileService.getFolderContent(getUser(), StringUtils.remove(folder, "root"), withFile);
	}

}