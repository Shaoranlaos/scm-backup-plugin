package de.shaoranlaos.scm_backup_plugin;

import java.util.Collection;
import java.util.Collections;

//import sonia.scm.repository.Changeset;
import sonia.scm.repository.RepositoryHook;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;

public class CommitSyncListener implements RepositoryHook {

	@Override
	public void onEvent(RepositoryHookEvent event) {
//		String repoName = event.getRepository().getName();
//		for (Changeset commit : event.getChangesets()) {
//			commit.
//		}
	}

	@Override
	public Collection<RepositoryHookType> getTypes() {
		return Collections.singleton(RepositoryHookType.POST_RECEIVE);
	}

	@Override
	public boolean isAsync() {
		return true;
	}

}
