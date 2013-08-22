package at.fhooe.usmile.gpjshell;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

import net.sourceforge.gpj.cardservices.AID;
import net.sourceforge.gpj.cardservices.AIDRegistry;
import net.sourceforge.gpj.cardservices.AIDRegistryEntry;
import net.sourceforge.gpj.cardservices.CapFile;
import net.sourceforge.gpj.cardservices.GPUtil;
import net.sourceforge.gpj.cardservices.GlobalPlatformService;
import net.sourceforge.gpj.cardservices.exceptions.GPInstallForLoadException;
import net.sourceforge.gpj.cardservices.exceptions.GPSecurityDomainSelectionException;

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

	private LogMe mMyLog;
	private GlobalPlatformService mGPService;
	private OpenMobileAPITerminal mTerminal = null;
	private Button buttonListApplet;
	
	public enum APDU_COMMAND {
		APDU_INSTALL,
		APDU_DELETE,
		APDU_LISTAPPLETS
	}

	private static final byte[] SD_SE_KEYS={0x40,0x41,0x42,0x43,0x44,0x45,0x46,0x47,0x48,0x49,0x4a,0x4b,0x4c,0x4d,0x4e,0x48 };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mMyLog = new LogMe();

//		LinearLayout layout = new LinearLayout(this);
//		layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
//				LayoutParams.WRAP_CONTENT));
//		layout.setOrientation(1);


		mLog = (TextView) findViewById(R.id.log);
		mLog.setMovementMethod(new ScrollingMovementMethod());
