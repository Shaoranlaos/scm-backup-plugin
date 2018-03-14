package de.shaoranlaos.scm_backup_plugin;

import com.google.inject.AbstractModule;

public class BackupModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(BackupContext.class);
	}

}
