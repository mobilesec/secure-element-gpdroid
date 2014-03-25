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
		values.put(DatabaseConnection.COLUMN1_CHANNEL_STRING_NAME, channelSet.getChannelNameString());
		values.put(DatabaseConnection.COLUMN2_CHANNEL_SCPVERSION, channelSet.getScpVersion());
		values.put(DatabaseConnection.COLUMN3_SECURITY_LEVEL, channelSet.getSecurityLevel());
		values.put(DatabaseConnection.COLUMN4_GEMALTO, channelSet.isGemalto() ? 1 : 0);
		
		if(mDatabase.query(DatabaseConnection.TABLE_CHANNELSET, new String[]{DatabaseConnection.COLUMN1_CHANNEL_STRING_NAME}, DatabaseConnection.COLUMN1_CHANNEL_STRING_NAME + "='" + channelSet.getChannelNameString() +"'", null, null, null, null).getCount() > 0)
			Log.i("channels updated: ", "" + mDatabase.update(DatabaseConnection.TABLE_CHANNELSET, values, DatabaseConnection.COLUMN1_CHANNEL_STRING_NAME + "='" + channelSet.getChannelNameString() + "'", null));
		else
			Log.i("channel inserted at row: ", "" + mDatabase.insert(DatabaseConnection.TABLE_CHANNELSET, null, values));
	}
	
	public Map<String, GPChannelSet> getChannelSets(){
		
		Map<String, GPChannelSet> channelSetsMap = new HashMap<String, GPChannelSet>();
		Cursor cursor = mDatabase.query(DatabaseConnection.TABLE_CHANNELSET, null, null, null, null, null, DatabaseConnection.COLUMN1_CHANNEL_STRING_NAME + " DESC");
		cursor.moveToFirst();
		for (int i = 0; i < cursor.getCount(); i++){
			String nameString = cursor.getString(cursor.getColumnIndex(DatabaseConnection.COLUMN1_CHANNEL_STRING_NAME));
			int scpVersion = cursor.getInt(cursor.getColumnIndex(DatabaseConnection.COLUMN2_CHANNEL_SCPVERSION));
			int securityLevel = cursor.getInt(cursor.getColumnIndex(DatabaseConnection.COLUMN3_SECURITY_LEVEL));
			boolean isGemalto = (cursor.getInt(cursor.getColumnIndex(DatabaseConnection.COLUMN4_GEMALTO))) == 1 ? true : false;
			
			GPChannelSet channel = new GPChannelSet(nameString, scpVersion, securityLevel, isGemalto);
			
			channelSetsMap.put(nameString, channel);
			cursor.moveToNext();
		}
		return channelSetsMap;
	}
	
	
	public int remove(String name){
		return mDatabase.delete(DatabaseConnection.TABLE_CHANNELSET, DatabaseConnection.COLUMN1_CHANNEL_STRING_NAME + "='" + name + "'", null);
	}
	
	
}
