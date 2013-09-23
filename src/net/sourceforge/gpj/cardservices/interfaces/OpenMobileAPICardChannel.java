package net.sourceforge.gpj.cardservices.interfaces;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import net.sourceforge.gpj.cardservices.GPUtil;

import org.simalliance.openmobileapi.Channel;

import android.util.Log;

public class OpenMobileAPICardChannel extends CardChannel{

	private OpenMobileAPICard mParentCard = null;
	private Channel mOpenMobileChannel = null;
	
	public OpenMobileAPICardChannel(Channel _openBasicChannel,
			OpenMobileAPICard _openMobileAPICard) {
		setOpenMobileChannel(_openBasicChannel);
		setParentCard(_openMobileAPICard);
	}

	@Override
	public Card getCard() {
		return mParentCard;
	}

	@Override
	public int getChannelNumber() {
		return 0;
	}

	@Override
	public ResponseAPDU transmit(CommandAPDU command) throws CardException {
		ResponseAPDU response = null;
		try {
			byte [] byteResponse = null;
//			byte test = (byte) ((byte) 0xA4 & 0xff);
//			byte test2 = ((byte) 0xA4);
			if ((command.getINS() == ((byte) 0xA4 & 0xff)) && (command.getP1() == (byte) 0x04)) {
				mParentCard.closeChannels();
				Log.d("CardChannel", "Select AID " +  GPUtil.byteArrayToString(command.getData()));
				setOpenMobileChannel(mParentCard.openLogicalChannel(command.getData()));
				
				if(mOpenMobileChannel!=null){
					byteResponse = mOpenMobileChannel.getSelectResponse();
				} else{
					byteResponse = new byte[]{0x6A,(byte) 0x82};
				}
			} else{
				byteResponse = mOpenMobileChannel.transmit(command.getBytes());
			}
			response = new ResponseAPDU(byteResponse);
		} catch (IOException e) {
			throw new CardException("Transmit failed",e);
		}
		return response;
	}

	@Override
	public int transmit(ByteBuffer command, ByteBuffer response)
			throws CardException {
		throw new CardException("Not supported yet");
	}

	@Override
	public void close() throws CardException {
		mOpenMobileChannel.close();
	}

	public OpenMobileAPICard getParentCard() {
		return mParentCard;
	}

	public void setParentCard(OpenMobileAPICard mParentCard) {
		this.mParentCard = mParentCard;
	}

	public Channel getOpenMobileChannel() {
		return mOpenMobileChannel;
	}

	public void setOpenMobileChannel(Channel mOpenMobileChannel) {
		this.mOpenMobileChannel = mOpenMobileChannel;
	}
}
