/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author dmn
 */
public class SendControl extends JPanel {

    private BackgroundTask backgroundTask;
    private JProgressBar progress;
    private JButton cancel;
    private JLabel label;

    public SendControl(BackgroundTask backgroundTask) {
        this.backgroundTask = backgroundTask;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBorder(new EmptyBorder(5, 5, 5, 5));

        label = new JLabel();
        label.setText(backgroundTask.toString());
        label.setToolTipText(backgroundTask.toString());
        //if (label.getPreferredSize().width < 200) { label.setPreferredSize(new Dimension(200, label.getPreferredSize().height)); }
        label.setMinimumSize(label.getPreferredSize());

        progress = new JProgressBar();
        progress.setStringPainted(true);
        progress.setPreferredSize(new Dimension(250, progress.getPreferredSize().height));
        progress.setMaximumSize(progress.getPreferredSize());
        progress.setIndeterminate(true);

        cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SendControl.this.backgroundTask.cancel(true);
            }
        });

        this.add(label);
        this.add(Box.createHorizontalStrut(10));
        this.add(Box.createHorizontalGlue());
        this.add(progress);
        this.add(Box.createHorizontalStrut(10));
        this.add(cancel);

        setMinimumSize(getPreferredSize());
        setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));

        backgroundTask.addPropertyChangeListener(new PropertyChangeListener() {

            SpeedCounter sc = (SendControl.this.backgroundTask instanceof SpeedCounter) ? (SpeedCounter) SendControl.this.backgroundTask : null;

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    if (!SendControl.this.backgroundTask.isCancelled()) {
                        progress.setIndeterminate(false);
                        int p = (Integer) evt.getNewValue();
                        progress.setValue(p);
                        if (sc != null) {
                            String s = bpsToString(sc.getBytesPerSecond());
                            progress.setString(p + "% (" + s + ")");
                        } else {
                            progress.setString(p + "%");
                        }
                    }
                } else if ("state".equals(evt.getPropertyName())) {
                    SwingWorker.StateValue state = (SwingWorker.StateValue) evt.getNewValue();
                    if (state == SwingWorker.StateValue.DONE) {
                        progress.setIndeterminate(false);
                        cancel.setEnabled(false);
                        try {
                            String result = SendControl.this.backgroundTask.get();
                            String nameOfTask = SendControl.this.backgroundTask.toString();
                            String value = (SendControl.this.backgroundTask.isSuccessful() ? "OK" : "error") +
                                    ((result == null || "".equals(result)) ? "" : " " + result);
                            SendControl.this.label.setText(((nameOfTask != null && !"".equals(nameOfTask)) ? nameOfTask + ": " : "") + value);
                            progress.setString((sc != null && sc.getOverallBytesPerSecond() > 0) ? bpsToString(sc.getOverallBytesPerSecond()) :
                                (SendControl.this.backgroundTask.isSuccessful() ? "OK" : "error"));
                        } catch (Exception ex) {
                            String nameOfTask = SendControl.this.backgroundTask.toString();
                            String value = "error";
                            SendControl.this.label.setText(((nameOfTask != null && !"".equals(nameOfTask)) ? nameOfTask + ": " : "") + value);
                            progress.setString((sc != null && sc.getOverallBytesPerSecond() > 0) ? bpsToString(sc.getOverallBytesPerSecond()) : "error");
                        }
                    }
                }
            }
        });
    }

    public static String bpsToString(int bps) {
        String s;
        if (bps >= 1024 * 1024 * 1024) {
            s = String.format("%.2f gb/s", new Double((double) bps / (1024 * 1024 * 1024)));
        } else if (bps >= 1024 * 1024) {
            s = String.format("%.2f mb/s", new Double((double) bps / (1024 * 1024)));
        } else if (bps >= 1024) {
            s = String.format("%.2f kb/s", new Double((double) bps / (1024)));
        } else {
            s = String.valueOf(bps) + " b/s";
        }
        return s;
    }
}
