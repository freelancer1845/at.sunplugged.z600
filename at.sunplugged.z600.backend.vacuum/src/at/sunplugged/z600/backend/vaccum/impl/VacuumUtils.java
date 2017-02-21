package at.sunplugged.z600.backend.vaccum.impl;

import java.io.IOException;

import at.sunplugged.z600.core.machinestate.api.OutletControl;
import at.sunplugged.z600.core.machinestate.api.OutletControl.Outlet;

public class VacuumUtils {

	public static void closeAllOutlets(OutletControl outletControl ) throws IOException {
		for (Outlet outlet : Outlet.values()) {
			outletControl.closeOutlet(outlet);
    	}
	}
	
	public static void closeAllOutletsButSpecified(OutletControl outletControl, Outlet... outlets) throws IOException {
		for (Outlet outlet : Outlet.values()) {
			for (Outlet notToClose : outlets) {
				if (!outlet.equals(notToClose)) {
					outletControl.closeOutlet(outlet);
				} else {
					break;
				}
			}
    	}
	}
	
	
	private VacuumUtils() {
		
	}
	
}
