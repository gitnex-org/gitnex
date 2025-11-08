package org.mian.gitnex.api.clients;

import java.util.List;
import org.mian.gitnex.api.models.contents.RepoGetContentsList;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * @author mmarif
 */
public interface ApiInterface {

	@GET("repos/{owner}/{repo}/contents")
	Call<List<RepoGetContentsList>> getRepoContents(
			@Path("owner") String owner, @Path("repo") String repo, @Query("ref") String ref);

	@GET("repos/{owner}/{repo}/contents/{path}")
	Call<List<RepoGetContentsList>> getRepoContents(
			@Path("owner") String owner,
			@Path("repo") String repo,
			@Path("path") String path,
			@Query("ref") String ref);
}
