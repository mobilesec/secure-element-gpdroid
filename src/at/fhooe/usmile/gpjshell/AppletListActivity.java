package at.fhooe.usmile.gpjshell;

import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.CardException;

import net.sourceforge.gpj.cardservices.AIDRegistry;
import net.sourceforge.gpj.cardservices.AIDRegistryEntry;
import net.sourceforge.gpj.cardservices.GPUtil;
import net.sourceforge.gpj.cardservices.exceptions.GPDeleteException;
import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AppletListActivity extends Activity implements AppletDetailActivity.NoticeAppletEventListener{

	private static final String LOG_TAG = "AppletListActivity";
	private List<String> appletNames = null;
	private ArrayAdapter<String> mListAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.applet_list);

		final ListView listview = (ListView) findViewById(R.id.listview);

		setListData(listview);

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
//				final String item = (String) parent.getItemAtPosition(position);

				GPConnection.getInstance().setSelectedApplet(position);

				showAppletDetailsDialog();
			}

		});

		
	}

	private void setListData(final ListView listview) {
		AIDRegistry registry = GPConnection.getInstance().getRegistry();
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
			GPConnection.getInstance().deleteSelectedApplet();
			MainActivity.log().log(LOG_TAG, "Successfully removed: "+GPConnection.getInstance().getSelectedApplet().getAID());
			GPConnection.getInstance().loadAppletsfromCard();
			
			appletNames = new ArrayList<String>();
			updateData(GPConnection.getInstance().getRegistry());
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

}
