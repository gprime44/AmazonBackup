package org.optimus.amazon.backup.server.services;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang3.ArrayUtils;
import org.optimus.amazon.backup.server.dto.FileDto;
import org.optimus.amazon.backup.server.dto.FileDto.STATE;
import org.optimus.amazon.backup.server.dto.FolderDto;
import org.optimus.amazon.backup.server.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

		Path folderToScan = getFile(login, folder);

		if (!Files.exists(folderToScan)) {
			throw new ServiceException("Folder {} doesn't exist", folderToScan);
		}

		if (!Files.isDirectory(folderToScan)) {
			throw new ServiceException("{} isn't a folder", folderToScan);
		}

		FolderDto folderDto = new FolderDto();
		folderDto.setPath(folder);
		folderDto.setName(folderToScan.getFileName().toString());
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
					subFolder.setDateUpdate(new Date(Files.getLastModifiedTime(entry).toMillis()));
					folderDto.getFolders().add(subFolder);

				} else if (withFile) {
					FileDto fileDto = new FileDto();
					fileDto.setName(entry.getFileName().toString());
					fileDto.setSize(Files.size(entry));
					fileDto.setState(state);
					fileDto.setPath(globalRootFolder.relativize(entry).toString());
					fileDto.setDateUpdate(new Date(Files.getLastModifiedTime(entry).toMillis()));
					folderDto.getFiles().add(fileDto);
				}
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return folderDto;
	}

	public Path getFile(String login, String path) throws ServiceException {
		Path globalRootFolder = Paths.get(rootFolder).resolve(login).resolve(localGlobalFolder);

		Path file = globalRootFolder.resolve(path);
		if (!Files.exists(file)) {
			throw new ServiceException("File {} does't exist", file);
		}
		if (!Files.isReadable(file)) {
			throw new ServiceException("File {} isn't readable", file);
		}

		return file;
	}

	public void delete(String login, String path) throws ServiceException {
		Path localFile = Paths.get(rootFolder).resolve(login).resolve(localDecodedFolder).resolve(path);
		Path remoteRemote = Paths.get(rootFolder).resolve(login).resolve(remoteDecodedFolder).resolve(path);

		if (Files.exists(localFile)) {
			try {
				Files.delete(localFile);
			} catch (IOException e) {
				throw new ServiceException("Unable to delete {}", localFile);
			}
		}

		if (Files.exists(remoteRemote)) {
			CommandLine cl = new CommandLine("acd_cli");
			cl.addArgument("trash");
			cl.addArgument("/" + login + "/" + path);

			LOGGER.debug("Execute : {}", cl.toString());

			try {
				LOGGER.info("Execution return : {}", new DefaultExecutor().execute(cl));
			} catch (ExecuteException e) {
				LOGGER.error(e.getMessage(), e);
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

	}

	public void saveFiles(String login, String path, MultipartFile[] files) throws ServiceException {
		if (ArrayUtils.isNotEmpty(files)) {
			for (MultipartFile file : files) {
				Path destPath = null;
				try {
					destPath = getFile(login, path).resolve(file.getOriginalFilename());
				} catch (ServiceException e) {
					throw new ServiceException("Unable to retrieve path {}", path);
				}
				try {
					Files.copy(file.getInputStream(), destPath, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					throw new ServiceException("Unable to upload file {}", destPath);
				}
			}
		}
	}
}
