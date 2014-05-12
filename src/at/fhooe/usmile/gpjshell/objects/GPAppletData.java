package at.fhooe.usmile.gpjshell.objects;

import java.util.List;

import net.sourceforge.gpj.cardservices.AIDRegistryEntry;

public class GPAppletData {
	private List<AIDRegistryEntry> mRegistry;
	private int mSelectedApplet;

	public GPAppletData(List<AIDRegistryEntry> mRegistry, int mSelectedApplet) {
		this.mRegistry = mRegistry;
		this.mSelectedApplet = mSelectedApplet;
	}
	
	public List<AIDRegistryEntry>  getRegistry() {
		return mRegistry;
	}

	public void setRegistry(List<AIDRegistryEntry>  _registry) {
		mRegistry = _registry;
	}

	public AIDRegistryEntry getSelectedApplet() {
		return mRegistry.get(mSelectedApplet);
	}

	public void setSelectedApplet(int _selectedApplet) {
		mSelectedApplet = _selectedApplet;
	}

	public int getSelectedAppletPosition() {
		return mSelectedApplet;
	}

	public void removeSelectedAppletFromList() {
		mRegistry.remove(mSelectedApplet);
	}

}