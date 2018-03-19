package de.shaoranlaos.scm_backup_plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

import sonia.scm.repository.SimpleRepositoryConfig;
import sonia.scm.store.Store;
import sonia.scm.store.StoreFactory;

@Singleton
public class BackupRunner implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(BackupRunner.class);

	@Inject
	private BackupContext context;
	private Store<SimpleRepositoryConfig> scmConfigStore;

	@Inject
	public BackupRunner(StoreFactory store) {
		scmConfigStore = store.getStore(SimpleRepositoryConfig.class, "svn");
	}
	
	@Override
	public void run() {
		BackupConfiguration config = context.getGlobalConfiguration();
		LOG.info("Execute Backup with config: {}", config.toString());

		String localBackupPath = config.getLocalBackupPath();
		String remoteSvnServer = config.getRemoteSvnServer();
		String remoteSvnUser = config.getRemoteSvnUser();
		String remoteSvnPassword = config.getRemoteSvnPassword();


		DefaultSVNOptions opts = new DefaultSVNOptions();
		opts.setAuthStorageEnabled(false);
		SVNClientManager manager = SVNClientManager.newInstance(opts,
				BasicAuthenticationManager.newInstance(
						remoteSvnUser,
						remoteSvnPassword.toCharArray()));
		File baseRepoDir = scmConfigStore.get().getRepositoryDirectory();
		File localBackupDir = new File(localBackupPath);

		if (!localBackupDir.exists()) {
			// create localBackupWorkingCopy
			try {
				manager.getUpdateClient().doCheckout(SVNURL.parseURIEncoded(remoteSvnServer), localBackupDir, SVNRevision.UNDEFINED, SVNRevision.HEAD, SVNDepth.INFINITY, false);
			} catch (SVNException e) {
				LOG.error("Error during first checkout of remote Repository.", e);
			}
		} else {
			for (File delFile : localBackupDir.listFiles()) {
				delFile.delete();
			}
		}

		Set<String> dumpFiles = new HashSet<>();
		for (String repo : config.getExistingRemoteRepos()) {
			try (OutputStream os = new FileOutputStream(new File(localBackupDir, repo))) {
				manager.getAdminClient().doDump(new File(baseRepoDir, repo), os, SVNRevision.create(0), SVNRevision.HEAD, false, false);
				dumpFiles.add(repo);
			} catch (SVNException e) {
				LOG.error("Error during dump", e);
			} catch (IOException e1) {
				LOG.error("Error during dumpFile creation/opening", e1);
			}
		}

		try {
			manager.getWCClient().doAdd(dumpFiles.toArray(new File[dumpFiles.size()]), true, true, true, SVNDepth.INFINITY, true, true, true);
		} catch (SVNException e) {
			LOG.error("Error during add", e);
		}

		try {
			manager.getCommitClient().doCollectCommitItems(new File[] {localBackupDir}, false, true, SVNDepth.INFINITY, null);
		} catch (SVNException e) {
			LOG.error("Error during commit", e);
		}
	}
}
