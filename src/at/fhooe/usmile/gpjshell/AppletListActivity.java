package at.fhooe.usmile.gpjshell;

import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.CardException;

import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.SEService.CallBack;

import net.sourceforge.gpj.cardservices.AIDRegistry;
import net.sourceforge.gpj.cardservices.AIDRegistryEntry;
import net.sourceforge.gpj.cardservices.GPUtil;
import net.sourceforge.gpj.cardservices.exceptions.GPDeleteException;
import net.sourceforge.gpj.cardservices.interfaces.OpenMobileAPITerminal;
import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.applet_list);

		final ListView listview = (ListView) findViewById(R.id.listview);

		Log.d("Michi", "oncreate applet list");
		setListData(listview);

		mKeySet = (GPKeyset) getIntent().getSerializableExtra(EXTRA_KEYSET);
		mChannelSet = (GPChannelSet) getIntent().getSerializableExtra(EXTRA_CHANNELSET);
		mSeekReader = (Integer) getIntent().getSerializableExtra(EXTRA_SEEKREADER);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
//				final String item = (String) parent.getItemAtPosition(position);

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
		Log.d("Michi", "onpause applet list");
		if (mTerminal != null) {
			mTerminal.shutdown();
		}
	}
	private void setListData(final ListView listview) {
		AIDRegistry registry = GPConnection.getInstance(getApplicationContext()).getRegistry();
		appletNames = new ArrayList<String>();
		
		updateData(registry);
		
		mListAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, appletNames);
		listview.setAdapter(mListAdapter);
		mListAdapter.notifyDataSetChanged();
	}

	private void updateData(AIDRegistry registry) {
		List<AIDRegistryEntry> applets = registry.allPackages();

		String spaces = "";
		int numSpaces = 0;
		
		for(int i =0; i < applets.size(); i++){
			AIDRegistryEntry entry = applets.get(i);

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
	
		try {			
			GPCommand cmd = new GPCommand(APDU_COMMAND.APDU_DELETE_SELECTED_APPLET, mSeekReader, null, (byte)0, null);
			GPConnection.getInstance(getApplicationContext()).performCommand(mTerminal, mKeySet, mChannelSet, cmd);
			
			MainActivity.log().log(LOG_TAG, "Successfully removed: "+GPConnection.getInstance(getApplicationContext()).getSelectedApplet().getAID());
			
			/**
			 * Reload
			 */
			GPConnection.getInstance(this).loadAppletsfromCard();
			
			appletNames = new ArrayList<String>();
			updateData(GPConnection.getInstance(getApplicationContext()).getRegistry());
			mListAdapter.clear();
			mListAdapter.addAll(appletNames);

		} catch (GPDeleteException e) {
			MainActivity.log().e(LOG_TAG, "GPDeleteException: ", e);
		} catch (CardException e) {
			MainActivity.log().e(LOG_TAG, "CardException: ", e);
		}
	}

	@Override
	public void onDialogOkClick(DialogFragment dialog) {
		
	}

	@Override
	public void serviceConnected(SEService service) {
		// TODO Auto-generated method stub
		
	}

}
