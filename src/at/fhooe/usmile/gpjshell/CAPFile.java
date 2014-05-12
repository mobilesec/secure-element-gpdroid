package at.fhooe.usmile.gpjshell;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import net.sourceforge.gpj.cardservices.AID;
import net.sourceforge.gpj.cardservices.CapFile;

public class CAPFile {
	private static byte[] CAP_HEADER_MAGIC = {(byte) 0xDE,(byte) 0xCA,(byte) 0xFF,(byte) 0xED};

	/**
	 * TODO 
	 * @param _file
	 * @return
	 */
	public static byte[] readHeader(File _file){
		return null;
	}
	
	public static AID readAID(String _url) throws MalformedURLException, IOException{
		CapFile cpFile = new CapFile(new URL(_url).openStream(), null);
		return cpFile.getPackageAID();
	}
}
