package org.optimus.amazon.backup.server.resources;

import org.optimus.amazon.backup.server.services.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class AccountResource extends AbstractResource {

	private final static Logger LOGGER = LoggerFactory.getLogger(AccountResource.class);

	@Autowired
	private AccountService accountService;

	@RequestMapping(method = RequestMethod.GET)
	public String checkAccount() throws Exception {
		String currentUser = getUser();

		LOGGER.info("checkAccount for {}", currentUser);

		if (accountService.checkUserFolders(currentUser)) {
			return currentUser;
		}

		return "ERROR";
	}

}