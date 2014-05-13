/*******************************************************************************
 * Copyright (c) 2014 Michael Hölzl <mihoelzl@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Hölzl <mihoelzl@gmail.com> - initial implementation
 ******************************************************************************/
package net.sourceforge.gpj.cardservices.interfaces;

import java.io.IOException;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;

import android.content.Context;
import android.util.Log;

public class OpenMobileAPITerminal extends CardTerminal implements SEService.CallBack{

	private static final String TERMINAL_NAME = "OpenMobile API for SE access";

	final String LOG_TAG = "HelloSmartcard";

	private int mReader = -1;
	private boolean isConnected = false;
	private SEService seService;
	private SEService.CallBack mCallback = null;
	
	public OpenMobileAPITerminal(Context _con, SEService.CallBack _connectedServiceCallback){		
		try {
			Log.i(LOG_TAG, "creating SEService object");
			mCallback = _connectedServiceCallback;
			seService = new SEService(_con, this);
		} catch (SecurityException e) {
			Log.e(LOG_TAG, "Binding not allowed, uses-permission org.simalliance.openmobileapi.SMARTCARD?");
		} catch (Exception e) {
			Log.e(LOG_TAG, "Exception: " + e.getMessage());
		}
	}
	public OpenMobileAPITerminal(Context _con, SEService.CallBack _connectedServiceCallback, int _reader){
		this(_con, _connectedServiceCallback);
		setReader(_reader);
	}

	@Override
	public String getName() {
		return TERMINAL_NAME;
	}

	@Override
	public Card connect(String protocol) throws CardException {
		Reader[] readers = checkCurrentStatusAndGetReaders();
		
		try {
			return new OpenMobileAPICard(readers[mReader].openSession());
		} catch (IOException e) {
			throw new CardException("Open Session to reader "+readers[mReader].getName()+" failed. ",e);
		} catch (RuntimeException e){
			throw new CardException("Open Session to reader "+readers[mReader].getName()+" failed. ",e);
		}
	}

	private Reader[] checkCurrentStatusAndGetReaders() throws CardException {
		if(seService==null || !isConnected) throw new CardException("OpenMobileAPI not connected yet");
		Reader[] readers = seService.getReaders();
		
		if(mReader == -1) throw new CardException("Missing reader argument");		
		if(mReader >= readers.length || mReader < 0) throw new CardException("OpenMobile Reader not available");
		return readers;
	}

	public Reader[] getReaders(){
		return seService.getReaders();
	}
	@Override
	public boolean isCardPresent() throws CardException {
		Reader[] readers = checkCurrentStatusAndGetReaders();
		return readers[mReader].isSecureElementPresent();
	}

	@Override
	public boolean waitForCardPresent(long timeout) throws CardException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean waitForCardAbsent(long timeout) throws CardException {
		// TODO Auto-generated method stub
		return false;
	}

	public int getReader() {
		return mReader;
	}

	public void setReader(int mReader) {
		this.mReader = mReader;
	}


	@Override
	public void serviceConnected(SEService service) {
		Log.i(LOG_TAG, "seviceConnected()");
		isConnected = true;
		
		mCallback.serviceConnected(service);
	}
	public void shutdown() {
		if (seService != null && seService.isConnected()) {
			seService.shutdown();
		}
	}
	public boolean isConnected() {
		return seService.isConnected();
	}
}
