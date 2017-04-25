package at.sunplugged.z600.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class ValueDialog {

    public static enum Answer {
        OK, CANCEL;
    }

    private final String title;

    private final String question;

    private final double minValue;

    private final double maxValue;

    private final Shell parentShell;

    private Shell shell;

    private Button btnOk;

    private Text txtInputfield;

    private Answer answer = Answer.CANCEL;

    private double value;

    public ValueDialog(String title, String question, double minValue, double maxValue, Shell parentShell) {
        this.title = title;
        this.question = question;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.parentShell = parentShell;
    }

    /**
     * @wbp.parser.entryPoint
     */
    public Answer open() {
        shell = new Shell(parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText(title);
        Rectangle parentBounds = parentShell.getBounds();
        shell.setLocation(parentBounds.x + parentBounds.width / 2 - 200,
                parentBounds.y + parentBounds.height / 2 - 200);

        shell.setSize(400, 400);
        shell.setLayout(new GridLayout(1, false));

        Composite mainComposite = new Composite(shell, SWT.NONE);
        mainComposite.setLayout(new GridLayout(1, false));
        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        createHeaderComposite(mainComposite);

        Label label = new Label(mainComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        label.setText("");

        createContentComposite(mainComposite);

        createButtonsBottomComposite(mainComposite);

        shell.open();
        Display display = parentShell.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return answer;

    }

    public double getValue() {
        return value;
    }

    private void createHeaderComposite(Composite parent) {
        Composite compositeHeader = new Composite(parent, SWT.NONE);
        compositeHeader.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
        compositeHeader.setBackground(SWTResourceManager.getColor(204, 255, 255));
        compositeHeader.setLayout(new FillLayout(SWT.HORIZONTAL));

        Label lblQuestionlabel = new Label(compositeHeader, SWT.NONE);
        lblQuestionlabel.setText(question);
    }

    private void createContentComposite(Composite mainComposite) {
        Composite compositeControl = new Composite(mainComposite, SWT.NONE);
        compositeControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        compositeControl.setLayout(new GridLayout(1, false));

        txtInputfield = new Text(compositeControl, SWT.BORDER | SWT.RIGHT);
        txtInputfield.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
        txtInputfield.setText("");
        txtInputfield.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtInputfield.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                try {
                    String currentText = txtInputfield.getText();
                    if (currentText.isEmpty() == true) {
                        btnOk.setEnabled(false);
                        return;
                    } else {
                        btnOk.setEnabled(true);
                    }
                    double value = Double.valueOf(currentText.replace(",", "."));
                    if (value > maxValue) {
                        txtInputfield.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
                        txtInputfield.setToolTipText("Value greater than max allowed " + maxValue);
                        btnOk.setEnabled(false);
                    } else if (value < minValue) {
                        txtInputfield.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
                        txtInputfield.setToolTipText("Value smaller that min allowed " + minValue);
                        btnOk.setEnabled(false);
                    } else {
                        txtInputfield.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
                        txtInputfield.setToolTipText("");
                        btnOk.setEnabled(true);
                    }
                } catch (NumberFormatException e1) {
                    txtInputfield.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
                    txtInputfield.setToolTipText("Format not allowed");
                    btnOk.setEnabled(false);
                }
            }
        });

        Group group = new Group(compositeControl, SWT.NONE);
        group.setLayout(new GridLayout(4, true));
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        Button button = new Button(group, SWT.NONE);
        GridData gd_button = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        gd_button.heightHint = 50;
        gd_button.widthHint = 50;
        button.setLayoutData(gd_button);
        button.setText("7");
        button.addSelectionListener(new NumpadButtonListener("7"));

        Button button_1 = new Button(group, SWT.NONE);
        button_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        button_1.setText("8");
        button_1.addSelectionListener(new NumpadButtonListener("8"));

        Button button_2 = new Button(group, SWT.NONE);
        button_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        button_2.setText("9");
        button_2.addSelectionListener(new NumpadButtonListener("9"));

        Button btnCe = new Button(group, SWT.NONE);
        btnCe.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        btnCe.setText("CE");
        btnCe.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String currentText = txtInputfield.getText();
                if (currentText.length() > 0) {
                    String newText = currentText.substring(0, currentText.length() - 1);
                    txtInputfield.setText(newText);
                }
            }

        });

        Button button_3 = new Button(group, SWT.NONE);
        GridData gd_button_3 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
        gd_button_3.heightHint = 50;
        button_3.setLayoutData(gd_button_3);
        button_3.setText("4");
        button_3.addSelectionListener(new NumpadButtonListener("4"));

        Button button_4 = new Button(group, SWT.NONE);
        button_4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        button_4.setText("5");
        button_4.addSelectionListener(new NumpadButtonListener("5"));

        Button button_5 = new Button(group, SWT.NONE);
        button_5.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        button_5.setText("6");
        button_5.addSelectionListener(new NumpadButtonListener("6"));

        Button btnC = new Button(group, SWT.NONE);
        btnC.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        btnC.setText("C");
        btnC.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                txtInputfield.setText("");
            }

        });

        Button button_6 = new Button(group, SWT.NONE);
        GridData gd_button_6 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
        gd_button_6.heightHint = 50;
        button_6.setLayoutData(gd_button_6);
        button_6.setText("1");
        button_6.addSelectionListener(new NumpadButtonListener("1"));

        Button button_7 = new Button(group, SWT.NONE);
        button_7.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        button_7.setText("2");
        button_7.addSelectionListener(new NumpadButtonListener("2"));

        Button button_8 = new Button(group, SWT.NONE);
        button_8.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        button_8.setText("3");
        button_8.addSelectionListener(new NumpadButtonListener("3"));

        new Label(group, SWT.NONE);

        Button button_9 = new Button(group, SWT.NONE);
        GridData gd_button_9 = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
        gd_button_9.heightHint = 50;
        button_9.setLayoutData(gd_button_9);
        button_9.setText("0");
        button_9.addSelectionListener(new NumpadButtonListener("0"));

        Button button_10 = new Button(group, SWT.NONE);
        button_10.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        button_10.setText(",");
        button_10.addSelectionListener(new NumpadButtonListener(","));
        new Label(group, SWT.NONE);
        new Label(group, SWT.NONE);
        new Label(group, SWT.NONE);
        new Label(group, SWT.NONE);
        new Label(group, SWT.NONE);

    }

    private void createButtonsBottomComposite(Composite mainComposite) {

        Composite compositeBottomButton = new Composite(mainComposite, SWT.NONE);
        compositeBottomButton.setLayout(new GridLayout(2, true));
        compositeBottomButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        compositeBottomButton.setBounds(0, 0, 64, 64);

        btnOk = new Button(compositeBottomButton, SWT.NONE);
        btnOk.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        btnOk.setText("Ok");
        btnOk.setEnabled(false);
        btnOk.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                answer = Answer.OK;
                value = Double.valueOf(txtInputfield.getText().replace(",", "."));
                shell.dispose();
            }

        });

        Button btnAbbrechen = new Button(compositeBottomButton, SWT.NONE);
        btnAbbrechen.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        btnAbbrechen.setText("Abbrechen");
        btnAbbrechen.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                answer = Answer.CANCEL;
                shell.dispose();
            }

        });

    }

    private final class NumpadButtonListener extends SelectionAdapter {

        private final String stringToAppend;

        public NumpadButtonListener(String stringToAppend) {
            this.stringToAppend = stringToAppend;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            txtInputfield.append(stringToAppend);
        }
    }

}
