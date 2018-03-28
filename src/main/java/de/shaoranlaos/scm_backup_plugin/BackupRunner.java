package de.shaoranlaos.scm_backup_plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitPacket;
import org.tmatesoft.svn.core.wc.SVNRevision;

import sonia.scm.repository.SvnConfig;
import sonia.scm.store.Store;
import sonia.scm.store.StoreFactory;

public class BackupRunner implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(BackupRunner.class);

	private BackupContext context;
	private Store<SvnConfig> scmConfigStore;

	public BackupRunner(StoreFactory store, BackupContext context) {
		this.context = context;
		scmConfigStore = store.getStore(SvnConfig.class, "svn");
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
		ISVNAuthenticationManager auth = BasicAuthenticationManager.newInstance(remoteSvnUser, remoteSvnPassword.toCharArray());
		SVNClientManager manager = SVNClientManager.newInstance(opts, auth);
		File baseRepoDir = scmConfigStore.get().getRepositoryDirectory();
		File localBackupDir = new File(localBackupPath);

		if (!localBackupDir.exists() || Arrays.binarySearch(localBackupDir.list(), ".svn") < 0) {
			// create localBackupWorkingCopy
			LOG.info("create backup working copy");
			try {
				manager.getUpdateClient().doCheckout(SVNURL.parseURIEncoded(remoteSvnServer), localBackupDir, SVNRevision.UNDEFINED, SVNRevision.HEAD, SVNDepth.INFINITY, false);
			} catch (SVNException e) {
				LOG.error("Error during first checkout of remote Repository.", e);
				return;
			}
		} else {
			LOG.info("Delete old backupfiles.");
			for (File delFile : localBackupDir.listFiles()) {
				if (!delFile.getName().equals(".svn")) {
					delFile.delete();
				}
			}
		}

		LOG.info("Dump Repositories into {}", localBackupDir);
		Set<File> dumpFiles = new HashSet<>();
		for (String repo : config.getExistingRemoteRepos()) {
			try (OutputStream os = new FileOutputStream(new File(localBackupDir, repo))) {
				manager.getAdminClient().doDump(new File(baseRepoDir, repo), os, SVNRevision.create(0), SVNRevision.HEAD, false, false);
				dumpFiles.add(new File(localBackupDir, repo));
				LOG.info("dumped {}", repo);
			} catch (SVNException e) {
				LOG.error("Error during dump", e);
			} catch (IOException e1) {
				LOG.error("Error during dumpFile creation/opening", e1);
			}
		}

		try {
			manager.getWCClient().doAdd(dumpFiles.toArray(new File[dumpFiles.size()]), true, false, true, SVNDepth.INFINITY, true, true, true);
			LOG.info("Added new dump files");
		} catch (SVNException e) {
			LOG.error("Error during add", e);
		}

		try {
			SVNCommitPacket commitPacket = manager.getCommitClient().doCollectCommitItems(new File[] {localBackupDir}, false, true, SVNDepth.INFINITY, null);
			LOG.info("{}", commitPacket);
			if (commitPacket != SVNCommitPacket.EMPTY) { 
				SVNCommitInfo info = manager.getCommitClient().doCommit(commitPacket, false, "" + new Date());
				LOG.info("{}", info);
			} else {
				LOG.info("nothing to commit");
			}
		} catch (SVNException e) {
			LOG.error("Error during commit", e);
		}
	}
}
