package org.mian.gitnex.database.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import static androidx.room.ForeignKey.CASCADE;

/**
 * @author M M Arif
 */

@Entity(tableName = "Repositories", foreignKeys = @ForeignKey(entity = UserAccount.class,
        parentColumns = "accountId",
        childColumns = "repoAccountId",
        onDelete = CASCADE),
        indices = {@Index("repoAccountId")})
public class Repository implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int repositoryId;

    private int repoAccountId;
    private String repositoryOwner;
    private String repositoryName;

    public int getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(int repositoryId) {
        this.repositoryId = repositoryId;
    }

    public int getRepoAccountId() {
        return repoAccountId;
    }

    public void setRepoAccountId(int repoAccountId) {
        this.repoAccountId = repoAccountId;
    }

    public String getRepositoryOwner() {
        return repositoryOwner;
    }

    public void setRepositoryOwner(String repositoryOwner) {
        this.repositoryOwner = repositoryOwner;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }
}
