package at.fhooe.usmile.gpjshell;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

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
		if (data.getSelectedApplet().getKind() == Kind.IssuerSecurityDomain
				|| data.getSelectedApplet().getKind() == Kind.SecurityDomain) {
			throw new CardException(
					"Deleting Security domain currently not supported");
		}
		mGPService.deleteAID(data.getSelectedApplet().getAID(), true);
	}

	/**
	 * initializes the keys for the smartcard to be used later. it uses a predefined keyset
	 * @param channel 
	 * @param keyset predefined keyset
	 */
	public void initializeKeys(CardChannel channel, GPKeyset keyset) {
		mGPService = new GlobalPlatformService(channel);
		mGPService.setKeys(keyset.getID(), keyset.getENCByte(),
				keyset.getMACByte(), keyset.getKEKByte());
	}

	public void open() throws GPSecurityDomainSelectionException, CardException {

		mGPService.addAPDUListener(mGPService);
		mGPService.open();
	}

	/**
	 * opens a secure channel with id from keyset and channel-settings
	 * @param uniqueIndex - unique ID of keyset
	 * @param keyId - keyID of keyset
	 * @param keyVersion - version of keyset
	 * @param scpVersion
	 * @param securityLevel
	 * @param gemalto
	 * @throws IllegalArgumentException
	 * @throws CardException
	 */
	public void openSecureChannel(int uniqueIndex, int keyId, int keyVersion,
			int scpVersion, int securityLevel, boolean gemalto)
			throws IllegalArgumentException, CardException {
		mGPService.openSecureChannel(uniqueIndex, keyId, keyVersion,
				scpVersion, securityLevel, gemalto);
	}

	public ResponseAPDU getData(CommandAPDU apdu) throws IllegalStateException, CardException {
		return mGPService.transmit(apdu);
	}
	

	/**
	 * Installs a selected cap-File (applet) to the smartcard. This method used
	 * predefined parameters and privileges for installation
	 * 
	 * @param _appletUrl
	 *            - url of the applet
	 * @param params
	 *            - install parameters
	 * @param privileges
	 *            - privileges used for installation
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws GPInstallForLoadException
	 * @throws GPLoadException
	 * @throws CardException
	 */
	public void installCapFile(String _appletUrl, byte[] params, byte privileges)
			throws IOException, MalformedURLException,
			GPInstallForLoadException, GPLoadException, CardException {
		CapFile cpFile = new CapFile(new URL(_appletUrl).openStream(), null);

		mGPService.loadCapFile(cpFile, false, false, 255 - 8, true, false);

		AID p = cpFile.getPackageAID();
		Log.d(LOG_TAG, "Installing Applet with package AID " + p.toString());

		for (AID a : cpFile.getAppletAIDs()) {
			mGPService.installAndMakeSelecatable(p, a, null, privileges,
					params, null);

			Log.d(LOG_TAG, "Finished installing applet. AID: " + a.toString());
		}
	}

	public GPAppletData loadAppletsfromCard() throws CardException {
		AIDRegistry registry = mGPService.getStatus();

		data.setRegistry(registry);

		return data;
	}
}
