package de.shaoranlaos.scm_backup_plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import sonia.scm.HandlerEvent;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryListener;

@Extension
public class RepositoryCreationListener implements RepositoryListener {

	/**
	* the logger for RepositoryWebHook
	*/
	private static final Logger LOG = LoggerFactory.getLogger(RepositoryCreationListener.class);

	///////////////////////////////////////////////////////////////////////////

	private BackupContext context;

	@Inject
	public RepositoryCreationListener(BackupContext context) {
		this.context = context;
	}

	@Override
	public void onEvent(Repository repo, HandlerEvent event) {
		LOG.info("Logevent received for Repository {}: {}", repo.getName(), event);

		switch (event) {
		case CREATE:
			
			break;
		case DELETE:
			break;
		default:
			break;
		}
	}
}
