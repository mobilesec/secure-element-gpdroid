package at.fhooe.usmile.gpjshell;

import java.io.IOException;
import java.util.NoSuchElementException;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Session;

public class OpenMobileAPICard extends Card {

	private Session mSession = null;

	public OpenMobileAPICard(Session _session) {
		mSession = _session;
	}

	@Override
	public ATR getATR() {
		if (mSession.getATR() != null) {
			return new ATR(mSession.getATR());
		}
		return null;
	}

	@Override
	public String getProtocol() {
		return "T=1";
	}

	@Override
	public CardChannel getBasicChannel() {
		try {
			return new OpenMobileAPICardChannel(
					mSession.openBasicChannel(null), this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public CardChannel openLogicalChannel() throws CardException {
		try {
			return new OpenMobileAPICardChannel(openLogicalChannel(null), this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected Channel openLogicalChannel(byte[] aid) throws CardException, IOException {
		try {
			return mSession.openLogicalChannel(aid);
		} catch (NoSuchElementException e){
			e.printStackTrace();
		}
		return null;
	}

	protected void closeChannels() throws CardException {

		mSession.closeChannels();
	}

	@Override
	public void beginExclusive() throws CardException {
		// TODO Auto-generated method stub

	}

	@Override
	public void endExclusive() throws CardException {
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] transmitControlCommand(int controlCode, byte[] command)
			throws CardException {
		throw new CardException("Not supported");
	}

	@Override
	public void disconnect(boolean reset) throws CardException {
		mSession.close();
	}

}
