package org.mian.gitnex.api.clients;

import java.util.List;
import org.mian.gitnex.api.models.contents.RepoGetContentsList;
import org.mian.gitnex.api.models.topics.Topics;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
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

	@GET("repos/{owner}/{repo}/topics") // get list of topics for a repo
	Call<Topics> getRepoTopics(
			@Path("owner") String owner,
			@Path("repo") String repo,
			@Query("page") int page,
			@Query("limit") int limit);

	@PUT("repos/{owner}/{repo}/topics/{topic}") // add a new topic for a repo
	Call<Void> addRepoTopic(
			@Path("owner") String owner, @Path("repo") String repo, @Path("topic") String topic);

	@DELETE("repos/{owner}/{repo}/topics/{topic}") // delete a repo topic
	Call<Void> deleteRepoTopic(
			@Path("owner") String owner, @Path("repo") String repo, @Path("topic") String topic);
}
