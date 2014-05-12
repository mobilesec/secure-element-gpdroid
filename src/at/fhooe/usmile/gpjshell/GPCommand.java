package at.fhooe.usmile.gpjshell;

import at.fhooe.usmile.gpjshell.MainActivity.APDU_COMMAND;

public class GPCommand {

	private byte[] mParams;
	private byte mPrivileges;
	private int mSeekReader;
	private APDU_COMMAND mCmd;

	public GPCommand(APDU_COMMAND _cmd, int _seekReader, byte[] _params,
			byte _privileges) {
		setCmd(_cmd);
		setSeekReader(_seekReader);
		setPrivileges(_privileges);
		setParams(_params);
		
	}

	public APDU_COMMAND getCmd() {
		return mCmd;
	}

	public void setCmd(APDU_COMMAND mCmd) {
		this.mCmd = mCmd;
	}

	public byte getPrivileges() {
		return mPrivileges;
	}

	public void setPrivileges(byte mPrivileges) {
		this.mPrivileges = mPrivileges;
	}

	public byte[] getParams() {
		return mParams;
	}

	public void setParams(byte[] mParams) {
		this.mParams = mParams;
	}

	public int getSeekReader() {
		return mSeekReader;
	}

	public void setSeekReader(int mSeekReader) {
		this.mSeekReader = mSeekReader;
	}

}
