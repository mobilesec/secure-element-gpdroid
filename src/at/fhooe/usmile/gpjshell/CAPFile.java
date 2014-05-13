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

import net.sourceforge.gpj.cardservices.AID;
import net.sourceforge.gpj.cardservices.CapFile;

public class CAPFile {

	public static AID readAID(String _url) throws MalformedURLException, IOException{
		CapFile cpFile = new CapFile(new URL(_url).openStream(), null);
		return cpFile.getPackageAID();
	}
}
