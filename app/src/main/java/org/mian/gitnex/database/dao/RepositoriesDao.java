package org.mian.gitnex.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import org.mian.gitnex.database.models.Repository;

/**
 * @author M M Arif
 */
@Dao
public interface RepositoriesDao {

	@Insert
	long newRepository(Repository repositories);

	@Query("SELECT * FROM Repositories ORDER BY repositoryId ASC")
	LiveData<List<Repository>> fetchAllRepositories();

	@Query("SELECT * FROM Repositories WHERE repoAccountId = :repoAccountId")
	LiveData<List<Repository>> getAllRepositoriesByAccountDao(int repoAccountId);

	@Query(
			"SELECT count(repositoryId) FROM Repositories WHERE repoAccountId = :repoAccountId AND repositoryOwner = :repositoryOwner AND repositoryName = :repositoryName")
	Integer checkRepositoryDao(int repoAccountId, String repositoryOwner, String repositoryName);

	@Query(
			"SELECT * FROM Repositories WHERE repoAccountId = :repoAccountId AND repositoryOwner = :repositoryOwner AND repositoryName = :repositoryName")
	Repository getSingleRepositoryDao(
			int repoAccountId, String repositoryOwner, String repositoryName);

	@Query("SELECT * FROM Repositories WHERE repositoryId = :repositoryId")
	Repository fetchRepositoryByIdDao(int repositoryId);

	@Query(
			"SELECT * FROM Repositories WHERE repositoryId = :repositoryId AND repoAccountId = :repoAccountId")
	Repository fetchRepositoryByAccountIdByRepositoryIdDao(int repositoryId, int repoAccountId);

	@Query(
			"UPDATE Repositories SET repositoryOwner = :repositoryOwner, repositoryName = :repositoryName  WHERE repositoryId = :repositoryId")
	void updateRepositoryOwnerAndName(
			String repositoryOwner, String repositoryName, int repositoryId);

	@Query("DELETE FROM Repositories WHERE repositoryId = :repositoryId")
	void deleteRepository(int repositoryId);

	@Query(
			"DELETE FROM Repositories WHERE repositoryName = :repositoryName AND repoAccountId = :currentActiveAccountId")
	void deleteRepositoryByName(int currentActiveAccountId, String repositoryName);

	@Query("DELETE FROM Repositories WHERE repoAccountId = :repoAccountId")
	void deleteRepositoriesByAccount(int repoAccountId);

	@Query("UPDATE Repositories SET mostVisited = :mostVisited WHERE repositoryId = :repositoryId")
	void updateRepositoryMostVisited(int mostVisited, int repositoryId);

	@Query(
			"SELECT * FROM Repositories WHERE mostVisited > 0  AND repoAccountId = :repoAccountId ORDER BY mostVisited DESC LIMIT 50")
	LiveData<List<Repository>> fetchAllMostVisited(int repoAccountId);

	@Query("UPDATE Repositories SET mostVisited = 0 WHERE repoAccountId = :repoAccountId")
	void resetAllRepositoryMostVisited(int repoAccountId);
}
