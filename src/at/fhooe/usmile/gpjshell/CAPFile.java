package at.fhooe.usmile.gpjshell;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import net.sourceforge.gpj.cardservices.AID;
import net.sourceforge.gpj.cardservices.CapFile;

public class CAPFile {

	public static AID readAID(String _url) throws MalformedURLException, IOException{
		CapFile cpFile = new CapFile(new URL(_url).openStream(), null);
		return cpFile.getPackageAID();
	}
}
