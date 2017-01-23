package org.optimus.amazon.backup.server.services;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.optimus.amazon.backup.server.dto.FileDto;
import org.optimus.amazon.backup.server.dto.FolderDto;
import org.optimus.amazon.backup.server.dto.FolderDto.STATE;
import org.optimus.amazon.backup.server.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileService {

	private final static Logger LOGGER = LoggerFactory.getLogger(FileService.class);

	@Value("${folder.root}")
	private String rootFolder;

	@Value("${folder.local.decoded}")
	private String localDecodedFolder;

	@Value("${folder.local.global}")
	private String localGlobalFolder;

	@Value("${folder.remote.decoded}")
	private String remoteDecodedFolder;

	public FolderDto getFolderContent(String login, String folder, boolean withFile) throws ServiceException {
		Path globalRootFolder = Paths.get(rootFolder).resolve(login).resolve(localGlobalFolder);
		Path localRootFolder = Paths.get(rootFolder).resolve(login).resolve(localDecodedFolder);
		Path remoteRootFolder = Paths.get(rootFolder).resolve(login).resolve(remoteDecodedFolder);

		Path folderToScan = globalRootFolder.resolve(folder);

		if (!Files.exists(folderToScan)) {
			throw new ServiceException("Folder {} doesn't exist", folderToScan);
		}

		if (!Files.isDirectory(folderToScan)) {
			throw new ServiceException("{} isn't a folder", folderToScan);
		}

		FolderDto folderDto = new FolderDto();
		folderDto.setPath(folder);
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderToScan)) {
			for (Path entry : stream) {
				boolean isLocal = Files.exists(localRootFolder.resolve(globalRootFolder.relativize(entry)));
				boolean isRemote = Files.exists(remoteRootFolder.resolve(globalRootFolder.relativize(entry)));
				STATE state = STATE.REMOTE;
				if (isLocal) {
					state = STATE.LOCAL;
					if (isRemote) {
						state = STATE.BOTH;
					}
				}

				if (Files.isDirectory(entry)) {
					FolderDto subFolder = new FolderDto();
					subFolder.setName(entry.getFileName().toString());
					subFolder.setSize(Files.size(entry));
					subFolder.setState(state);
					subFolder.setPath(globalRootFolder.relativize(entry).toString());
					folderDto.getFolders().add(subFolder);

				} else if (withFile) {
					FileDto fileDto = new FileDto();
					fileDto.setName(entry.getFileName().toString());
					fileDto.setSize(Files.size(entry));
					fileDto.setState(state);
					fileDto.setPath(globalRootFolder.relativize(entry).toString());
					folderDto.getFiles().add(fileDto);
				}
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return folderDto;
	}
}
