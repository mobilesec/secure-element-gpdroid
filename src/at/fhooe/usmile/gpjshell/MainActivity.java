package at.fhooe.usmile.gpjshell;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

import net.sourceforge.gpj.cardservices.AID;
import net.sourceforge.gpj.cardservices.AIDRegistry;
import net.sourceforge.gpj.cardservices.AIDRegistryEntry;
import net.sourceforge.gpj.cardservices.GPUtil;
import net.sourceforge.gpj.cardservices.GlobalPlatformService;
import net.sourceforge.gpj.cardservices.exceptions.GPInstallForLoadException;
import net.sourceforge.gpj.cardservices.exceptions.GPLoadException;
import net.sourceforge.gpj.cardservices.exceptions.GPSecurityDomainSelectionException;
import net.sourceforge.gpj.cardservices.interfaces.OpenMobileAPITerminal;

import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import at.fhooe.usmile.gpjshell.db.ChannelSetDataSource;
import at.fhooe.usmile.gpjshell.db.KeysetDataSource;
import at.fhooe.usmile.gpjshell.objects.GPAppletData;
import at.fhooe.usmile.gpjshell.objects.GPChannelSet;
import at.fhooe.usmile.gpjshell.objects.GPKeyset;

public class MainActivity extends Activity implements SEService.CallBack,
		TCPFileResultListener {

	private final static String LOG_TAG = "GPJShell";
	public final static int ACTIVITYRESULT_FILESELECTED = 101;
	public final static int ACTIVITYRESULT_KEYSET_SET = 102;
	public final static int ACTIVITYRESULT_CHANNEL_SET = 103;
	private TextView mLog;

	
	//UI Elements
	private Spinner mReaderSpinner = null;
	private Spinner mKeysetSpinner = null;
	private Spinner mChannelSpinner = null;
	private Button buttonConnect = null;
	private Button mButtonAddKeyset = null;
	private Button mButtonAddChannelSet = null;

	private static LogMe MAIN_Log;

	private OpenMobileAPITerminal mTerminal = null;
	private Button buttonListApplet, buttonSelectApplet;

	private TCPConnection mTCPConnection = null;
	private ArrayAdapter<String> mKeysetAdapter;
	private ArrayAdapter<String> mChannelSetAdapter;

	public enum APDU_COMMAND {
		APDU_INSTALL, APDU_DELETE, APDU_LISTAPPLETS, APDU_SELECT, APDU_SEND
	}

	private String mAppletUrl = null;
	private TextView mFileNameView = null;
	private Map<String, GPKeyset> mKeysetMap = null;
	private List<String> mChannelSets = null;
	private Map<String, GPChannelSet> mChannelSetMap = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		MAIN_Log = new LogMe();

		// LinearLayout layout = new LinearLayout(this);
		// layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT));
		// layout.setOrientation(1);
		


		mFileNameView = (TextView) findViewById(R.id.text1);
		mButtonAddKeyset = (Button) findViewById(R.id.btn_add_keyset);
		mButtonAddKeyset.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, AddKeysetActivity.class);
				startActivityForResult(intent, ACTIVITYRESULT_KEYSET_SET);
			}
		});
		
		mButtonAddChannelSet = (Button) findViewById(R.id.btn_add_channelset);
		mButtonAddChannelSet.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, AddChannelSetActivity.class);
				startActivityForResult(intent, ACTIVITYRESULT_CHANNEL_SET);
			}
		});
		
		loadPreferences();

		mLog = (TextView) findViewById(R.id.log);
		mLog.setMovementMethod(new ScrollingMovementMethod());
		// layout.addView(mLog);
		MAIN_Log.d(LOG_TAG, "Start GPJ Shell");
		GlobalPlatformService.usage();

		mTerminal = new OpenMobileAPITerminal(this, this);
		


		mTCPConnection = new TCPConnection(this, this);
		Thread td = new Thread(mTCPConnection);
		td.start();

	}

	private void loadPreferences() {

		AppPreferences prefs = new AppPreferences(getApplicationContext());
		if (!("".equals(prefs.getSelectedCap()))) {
			mAppletUrl = prefs.getSelectedCap();
			mFileNameView.setText(Uri.parse(mAppletUrl).getLastPathSegment());
		}
	}

	@Override
	protected void onDestroy() {
		if (mTerminal != null) {
			mTerminal.shutdown();
		}
		if(mTCPConnection!=null){
			mTCPConnection.stopConnection();
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onActivityResult(int _requestCode, int _resultCode, Intent _data) {

		MAIN_Log.d(LOG_TAG, "Resultcode " + _resultCode);
		if (_resultCode == Activity.RESULT_OK) {
			
			switch (_requestCode) {
			case ACTIVITYRESULT_FILESELECTED:
				Uri uri = _data.getData();
				MAIN_Log.d(LOG_TAG, "File Uri: " + uri.toString());
				mAppletUrl = uri.toString();
				new AppPreferences(getApplicationContext())
						.saveSelectedCap(mAppletUrl);

				mFileNameView.setText(uri.getLastPathSegment());
				// performCommand(APDU_COMMAND.APDU_INSTALL,
				// mReaderSpinner.getSelectedItemPosition(), uri.toString());
				break;

			case ACTIVITYRESULT_KEYSET_SET:
				GPKeyset keyset = (GPKeyset) _data.getExtras().get(GPKeyset.KEYSET);
				//set actual reader to keyset
				keyset.setReaderName((String) mReaderSpinner.getSelectedItem()); 
				
				KeysetDataSource keySource = new KeysetDataSource(this);
				
				keySource.open();
				keySource.insertKeyset(keyset);
				mKeysetMap = keySource.getKeysets((String) mReaderSpinner.getSelectedItem());
				keySource.close();
				
				addKeysetItemsOnSpinner(Arrays.asList(mKeysetMap.keySet().toArray(new String[0])));
				
				break;
				
			case ACTIVITYRESULT_CHANNEL_SET:
				GPChannelSet channel = (GPChannelSet) _data.getExtras().get(GPChannelSet.CHANNEL_SET);
				channel.setReaderName((String) mReaderSpinner.getSelectedItem()); 
				
				ChannelSetDataSource channelSource = new ChannelSetDataSource(this);
				
				channelSource.open();
				channelSource.insertChannelSet(channel);
				mChannelSetMap = channelSource.getChannelSets();
				channelSource.close();
				
				addKeysetItemsOnSpinner(Arrays.asList(mKeysetMap.keySet().toArray(new String[0])));
				
				
				break;
			default:
				break;
			}
			
		} else if (_resultCode == Activity.RESULT_CANCELED) {
			MAIN_Log.d(LOG_TAG, "file not selected");
		}

	}
	
	public void addKeysetItemsOnSpinner(List<String> keysets) {
		mKeysetSpinner = (Spinner) findViewById(R.id.keyset_spinner);
		
		mKeysetAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, keysets);
		mKeysetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mKeysetSpinner.setAdapter(mKeysetAdapter);

	}
	
	public void addChannelSetItemsOnSpinner(List<String> keysets) {
		mChannelSpinner = (Spinner) findViewById(R.id.channel_spinner);
		
		mChannelSetAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, keysets);
		mChannelSetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mChannelSpinner.setAdapter(mKeysetAdapter);

	}

	// add items into spinner dynamically
	public void addReaderItemsOnSpinner(Reader[] _readers) {

		mReaderSpinner = (Spinner) findViewById(R.id.reader_spinner);
		buttonConnect = (Button) findViewById(R.id.btn_addkeyset_positive);
		buttonListApplet = (Button) findViewById(R.id.btn_addkeyset_negative);
		buttonSelectApplet = (Button) findViewById(R.id.button3);

		if (mReaderSpinner != null) {
			List<String> list = new ArrayList<String>();
			for (int i = 0; i < _readers.length; i++) {
				Reader reader = _readers[i];

				list.add(reader.getName());
			}
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, list);
			dataAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mReaderSpinner.setAdapter(dataAdapter);
			
			//refresh keyset spinner when new reader is selected
			mReaderSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					KeysetDataSource source = new KeysetDataSource(MainActivity.this);
					source.open();
					mKeysetMap = source.getKeysets((String) mReaderSpinner.getSelectedItem());
					source.close();
					addKeysetItemsOnSpinner(Arrays.asList(mKeysetMap.keySet().toArray(new String[0])));
					
					ChannelSetDataSource channelSource = new ChannelSetDataSource(MainActivity.this);
					channelSource.open();
					mChannelSetMap = channelSource.getChannelSets((String) mReaderSpinner.getSelectedItem());
					channelSource.close();
					addChannelSetItemsOnSpinner(Arrays.asList(mChannelSetMap.keySet().toArray(new String[0])));
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
			
			//TODO: add channelselection

			buttonConnect.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					performCommand(APDU_COMMAND.APDU_INSTALL,
							mReaderSpinner.getSelectedItemPosition());

				}
			});
			buttonSelectApplet.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("file/*.cap");
					intent.addCategory(Intent.CATEGORY_OPENABLE);

					startActivityForResult(Intent.createChooser(intent,
							"Select a File to Upload"),
							ACTIVITYRESULT_FILESELECTED);
					// startActivityForResult(intent,
					// ACTIVITYRESULT_FILESELECTED);
					// performCommand(APDU_COMMAND.APDU_INSTALL,
					// mReaderSpinner.getSelectedItemPosition());
				}

			});
			buttonListApplet.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					performCommand(APDU_COMMAND.APDU_LISTAPPLETS,
							mReaderSpinner.getSelectedItemPosition());
				}

			});
		}
	}

	public void serviceConnected(SEService arg0) {
		addReaderItemsOnSpinner(mTerminal.getReaders());
		KeysetDataSource source = new KeysetDataSource(this);
		source.open();
		mKeysetMap = source.getKeysets((String) mReaderSpinner.getSelectedItem());
		source.close();
		addKeysetItemsOnSpinner(Arrays.asList(mKeysetMap.keySet().toArray(new String[0])));
		
		addChannelSetItemsOnSpinner(mChannelSets);
	}

	private void performCommand(APDU_COMMAND _cmd, int _seekReader) {
		performCommand(_cmd, _seekReader, null);
	}

	private void performCommand(APDU_COMMAND _cmd, int _seekReader,
			Object _param) {
		GPKeyset keyset = mKeysetMap.get((String) mKeysetSpinner.getSelectedItem());
		GPChannelSet channelSet = mChannelSetMap.get((String) mChannelSpinner.getSelectedItem());
		
		try {
			Card c = null;
			try {
				mTerminal.setReader(_seekReader);
				c = mTerminal.connect("*");
			} catch (CardException e) {
				if (e.getMessage() != null
						&& e.getMessage().equalsIgnoreCase(
								"SCARD_E_NO_SMARTCARD")) {
					System.err.println("No card in reader "
							+ mTerminal.getName());
				} else {
					e.printStackTrace();
				}
				return;
			}

			System.out
					.println("Found card in terminal: " + mTerminal.getName());
			if (c.getATR() != null) {
				System.out.println("ATR: "
						+ GPUtil.byteArrayToString(c.getATR().getBytes()));
			}
			CardChannel channel = c.openLogicalChannel();
			
			GPConnection.getInstance().initializeKeys(channel, keyset);
			GPConnection.getInstance().open();

			MAIN_Log.d(LOG_TAG,
					"GPShell finished opening OpenMobileAPI Terminal");

			GPConnection.getInstance().openSecureChannel(channelSet.getChannelSet(),
					channelSet.getChannelId(),
					channelSet.getScpVersion(),
					channelSet.getSecurityLevel(),
					channelSet.isGemalto());

			MAIN_Log.d(LOG_TAG, "Secure channel opened");

			switch (_cmd) {
			case APDU_INSTALL:
				if (_param != null && _param instanceof String) {
					installApplet((String)_param);
				} else {
					installApplet();
				}
				break;
			case APDU_DELETE:
			case APDU_LISTAPPLETS:
				listApplets();
				break;
			default:
				break;

			}

		} catch (GPSecurityDomainSelectionException e) {
			MAIN_Log.e(LOG_TAG, "GPSecurityDomainSelectionException ", e);
			e.printStackTrace();
		} catch (GPInstallForLoadException e) {
			MAIN_Log.e(LOG_TAG,
					"GPInstallForLoadException - Applet already installed? ", e);
			e.printStackTrace();
		} catch (CardException e) {
			MAIN_Log.e(LOG_TAG, "CardException ", e);
			e.printStackTrace();
		} catch (MalformedURLException e) {
			MAIN_Log.e(LOG_TAG, "MalformedURLException ", e);
			e.printStackTrace();
		} catch (IOException e) {
			MAIN_Log.e(LOG_TAG, "IOException ", e);
			e.printStackTrace();
		}
	}

	private void installApplet() throws IOException, MalformedURLException,
			GPInstallForLoadException, GPLoadException, CardException {
		installApplet(mAppletUrl);
	}

	private void installApplet(String _url) throws IOException,
			MalformedURLException, GPInstallForLoadException, GPLoadException,
			CardException {
		// String fileUrl =
		// "file:"+Environment.getExternalStorageDirectory().getPath() +
		// "/usmile/instApplet/apdutester.cap";
		if (_url == null) {
			MAIN_Log.d(LOG_TAG, "no Applet selected");
			return;
		}
		if (!(_url).endsWith(".cap")) {
			throw new IOException("Not a valid path or not a cap file");
		}
		// String fileUrl = (String) _param;
		MAIN_Log.d(LOG_TAG, "Loading Applet from " + _url);

		GPConnection.getInstance().installCapFile(_url);

		MAIN_Log.d(LOG_TAG, "Installation successful");
	}

	@Override
	public void fileReceived(String _url) {
		performCommand(APDU_COMMAND.APDU_INSTALL, mReaderSpinner.getSelectedItemPosition(),_url);
	}

	private void listApplets() throws CardException {
		GPAppletData mApplets = GPConnection.getInstance()
				.loadAppletsfromCard();
		Intent intent = new Intent(this, AppletListActivity.class);
		startActivity(intent);

		MAIN_Log.d(
				LOG_TAG,
				"Read all applets from reader "
						+ mReaderSpinner.getSelectedItem() + ". "
						+ mApplets.getRegistry().allPackages().size()
						+ " Applets.");

		// listAppletsToLog();
	}

	private void listAppletsToLog() throws CardException {

		AIDRegistry registry = GPConnection.getInstance().getRegistry();
		for (AIDRegistryEntry e : registry) {
			AID aid = e.getAID();
			int numSpaces = (15 - aid.getLength());
			String spaces = "";
			String spaces2 = "";
			for (int i = 0; i < numSpaces; i++) {
				spaces = spaces + "   ";
				spaces2 = spaces2 + " ";
			}
			MAIN_Log.d(
					LOG_TAG,
					"AID: " + GPUtil.byteArrayToString(aid.getBytes()) + spaces
							+ " "
							+ GPUtil.byteArrayToReadableString(aid.getBytes())
							+ spaces2);
			MAIN_Log.d(LOG_TAG, String.format(" %s LC: %d PR: 0x%02X\n", e
					.getKind().toShortString(), e.getLifeCycleState(), e
					.getPrivileges()));
			for (AID a : e.getExecutableAIDs()) {
				numSpaces = (15 - a.getLength()) * 3;
				spaces = "";
				for (int i = 0; i < numSpaces; i++)
					spaces = spaces + " ";
				MAIN_Log.d(
						LOG_TAG,
						"     "
								+ GPUtil.byteArrayToString(a.getBytes())
								+ spaces
								+ " "
								+ GPUtil.byteArrayToReadableString(a.getBytes()));
			}
			MAIN_Log.d(LOG_TAG, "------------------------------------");
		}

	}

	public static LogMe log() {
		return MAIN_Log;
	}

	public class LogMe {
		public void log(String _tag, String _text) {
			Log.d(_tag, _text);
			mLog.append(Html.fromHtml("<font color=\"#ff0000\">" + _tag
					+ "</font> : " + _text + "<br>"));
		}

		public void e(String _tag, String _text) {
			log(_tag, _text);
		}

		public void e(String _tag, String _text, Exception _e) {
			log(_tag, _text + _e.getMessage());
		}

		public void d(String _tag, String _text) {
			log(_tag, _text);
		}

		public void i(String _tag, String _text) {
			log(_tag, _text);
		}
	}

}
