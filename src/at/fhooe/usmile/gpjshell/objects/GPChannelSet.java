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
package at.fhooe.usmile.gpjshell.objects;

import java.io.Serializable;

public class GPChannelSet implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8712625998639336249L;
	public static final String CHANNEL_SET = "channelset";
	
	private String channelNameString;
	private int scpVersion;
	private int securityLevel;
	private boolean isGemalto;
	
	public GPChannelSet() {
		this.channelNameString = null;
		this.scpVersion = 0;
		this.securityLevel = 0;
		isGemalto = false;
	}
	
	
	public GPChannelSet(String name, int scpVersion, int securityLevel, boolean gemalto) {
		this.channelNameString = name;
		this.scpVersion = scpVersion;
		this.securityLevel = securityLevel;
		isGemalto = gemalto;
	}


	public String getChannelNameString() {
		return channelNameString;
	}


	public void setChannelNameString(String channelNameString) {
		this.channelNameString = channelNameString;
	}

	public int getScpVersion() {
		return scpVersion;
	}


	public void setScpVersion(int scpVersion) {
		this.scpVersion = scpVersion;
	}


	public int getSecurityLevel() {
		return securityLevel;
	}


	public void setSecurityLevel(int securityLevel) {
		this.securityLevel = securityLevel;
	}


	public boolean isGemalto() {
		return isGemalto;
	}


	public void setGemalto(boolean isGemalto) {
		this.isGemalto = isGemalto;
	}
}
