package de.shaoranlaos.scm_backup_plugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.store.Store;
import sonia.scm.store.StoreFactory;

@Singleton
public class BackupContext {

	private static final Logger LOG = LoggerFactory.getLogger(BackupRunner.class);

	private static final String STORE_NAME = "backup_configs";

	private Store<BackupConfiguration> store;
	private ScheduledExecutorService executor;

	@Inject
	public BackupContext(StoreFactory storeFactory) {
		this.store = storeFactory.getStore(BackupConfiguration.class, STORE_NAME);
		

		if (store.get() == null) {
			LOG.info("No Config found, Create an empty one.");
			setGlobalConfiguration(new BackupConfiguration());
		}

		executor = Executors.newScheduledThreadPool(1);
		if (getGlobalConfiguration().getActive()) {
			LOG.info("Start BackupRunner as backgroundthread.");
			startBackgroundTask();
		}
	}
	
	private void startBackgroundTask() {
		executor.scheduleAtFixedRate(new BackupRunner(),
				store.get().getBackupRate(),
				store.get().getBackupRate(),
				TimeUnit.MINUTES);
	}

	public BackupConfiguration getGlobalConfiguration() {
		return store.get();
	}

	public void setGlobalConfiguration(BackupConfiguration globalConfiguration) {
		if (!globalConfiguration.getActive()) {
			executor.shutdown();
		} else {
			if (executor == null || executor.isShutdown()) {
				executor = Executors.newScheduledThreadPool(1);
				startBackgroundTask();
			}
		}
		store.set(globalConfiguration);
	}
}
