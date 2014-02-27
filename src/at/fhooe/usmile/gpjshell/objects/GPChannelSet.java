package at.fhooe.usmile.gpjshell.objects;

import java.io.Serializable;

public class GPChannelSet implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8712625998639336249L;
	public static final String CHANNEL_SET = "channelset";
	
	private String channelNameString;
	private int channelSet; 
	private int channelId;
	private int scpVersion;
	private int securityLevel;
	private boolean isGemalto;
	private String readerName;
	
	public GPChannelSet() {
		this.channelNameString = null;
		this.channelSet = 0;
		this.channelId = 0;
		this.scpVersion = 0;
		this.securityLevel = 0;
		isGemalto = false;
	}
	
	
	public GPChannelSet(String name, int channelSet, int channelId, int scpVersion, int securityLevel, boolean gemalto) {
		this.channelNameString = name;
		this.channelSet = channelSet;
		this.channelId = channelId;
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


	public int getChannelSet() {
		return channelSet;
	}


	public void setChannelSet(int channelSet) {
		this.channelSet = channelSet;
	}


	public int getChannelId() {
		return channelId;
	}


	public void setChannelId(int channelId) {
		this.channelId = channelId;
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


	public String getReaderName() {
		return readerName;
	}


	public void setReaderName(String readerName) {
		this.readerName = readerName;
	}
	
	
}