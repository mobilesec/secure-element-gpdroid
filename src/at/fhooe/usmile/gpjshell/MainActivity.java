package at.fhooe.usmile.gpjshell;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity implements SEService.CallBack {

	private final static String LOG_TAG = "GPJShell";
	private final static int ACTIVITYRESULT_FILESELECTED = 101;
	private TextView mLog;

	private Spinner mReaderSpinner = null;
	private Button buttonConnect = null;

	private static LogMe MAIN_Log;
	
	private OpenMobileAPITerminal mTerminal = null;
	private Button buttonListApplet, buttonSelectApplet;

	public enum APDU_COMMAND {
		APDU_INSTALL, APDU_DELETE, APDU_LISTAPPLETS, APDU_SELECT, APDU_SEND
	}

	private String mAppletUrl = null;
	private TextView mFileNameView = null;

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
		
		loadPreferences();
		
		mLog = (TextView) findViewById(R.id.log);
		mLog.setMovementMethod(new ScrollingMovementMethod());
		// layout.addView(mLog);
		MAIN_Log.d(LOG_TAG, "Start GPJ Shell");
		GlobalPlatformService.usage();

		mTerminal = new OpenMobileAPITerminal(this, this);
	}

	private void loadPreferences() {

		AppPreferences prefs = new AppPreferences(getApplicationContext());
		if(!("".equals(prefs.getSelectedCap()))){
			mAppletUrl = prefs.getSelectedCap();
			mFileNameView.setText(Uri.parse(mAppletUrl).getLastPathSegment());
		}
	}

	@Override
	protected void onDestroy() {
		if (mTerminal != null) {
			mTerminal.shutdown();
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

			if (_requestCode == ACTIVITYRESULT_FILESELECTED) {
				Uri uri = _data.getData();
				MAIN_Log.d(LOG_TAG, "File Uri: " + uri.toString());
				mAppletUrl = uri.toString();
				new AppPreferences(getApplicationContext()).saveSelectedCap(mAppletUrl);
				
				mFileNameView.setText(uri.getLastPathSegment());
				// performCommand(APDU_COMMAND.APDU_INSTALL,
				// mReaderSpinner.getSelectedItemPosition(), uri.toString());
			}
		} else if (_resultCode == Activity.RESULT_CANCELED) {
			MAIN_Log.d(LOG_TAG, "file not selected");
		}

	}

	// add items into spinner dynamically
	public void addReaderItemsOnSpinner(Reader[] _readers) {

		mReaderSpinner = (Spinner) findViewById(R.id.reader_spinner);
		buttonConnect = (Button) findViewById(R.id.button1);
		buttonListApplet = (Button) findViewById(R.id.button2);
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
	}

	private void performCommand(APDU_COMMAND _cmd, int _seekReader) {
		performCommand(_cmd, _seekReader, null);
	}

	private void performCommand(APDU_COMMAND _cmd, int _seekReader,
			Object _param) {
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

			GPConnection.getInstance().initializeKeys(channel);
			GPConnection.getInstance().open();

			MAIN_Log.d(LOG_TAG, "GPShell finished opening OpenMobileAPI Terminal");

			GPConnection.getInstance().openSecureChannel(
					(String) mReaderSpinner.getSelectedItem());

			MAIN_Log.d(LOG_TAG, "Secure channel opened");

			switch (_cmd) {
			case APDU_INSTALL:
				installApplet();
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
		// String fileUrl =
		// "file:"+Environment.getExternalStorageDirectory().getPath() +
		// "/usmile/instApplet/apdutester.cap";
		if (mAppletUrl == null) {
			MAIN_Log.d(LOG_TAG, "no Applet selected");
			return;
		}
		if (!(mAppletUrl).endsWith(".cap")) {
			throw new IOException("Not a valid path or not a cap file");
		}
		// String fileUrl = (String) _param;
		MAIN_Log.d(LOG_TAG, "Loading Applet from " + mAppletUrl);

		GPConnection.getInstance().installCapFile(mAppletUrl);

		MAIN_Log.d(LOG_TAG, "Installation successful");
	}

	private void listApplets() throws CardException {
		GPAppletData mApplets = GPConnection.getInstance().loadAppletsfromCard();
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
	
	public static LogMe log(){
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
