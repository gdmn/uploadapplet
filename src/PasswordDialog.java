
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class PasswordDialog extends JDialog
        implements ActionListener {

    private static PasswordDialog dialog;
    private static String value = "";
    private JPasswordField passwordField;
    private JTextField loginField;

    public static String showDialog(Component frameComp,
            Component locationComp,
            String labelText,
            String title,
            String initialValue) {
        Frame frame = JOptionPane.getFrameForComponent(frameComp);
        dialog = new PasswordDialog(frame,
                locationComp,
                labelText,
                title,
                initialValue);
        dialog.setVisible(true);
        return value;
    }

    private void setValue(String newValue) {
        value = newValue;
        int p = value == null ? -1 : value.indexOf(':');
        if (p > 0) {
            loginField.setText(value.substring(0, p));
            passwordField.setText(value.substring(p + 1));
        } else {
            loginField.setText("");
            passwordField.setText("");
        }
    }

    private PasswordDialog(Frame frame,
            Component locationComp,
            String labelText,
            String title,
            String initialValue) {
        super(frame, title, true);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        final JButton okButton = new JButton("OK");
        okButton.setActionCommand("ok");
        okButton.addActionListener(this);
        getRootPane().setDefaultButton(okButton);
        okButton.setPreferredSize(cancelButton.getPreferredSize());

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
        JLabel loginLabel = new JLabel("User:");
        JLabel passwordLabel = new JLabel("Password:");
        loginField = new JTextField();
        passwordField = new JPasswordField();
        loginLabel.setLabelFor(loginField);
        passwordLabel.setLabelFor(passwordField);
        JLabel textLabel = new JLabel(labelText);
        loginLabel.setPreferredSize(new Dimension(200, loginLabel.getPreferredSize().height));
        for (JComponent l : new JComponent[]{loginLabel, passwordLabel, textLabel, loginField, passwordField}) {
            l.setAlignmentX(0);
        }
        centerPanel.add(textLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(loginLabel);
        centerPanel.add(loginField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(passwordLabel);
        centerPanel.add(passwordField);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        for (JTextField l : new JTextField[]{loginField, passwordField}) {
            l.addFocusListener(new FocusAdapter() {

                @Override
                public void focusGained(FocusEvent e) {
                    ((JTextField) e.getSource()).selectAll();
                }
            });
            l.addKeyListener(new KeyAdapter() {

                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        PasswordDialog.this.actionPerformed(null);
                    }
                }
            });
        }
        loginField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                passwordField.requestFocusInWindow();
            }
        });
        passwordField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                okButton.doClick();
            }
        });

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout());
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
        buttonPane.add(okButton);
        buttonPane.add(cancelButton);

        Container contentPane = getContentPane();
        contentPane.add(centerPanel, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.PAGE_END);

        setValue(initialValue);
        pack();
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
        this.setResizable(false);
        setLocationRelativeTo(locationComp);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PasswordDialog.value = null;
        if (e != null && "ok".equals(e.getActionCommand()) && loginField.getText().length() > 0 && passwordField.getText().length() > 0) {
            PasswordDialog.value = (String) (loginField.getText() + ':' + passwordField.getText());
        }
        PasswordDialog.dialog.setVisible(false);
    }

//    public static void main(final String args[]) {
//        System.out.println("result: " + PasswordDialog.showDialog(null, null, "hello opis", "Login", null));
//        System.exit(0);
//    }
}
