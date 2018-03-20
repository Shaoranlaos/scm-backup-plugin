package de.shaoranlaos.scm_backup_plugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.security.CipherHandler;
import sonia.scm.store.Store;
import sonia.scm.store.StoreFactory;

@Singleton
public class BackupContext {

	private static final Logger LOG = LoggerFactory.getLogger(BackupRunner.class);

	private static final String STORE_NAME = "backup_configs";

	private Store<BackupConfiguration> store;
	private BackupConfiguration config;

	private ScheduledExecutorService executor;
	@Inject
	private BackupRunner backupRunner;

	@Inject
	public BackupContext(StoreFactory storeFactory, CipherHandler cipher, RepositoryManager manager) {
		CipherUtil.setCipherHandler(cipher);
		this.store = storeFactory.getStore(BackupConfiguration.class, STORE_NAME);
		
		config = store.get(); 
		if (config == null) {
			LOG.info("No Config found, Create an empty one.");
			config = new BackupConfiguration();
			for(Repository repo : manager.getAll()) {
				if (repo.getType() == "svn") {
					config.getExistingRemoteRepos().add(repo.getName());
				}
			}
			setGlobalConfiguration(config);
		}
	}

	private void createExecutor() {
		executor = Executors.newScheduledThreadPool(1);
	}
	
	private void startBackgroundTask() {
		LOG.info("Start BackupRunner as backgroundthread.");
		executor.scheduleAtFixedRate(backupRunner,
				store.get().getBackupRate(),
				store.get().getBackupRate(),
				TimeUnit.MINUTES);
	}

	public BackupConfiguration getGlobalConfiguration() {
		return store.get();
	}

	public void setGlobalConfiguration(BackupConfiguration globalConfiguration) {
		if (globalConfiguration == null) {
			throw new IllegalArgumentException("globalConfiguration can not be null!");
		}

		if (!globalConfiguration.getActive() && (config != null && config.getActive())) {
			executor.shutdown();
		} else if (globalConfiguration.getActive() && (config == null || !config.getActive())) {
			if (executor == null || executor.isShutdown()) {
				createExecutor();
				startBackgroundTask();
			}
		}

		this.config = globalConfiguration;
		storeConfig();
	}

	public void storeConfig() {
		store.set(config);
	}
}
