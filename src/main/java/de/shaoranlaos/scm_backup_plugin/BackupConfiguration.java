package de.shaoranlaos.scm_backup_plugin;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import sonia.scm.security.CipherHandler;

@XmlRootElement(name = "backup-to-svn")
@XmlAccessorType(XmlAccessType.NONE)
public class BackupConfiguration {

	@Inject
	private CipherHandler cipher;

	@XmlElement(name = "active", defaultValue = "false", type = Boolean.class)
	private Boolean active = false;
	
	@XmlElement(name = "remoteRepos", type = HashSet.class)
	private Set<String> existingRemoteRepos = new HashSet<>();

	@XmlElement(name = "remoteSvnServer", required = true)
	private String remoteSvnServer;

	@XmlElement(name = "remoteSvnUser", required = true)
	private String remoteSvnUser;

	@XmlElement(name = "remoteSvnPassword", required = true)
	private String remoteSvnPassword;

	@XmlElement(name = "backupRate", defaultValue = "60", type = Integer.class)
	private Integer backupRate = 60;

	@XmlElement(name = "localBackupPath", required = true)
	private String localBackupPath;

	public Set<String> getExistingRemoteRepos() {
		return existingRemoteRepos;
	}

	public void setExistingRemoteRepos(Set<String> existingRemoteRepos) {
		this.existingRemoteRepos = existingRemoteRepos;
	}

	public String getRemoteSvnServer() {
		return remoteSvnServer;
	}

	public void setRemoteSvnServer(String remoteSvnServer) {
		this.remoteSvnServer = remoteSvnServer;
	}

	public String getRemoteSvnUser() {
		return remoteSvnUser;
	}

	public void setRemoteSvnUser(String remoteSvnUser) {
		this.remoteSvnUser = remoteSvnUser;
	}

	public String getRemoteSvnPassword() {
		return cipher.decode(remoteSvnPassword);
	}

	public void setRemoteSvnPassword(String remoteSvnPassword) {
		this.remoteSvnPassword = cipher.encode(remoteSvnPassword);
	}

	public Integer getBackupRate() {
		return backupRate;
	}

	public void setBackupRate(Integer backupRate) {
		this.backupRate = backupRate;
	}

	public String getLocalBackupPath() {
		return localBackupPath;
	}

	public void setLocalBackupPath(String localBackupPath) {
		this.localBackupPath = localBackupPath;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
}
