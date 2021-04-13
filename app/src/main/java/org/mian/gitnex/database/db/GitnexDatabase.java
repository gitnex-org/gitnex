package org.mian.gitnex.database.db;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import org.mian.gitnex.database.dao.DraftsDao;
import org.mian.gitnex.database.dao.RepositoriesDao;
import org.mian.gitnex.database.dao.UserAccountsDao;
import org.mian.gitnex.database.models.Draft;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.database.models.UserAccount;

/**
 * Author M M Arif
 */

@Database(entities = {Draft.class, Repository.class, UserAccount.class},
        version = 3, exportSchema = false)
public abstract class GitnexDatabase extends RoomDatabase {

	private static final String DB_NAME = "gitnex";
    private static GitnexDatabase gitnexDatabase;

    public abstract DraftsDao draftsDao();
    public abstract RepositoriesDao repositoriesDao();
    public abstract UserAccountsDao userAccountsDao();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //database.execSQL("DROP TABLE Drafts");
	        database.execSQL("ALTER TABLE 'Drafts' ADD COLUMN 'commentId' TEXT");
        }
    };

	private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			database.execSQL("ALTER TABLE 'Drafts' ADD COLUMN 'issueType' TEXT");
		}
	};

	public static GitnexDatabase getDatabaseInstance(Context context) {

		if (gitnexDatabase == null) {
			synchronized(GitnexDatabase.class) {
				if(gitnexDatabase == null) {

					gitnexDatabase = Room.databaseBuilder(context, GitnexDatabase.class, DB_NAME)
						// .fallbackToDestructiveMigration()
						.allowMainThreadQueries()
						.addMigrations(MIGRATION_1_2, MIGRATION_2_3)
						.build();

				}
			}
		}

		return gitnexDatabase;

	}
}
