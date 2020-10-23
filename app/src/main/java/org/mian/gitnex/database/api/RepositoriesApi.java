package org.mian.gitnex.database.api;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import org.mian.gitnex.database.dao.RepositoriesDao;
import org.mian.gitnex.database.db.GitnexDatabase;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import java.util.List;

/**
 * Author M M Arif
 */

public class RepositoriesApi {

	private static RepositoriesDao repositoriesDao;
	private static long repositoryId;
	private static Repository repository;
	private static Integer checkRepository;

	public RepositoriesApi(Context context) {

		GitnexDatabase db;
		db = GitnexDatabase.getDatabaseInstance(context);
		repositoriesDao = db.repositoriesDao();
	}

	public long insertRepository(int repoAccountId, String repositoryOwner, String repositoryName) {

		Repository repository = new Repository();
		repository.setRepoAccountId(repoAccountId);
		repository.setRepositoryOwner(repositoryOwner);
		repository.setRepositoryName(repositoryName);

		return insertRepositoryAsyncTask(repository);
	}

	public long insertRepositoryAsyncTask(Repository repository) {

		try {

			Thread thread = new Thread(() -> repositoryId = repositoriesDao.newRepository(repository));
			thread.start();
			thread.join();
		}
		catch(InterruptedException e) {

			Log.e(StaticGlobalVariables.repositoriesApi, e.toString());
		}

		return repositoryId;
	}

	public Repository getRepository(int repoAccountId, String repositoryOwner, String repositoryName) {

		try {

			Thread thread = new Thread(() -> repository = repositoriesDao.getSingleRepositoryDao(repoAccountId, repositoryOwner, repositoryName));
			thread.start();
			thread.join();
		}
		catch(InterruptedException e) {

			Log.e(StaticGlobalVariables.repositoriesApi, e.toString());
		}

		return repository;
	}

	public LiveData<List<Repository>> getAllRepositories() {

		return repositoriesDao.fetchAllRepositories();
	}

	public LiveData<List<Repository>> getAllRepositoriesByAccount(int repoAccountId) {

		return repositoriesDao.getAllRepositoriesByAccountDao(repoAccountId);
	}

	public Integer checkRepository(int repoAccountId, String repositoryOwner, String repositoryName) {

		try {

			Thread thread = new Thread(() -> checkRepository = repositoriesDao.checkRepositoryDao(repoAccountId, repositoryOwner, repositoryName));
			thread.start();
			thread.join();
		}
		catch(InterruptedException e) {

			Log.e(StaticGlobalVariables.repositoriesApi, e.toString());
		}

		return checkRepository;
	}

	public Repository fetchRepositoryById(int repositoryId) {

		try {

			Thread thread = new Thread(() -> repository = repositoriesDao.fetchRepositoryByIdDao(repositoryId));
			thread.start();
			thread.join();
		}
		catch(InterruptedException e) {

			Log.e(StaticGlobalVariables.repositoriesApi, e.toString());
		}

		return repository;
	}

	public Repository fetchRepositoryByAccountIdByRepositoryId(int repositoryId, int repoAccountId) {

		try {

			Thread thread = new Thread(() -> repository = repositoriesDao.fetchRepositoryByAccountIdByRepositoryIdDao(repositoryId, repoAccountId));
			thread.start();
			thread.join();
		}
		catch(InterruptedException e) {

			Log.e(StaticGlobalVariables.repositoriesApi, e.toString());
		}

		return repository;
	}

	public static void updateRepositoryOwnerAndName(String repositoryOwner, String repositoryName, int repositoryId) {

		new Thread(() -> repositoriesDao.updateRepositoryOwnerAndName(repositoryOwner, repositoryName, repositoryId)).start();
	}

	public static void deleteRepositoriesByAccount(final int repoAccountId) {

		new Thread(() -> repositoriesDao.deleteRepositoriesByAccount(repoAccountId)).start();
	}

	public static void deleteRepository(final int repositoryId) {

		new Thread(() -> repositoriesDao.deleteRepository(repositoryId)).start();
	}

}
