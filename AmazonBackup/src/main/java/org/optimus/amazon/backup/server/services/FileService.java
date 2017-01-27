package org.optimus.amazon.backup.server.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.optimus.amazon.backup.server.dto.FileDto;
import org.optimus.amazon.backup.server.dto.FileDto.STATE;
import org.optimus.amazon.backup.server.dto.FolderDto;
import org.optimus.amazon.backup.server.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

	private final static Logger LOGGER = LoggerFactory.getLogger(FileService.class);

	@Value("${folder.root}")
	private String rootFolder;

	@Value("${folder.local.decoded}")
	private String localDecodedFolder;

	@Value("${folder.local.encoded}")
	private String localEncodedFolder;

	@Value("${folder.local.global}")
	private String localGlobalFolder;

	@Value("${folder.remote.decoded}")
	private String remoteDecodedFolder;

	@Value("${folder.remote.encoded}")
	private String remoteEncodedFolder;

	@Value("${encfs.file}")
	private String encfsFile;

	@Value("${encfs.password}")
	private String encfsPassword;

	public FolderDto getFolderContent(String login, String folder, boolean withFile) throws ServiceException {
		Path globalRootFolder = Paths.get(rootFolder).resolve(login).resolve(localGlobalFolder);
		Path localRootFolder = Paths.get(rootFolder).resolve(login).resolve(localDecodedFolder);
		Path remoteRootFolder = Paths.get(rootFolder).resolve(login).resolve(remoteDecodedFolder);

		Path folderToScan = getFileInGlobalFolder(login, folder);

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

	public Path getFileInGlobalFolder(String login, String path) throws ServiceException {
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
		Path remotePath = Paths.get(rootFolder).resolve(login).resolve(remoteDecodedFolder);
		Path remoteFile = remotePath.resolve(path);

		if (Files.exists(localFile)) {
			LOGGER.debug("File {} exist in local storage {}", path, localFile);
			try {
				Files.delete(localFile);
			} catch (IOException e) {
				throw new ServiceException("Unable to delete {}", localFile);
			}
		}

		if (Files.exists(remoteFile)) {
			LOGGER.debug("File {} exist in remote storage {}", path, remoteFile);

			String encodedPath = getEncodedPath(remotePath, path);

			Path remoteEncodedPath = Paths.get(rootFolder).resolve(login).resolve(remoteEncodedFolder).resolve(encodedPath);
			if (Files.exists(remoteEncodedPath)) {
				LOGGER.debug("Encoded path {} found, so delete it on ACD", remoteEncodedPath);
				CommandLine cl = new CommandLine("acd_cli");
				cl.addArgument("trash");
				cl.addArgument(encodedPath);

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
	}

	private String getEncodedPath(Path rootPath, String path) throws ServiceException {
		LOGGER.debug("Get encoded path for {} on {}", path, rootPath);

		CommandLine cl = new CommandLine("encfsctl");
		cl.addArgument("encode");
		cl.addArgument(rootPath.toAbsolutePath().toString());
		cl.addArgument(path);

		LOGGER.debug("Execute : {}", cl.toString());

		Map<String, String> env = new HashMap<>();
		env.put("ENCFS6_CONFIG", encfsFile);
		try (OutputStream os = new ByteArrayOutputStream(); InputStream is = new ByteArrayInputStream(encfsPassword.getBytes());) {
			DefaultExecutor executor = new DefaultExecutor();
			ExecuteStreamHandler streamHandler = new PumpStreamHandler(os, os, is);
			executor.setStreamHandler(streamHandler);
			LOGGER.debug("Execution return code : {}", executor.execute(cl, env));

			String encodedPath = os.toString();
			LOGGER.debug("Encoded path for {} is {}", path, encodedPath);
			return encodedPath;
		} catch (ExecuteException e) {
			throw new ServiceException("Unable to retrieve encoded path", e);
		} catch (IOException e) {
			throw new ServiceException("Unable to retrieve encoded path", e);
		}
	}

	public void saveFiles(String login, String path, MultipartFile[] files) throws ServiceException {
		if (ArrayUtils.isNotEmpty(files)) {
			for (MultipartFile file : files) {
				Path destPath = null;
				try {
					destPath = getFileInGlobalFolder(login, path).resolve(file.getOriginalFilename());
				} catch (ServiceException e) {
					throw new ServiceException("Unable to retrieve path {}", path);
				}
				try {
					LOGGER.debug("Save file to local path {}", destPath);
					Files.copy(file.getInputStream(), destPath, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					throw new ServiceException("Unable to upload file {}", destPath);
				}

				saveToACD(login, destPath);
			}
		}
	}

	@Async
	private void saveToACD(String login, Path fileToSave) {
		Path globalRootFolder = Paths.get(rootFolder).resolve(login).resolve(localGlobalFolder);
		Path localRootFolder = Paths.get(rootFolder).resolve(login).resolve(localDecodedFolder);

		String encodedPath = getEncodedPath(localRootFolder, globalRootFolder.relativize(fileToSave).toString());
		
		LOGGER.debug("");
		Path toUpload = acdEncodedPath.resolve(localEncodedPath.relativize(file)).getParent();

		CommandLine cmdLine = new CommandLine("acd_cli");
		cmdLine.addArgument("upload");
		cmdLine.addArgument(file.toAbsolutePath().toString());
		cmdLine.addArgument(toUpload.toAbsolutePath().toString());

		ByteArrayOutputStream outputStream = null;
		int nbAttempt = 0;
		while (nbAttempt < 3) {
			try {
				log(file, "Upload " + toUpload.toAbsolutePath().toString() + " attempt " + ++nbAttempt);
				DefaultExecutor executor = new DefaultExecutor();
				outputStream = new ByteArrayOutputStream();
				PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
				executor.setStreamHandler(streamHandler);
				long start = Calendar.getInstance().getTimeInMillis();
				executor.execute(cmdLine);
				log(file, outputStream.toString() + "(" + (Calendar.getInstance().getTimeInMillis() - start) / 1000 + " sec)");
				log(file, "Delete " + file.getFileName().toString());
				Files.delete(file);
				break;
			} catch (Exception e) {
				log(file, e.getMessage());
			} finally {
				IOUtils.closeQuietly(outputStream);
			}
		}
	}
}
