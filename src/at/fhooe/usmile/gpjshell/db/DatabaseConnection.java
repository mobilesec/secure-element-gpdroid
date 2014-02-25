/**
 * 
 */
package at.fhooe.usmile.gpjshell.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Thomas
 * 
 */
public class DatabaseConnection extends SQLiteOpenHelper {
	public static final String TABLE_KEYSETS = "keysets";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN1_KEYSET_ID = "keysetid";
	public static final String COLUMN2_VERSION = "version";
	public static final String COLUMN3_MAC = "mac";
	public static final String COLUMN4_ENC = "dek";
	public static final String COLUMN5_KEK = "kek";
	public static final String COLUMN6_NAME = "name";
	public static final String COLUMN7_READER = "reader";
	
	
	public static final String DB_NAME = "GPDroid_DB";
	private static final int VERSION = 1;

	// contains sql command to create the database
	private static final String DB_CREATE = "create table " + TABLE_KEYSETS
			+ "(" + COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN1_KEYSET_ID + " INTEGER not null,"
			+ COLUMN2_VERSION + " INTEGER, "
			+ COLUMN3_MAC + " TEXT, "
			+ COLUMN4_ENC + " TEXT, "
			+ COLUMN5_KEK + " TEXT, "
			+ COLUMN6_NAME + " TEXT, "
			+ COLUMN7_READER + " TEXT);";

	/**
	 * Instantiates a DataBaseConnection object by calling super constructor.
	 * 
	 * @param context
	 */
	public DatabaseConnection(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DB_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DatabaseConnection.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_KEYSETS);
		onCreate(db);
	}

}
