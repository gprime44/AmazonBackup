package org.optimus.amazon.backup.server.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.PumpStreamHandler;
import org.optimus.amazon.backup.server.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

	private final static Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

	@Value("${folder.root}")
	private String rootFolder;

	@Value("${encfs.file}")
	private String encfsFile;

	@Value("${encfs.password}")
	private String encfsPassword;

	@Value("${folder.local.encoded}")
	private String localEncodedFolder;

	@Value("${folder.local.decoded}")
	private String localDecodedFolder;

	@Value("${folder.local.global}")
	private String localGlobalFolder;

	@Value("${folder.remote.encoded}")
	private String remoteEncodedFolder;

	@Value("${folder.remote.decoded}")
	private String remoteDecodedFolder;

	public boolean checkUserFolders(String login) throws ServiceException {
		boolean ret = false;
		Path rootPath = Paths.get(rootFolder);
		if (!Files.exists(rootPath)) {
			throw new ServiceException("Folder {} doesn't exsit", rootPath);
		}

		Path userPath = rootPath.resolve(login);
		Path localEncodedPath = userPath.resolve(localEncodedFolder);
		createDirectory(localEncodedPath);

		Path localDecodedPath = userPath.resolve(localDecodedFolder);
		createDirectory(localDecodedPath);

		Path remoteEncodedPath = userPath.resolve(remoteEncodedFolder);
		createDirectory(remoteEncodedPath);

		Path remoteDecodedPath = userPath.resolve(remoteDecodedFolder);
		createDirectory(remoteDecodedPath);

		Path localGlobalPath = userPath.resolve(localGlobalFolder);
		createDirectory(localGlobalPath);

		Set<Path> fileStores = new HashSet<>();
		LOGGER.debug("Search file stores ...");
		for (FileStore fileStore : FileSystems.getDefault().getFileStores()) {
			LOGGER.debug(" ... {}", fileStore.name());
			fileStores.add(Paths.get(fileStore.name()));
		}

		if (!fileStores.contains(localDecodedPath.toAbsolutePath().toString())) {
			ret = true;
			createMountPoint(localEncodedPath, localDecodedPath);
		}

		if (!fileStores.contains(remoteEncodedPath.toAbsolutePath().toString())) {
			ret = true;
			createRemoteMountPoint(login, remoteEncodedPath);
		}

		if (!fileStores.contains(remoteDecodedPath.toAbsolutePath().toString())) {
			ret = true;
			createMountPoint(remoteEncodedPath, remoteDecodedPath);
		}

		if (!fileStores.contains(localGlobalPath.toAbsolutePath().toString())) {
			ret = true;
			createGlobalMountPoint(localDecodedPath, remoteDecodedPath, localGlobalPath);
		}

		return ret;
	}

	private void createGlobalMountPoint(Path localDecodedPath, Path remoteDecodedPath, Path localGlobalPath) {
		LOGGER.info("Create global mount point from {} and {} to {}", localDecodedPath, remoteDecodedPath, localGlobalPath);
		CommandLine cl = new CommandLine("unionfs-fuse");
		cl.addArgument("-o");
		cl.addArgument("cow");
		cl.addArgument("-o");
		cl.addArgument("allow_root");
		cl.addArgument(localDecodedPath.toAbsolutePath().toString() + "=RW:" + remoteDecodedPath.toAbsolutePath().toString() + "=RO");
		cl.addArgument(localGlobalPath.toAbsolutePath().toString());

		LOGGER.debug("Execute : {}", cl.toString());
		
		try {
			LOGGER.info("Execution return : {}", new DefaultExecutor().execute(cl));
		} catch (ExecuteException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void createRemoteMountPoint(String login, Path remoteEncodedPath) {
		LOGGER.info("Create remote mount point from {} to {}", login, remoteEncodedPath);
		CommandLine cl = new CommandLine("acd_cli");
		cl.addArgument("-nl");
		cl.addArgument("mount");
		cl.addArgument("--modules='subdir,subdir=/" + login + "'");
		cl.addArgument("--allow-root");
		cl.addArgument(remoteEncodedPath.toAbsolutePath().toString());

		LOGGER.debug("Execute : {}", cl.toString());
		
		try {
			LOGGER.info("Execution return : {}", new DefaultExecutor().execute(cl));
		} catch (ExecuteException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void createMountPoint(Path encodedFolder, Path decodedFolder) {
		LOGGER.info("Create mount point from {} to {}", encodedFolder, decodedFolder);
		CommandLine cl = new CommandLine("encfs");
		cl.addArgument("-o");
		cl.addArgument("allow_root");
		cl.addArgument(encodedFolder.toAbsolutePath().toString());
		cl.addArgument(decodedFolder.toAbsolutePath().toString());

		LOGGER.debug("Execute : {}", cl.toString());
		
		Map<String, String> env = new HashMap<>();
		env.put("ENCFS6_CONFIG", encfsFile);
		try (OutputStream os = new ByteArrayOutputStream(); InputStream is = new ByteArrayInputStream(encfsPassword.getBytes());) {
			DefaultExecutor executor = new DefaultExecutor();
			ExecuteStreamHandler streamHandler = new PumpStreamHandler(os, os, is);
			executor.setStreamHandler(streamHandler);
			LOGGER.info("Execution return : {} - {}", executor.execute(cl, env), os.toString());
		} catch (ExecuteException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void createDirectory(Path localEncodedPath) {
		if (!Files.exists(localEncodedPath)) {
			try {
				Files.createDirectories(localEncodedPath);
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}
}
