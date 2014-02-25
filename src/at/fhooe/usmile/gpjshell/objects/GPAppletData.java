package at.fhooe.usmile.gpjshell.objects;

import net.sourceforge.gpj.cardservices.AIDRegistry;
import net.sourceforge.gpj.cardservices.AIDRegistryEntry;

public class GPAppletData {
	private AIDRegistry mRegistry;
	private int mSelectedApplet;

	public GPAppletData(AIDRegistry mRegistry, int mSelectedApplet) {
		this.mRegistry = mRegistry;
		this.mSelectedApplet = mSelectedApplet;
	}
	
	public AIDRegistry getRegistry() {
		return mRegistry;
	}

	public void setRegistry(AIDRegistry _registry) {
		mRegistry = _registry;
	}

	public AIDRegistryEntry getSelectedApplet() {
		return mRegistry.allPackages().get(mSelectedApplet);
	}

	public void setSelectedApplet(int _selectedApplet) {
		mSelectedApplet = _selectedApplet;
	}

}