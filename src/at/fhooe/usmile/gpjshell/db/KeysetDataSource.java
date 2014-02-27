package at.fhooe.usmile.gpjshell.db;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import at.fhooe.usmile.gpjshell.objects.GPKeyset;

public class KeysetDataSource {
//	private Context mContext;
	private DatabaseConnection mConnection;
	private SQLiteDatabase mDatabase;
	
	public KeysetDataSource(Context context) {
//		mContext = context;
		mConnection = new DatabaseConnection(context);
	}
	
	public void open(){
		mDatabase = mConnection.getWritableDatabase();
	}
	
	public void close(){
		mConnection.close();
	}
	
	public void insertKeyset(GPKeyset keyset){
		ContentValues values = new ContentValues();
		values.put(DatabaseConnection.COLUMN1_KEYSET_ID, keyset.getID());
		values.put(DatabaseConnection.COLUMN2_VERSION, keyset.getVersion());
		values.put(DatabaseConnection.COLUMN3_MAC, keyset.getMAC());
		values.put(DatabaseConnection.COLUMN4_ENC, keyset.getENC());
		values.put(DatabaseConnection.COLUMN5_KEK, keyset.getKEK());
		values.put(DatabaseConnection.COLUMN6_NAME, keyset.getName());
		values.put(DatabaseConnection.COLUMN7_READER, keyset.getReaderName());
		
		if(mDatabase.query(DatabaseConnection.TABLE_KEYSETS, new String[]{DatabaseConnection.COLUMN1_KEYSET_ID}, DatabaseConnection.COLUMN1_KEYSET_ID + "='" + keyset.getID() +"'", null, null, null, null).getCount() > 0)
			mDatabase.update(DatabaseConnection.TABLE_KEYSETS, values, DatabaseConnection.COLUMN1_KEYSET_ID + "='" + keyset.getID() +"'", null);
		else
			mDatabase.insert(DatabaseConnection.TABLE_KEYSETS, null, values);
	}
	
	public Map<String, GPKeyset> getKeysets(){
		Map<String, GPKeyset> keysets = new HashMap<String, GPKeyset>();
		Cursor cursor = mDatabase.query(DatabaseConnection.TABLE_KEYSETS, null, null, null, null, null, DatabaseConnection.COLUMN1_KEYSET_ID + " DESC");
		cursor.moveToFirst();
		for (int i = 0; i < cursor.getCount(); i++){
			String name = cursor.getString(cursor.getColumnIndex(DatabaseConnection.COLUMN6_NAME));
			int id = cursor.getInt(cursor.getColumnIndex(DatabaseConnection.COLUMN1_KEYSET_ID));
			int version = cursor.getInt(cursor.getColumnIndex(DatabaseConnection.COLUMN2_VERSION));
			String MAC = cursor.getString(cursor.getColumnIndex(DatabaseConnection.COLUMN3_MAC));
			String DEK = cursor.getString(cursor.getColumnIndex(DatabaseConnection.COLUMN4_ENC));
			String KEK = cursor.getString(cursor.getColumnIndex(DatabaseConnection.COLUMN5_KEK));
			String reader = cursor.getString(cursor.getColumnIndex(DatabaseConnection.COLUMN7_READER));
			
			keysets.put(name, new GPKeyset(name, id, version, MAC, DEK, KEK, reader));
			cursor.moveToNext();
		}
		return keysets;
	}
	
	public Map<String, GPKeyset> getKeysets(String reader){
		Map<String, GPKeyset> keysets = new HashMap<String, GPKeyset>();
		Cursor cursor = mDatabase.query(DatabaseConnection.TABLE_KEYSETS, null, DatabaseConnection.COLUMN7_READER + " = '" + reader + "'", null, null, null, DatabaseConnection.COLUMN1_KEYSET_ID + " DESC");
		cursor.moveToFirst();
		for (int i = 0; i < cursor.getCount(); i++){
			String name = cursor.getString(cursor.getColumnIndex(DatabaseConnection.COLUMN6_NAME));
			int id = cursor.getInt(cursor.getColumnIndex(DatabaseConnection.COLUMN1_KEYSET_ID));
			int version = cursor.getInt(cursor.getColumnIndex(DatabaseConnection.COLUMN2_VERSION));
			String MAC = cursor.getString(cursor.getColumnIndex(DatabaseConnection.COLUMN3_MAC));
			String DEK = cursor.getString(cursor.getColumnIndex(DatabaseConnection.COLUMN4_ENC));
			String KEK = cursor.getString(cursor.getColumnIndex(DatabaseConnection.COLUMN5_KEK));
			String readerName = cursor.getString(cursor.getColumnIndex(DatabaseConnection.COLUMN7_READER));
			
			keysets.put(name, new GPKeyset(name, id, version, MAC, DEK, KEK, readerName));
			cursor.moveToNext();
		}
		return keysets;
	}
	
	public int remove(int id){
		return mDatabase.delete(DatabaseConnection.TABLE_KEYSETS, DatabaseConnection.COLUMN1_KEYSET_ID + "='" + id + "'", null);
	}
	
	
}
