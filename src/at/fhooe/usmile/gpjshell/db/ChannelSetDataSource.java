package at.fhooe.usmile.gpjshell.db;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import at.fhooe.usmile.gpjshell.objects.GPChannelSet;

public class ChannelSetDataSource {
//	private Context mContext;
	private DatabaseConnection mConnection;
	private SQLiteDatabase mDatabase;
	
	public ChannelSetDataSource(Context context) {
//		mContext = context;
		mConnection = new DatabaseConnection(context);
	}
	
	public void open(){
		mDatabase = mConnection.getWritableDatabase();
	}
	
	public void close(){
		mConnection.close();
	}
	
	public void insertChannelSet(GPChannelSet channelSet){
		ContentValues values = new ContentValues();
		values.put(DatabaseConnection.COLUMN1_CHANNEL_NAME, channelSet.getChannelSet());
		values.put(DatabaseConnection.COLUMN2_CHANNEL_ID, channelSet.getChannelId());
		values.put(DatabaseConnection.COLUMN3_CHANNEL_VERSION, channelSet.getScpVersion());
		values.put(DatabaseConnection.COLUMN4_SECURITY_LEVEL, channelSet.getSecurityLevel());
		values.put(DatabaseConnection.COLUMN5_GEMALTO, channelSet.isGemalto() ? 1 : 0);
		values.put(DatabaseConnection.COLUMN6_CHANNEL_READER, channelSet.getReaderName());
		values.put(DatabaseConnection.COLUMN7_CHANNEL_STRING_NAME, channelSet.getChannelNameString());
		
		if(mDatabase.query(DatabaseConnection.TABLE_CHANNELSET, new String[]{DatabaseConnection.COLUMN2_CHANNEL_ID}, DatabaseConnection.COLUMN2_CHANNEL_ID + "='" + channelSet.getChannelId() +"'", null, null, null, null).getCount() > 0)
			Log.i("channels updated: ", "" + mDatabase.update(DatabaseConnection.TABLE_CHANNELSET, values, DatabaseConnection.COLUMN2_CHANNEL_ID + "='" + channelSet.getChannelId() + "'", null));
		else
			Log.i("channel inserted at row: ", "" + mDatabase.insert(DatabaseConnection.TABLE_CHANNELSET, null, values));
	}
	
	public Map<String, GPChannelSet> getChannelSets(){
		Map<String, GPChannelSet> channelSetsMap = new HashMap<String, GPChannelSet>();
		Cursor cursor = mDatabase.query(DatabaseConnection.TABLE_CHANNELSET, null, null, null, null, null, DatabaseConnection.COLUMN2_CHANNEL_ID + " DESC");
		cursor.moveToFirst();
		for (int i = 0; i < cursor.getCount(); i++){
			String nameString = cursor.getString(cursor.getColumnIndex(DatabaseConnection.COLUMN7_CHANNEL_STRING_NAME));
			int id = cursor.getInt(cursor.getColumnIndex(DatabaseConnection.COLUMN2_CHANNEL_ID));
			int version = cursor.getInt(cursor.getColumnIndex(DatabaseConnection.COLUMN3_CHANNEL_VERSION));
			int name = cursor.getInt(cursor.getColumnIndex(DatabaseConnection.COLUMN1_CHANNEL_NAME));
			int securityLevel = cursor.getInt(cursor.getColumnIndex(DatabaseConnection.COLUMN4_SECURITY_LEVEL));
			boolean isGemalto = (cursor.getInt(cursor.getColumnIndex(DatabaseConnection.COLUMN5_GEMALTO))) == 1 ? true : false;
			String reader = (cursor.getString(cursor.getColumnIndex(DatabaseConnection.COLUMN6_CHANNEL_READER)));
			
			GPChannelSet channel = new GPChannelSet(nameString, name, id, version, securityLevel, isGemalto);
			channel.setReaderName(reader);
			
			channelSetsMap.put(nameString, channel);
			cursor.moveToNext();
		}
		return channelSetsMap;
	}

	public Map<String, GPChannelSet> getChannelSets(String reader){
		Map<String, GPChannelSet> channelSetsMap = new HashMap<String, GPChannelSet>();
		Cursor cursor = mDatabase.query(DatabaseConnection.TABLE_CHANNELSET, null, DatabaseConnection.COLUMN6_CHANNEL_READER + " = '" + reader + "'", null, null, null, DatabaseConnection.COLUMN2_CHANNEL_ID + " DESC");
		cursor.moveToFirst();
		for (int i = 0; i < cursor.getCount(); i++){
			String nameString = cursor.getString(cursor.getColumnIndex(DatabaseConnection.COLUMN7_CHANNEL_STRING_NAME));
			int id = cursor.getInt(cursor.getColumnIndex(DatabaseConnection.COLUMN2_CHANNEL_ID));
			int version = cursor.getInt(cursor.getColumnIndex(DatabaseConnection.COLUMN3_CHANNEL_VERSION));
			int name = cursor.getInt(cursor.getColumnIndex(DatabaseConnection.COLUMN1_CHANNEL_NAME));
			int securityLevel = cursor.getInt(cursor.getColumnIndex(DatabaseConnection.COLUMN4_SECURITY_LEVEL));
			boolean isGemalto = (cursor.getInt(cursor.getColumnIndex(DatabaseConnection.COLUMN5_GEMALTO))) == 1 ? true : false;
			String readerName = (cursor.getString(cursor.getColumnIndex(DatabaseConnection.COLUMN6_CHANNEL_READER)));
			
			GPChannelSet channel = new GPChannelSet(nameString, name, id, version, securityLevel, isGemalto);
			channel.setReaderName(readerName);
			
			channelSetsMap.put(nameString, channel);
			cursor.moveToNext();
		}
		return channelSetsMap;
	}
	
	
	public int remove(int id){
		return mDatabase.delete(DatabaseConnection.TABLE_CHANNELSET, DatabaseConnection.COLUMN2_CHANNEL_ID + "='" + id + "'", null);
	}
	
	
}
