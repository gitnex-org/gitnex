package org.mian.gitnex.database.db;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import org.mian.gitnex.database.dao.AppSettingsDao;
import org.mian.gitnex.database.dao.NotesDao;
import org.mian.gitnex.database.dao.RepositoriesDao;
import org.mian.gitnex.database.dao.UserAccountsDao;
import org.mian.gitnex.database.models.AppSettings;
import org.mian.gitnex.database.models.Notes;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.database.models.UserAccount;

/**
 * @author mmarif
 */
@Database(
		entities = {Repository.class, UserAccount.class, Notes.class, AppSettings.class},
		version = 11,
		exportSchema = false)
public abstract class GitnexDatabase extends RoomDatabase {

	private static final String DB_NAME = "gitnex";
	private static final Migration MIGRATION_1_2 =
			new Migration(1, 2) {
				@Override
				public void migrate(@NonNull SupportSQLiteDatabase database) {
					database.execSQL("ALTER TABLE 'Drafts' ADD COLUMN 'commentId' TEXT");
				}
			};
	private static final Migration MIGRATION_2_3 =
			new Migration(2, 3) {
				@Override
				public void migrate(@NonNull SupportSQLiteDatabase database) {
					database.execSQL("ALTER TABLE 'Drafts' ADD COLUMN 'issueType' TEXT");
				}
			};
	private static final Migration MIGRATION_3_4 =
			new Migration(3, 4) {

				@Override
				public void migrate(@NonNull SupportSQLiteDatabase database) {
					database.execSQL(
							"ALTER TABLE 'userAccounts' ADD COLUMN 'isLoggedIn' INTEGER NOT NULL DEFAULT 1");
				}
			};
	private static final Migration MIGRATION_4_5 =
			new Migration(4, 5) {

				@Override
				public void migrate(@NonNull SupportSQLiteDatabase database) {
					database.execSQL(
							"ALTER TABLE 'userAccounts' ADD COLUMN 'maxResponseItems' INTEGER NOT NULL DEFAULT 50");
					database.execSQL(
							"ALTER TABLE 'userAccounts' ADD COLUMN 'defaultPagingNumber' INTEGER NOT NULL DEFAULT 30");
				}
			};
	private static final Migration MIGRATION_5_6 =
			new Migration(5, 6) {

				@Override
				public void migrate(@NonNull SupportSQLiteDatabase database) {
					database.execSQL(
							"ALTER TABLE 'Repositories' ADD COLUMN 'mostVisited' INTEGER NOT NULL DEFAULT 0");
				}
			};
	private static final Migration MIGRATION_6_7 =
			new Migration(6, 7) {

				@Override
				public void migrate(@NonNull SupportSQLiteDatabase database) {
					database.execSQL(
							"CREATE TABLE IF NOT EXISTS 'Notes' ('noteId' INTEGER NOT NULL, 'content' TEXT, 'datetime' INTEGER, 'modified' INTEGER, PRIMARY KEY('noteid'))");
				}
			};

	private static final Migration MIGRATION_7_8 =
			new Migration(7, 8) {

				@Override
				public void migrate(@NonNull SupportSQLiteDatabase database) {
					database.execSQL(
							"CREATE TABLE IF NOT EXISTS 'appSettings' ('settingId' INTEGER NOT NULL, 'settingKey' TEXT, 'settingValue' TEXT, 'settingDefault' TEXT, PRIMARY KEY('settingId'))");
				}
			};

	private static final Migration MIGRATION_8_9 =
			new Migration(8, 9) {

				@Override
				public void migrate(@NonNull SupportSQLiteDatabase database) {
					database.execSQL(
							"ALTER TABLE 'userAccounts' ADD COLUMN 'maxAttachmentsSize' INTEGER NOT NULL DEFAULT 2");
					database.execSQL(
							"ALTER TABLE 'userAccounts' ADD COLUMN 'maxNumberOfAttachments' INTEGER NOT NULL DEFAULT 5");
				}
			};

	private static final Migration MIGRATION_9_10 =
			new Migration(9, 10) {

				@Override
				public void migrate(@NonNull SupportSQLiteDatabase database) {
					database.execSQL("DROP table Drafts");
				}
			};

	private static final Migration MIGRATION_10_11 =
			new Migration(10, 11) {

				@Override
				public void migrate(@NonNull SupportSQLiteDatabase database) {
					database.execSQL("ALTER TABLE 'userAccounts' ADD COLUMN 'provider' TEXT");
				}
			};

	private static volatile GitnexDatabase gitnexDatabase;

	public static GitnexDatabase getDatabaseInstance(Context context) {

		if (gitnexDatabase == null) {
			synchronized (GitnexDatabase.class) {
				if (gitnexDatabase == null) {

					gitnexDatabase =
							Room.databaseBuilder(context, GitnexDatabase.class, DB_NAME)
									// .fallbackToDestructiveMigration()
									.allowMainThreadQueries()
									.addMigrations(
											MIGRATION_1_2,
											MIGRATION_2_3,
											MIGRATION_3_4,
											MIGRATION_4_5,
											MIGRATION_5_6,
											MIGRATION_6_7,
											MIGRATION_7_8,
											MIGRATION_8_9,
											MIGRATION_9_10,
											MIGRATION_10_11)
									.build();
				}
			}
		}

		return gitnexDatabase;
	}

	public abstract RepositoriesDao repositoriesDao();

	public abstract UserAccountsDao userAccountsDao();

	public abstract NotesDao notesDao();

	public abstract AppSettingsDao appSettingsDao();
}
