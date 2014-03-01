package at.fhooe.usmile.gpjshell.objects;

import java.io.Serializable;

import at.fhooe.usmile.gpjshell.GPUtils;

public class GPKeyset implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3278175173044550612L;
	
	public static final String KEYSET = "keyset";
	
	private int uniqueID;
	private int ID;
	private int version;
	private String name;
	private String MAC;
	private String ENC;
	private String KEK;
	private String readerName;
	
	public GPKeyset(int uniqueID, String name, int ID, int version, String MAC, String DEK, String KEK, String readerName) {
		this.ID = ID;
		this.version = version;
		this.setName(name + " - " + ID);
		this.MAC = MAC;
		this.ENC = DEK;
		this.KEK = KEK;
		this.setReaderName(readerName);
		this.setUniqueID(uniqueID);
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
		return GPUtils.convertHexStringToByteArray(MAC);
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
		return GPUtils.convertHexStringToByteArray(ENC);
	}
	
	public void setENC(String eNC) {
		ENC = eNC;
	}

	public String getKEK() {
		return KEK;
	}
	
	public byte[] getKEKByte() {
		return GPUtils.convertHexStringToByteArray(KEK);
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

	public int getUniqueID() {
		return uniqueID;
	}

	public void setUniqueID(int uniqueID) {
		this.uniqueID = uniqueID;
	}
}
