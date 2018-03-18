package de.shaoranlaos.scm_backup_plugin;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Singleton
@Path("config/backup/to-svn")
public class BackupConfigurationResource {

	@Inject
	private BackupContext context;

	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public BackupConfiguration getConfig()
	{
		return context.getGlobalConfiguration().cloneWithoutSet();
	}

	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response setConfig(@Context UriInfo uriInfo, BackupConfiguration config)
	        throws IOException
	{
		config.setExistingRemoteRepos(context.getGlobalConfiguration().getExistingRemoteRepos());
		context.setGlobalConfiguration(config);

		return Response.created(uriInfo.getRequestUri()).build();
	}
}
