package at.fhooe.usmile.gpjshell;

public interface TCPFileResultListener {

	void fileReceived(String _url, int _reader, int _keySet, int _secureChannelSet);
}
