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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import net.sourceforge.gpj.cardservices.AID;
import net.sourceforge.gpj.cardservices.AIDRegistryEntry;
import net.sourceforge.gpj.cardservices.AIDRegistryEntry.Kind;
import net.sourceforge.gpj.cardservices.CapFile;
import net.sourceforge.gpj.cardservices.GPUtil;
import net.sourceforge.gpj.cardservices.GlobalPlatformService;
import net.sourceforge.gpj.cardservices.exceptions.GPDeleteException;
import net.sourceforge.gpj.cardservices.exceptions.GPInstallForLoadException;
import net.sourceforge.gpj.cardservices.exceptions.GPLoadException;
import net.sourceforge.gpj.cardservices.exceptions.GPSecurityDomainSelectionException;
import net.sourceforge.gpj.cardservices.interfaces.OpenMobileAPITerminal;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import at.fhooe.usmile.gpjshell.objects.GPAppletData;
import at.fhooe.usmile.gpjshell.objects.GPChannelSet;
import at.fhooe.usmile.gpjshell.objects.GPKeyset;

public class GPConnection {
	
	private static final String LOG_TAG = "GPConnection";
	
	private static GPConnection _INSTANCE = null;
	private GPAppletData data = null;
	private GlobalPlatformService mGPService;

	private Context mContext;

	public static GPConnection getInstance(Context _con) {	
		synchronized (GPConnection.class) {
			if (_INSTANCE == null) {
				_INSTANCE = new GPConnection(_con);
			}
			return _INSTANCE;
		}
	}

	private GPConnection(Context _con) {
		mContext = _con;
		data = new GPAppletData(null, -1);
	}


	public List<AIDRegistryEntry> getRegistry() {
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
		data.removeSelectedAppletFromList();
	}

	public void deleteAID(AID deleteAID) throws GPDeleteException, CardException {

		mGPService.deleteAID(deleteAID, true);
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

	
	public ResponseAPDU getData(int p1, int p2) throws IllegalStateException, CardException {
		CommandAPDU getData = new CommandAPDU(
				GlobalPlatformService.CLA_GP,
				GlobalPlatformService.GET_DATA, p1, p2);

		return mGPService.transmit(getData);
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

		mGPService.loadCapFile(cpFile, false, false, 255 - 16, true, false);

		AID p = cpFile.getPackageAID();
		Log.d(LOG_TAG, "Installing Applet with package AID " + p.toString());

		for (AID a : cpFile.getAppletAIDs()) {
			mGPService.installAndMakeSelecatable(p, a, null, privileges,
					params, null);

			Log.d(LOG_TAG, "Finished installing applet. AID: " + a.toString());
		}
	}

	public GPAppletData loadAppletsfromCard() throws CardException {
		data.setRegistry( mGPService.getStatus().allPackages());

		return data;
	}
	
	private void deleteApplet(AID aid) {
		try {
			deleteAID(aid);
		} catch (GPDeleteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	private String installApplet(String _url) throws IOException,
			MalformedURLException, GPInstallForLoadException, GPLoadException,
			CardException {
		return installApplet(_url, null, (byte) 0);
	}

	private String installApplet(String _url, byte[] params, byte privileges)
			throws IOException, MalformedURLException,
			GPInstallForLoadException, GPLoadException, CardException {
		// String fileUrl =
		// "file:"+Environment.getExternalStorageDirectory().getPath() +
		// "/usmile/instApplet/apdutester.cap";
		if (_url == null) {
			return "no Applet selected";
		}
		if (!(_url).endsWith(".cap")) {
			throw new IOException("Not a valid path or not a cap file");
		}
		// String fileUrl = (String) _param;
		String ret = "Loading Applet from " + _url+"<br/>";

		installCapFile(_url, params, privileges);

		return ret+"Installation successful";
	}

	/**
	 * lists all applets installed on the currently selected smartcard
	 * 
	 * @throws CardException
	 */
	private String listApplets(String _reader) throws CardException {
		GPAppletData mApplets = loadAppletsfromCard();

		return "Read all applets from reader "
						+ _reader + ". <br>"
						+ mApplets.getRegistry().size()
						+ " Applets.";
	}
	/**
	 * @param keyset
	 * @param channelSet
	 * @param _cmd
	 * @return
	 */
	public String performCommand(OpenMobileAPITerminal _term, GPKeyset keyset, GPChannelSet channelSet,
			GPCommand _cmd) {
		String ret = "";
		try {
			Card c = null;
			boolean closeConn = true;
			
			_term.setReader(_cmd.getSeekReader());
			c = _term.connect("*");

			System.out
					.println("Found card in terminal: " + _term.getName());
			if (c.getATR() != null) {
				System.out.println("ATR: "
						+ GPUtil.byteArrayToString(c.getATR().getBytes()));
			}
			CardChannel channel = c.openLogicalChannel();

			initializeKeys(channel, keyset);
			open();

			// opening channel with index of keyset - is unique
			openSecureChannel(keyset.getID(),
					keyset.getID(), keyset.getVersion(),
					channelSet.getScpVersion(), channelSet.getSecurityLevel(),
					channelSet.isGemalto());

			Log.d(LOG_TAG, "Secure channel opened");

			switch (_cmd.getCmd()) {
			case APDU_INSTALL:
				if (_cmd.getInstallParams() != null) {
					ret = installApplet((String)_cmd.getCommandParameter(), _cmd.getInstallParams(), _cmd.getPrivileges());
				} else {
					ret = installApplet((String)_cmd.getCommandParameter());
				}
				break;

			case APDU_DELETE_SENT_APPLET:
				AID aid;
				aid = CAPFile.readAID((String)_cmd.getCommandParameter());
				ret = "TCPConn" + GPUtils.byteArrayToString(aid.getBytes());
				deleteApplet(aid);
				break;

			case APDU_DELETE_SELECTED_APPLET:
				deleteSelectedApplet();
				ret = "Applet deleted";
				break;
			case APDU_DISPLAYAPPLETS_ONCARD:
				ret = listApplets(_cmd.getSeekReaderName());
				channel.close();	
				
				Intent intent = new Intent(mContext, AppletListActivity.class);
				intent.putExtra(AppletListActivity.EXTRA_CHANNELSET, channelSet);
				intent.putExtra(AppletListActivity.EXTRA_KEYSET, keyset);
				intent.putExtra(AppletListActivity.EXTRA_SEEKREADER, _cmd.getSeekReader());
				
				mContext.startActivity(intent);
				closeConn = false;
				break;

			case APDU_GET_DATA:
				ResponseAPDU response = getData(((Integer[])_cmd.getCommandParameter())[0],((Integer[])_cmd.getCommandParameter())[1]);
				ret = "Response: " + //TODO: write a parser 
						GPUtils.byteArrayToString(response.getData());
			default:
				break;
			}

			if(closeConn){
				channel.close();	
				c.disconnect(true);				
			}
		} catch (GPSecurityDomainSelectionException e) {
			ret = "GPSecurityDomainSelectionException " + e.getLocalizedMessage();
			e.printStackTrace();
		} catch (GPInstallForLoadException e) {
			ret = "GPInstallForLoadException - Applet already installed? "+ e.getLocalizedMessage();
			e.printStackTrace();
		} catch (CardException e) {
			ret = "CardException "+ e.getLocalizedMessage();
			e.printStackTrace();
		} catch (MalformedURLException e) {
			ret = "MalformedURLException "+ e.getLocalizedMessage();
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			ret= "IOException "+e.getLocalizedMessage();
		}
		return ret;
	}

}
