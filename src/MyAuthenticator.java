
import java.net.Authenticator;
import java.net.PasswordAuthentication;

class MyAuthenticator extends Authenticator {

    private String value = null;
    private static MyAuthenticator instance;
    private final Object lock = new Object();

    private MyAuthenticator() {
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        String prev = value;
        synchronized (lock) {
            if (prev == null && value == null) {
                value = PasswordDialog.showDialog(null, null, "Login yourself to " + getRequestingHost() + (getRequestingPrompt() == null ? "" : ", " + getRequestingPrompt()) + ":", getRequestingHost(), null);
            }
            int p = value == null ? -1 : value.indexOf(':');
            if (p > 0) {
                String user = value.substring(0, p);
                String passwd = value.substring(p + 1);
                return new PasswordAuthentication(user, passwd.toCharArray());
            }
            return null;
        }
    }

    public static MyAuthenticator getAuthenticator() {
        if (instance == null) {
            instance = new MyAuthenticator();
        }
        return instance;
    }
}
