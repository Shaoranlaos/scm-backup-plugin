package de.shaoranlaos.scm_backup_plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackupRunner implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(BackupRunner.class);

	@Override
	public void run() {
		LOG.info("Execute Backup");
	}

}
