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
import at.fhooe.usmile.gpjshell.objects.GPAppletData;
import at.fhooe.usmile.gpjshell.objects.GPKeyset;

public class GPConnection {
	private static final String LOG_TAG = "GPConnection";
	
	private static GPConnection _INSTANCE = null;
	private GPAppletData data = null;
	private GlobalPlatformService mGPService;
	
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

	public void initializeKeys(CardChannel channel, GPKeyset keyset) {
		mGPService = new GlobalPlatformService(channel);
		mGPService.setKeys(keyset.getID(), keyset.getENCByte(), keyset.getMACByte(), keyset.getKEKByte());
//		mGPService.setKeys(UICC_KEY_ID, UICC_SE_KEY_ENC, UICC_SE_KEY_MAC,
//					UICC_SE_KEY_KEK);
	}

	public void open() throws GPSecurityDomainSelectionException, CardException {

		mGPService.addAPDUListener(mGPService);
		mGPService.open();
	}

//	public void openSecureChannel() throws IllegalArgumentException, CardException {
//		if(mLastReaderName == null){
//			throw new IllegalArgumentException("No Reader selected");
//		}
//		openSecureChannel(mLastReaderName);
//	}
//	
//	public void openSecureChannel(String _readerName) throws IllegalArgumentException, CardException {
//		mLastReaderName=_readerName;
//		if(_readerName.equals(READER_UICC)){
//			mGPService.openSecureChannel(UICC_KEY_ID, 0, 0, 3, true);
//		} else if(_readerName.equals(READER_SDDEVICEFIDELITY)){
//			mGPService.openSecureChannel(SD_KEY_ID, 0, 0, 1, false);
//		}
//	}
	
	public void openSecureChannel(int uniqueIndex, int keyId, int keyVersion, int scpVersion, int securityLevel, boolean gemalto) throws IllegalArgumentException, CardException {
		mGPService.openSecureChannel(uniqueIndex, keyId, keyVersion, scpVersion, securityLevel, gemalto);
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
	
	public void installCapFile(String _appletUrl, byte[] params, byte privileges) throws IOException, MalformedURLException, GPInstallForLoadException, GPLoadException, CardException {
		CapFile cpFile = new CapFile(new URL(_appletUrl).openStream(), null);
		
		mGPService.loadCapFile(cpFile, false, false, 255-8, true, false);

		AID p = cpFile.getPackageAID();
		Log.d(LOG_TAG, "Installing Applet with package AID "+p.toString());
		
		for (AID a : cpFile.getAppletAIDs()) {
		    mGPService.installAndMakeSelecatable(p, a,
		            null, privileges,
		            params, null);

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
