/*******************************************************************************
 * Copyright (c) 2014 Michael Hölzl <mihoelzl@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Hölzl <mihoelzl@gmail.com> - initial implementation
 *     Thomas Sigmund - data base, key set, channel set selection and GET DATA integration
 ******************************************************************************/
package at.fhooe.usmile.gpjshell;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.gpj.cardservices.AID;
import net.sourceforge.gpj.cardservices.AIDRegistryEntry;
import net.sourceforge.gpj.cardservices.GPUtil;
import net.sourceforge.gpj.cardservices.interfaces.OpenMobileAPITerminal;

import org.simalliance.openmobileapi.SEService;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import at.fhooe.usmile.gpjshell.MainActivity.APDU_COMMAND;
import at.fhooe.usmile.gpjshell.objects.GPChannelSet;
import at.fhooe.usmile.gpjshell.objects.GPKeyset;

public class AppletListActivity extends Activity implements AppletDetailActivity.NoticeAppletEventListener, SEService.CallBack{

	private static final String LOG_TAG = "AppletListActivity";
	public static final String EXTRA_CHANNELSET = "extra_channelset";
	public static final String EXTRA_KEYSET = "extra_keyset";
	public static final String EXTRA_SEEKREADER = "extra_reader";
	private List<String> appletNames = null;
	private ArrayAdapter<String> mListAdapter;
	private OpenMobileAPITerminal mTerminal;
	private GPKeyset mKeySet;
	private GPChannelSet mChannelSet;
	private int mSeekReader;
	private List<AIDRegistryEntry> mRegistry;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.applet_list);

		final ListView listview = (ListView) findViewById(R.id.listview);

		setListData(listview);

		mKeySet = (GPKeyset) getIntent().getSerializableExtra(EXTRA_KEYSET);
		mChannelSet = (GPChannelSet) getIntent().getSerializableExtra(EXTRA_CHANNELSET);
		mSeekReader = (Integer) getIntent().getSerializableExtra(EXTRA_SEEKREADER);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				GPConnection.getInstance(getApplicationContext()).setSelectedApplet(position);

				showAppletDetailsDialog();
			}

		});

		
	}

	@Override
	protected void onResume() {
		super.onResume();
		mTerminal = new OpenMobileAPITerminal(this, this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mTerminal != null) {
			mTerminal.shutdown();
		}
	}
	private void returnToHome(){
		Intent homeIntent= new Intent(this, MainActivity.class);
		homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(homeIntent);
	}
	
	private void setListData(final ListView listview) {
		List<AIDRegistryEntry> registry = GPConnection.getInstance(getApplicationContext()).getRegistry();
		appletNames = new ArrayList<String>();
		
		if(registry==null){
			returnToHome();
		} else {
			
			mRegistry = registry;
			updateData(registry);
			
			mListAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, appletNames);
			listview.setAdapter(mListAdapter);
			mListAdapter.notifyDataSetChanged();
		}
	}

	private void updateData(List<AIDRegistryEntry> registry) {
		
		String spaces = "";
		int numSpaces = 0;
		
		for(int i =0; i < registry.size(); i++){
			AIDRegistryEntry entry = registry.get(i);

			numSpaces = (10 - entry.getAID().getLength()) * 3;
			spaces = "";
			for (int j = 0; j < numSpaces; j++) {
				spaces = spaces + " ";
			}
			
			appletNames.add("" + GPUtil.byteArrayToString(entry.getAID().getBytes())
					+ spaces + " "
					+ GPUtil.byteArrayToReadableString(entry.getAID().getBytes()));
		}
	}
	
	public void showAppletDetailsDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new AppletDetailActivity();
        dialog.show(getFragmentManager(), "AppletDetailFragment");
        
    }


	@Override
	public void onDialogDeleteClick(DialogFragment dialog) {
		AID delAID= GPConnection.getInstance(getApplicationContext()).getSelectedApplet().getAID();
		GPCommand cmd = new GPCommand(APDU_COMMAND.APDU_DELETE_SELECTED_APPLET, mSeekReader, null, (byte)0, null);
		GPConnection.getInstance(getApplicationContext()).performCommand(mTerminal, mKeySet, mChannelSet, cmd);
		
		MainActivity.log().log(LOG_TAG, "Successfully removed: "+delAID);
		
		/**
		 * Reload
		 */
		mRegistry = GPConnection.getInstance(this).getRegistry();
		
		appletNames = new ArrayList<String>();
		updateData(mRegistry);
		mListAdapter.clear();
		mListAdapter.addAll(appletNames);
	}

	@Override
	public void onDialogOkClick(DialogFragment dialog) {
		
	}

	@Override
	public void serviceConnected(SEService service) {
		// TODO Auto-generated method stub
		
	}

}