//		layout.addView(mLog);
		mMyLog.d(LOG_TAG, "Start GPJ Shell");
		GlobalPlatformService.usage();

		mTerminal = new OpenMobileAPITerminal(this, this);
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
	public void onActivityResult(int _requestCode, int _resultCode, Intent _data){

        mMyLog.d(LOG_TAG, "Resultcode " +_resultCode);
        if (_resultCode == Activity.RESULT_OK) {

                if (_requestCode == ACTIVITYRESULT_FILESELECTED) {
                        Uri uri = _data.getData();
                        mMyLog.d(LOG_TAG, "File Uri: " + uri.toString());
        				performCommand(APDU_COMMAND.APDU_INSTALL, mReaderSpinner.getSelectedItemPosition(), uri.toString());
                }                
        } else if (_resultCode == Activity.RESULT_CANCELED) {
                mMyLog.d(LOG_TAG, "file not selected");
        }

	}

	// add items into spinner dynamically
	public void addReaderItemsOnSpinner(Reader[] _readers) {

		mReaderSpinner = (Spinner) findViewById(R.id.reader_spinner);
		buttonConnect = (Button) findViewById(R.id.button1);
		buttonListApplet = (Button) findViewById(R.id.button2);

		if (mReaderSpinner != null) {
			List<String> list = new ArrayList<String>();
			for (Reader reader : _readers) {
				list.add(reader.getName());
			}
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, list);
			dataAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mReaderSpinner.setAdapter(dataAdapter);

			buttonConnect.setOnClickListener(new OnClickListener() {


				@Override
				public void onClick(View v) {

				    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				    intent.setType("file/*.cap");
				    intent.addCategory(Intent.CATEGORY_OPENABLE);

				    startActivityForResult( Intent.createChooser(intent, "Select a File to Upload"),
				    		ACTIVITYRESULT_FILESELECTED);
//				    startActivityForResult(intent, ACTIVITYRESULT_FILESELECTED);
//					performCommand(APDU_COMMAND.APDU_INSTALL, mReaderSpinner.getSelectedItemPosition());
				}

			});
			buttonListApplet.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					performCommand(APDU_COMMAND.APDU_LISTAPPLETS, mReaderSpinner.getSelectedItemPosition());
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
	private void performCommand(APDU_COMMAND _cmd, int _seekReader, Object _param) {
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
				} else
					e.printStackTrace();
			}

			System.out
					.println("Found card in terminal: " + mTerminal.getName());
			if(c.getATR()!=null){
				System.out.println("ATR: "
						+ GPUtil.byteArrayToString(c.getATR().getBytes()));
			}
			CardChannel channel = c.openLogicalChannel();

			mGPService = new GlobalPlatformService(channel);

			mGPService.addAPDUListener(mGPService);
			mGPService.open();

			mMyLog.d(LOG_TAG, "GPShell finished opening OpenMobileAPI Terminal");
			mGPService.setKeys(0, SD_SE_KEYS, SD_SE_KEYS, SD_SE_KEYS);
			mGPService.openSecureChannel(0, 0, 0, 1, false);

			mMyLog.d(LOG_TAG, "Secure channel opened");
			
			switch(_cmd){
			case APDU_INSTALL:
//				String fileUrl = "file:"+Environment.getExternalStorageDirectory().getPath() + "/usmile/instApplet/apdutester.cap";
				if(!(_param instanceof String) || !((String)_param).endsWith(".cap")){
					throw new IOException("Not a valid path or not a cap file");
				}
				String fileUrl = (String) _param;
				mMyLog.d(LOG_TAG, "Loading Applet from "+fileUrl);
				
				CapFile cpFile = new CapFile(new URL(fileUrl).openStream(), null);
				
				mGPService.loadCapFile(cpFile, false, false, 255-8, false, false);

	            AID p = cpFile.getPackageAID();
	            mMyLog.d(LOG_TAG, "Installing Applet with package AID "+p.toString());
				
	            for (AID a : cpFile.getAppletAIDs()) {
	                mGPService.installAndMakeSelecatable(p, a,
	                        null, (byte) 0,
	                        null, null);

	    			mMyLog.d(LOG_TAG, "Finished installing applet. AID: "+ a.toString());
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
			mMyLog.e(LOG_TAG, "GPSecurityDomainSelectionException ", e);
			e.printStackTrace();
		} catch (GPInstallForLoadException e){
			mMyLog.e(LOG_TAG, "GPInstallForLoadException - Applet already installed? ", e);
			e.printStackTrace();
		} catch (CardException e) {
			mMyLog.e(LOG_TAG, "CardException ", e);
			e.printStackTrace();
		} catch (MalformedURLException e) {
			mMyLog.e(LOG_TAG, "MalformedURLException ", e);
			e.printStackTrace();
		} catch (IOException e) {
			mMyLog.e(LOG_TAG, "IOException ", e);
			e.printStackTrace();
		}
	}

	private void listApplets() throws CardException{

        AIDRegistry registry = mGPService.getStatus();
        for (AIDRegistryEntry e : registry) {
            AID aid = e.getAID();
            int numSpaces = (15 - aid.getLength());
            String spaces = "";
            String spaces2 = "";
            for (int i = 0; i < numSpaces; i++) {
                spaces = spaces + "   ";
                spaces2 = spaces2 + " ";
            }
            mMyLog.d(LOG_TAG, "AID: "
                    + GPUtil.byteArrayToString(aid.getBytes())
                    + spaces
                    + " "
                    + GPUtil.byteArrayToReadableString(aid
                            .getBytes()) + spaces2);
            mMyLog.d(LOG_TAG, String.format(" %s LC: %d PR: 0x%02X\n", e
                    .getKind().toShortString(), e
                    .getLifeCycleState(), e.getPrivileges()));
            for (AID a : e.getExecutableAIDs()) {
                numSpaces = (15 - a.getLength()) * 3;
                spaces = "";
                for (int i = 0; i < numSpaces; i++)
                    spaces = spaces + " ";
                mMyLog.d(LOG_TAG, "     "
                                + GPUtil.byteArrayToString(a
                                        .getBytes())
                                + spaces
                                + " "
                                + GPUtil
                                        .byteArrayToReadableString(a
                                                .getBytes()));
            }
            mMyLog.d(LOG_TAG, "------------------------------------");
        }

	}
	private class LogMe {
		private void log(String _tag, String _text) {
			Log.d(_tag, _text);
			mLog.append(Html.fromHtml("<font color=\"#ff0000\">" + _tag
					+ "</font> : " + _text + "<br>"));
		}

		private void e(String _tag, String _text) {
			log(_tag, _text);
		}

		private void e(String _tag, String _text, Exception _e) {
			log(_tag, _text + _e.getMessage());
		}

		private void d(String _tag, String _text) {
			log(_tag, _text);
		}

		private void i(String _tag, String _text) {
			log(_tag, _text);
		}
	}

}
