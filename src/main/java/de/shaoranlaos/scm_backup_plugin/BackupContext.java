package de.shaoranlaos.scm_backup_plugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
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
	private BackupRunner backupRunner;

	@Inject
	public BackupContext(StoreFactory storeFactory, CipherHandler cipher, RepositoryManager manager) {
		CipherUtil.setCipherHandler(cipher);
		this.store = storeFactory.getStore(BackupConfiguration.class, STORE_NAME);
		this.backupRunner = new BackupRunner(storeFactory, this);
		
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
		} else if (config.getActive()) {
			startBackgroundTask();
		}
		LOG.info("Plugin Backup-to-svn started.");
	}

	private void createExecutor() {
		executor = Executors.newScheduledThreadPool(1);
	}
	
	private void startBackgroundTask() {
		LOG.info("Start BackupRunner as backgroundthread.");
		if (executor == null || executor.isShutdown()) {
			createExecutor();
		}

		final ScheduledFuture<?> future = executor.scheduleAtFixedRate(backupRunner,
				config.getBackupRate(),
				config.getBackupRate(),
				TimeUnit.MINUTES);
		Executors.newSingleThreadExecutor().execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					LOG.error("{}",future.get());
				} catch (Exception e) {
					LOG.error("Exectption during Task run", e);
				}
			}
		});
	}

	public BackupConfiguration getGlobalConfiguration() {
		return store.get();
	}

	private void startBackgroundTaskConditional(BackupConfiguration oldConfig) {
		LOG.info("newConfig: {}", config);
		LOG.info("oldConfig: {}", oldConfig);
		if (!config.getActive() && (oldConfig != null && oldConfig.getActive())) {
			executor.shutdown();
		} else if (config.getActive() && (oldConfig == null || !oldConfig.getActive())) {
			startBackgroundTask();
		} else if (oldConfig.getActive() && config.getBackupRate() != oldConfig.getBackupRate()) {
			executor.shutdown();
			startBackgroundTask();
		}
	}

	public void setGlobalConfiguration(BackupConfiguration globalConfiguration) {
		if (globalConfiguration == null) {
			throw new IllegalArgumentException("globalConfiguration can not be null!");
		}
		BackupConfiguration cloneConfig = config.cloneWithoutSet();
		this.config = globalConfiguration;
		storeConfig();
		startBackgroundTaskConditional(cloneConfig);
		LOG.info("Saved new config: {}", this.config);
	}

	public void storeConfig() {
		store.set(config);
	}
}
