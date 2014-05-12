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

public class GPUtils {
	public static byte[] convertHexStringToByteArray(String string, String separator){
		String[] stringBytes = string.split(separator);
		byte[] bytes = new byte[stringBytes.length];
		for (int i = 0; i < stringBytes.length; i++){
			int index = stringBytes[i].indexOf("x");
			bytes[i] = (byte) ((Character.digit(stringBytes[i].charAt(index+1), 16) << 4)
                    + Character.digit(stringBytes[i].charAt(index+2), 16));
		}
		return bytes;
	}
	
	public static byte[] convertHexStringToByteArray(String string){
		int len = string.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(string.charAt(i), 16) << 4)
	                             + Character.digit(string.charAt(i+1), 16));
	    }
	    return data;
	}
	
	public static String byteArrayToString(byte[] ba)
	{
	  StringBuilder hex = new StringBuilder(ba.length * 2);
	  for (byte b : ba){
	    hex.append(String.format("%02X", b));
	  }
	  return hex.toString();
	}
}
