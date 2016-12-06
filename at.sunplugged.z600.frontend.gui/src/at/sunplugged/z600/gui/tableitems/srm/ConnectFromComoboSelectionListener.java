package at.sunplugged.z600.gui.tableitems.srm;

import java.io.IOException;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.srm50.api.SrmCommunicator;

public class ConnectFromComoboSelectionListener implements SelectionListener {

    private final SrmTabItemFactory srmTabItemFactory;

    private final Combo combo;

    public ConnectFromComoboSelectionListener(Combo combo, SrmTabItemFactory srmTabItemFactory) {
        this.combo = combo;
        this.srmTabItemFactory = srmTabItemFactory;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        int selectedIndex = combo.getSelectionIndex();
        if (selectedIndex != -1) {
            try {
                srmTabItemFactory.getSrmCommunicator().connect(combo.getItem(selectedIndex));

            } catch (IOException e1) {
                srmTabItemFactory.getLogService().log(LogService.LOG_ERROR, "Failed on user connect: ", e1);
            }
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }

}
