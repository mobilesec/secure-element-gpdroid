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
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

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
import at.fhooe.usmile.gpjshell.objects.GPConstants;
import at.fhooe.usmile.gpjshell.objects.GPKeyset;

public class MainActivity extends Activity implements SEService.CallBack,
		TCPFileResultListener {

	private final static String LOG_TAG = "GPJShell";
	public final static int ACTIVITYRESULT_FILESELECTED = 101;
	public final static int ACTIVITYRESULT_KEYSET_SET = 102;
	public final static int ACTIVITYRESULT_CHANNEL_SET = 103;
	public final static int ACTIVITYRESULT_INSTALL_PARAM_SET = 104;
	public final static int ACTIVITYRESULT_GET_DATA = 105;
	private TextView mLog;

	// UI Elements
	private Spinner mReaderSpinner = null;
	private Spinner mKeysetSpinner = null;
	private Spinner mChannelSpinner = null;
	private Button buttonConnect = null;
	private Button mButtonAddKeyset = null;
	private Button mButtonAddChannelSet = null;
	private Button mButtonRemoveKeyset = null;
	private Button mButtonRemoveChannelset = null;
	private Button mButtonGetData = null;

	private static LogMe MAIN_Log;

	private OpenMobileAPITerminal mTerminal = null;
	private Button buttonListApplet, buttonSelectApplet;

	private TCPConnection mTCPConnection = null;
	private ArrayAdapter<String> mKeysetAdapter;
	private ArrayAdapter<String> mChannelSetAdapter;

	public enum APDU_COMMAND {
		APDU_INSTALL, APDU_DELETE, APDU_LISTAPPLETS, APDU_SELECT, APDU_SEND, APDU_GET_DATA
	}

	private String mAppletUrl = null;
	private TextView mFileNameView = null;
	private Map<String, GPKeyset> mKeysetMap = null;
	private Map<String, GPChannelSet> mChannelSetMap = null;
	private int mP1 = 0;
	private int mP2 = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		MAIN_Log = new LogMe();

		mButtonGetData = (Button) findViewById(R.id.btn_get_data);

		mFileNameView = (TextView) findViewById(R.id.text1);
		mButtonAddKeyset = (Button) findViewById(R.id.btn_add_keyset);
		mButtonAddKeyset.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						AddKeysetActivity.class);
				if (mReaderSpinner != null)
					intent.putExtra("readername", mReaderSpinner
							.getSelectedItem().toString());
				startActivityForResult(intent, ACTIVITYRESULT_KEYSET_SET);
			}
		});

		mButtonAddChannelSet = (Button) findViewById(R.id.btn_add_channelset);
		mButtonAddChannelSet.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						AddChannelSetActivity.class);
				startActivityForResult(intent, ACTIVITYRESULT_CHANNEL_SET);
			}
		});

		mButtonRemoveChannelset = (Button) findViewById(R.id.btn_remove_channelset);
		mButtonRemoveChannelset.setOnClickListener(new View.OnClickListener() {
			// Remove actual selected channelset
			@Override
			public void onClick(View v) {
				GPChannelSet channel = mChannelSetMap.get(mChannelSpinner
						.getSelectedItem());
				if (channel != null) {
					ChannelSetDataSource channelSource = new ChannelSetDataSource(
							MainActivity.this);
					channelSource.open();
					channelSource.remove(channel.getChannelNameString());
					channelSource.close();

					mChannelSetAdapter.remove(channel.getChannelNameString());
					mChannelSetAdapter.notifyDataSetChanged();
				}
			}
		});

		mButtonRemoveKeyset = (Button) findViewById(R.id.btn_remove_keyset);
		mButtonRemoveKeyset.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				GPKeyset keyset = mKeysetMap.get(mKeysetSpinner
						.getSelectedItem());
				if (keyset != null) {
					KeysetDataSource keysetSource = new KeysetDataSource(
							MainActivity.this);
					keysetSource.open();
					keysetSource.remove(keyset.getUniqueID());
					keysetSource.close();

					mKeysetAdapter.remove(keyset.getName());
					mKeysetAdapter.notifyDataSetChanged();
				}
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
		if (mTCPConnection != null) {
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
				GPKeyset keyset = (GPKeyset) _data.getExtras().get(
						GPKeyset.KEYSET);
				// set actual reader to keyset - each keyset is bound to a
				// reader
				keyset.setReaderName((String) mReaderSpinner.getSelectedItem());

				KeysetDataSource keySource = new KeysetDataSource(this);

				keySource.open();
				keySource.insertKeyset(keyset);
				mKeysetMap = keySource.getKeysets((String) mReaderSpinner
						.getSelectedItem());
				keySource.close();

				addKeysetItemsOnSpinner(Arrays.asList(mKeysetMap.keySet()
						.toArray(new String[0])));

				break;

			case ACTIVITYRESULT_CHANNEL_SET:
				GPChannelSet channel = (GPChannelSet) _data.getExtras().get(
						GPChannelSet.CHANNEL_SET);

				ChannelSetDataSource channelSource = new ChannelSetDataSource(
						this);

				channelSource.open();
				channelSource.insertChannelSet(channel);
				mChannelSetMap = channelSource.getChannelSets();
				channelSource.close();

				addChannelSetItemsOnSpinner(Arrays.asList(mChannelSetMap
						.keySet().toArray(new String[0])));

				break;

			case ACTIVITYRESULT_INSTALL_PARAM_SET:
				byte[] params = _data.getExtras().getByteArray("params");
				byte privileges = _data.getExtras().getByte("privileges");

				try {
					performCommand(APDU_COMMAND.APDU_INSTALL,
							mReaderSpinner.getSelectedItemPosition(), params,
							privileges);
				} catch (Exception e) {
					MAIN_Log.e(LOG_TAG, "Error while installing: ", e);
					e.printStackTrace();
				}
				break;

			case ACTIVITYRESULT_GET_DATA:
				mP1 = _data.getExtras().getInt("p1");
				mP2 = _data.getExtras().getInt("p2");
				MAIN_Log.d("Parameters: ", "P1=" + mP1 + ", P2=" + mP2);
				performCommand(APDU_COMMAND.APDU_GET_DATA,
						mReaderSpinner.getSelectedItemPosition(), null,
						(byte) 0);
				break;
			default:
				break;
			}

		} else if (_resultCode == Activity.RESULT_CANCELED) {
			MAIN_Log.d(LOG_TAG, "Result not valid");
		}

	}

	/**
	 * sets items to the spinner of keysets
	 * 
	 * @param keysets
	 *            keysets from DB according to the set smartcard
	 */
	public void addKeysetItemsOnSpinner(List<String> keysets) {
		mKeysetSpinner = (Spinner) findViewById(R.id.keyset_spinner);

		// add list to a new initialized list, else elements are not removable
		// from adapter later
		List<String> keysetList = new ArrayList<String>();
		keysetList.addAll(keysets);

		mKeysetAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, keysetList);
		mKeysetAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mKeysetSpinner.setAdapter(mKeysetAdapter);
		mKeysetAdapter.notifyDataSetChanged();
	}

	/**
	 * sets the channelsettings to the channelspinner
	 * 
	 * @param channelSets
	 *            all available channelsets from DB
	 */
	public void addChannelSetItemsOnSpinner(List<String> channelSets) {
		mChannelSpinner = (Spinner) findViewById(R.id.channel_spinner);

		// add list to a new initialized list, else elements are not removable
		// from adapter later
		List<String> channelSetList = new ArrayList<String>();
		channelSetList.addAll(channelSets);

		mChannelSetAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, channelSetList);
		mChannelSetAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mChannelSpinner.setAdapter(mChannelSetAdapter);
		mChannelSetAdapter.notifyDataSetChanged();
	}

	// add items into spinner dynamically
	public void addReaderItemsOnSpinner(Reader[] _readers) {

		mReaderSpinner = (Spinner) findViewById(R.id.reader_spinner);
		buttonConnect = (Button) findViewById(R.id.btn_install_applet);
		buttonListApplet = (Button) findViewById(R.id.btn_list_applets);
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

			// refresh keyset spinner when new reader is selected
			mReaderSpinner
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							KeysetDataSource source = new KeysetDataSource(
									MainActivity.this);
							source.open();
							mKeysetMap = source
									.getKeysets((String) mReaderSpinner
											.getSelectedItem());
							source.close();
							addKeysetItemsOnSpinner(Arrays.asList(mKeysetMap
									.keySet().toArray(new String[0])));

							ChannelSetDataSource channelSource = new ChannelSetDataSource(
									MainActivity.this);
							channelSource.open();
							mChannelSetMap = channelSource.getChannelSets();
							channelSource.close();
							addChannelSetItemsOnSpinner(Arrays
									.asList(mChannelSetMap.keySet().toArray(
											new String[0])));
						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {
						}
					});

			buttonConnect.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this,
							SetInstallParamActivity.class);
					startActivityForResult(intent,
							ACTIVITYRESULT_INSTALL_PARAM_SET);

				}
				// @Override
				// public void onClick(View arg0) {
				// performCommand(APDU_COMMAND.APDU_INSTALL,
				// mReaderSpinner.getSelectedItemPosition());
				//
				// }
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
							mReaderSpinner.getSelectedItemPosition(), null,
							(byte) 0);

				}
			});

			mButtonGetData.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this,
							GetDataActivity.class);
					startActivityForResult(intent, ACTIVITYRESULT_GET_DATA);

				}
			});
		}
	}

	public void serviceConnected(SEService arg0) {

		addReaderItemsOnSpinner(mTerminal.getReaders());

		// --------- ADD DEFAULT KEYS TO DB -------------

		Reader[] readers = mTerminal.getReaders();

		KeysetDataSource keysetSource = new KeysetDataSource(this);
		keysetSource.open();
		for (int i = 1; i <= readers.length; i++) {
			Reader reader = readers[i - 1];
			// set unique id to -1. it will be set by DB later, because -1 will
			// not be found
			GPKeyset defaultKeyset = new GPKeyset(-1, "Default", 0, 0,
					GPUtils.byteArrayToString(GPConstants.DEFAULT_KEYS),
					GPUtils.byteArrayToString(GPConstants.DEFAULT_KEYS),
					GPUtils.byteArrayToString(GPConstants.DEFAULT_KEYS),
					reader.getName());

			keysetSource.insertKeyset(defaultKeyset);
		}

		// initialize keysetmap
		mKeysetMap = keysetSource.getKeysets((String) mReaderSpinner
				.getSelectedItem());

		keysetSource.close();

		ChannelSetDataSource channelSource = new ChannelSetDataSource(this);
		channelSource.open();
		channelSource.insertChannelSet(new GPChannelSet("Default",
				GlobalPlatformService.SCP_ANY, 3, false));

		// initialize channelmap
		mChannelSetMap = channelSource.getChannelSets();
		channelSource.close();

		// ------------ END ADDING DEFAULT ------------
	}

	/**
	 * performs selected command from APDU enum params and privileges are
	 * necessary for new installations, else they may be set to null
	 * 
	 * @param _cmd
	 *            APDU-enum command
	 * @param _seekReader
	 *            actual selected reader
	 * @param params
	 *            necessary for installations, else null
	 * @param privileges
	 *            necessary for installations, else (byte) 0
	 */
	private void performCommand(APDU_COMMAND _cmd, int _seekReader,
			byte[] params, byte privileges) {
		GPKeyset keyset = mKeysetMap.get((String) mKeysetSpinner
				.getSelectedItem());
		GPChannelSet channelSet = mChannelSetMap.get((String) mChannelSpinner
				.getSelectedItem());

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

			// opening channel with index of keyset - is unique
			GPConnection.getInstance().openSecureChannel(keyset.getID(),
					keyset.getID(), keyset.getVersion(),
					channelSet.getScpVersion(), channelSet.getSecurityLevel(),
					channelSet.isGemalto());

			MAIN_Log.d(LOG_TAG, "Secure channel opened");

			switch (_cmd) {
			case APDU_INSTALL:
				if (params != null) {
					installApplet(mAppletUrl, params, privileges);
				} else {
					installApplet();
				}
				break;
			case APDU_DELETE:
			case APDU_LISTAPPLETS:
				listApplets();
				break;
			case APDU_GET_DATA:
				// parameters will be set in onActivtyResult;

				
				ResponseAPDU response = GPConnection.getInstance().getData(mP1, mP2);
				MAIN_Log.d("Response",
						GPUtils.byteArrayToString(response.getData()));

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

	@Deprecated
	private void performCommand(APDU_COMMAND _cmd, int _seekReader,
			Object _param) {
		GPKeyset keyset = mKeysetMap.get((String) mKeysetSpinner
				.getSelectedItem());
		GPChannelSet channelSet = mChannelSetMap.get((String) mChannelSpinner
				.getSelectedItem());

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

			// opening channel with index of keyset - is unique
			GPConnection.getInstance().openSecureChannel(keyset.getID(),
					keyset.getID(), keyset.getVersion(),
					channelSet.getScpVersion(), channelSet.getSecurityLevel(),
					channelSet.isGemalto());

			MAIN_Log.d(LOG_TAG, "Secure channel opened");

			switch (_cmd) {
			case APDU_INSTALL:
				if (_param != null && _param instanceof String) {
					installApplet((String) _param);
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

	/**
	 * installs an applet from preset url
	 * 
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws GPInstallForLoadException
	 * @throws GPLoadException
	 * @throws CardException
	 */
	private void installApplet() throws IOException, MalformedURLException,
			GPInstallForLoadException, GPLoadException, CardException {
		installApplet(mAppletUrl);
	}

	/**
	 * installs an applet from preset url
	 * 
	 * @param _url
	 *            where the applet is located
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws GPInstallForLoadException
	 * @throws GPLoadException
	 * @throws CardException
	 */
	private void installApplet(String _url) throws IOException,
			MalformedURLException, GPInstallForLoadException, GPLoadException,
			CardException {
		installApplet(_url, null, (byte) 0);
	}

	private void installApplet(String _url, byte[] params, byte privileges)
			throws IOException, MalformedURLException,
			GPInstallForLoadException, GPLoadException, CardException {
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

		GPConnection.getInstance().installCapFile(_url, params, privileges);

		MAIN_Log.d(LOG_TAG, "Installation successful");
	}

	@Override
	public void fileReceived(String _url) {
		mAppletUrl = _url;
		performCommand(APDU_COMMAND.APDU_INSTALL,
				mReaderSpinner.getSelectedItemPosition(), null, (byte) 0);
	}

	/**
	 * lists all applets installed on the currently selected smartcard
	 * 
	 * @throws CardException
	 */
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
