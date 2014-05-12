package at.fhooe.usmile.gpjshell.db;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import at.fhooe.usmile.gpjshell.objects.GPKeyset;

public class KeysetDataSource {
	// private Context mContext;
	private DatabaseConnection mConnection;
	private SQLiteDatabase mDatabase;

	public KeysetDataSource(Context context) {
		// mContext = context;
		mConnection = new DatabaseConnection(context);
	}

	public void open() {
		mDatabase = mConnection.getWritableDatabase();
	}

	public void close() {
		mConnection.close();
	}

	public void insertKeyset(GPKeyset keyset) {
		ContentValues values = new ContentValues();
		values.put(DatabaseConnection.COLUMN1_KEYSET_ID, keyset.getID());
		values.put(DatabaseConnection.COLUMN2_VERSION, keyset.getVersion());
		values.put(DatabaseConnection.COLUMN3_MAC, keyset.getMAC());
		values.put(DatabaseConnection.COLUMN4_ENC, keyset.getENC());
		values.put(DatabaseConnection.COLUMN5_KEK, keyset.getKEK());
		values.put(DatabaseConnection.COLUMN6_NAME, keyset.getName());
		values.put(DatabaseConnection.COLUMN7_READER, keyset.getReaderName());

		//if it is a new keyset, then uid is preset to -1
		if (keyset.getUniqueID() == -1) {
			if (containsKeyset(keyset.getName(), keyset.getReaderName()))
				mDatabase.update(DatabaseConnection.TABLE_KEYSETS, values,
						DatabaseConnection.COLUMN6_NAME + "='" + keyset.getName() + "' AND "
								+ DatabaseConnection.COLUMN7_READER + "='" + keyset.getReaderName()
								+ "'", null);
			else
				mDatabase.insert(DatabaseConnection.TABLE_KEYSETS, null, values);
		} else {	//else these are the default keys, which get inserted at first positions
			if (containsUID(keyset.getUniqueID())) {
				mDatabase.update(DatabaseConnection.TABLE_KEYSETS, values,
						DatabaseConnection.COLUMN_ID + "='" + keyset.getUniqueID()
								+ "'", null);
			} else {
				mDatabase.insert(DatabaseConnection.TABLE_KEYSETS, null, values);
			}
		}
	}

	public boolean containsKeyset(String name, String reader) {
		return mDatabase.query(
				DatabaseConnection.TABLE_KEYSETS,
				new String[] { DatabaseConnection.COLUMN6_NAME,
						DatabaseConnection.COLUMN7_READER },
				DatabaseConnection.COLUMN6_NAME + "='" + name + "' AND "
						+ DatabaseConnection.COLUMN7_READER + "='" + reader
						+ "'", null, null, null, null).getCount() > 0;
	}
	
	public boolean containsUID(int id) {
		return mDatabase.query(
				DatabaseConnection.TABLE_KEYSETS,
				new String[] { DatabaseConnection.COLUMN_ID},
				DatabaseConnection.COLUMN_ID + "='" + id + "'", null, null, null, null).getCount() > 0;
	}

	public Map<String, GPKeyset> getKeysets(String reader) {
		String whereClause = null;
		if (reader != null)
			whereClause = DatabaseConnection.COLUMN7_READER + " = '" + reader
					+ "'";

		Map<String, GPKeyset> keysets = new HashMap<String, GPKeyset>();
		Cursor cursor = mDatabase.query(DatabaseConnection.TABLE_KEYSETS, null,
				whereClause, null, null, null,
				DatabaseConnection.COLUMN1_KEYSET_ID + " DESC");
		cursor.moveToFirst();
		for (int i = 0; i < cursor.getCount(); i++) {
			int uid = cursor.getInt(cursor
					.getColumnIndex(DatabaseConnection.COLUMN_ID));
			String name = cursor.getString(cursor
					.getColumnIndex(DatabaseConnection.COLUMN6_NAME));
			int id = cursor.getInt(cursor
					.getColumnIndex(DatabaseConnection.COLUMN1_KEYSET_ID));
			int version = cursor.getInt(cursor
					.getColumnIndex(DatabaseConnection.COLUMN2_VERSION));
			String MAC = cursor.getString(cursor
					.getColumnIndex(DatabaseConnection.COLUMN3_MAC));
			String DEK = cursor.getString(cursor
					.getColumnIndex(DatabaseConnection.COLUMN4_ENC));
			String KEK = cursor.getString(cursor
					.getColumnIndex(DatabaseConnection.COLUMN5_KEK));
			String readerName = cursor.getString(cursor
					.getColumnIndex(DatabaseConnection.COLUMN7_READER));

			GPKeyset newKeyset= new GPKeyset(uid, name, id, version, MAC, DEK,
					KEK, readerName);
			keysets.put(newKeyset.getDisplayName(), newKeyset);
			cursor.moveToNext();
			
			Log.d("KeysetData", "name: " + name + "; id: "+id+"; mac: "+MAC);
		}
		return keysets;
	}

	public int remove(int uid) {
		return mDatabase.delete(DatabaseConnection.TABLE_KEYSETS,
				DatabaseConnection.COLUMN_ID + "='" + uid + "'", null);
	}

}
