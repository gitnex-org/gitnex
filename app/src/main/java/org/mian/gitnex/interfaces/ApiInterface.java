package org.mian.gitnex.interfaces;

import com.google.gson.JsonElement;
import org.mian.gitnex.models.AddEmail;
import org.mian.gitnex.models.Branches;
import org.mian.gitnex.models.ExploreRepositories;
import org.mian.gitnex.models.Files;
import org.mian.gitnex.models.NewFile;
import org.mian.gitnex.models.UpdateIssueAssignee;
import org.mian.gitnex.models.UpdateIssueState;
import org.mian.gitnex.models.Collaborators;
import org.mian.gitnex.models.CreateIssue;
import org.mian.gitnex.models.CreateLabel;
import org.mian.gitnex.models.Emails;
import org.mian.gitnex.models.GiteaVersion;
import org.mian.gitnex.models.IssueComments;
import org.mian.gitnex.models.Issues;
import org.mian.gitnex.models.Labels;
import org.mian.gitnex.models.Milestones;
import org.mian.gitnex.models.OrgOwner;
import org.mian.gitnex.models.Organization;
import org.mian.gitnex.models.OrganizationRepository;
import org.mian.gitnex.models.Permission;
import org.mian.gitnex.models.Releases;
import org.mian.gitnex.models.Teams;
import org.mian.gitnex.models.UserInfo;
import org.mian.gitnex.models.UserOrganizations;
import org.mian.gitnex.models.UserRepositories;
import org.mian.gitnex.models.UserSearch;
import org.mian.gitnex.models.UserTokens;
import org.mian.gitnex.models.WatchRepository;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Author M M Arif
 */

public interface ApiInterface {

    @GET("version") // gitea version API
    Call<GiteaVersion> getGiteaVersion(@Header("Authorization") String token);

    @GET("user") // username, full name, email
    Call<UserInfo> getUserInfo(@Header("Authorization") String token);

    @GET("users/{username}/tokens") // get user token
    Call<List<UserTokens>> getUserTokens(@Header("Authorization") String authorization, @Path("username") String loginUid);

    @GET("users/{username}/tokens") // get user token with 2fa otp
    Call<List<UserTokens>> getUserTokensWithOTP(@Header("Authorization") String authorization, @Header("X-Gitea-OTP") int loginOTP, @Path("username") String loginUid);

    @POST("users/{username}/tokens") // create new token
    Call<UserTokens> createNewToken(@Header("Authorization") String authorization, @Path("username") String loginUid, @Body UserTokens jsonStr);

    @POST("users/{username}/tokens") // create new token with 2fa otp
    Call<UserTokens> createNewTokenWithOTP(@Header("Authorization") String authorization, @Header("X-Gitea-OTP") int loginOTP, @Path("username") String loginUid, @Body UserTokens jsonStr);

    @GET("user/orgs") // get user organizations
    Call<List<UserOrganizations>> getUserOrgs(@Header("Authorization") String token);

    @POST("orgs") // create new organization
    Call<UserOrganizations> createNewOrganization(@Header("Authorization") String token, @Body UserOrganizations jsonStr);

    @POST("org/{org}/repos") // create new repository under org
    Call<OrganizationRepository> createNewUserOrgRepository(@Header("Authorization") String token, @Path("org") String orgName, @Body OrganizationRepository jsonStr);

    @GET("user/orgs") // get user organizations
    Call<List<OrgOwner>> getOrgOwners(@Header("Authorization") String token);

    @GET("user/repos") // get user repositories
    Call<List<UserRepositories>> getUserRepositories(@Header("Authorization") String token);

    @POST("user/repos") // create new repository
    Call<OrganizationRepository> createNewUserRepository(@Header("Authorization") String token, @Body OrganizationRepository jsonStr);

    @GET("repos/{owner}/{repo}") // get repo information
    Call<UserRepositories> getUserRepository(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName);

    @GET("repos/{owner}/{repo}/issues") // get issues by repo
    Call<List<Issues>> getIssues(@Header("Authorization") String token, @Path("owner") String owner, @Path("repo") String repo, @Query("page") int page);

    @GET("repos/{owner}/{repo}/issues") // get closed issues by repo
    Call<List<Issues>> getClosedIssues(@Header("Authorization") String token, @Path("owner") String owner, @Path("repo") String repo, @Query("page") int page, @Query("state") String issueState);

