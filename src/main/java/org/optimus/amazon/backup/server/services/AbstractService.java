package org.optimus.amazon.backup.server.services;

import org.springframework.beans.factory.annotation.Value;

public class AbstractService {

	@Value("${folder.local.root}")
	protected String localRootFolder;

	@Value("folder.remote.root")
	protected String remoteRootFolder;

	@Value("${encfs.file}")
	protected String encfsFile;

	@Value("${encfs.password}")
	protected String encfsPassword;

	@Value("${folder.local.encoded}")
	protected String localEncodedFolder;

	@Value("${folder.local.decoded}")
	protected String localDecodedFolder;

	@Value("${folder.local.global}")
	protected String localGlobalFolder;

	@Value("${folder.remote.encoded}")
	protected String remoteEncodedFolder;

	@Value("${folder.remote.decoded}")
	protected String remoteDecodedFolder;

}
