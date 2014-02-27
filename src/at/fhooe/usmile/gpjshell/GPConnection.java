package at.fhooe.usmile.gpjshell;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

import net.sourceforge.gpj.cardservices.AID;
import net.sourceforge.gpj.cardservices.AIDRegistry;
import net.sourceforge.gpj.cardservices.AIDRegistryEntry;
import net.sourceforge.gpj.cardservices.AIDRegistryEntry.Kind;
import net.sourceforge.gpj.cardservices.CapFile;
import net.sourceforge.gpj.cardservices.GlobalPlatformService;
import net.sourceforge.gpj.cardservices.exceptions.GPDeleteException;
import net.sourceforge.gpj.cardservices.exceptions.GPInstallForLoadException;
import net.sourceforge.gpj.cardservices.exceptions.GPLoadException;
import net.sourceforge.gpj.cardservices.exceptions.GPSecurityDomainSelectionException;
import android.util.Log;

public class GPConnection {

	private static final byte[] SD_SE_KEYS={0x40,0x41,0x42,0x43,0x44,0x45,0x46,0x47,0x48,0x49,0x4a,0x4b,0x4c,0x4d,0x4e,0x48 };
	private static final byte[] I2C_KEYS={0x40,0x41,0x42,0x43,0x44,0x45,0x46,0x47,0x48,0x49,0x4a,0x4b,0x4c,0x4d,0x4e,0x4f };
	
	private static final byte[] UICC_SE_KEY_MAC={(byte)0x46,(byte)0x68,(byte)0xf2,(byte)0xe6,(byte)0x3e,(byte)0x37,(byte)0xec,(byte)0xd5,(byte)0x25,(byte)0xf4,(byte)0x8a,(byte)0x62,(byte)0x0d,(byte)0x3d,(byte)0x29,(byte)0xa7};
	private static final byte[] UICC_SE_KEY_ENC={(byte)0x43,(byte)0xa8,(byte)0xab,(byte)0x4a,(byte)0xd0,(byte)0x9b,(byte)0x1a,(byte)0xfe,(byte)0x1c,(byte)0xf2,(byte)0x25,(byte)0x85,(byte)0x67,(byte)0x3d,(byte)0xa1,(byte)0x7c};
	private static final byte[] UICC_SE_KEY_KEK={(byte)0x9e,(byte)0x1f,(byte)0x8f,(byte)0xc8,(byte)0xc1,(byte)0x5b,(byte)0xe5,(byte)0x9d,(byte)0xfd,(byte)0x07,(byte)0xef,(byte)0x80,(byte)0xea,(byte)0xe9,(byte)0xd6,(byte)0xb5};

	private static final byte[] GEMALTO_UICC = {(byte) 0xa0, 0x00, 0x00, 0x00, 0x18,0x43, 0x4D,(byte)0xFF,0x33,(byte)0xFF,(byte)0xFF,(byte)0x89,(byte)0xC0,0x00,0x00};

	private static final String READER_UICC = "SIM - UICC";
	private static final String READER_UICC2 = "SIM - UICC2";
	private static final String READER_SDDEVICEFIDELITY = "SD - DeviceFidelity SD Card";
	private static final String READER_I2C = "I2c - A7001";
	private static final int UICC_KEY_ID = 32;
	private static final int SD_KEY_ID = 0;
	private static final int I2C_KEY_ID = 0;
	

	
	private static final String LOG_TAG = "GPConnection";
	
	private static GPConnection _INSTANCE = null;
	private GPAppletData data = null;
	private GlobalPlatformService mGPService;
	private String mLastReaderName;

	public static GPConnection getInstance() {
		synchronized (GPConnection.class) {
			if (_INSTANCE == null) {
				_INSTANCE = new GPConnection();
			}
			return _INSTANCE;
		}
	}

	private GPConnection() {
		data = new GPAppletData(null, -1);
	}

//	public void setRegistry(AIDRegistry _registry) {
//		data.setRegistry(_registry);
//	}

	public AIDRegistry getRegistry() {
		return data.getRegistry();
	}

	public void setSelectedApplet(int position) {
		data.setSelectedApplet(position);
	}

	public AIDRegistryEntry getSelectedApplet() {
		return data.getSelectedApplet();
	}

	public void deleteSelectedApplet() throws GPDeleteException, CardException {
		if(data.getSelectedApplet().getKind()== Kind.IssuerSecurityDomain || data.getSelectedApplet().getKind() == Kind.SecurityDomain){
			throw new CardException("Deleting Security domain currently not supported");
		}
		mGPService.deleteAID(data.getSelectedApplet().getAID(), true);
	}

	public void initializeKeys(CardChannel channel) {
		mGPService = new GlobalPlatformService(channel);
		mGPService.setKeys(SD_KEY_ID, SD_SE_KEYS, SD_SE_KEYS, SD_SE_KEYS);
		mGPService.setKeys(I2C_KEY_ID, SD_SE_KEYS, SD_SE_KEYS, SD_SE_KEYS);
		
//		mGPService.setKeys(UICC_KEY_ID, UICC_SE_KEY_ENC, UICC_SE_KEY_MAC,
//					UICC_SE_KEY_KEK);
	}

	public void open() throws GPSecurityDomainSelectionException, CardException {

		mGPService.addAPDUListener(mGPService);
		mGPService.open();
	}

	public void openSecureChannel() throws IllegalArgumentException, CardException {
		if(mLastReaderName == null){
			throw new IllegalArgumentException("No Reader selected");
		}
		openSecureChannel(mLastReaderName);
	}
	
	public void openSecureChannel(String _readerName) throws IllegalArgumentException, CardException {
		mLastReaderName=_readerName;
		if(_readerName.equals(READER_UICC) || _readerName.equals(READER_UICC2)){
			mGPService.openSecureChannel(UICC_KEY_ID, UICC_KEY_ID, GlobalPlatformService.SCP_02_15, 3, true);
		} else if(_readerName.equals(READER_SDDEVICEFIDELITY)){
			mGPService.openSecureChannel(SD_KEY_ID, SD_KEY_ID, 0, 1, false);
		} else if(_readerName.equals(READER_I2C)){
//			mGPService.openSecureChannel(I2C_KEY_ID, SD_KEY_ID, 0, GlobalPlatformService.SCP_02_15, false);
			mGPService.openWithDefaultKeys();
		}
	}
	

	public void installCapFile(String _appletUrl) throws IOException, MalformedURLException, GPInstallForLoadException, GPLoadException, CardException {
		CapFile cpFile = new CapFile(new URL(_appletUrl).openStream(), null);
		
		mGPService.loadCapFile(cpFile, false, false, 255-8, false, false);

		AID p = cpFile.getPackageAID();
		Log.d(LOG_TAG, "Installing Applet with package AID "+p.toString());
		
		for (AID a : cpFile.getAppletAIDs()) {
		    mGPService.installAndMakeSelecatable(p, a,
		            null, (byte) 0,
		            null, null);

			Log.d(LOG_TAG, "Finished installing applet. AID: "+ a.toString());
		}
	}

	public GPAppletData loadAppletsfromCard() throws CardException {
		AIDRegistry registry = mGPService.getStatus();
        
        data.setRegistry(registry);
        
        return data;
//        for (AIDRegistryEntry e : registry) {
//        	AID aid = e.getAID();
//            int numSpaces = (15 - aid.getLength());
//            String spaces = "";
//            String spaces2 = "";
//            for (int i = 0; i < numSpaces; i++) {
//                spaces = spaces + "   ";
//                spaces2 = spaces2 + " ";
//            }
//        }
	}
}