    @GET("repos/{owner}/{repo}/issues/{index}") // get issue by id
    Call<Issues> getIssueByIndex(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Path("index") int issueIndex);

    @GET("repos/{owner}/{repo}/issues/{index}/comments") // get issue comments
    Call<List<IssueComments>> getIssueComments(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Path("index") int issueIndex);

    @POST("repos/{owner}/{repo}/issues/{index}/comments") // reply to issue
    Call<Issues> replyCommentToIssue(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Path("index") int issueIndex, @Body Issues jsonStr);

    @GET("repos/{owner}/{repo}/milestones") // get milestones by repo
    Call<List<Milestones>> getMilestones(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Query("state") String state);

    @GET("repos/{owner}/{repo}/branches") // get branches
    Call<List<Branches>> getBranches(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName);

    @GET("repos/{owner}/{repo}/releases") // get releases
    Call<List<Releases>> getReleases(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName);

    @GET("repos/{owner}/{repo}/collaborators") // get collaborators list
    Call<List<Collaborators>> getCollaborators(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName);

    @POST("repos/{owner}/{repo}/milestones") // create new milestone
    Call<Milestones> createMilestone(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Body Milestones jsonStr);

    @POST("repos/{owner}/{repo}/issues") // create new issue
    Call<JsonElement> createNewIssue(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Body CreateIssue jsonStr);

    @GET("repos/{owner}/{repo}/labels") // get labels list
    Call<List<Labels>> getlabels(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName);

    @GET("users/{username}/repos") // get current logged in user repositories
    Call<List<UserRepositories>> getCurrentUserRepositories(@Header("Authorization") String token, @Path("username") String username);

    @POST("repos/{owner}/{repo}/labels") // create label
    Call<CreateLabel> createLabel(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Body CreateLabel jsonStr);

    @DELETE("repos/{owner}/{repo}/labels/{index}") // delete a label
    Call<Labels> deleteLabel(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Path("index") int labelIndex);

    @PATCH("repos/{owner}/{repo}/labels/{index}") // update / patch a label
    Call<CreateLabel> patchLabel(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Path("index") int labelIndex, @Body CreateLabel jsonStr);

    @GET("user/starred") // get user starred repositories
    Call<List<UserRepositories>> getUserStarredRepos(@Header("Authorization") String token);

    @GET("orgs/{orgName}/repos") // get repositories by org
    Call<List<UserRepositories>> getReposByOrg(@Header("Authorization") String token, @Path("orgName") String orgName);

    @GET("orgs/{orgName}/teams") // get teams by org
    Call<List<Teams>> getTeamsByOrg(@Header("Authorization") String token, @Path("orgName") String orgName);

    @GET("orgs/{orgName}/members") // get members by org
    Call<List<UserInfo>> getMembersByOrg(@Header("Authorization") String token, @Path("orgName") String orgName);

    @GET("teams/{teamIndex}/members") // get team members by org
    Call<List<UserInfo>> getTeamMembersByOrg(@Header("Authorization") String token, @Path("teamIndex") int teamIndex);

    @POST("orgs/{orgName}/teams") // create new team
    Call<Teams> createTeamsByOrg(@Header("Authorization") String token, @Path("orgName") String orgName, @Body Teams jsonStr);

    @GET("users/search") // search users
    Call<UserSearch> getUserBySearch(@Header("Authorization") String token, @Query("q") String searchKeyword, @Query("limit") int limit);

    @GET("repos/{owner}/{repo}/collaborators/{collaborator}") // check collaborator in repo
    Call<Collaborators> checkRepoCollaborator(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Path("collaborator") String repoCollaborator);

    @DELETE("repos/{owner}/{repo}/collaborators/{username}") // delete a collaborator from repository
    Call<Collaborators> deleteCollaborator(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Path("username") String username);

    @PUT("repos/{owner}/{repo}/collaborators/{username}") // add a collaborator to repository
    Call<Permission> addCollaborator(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Path("username") String username, @Body Permission jsonStr);

    @PATCH("repos/{owner}/{repo}/issues/comments/{commentId}") // edit a comment
    Call<IssueComments> patchIssueComment(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Path("commentId") int commentId, @Body IssueComments jsonStr);

    @GET("user/followers") // get user followers
    Call<List<UserInfo>> getFollowers(@Header("Authorization") String token);

