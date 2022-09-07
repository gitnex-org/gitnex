package org.mian.gitnex.database.api;

import android.content.Context;
import androidx.lifecycle.LiveData;
import org.mian.gitnex.database.dao.RepositoriesDao;
import org.mian.gitnex.database.models.Repository;
import java.util.List;

/**
 * @author M M Arif
 */

public class RepositoriesApi extends BaseApi {

	private final RepositoriesDao repositoriesDao;

	RepositoriesApi(Context context) {
		super(context);
		repositoriesDao = gitnexDatabase.repositoriesDao();
	}

	public long insertRepository(int repoAccountId, String repositoryOwner, String repositoryName, int mostVisited) {

		Repository repository = new Repository();
		repository.setRepoAccountId(repoAccountId);
		repository.setRepositoryOwner(repositoryOwner);
		repository.setRepositoryName(repositoryName);
		repository.setMostVisited(mostVisited);

		return insertRepositoryAsyncTask(repository);
	}

	public long insertRepositoryAsyncTask(Repository repository) {
		return repositoriesDao.newRepository(repository);
	}

	public Repository getRepository(int repoAccountId, String repositoryOwner, String repositoryName) {
		return repositoriesDao.getSingleRepositoryDao(repoAccountId, repositoryOwner, repositoryName);
	}

	public LiveData<List<Repository>> getAllRepositories() {
		return repositoriesDao.fetchAllRepositories();
	}

	public LiveData<List<Repository>> getAllRepositoriesByAccount(int repoAccountId) {
		return repositoriesDao.getAllRepositoriesByAccountDao(repoAccountId);
	}

	public Integer checkRepository(int repoAccountId, String repositoryOwner, String repositoryName) {
		return repositoriesDao.checkRepositoryDao(repoAccountId, repositoryOwner, repositoryName);
	}

	public Repository fetchRepositoryById(int repositoryId) {
		return repositoriesDao.fetchRepositoryByIdDao(repositoryId);
	}

	public Repository fetchRepositoryByAccountIdByRepositoryId(int repositoryId, int repoAccountId) {
		return repositoriesDao.fetchRepositoryByAccountIdByRepositoryIdDao(repositoryId, repoAccountId);
	}

	public void updateRepositoryOwnerAndName(String repositoryOwner, String repositoryName, int repositoryId) {
		executorService.execute(() -> repositoriesDao.updateRepositoryOwnerAndName(repositoryOwner, repositoryName, repositoryId));
	}

	public void deleteRepositoriesByAccount(final int repoAccountId) {
		executorService.execute(() -> repositoriesDao.deleteRepositoriesByAccount(repoAccountId));
	}

	public void deleteRepository(final int repositoryId) {
		executorService.execute(() -> repositoriesDao.deleteRepository(repositoryId));
	}

	public void updateRepositoryMostVisited(int mostVisited, int repositoryId) {
		executorService.execute(() -> repositoriesDao.updateRepositoryMostVisited(mostVisited, repositoryId));
	}

	public void resetAllRepositoryMostVisited(int repoAccountId) {
		executorService.execute(() -> repositoriesDao.resetAllRepositoryMostVisited(repoAccountId));
	}

	public LiveData<List<Repository>> fetchAllMostVisited(int repoAccountId) {
		return repositoriesDao.fetchAllMostVisited(repoAccountId);
	}

}
