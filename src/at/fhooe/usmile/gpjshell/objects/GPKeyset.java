package at.fhooe.usmile.gpjshell.objects;

import java.io.Serializable;

public class GPKeyset implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3278175173044550612L;
	
	public static final String KEYSET = "keyset";
	
	private int ID;
	private int version;
	private String name;
	private String MAC;
	private String ENC;
	private String KEK;
	private String readerName;
	
	public GPKeyset(String name, int ID, int version, String MAC, String DEK, String KEK, String readerName) {
		this.ID = ID;
		this.version = version;
		this.setName(name);
		this.MAC = MAC;
		this.ENC = DEK;
		this.KEK = KEK;
		this.setReaderName(readerName);
	}
	
	public byte[] convertHexStringToByteArray(String string, String separator){
		String[] stringBytes = string.split(separator);
		byte[] bytes = new byte[stringBytes.length];
		for (int i = 0; i < stringBytes.length; i++){
			int index = stringBytes[i].indexOf("x");
			bytes[i] = (byte) ((Character.digit(stringBytes[i].charAt(index+1), 16) << 4)
                    + Character.digit(stringBytes[i].charAt(index+2), 16));
		}
		return bytes;
	}
	
	public byte[] convertHexStringToByteArray(String string) {
		return convertHexStringToByteArray(string, ",");
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public byte[] getMACByte() {
		return convertHexStringToByteArray(MAC);
	}
	
	public String getMAC() {
		return MAC;
	}

	public void setMAC(String mAC) {
		MAC = mAC;
	}

	public String getENC() {
		return ENC;
	}
	
	public byte[] getENCByte() {
		return convertHexStringToByteArray(ENC);
	}
	
	public void setENC(String eNC) {
		ENC = eNC;
	}

	public String getKEK() {
		return KEK;
	}
	
	public byte[] getKEKByte() {
		return convertHexStringToByteArray(KEK);
	}

	public void setKEK(String kEK) {
		KEK = kEK;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getReaderName() {
		return readerName;
	}

	public void setReaderName(String readerName) {
		this.readerName = readerName;
	}

}