    @GET("user/following") // get following
    Call<List<UserInfo>> getFollowing(@Header("Authorization") String token);

    @POST("user/emails") // add new email
    Call<JsonElement> addNewEmail(@Header("Authorization") String token, @Body AddEmail jsonStr);

    @GET("user/emails") // get user emails
    Call<List<Emails>> getUserEmails(@Header("Authorization") String token);

    @GET("repos/{owner}/{repo}/issues/{index}/labels") // get issue labels
    Call<List<Labels>> getIssueLabels(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Path("index") int issueIndex);

    @PUT("repos/{owner}/{repo}/issues/{index}/labels") // replace an issue's labels
    Call<JsonElement> updateIssueLabels(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Path("index") int issueIndex, @Body Labels jsonStr);

    @GET("repos/{owner}/{repo}/raw/{filename}") // get file contents
    Call<String> getFileContents(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Path("filename") String filename);

    @POST("admin/users") // create new user
    Call<UserInfo> createNewUser(@Header("Authorization") String token, @Body UserInfo jsonStr);

    @PATCH("repos/{owner}/{repo}/issues/{issueIndex}") // patch issue data
    Call<JsonElement> patchIssue(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Path("issueIndex") int issueIndex, @Body CreateIssue jsonStr);

    @GET("orgs/{orgName}") // get an organization
    Call<Organization> getOrganization(@Header("Authorization") String token, @Path("orgName") String orgName);

    @PATCH("repos/{owner}/{repo}/issues/{issueIndex}") // close / reopen issue
    Call<JsonElement> closeReopenIssue(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Path("issueIndex") int issueIndex, @Body UpdateIssueState jsonStr);

    @POST("repos/{owner}/{repo}/releases") // create new release
    Call<Releases> createNewRelease(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Body Releases jsonStr);

    @PATCH("repos/{owner}/{repo}/issues/{issueIndex}") // patch issue assignees
    Call<JsonElement> patchIssueAssignee(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Path("issueIndex") int issueIndex, @Body UpdateIssueAssignee jsonStr);

    @GET("admin/users") // get all users
    Call<List<UserInfo>> adminGetUsers(@Header("Authorization") String token);

    @GET("repos/{owner}/{repo}/stargazers") // get all repo stars
    Call<List<UserInfo>> getRepoStargazers(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName);

    @GET("repos/{owner}/{repo}/subscribers") // get all repo watchers
    Call<List<UserInfo>> getRepoWatchers(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName);

    @GET("repos/search") // get all the repos which match the query string
    Call<ExploreRepositories> queryRepos(@Header("Authorization") String token, @Query("q") String searchKeyword, @Query("private") Boolean repoTypeInclude, @Query("sort") String sort, @Query("order") String order);

    @POST("repos/{owner}/{repo}/contents/{file}") // create new file
    Call<JsonElement> createNewFile(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Path("file") String fileName, @Body NewFile jsonStr);

    @GET("repos/{owner}/{repo}/contents") // get all the files and dirs of a repository
    Call<List<Files>> getFiles(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName);

    @GET("repos/{owner}/{repo}/contents/{file}") // get single file contents
    Call<Files> getSingleFileContents(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Path("file") String file);

    @GET("repos/{owner}/{repo}/contents/{fileDir}") // get all the sub files and dirs of a repository
    Call<List<Files>> getDirFiles(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName, @Path("fileDir") String fileDir);

    @GET("user/starred/{owner}/{repo}") // check star status of a repository
    Call<JsonElement> checkRepoStarStatus(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName);

    @PUT("user/starred/{owner}/{repo}") // star a repository
    Call<JsonElement> starRepository(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName);

    @DELETE("user/starred/{owner}/{repo}") // un star a repository
    Call<JsonElement> unStarRepository(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName);

    @GET("repos/{owner}/{repo}/subscription") // check watch status of a repository
    Call<WatchRepository> checkRepoWatchStatus(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName);

    @PUT("repos/{owner}/{repo}/subscription") // watch a repository
    Call<JsonElement> watchRepository(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName);

    @DELETE("repos/{owner}/{repo}/subscription") // un watch a repository
    Call<JsonElement> unWatchRepository(@Header("Authorization") String token, @Path("owner") String ownerName, @Path("repo") String repoName);
}
