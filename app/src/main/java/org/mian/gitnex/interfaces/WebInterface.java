package org.mian.gitnex.interfaces;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Author M M Arif
 */

public interface WebInterface {

    @GET("{owner}/{repo}/pulls/{index}.diff") // get pull diff file contents
    Call<ResponseBody> getPullDiffContent(@Path("owner") String owner, @Path("repo") String repo, @Path("index") String pullIndex);

}
