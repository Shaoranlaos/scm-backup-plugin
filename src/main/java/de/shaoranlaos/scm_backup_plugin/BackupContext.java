package de.shaoranlaos.scm_backup_plugin;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.store.Store;
import sonia.scm.store.StoreFactory;

@Singleton
public class BackupContext {

	private static final String STORE_NAME = "backup_script";

	private Store<BackupConfiguration> store;
	private BackupConfiguration globalConfiguration;

	@Inject
	public BackupContext(StoreFactory storeFactory) {
		this.store = storeFactory.getStore(BackupConfiguration.class, STORE_NAME);
		globalConfiguration = store.get();

		if (globalConfiguration == null) {
			setGlobalConfiguration(new BackupConfiguration());
		}
	}

	public BackupConfiguration getGlobalConfiguration() {
		return globalConfiguration;
	}

	public void setGlobalConfiguration(BackupConfiguration globalConfiguration) {
		this.globalConfiguration = globalConfiguration;
		store.set(globalConfiguration);
	}
}
