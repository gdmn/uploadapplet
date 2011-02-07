
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JMPlayer {

    //private static Logger logger = Logger.getLogger(JMPlayer.class.getName());
    private SystemProcessWrapper mplayerProcess;
	private String httpProxy;

    public JMPlayer(String bin, String args, String httpProxy) throws IOException {
        //mplayerProcess = new SystemProcessWrapper("/usr/bin/mplayer", "-ao alsa -quiet -slave -idle");
		this.httpProxy = httpProxy;
        mplayerProcess = new SystemProcessWrapper(bin, "-quiet -slave -idle " + (args != null ? args : ""));
        mplayerProcess.addPropertyChangeListener(null, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println("> " + evt.getPropertyName() + ": " + evt.getNewValue());
            }
        });

    public void openResource(String resource) throws IOException {
        execute("loadfile \"" + (httpProxy == null || !resource.startsWith("http://") ? "" : "http_proxy://" + httpProxy + "/") + resource + "\" 0\n", "Starting playback...");
    }

    public void open(File file) throws IOException {
        String path = file.getAbsolutePath().replace('\\', '/');
        openResource(path);
    }

    public void close() {
        execute("quit\n", "Exiting...");
        mplayerProcess = null;
    }

    public String getPlayingPath() {
        String path = getProperty("path");
        return path == null ? null : path;
    }

    public String getPlayingFilename() {
        String path = getProperty("filename");
        return path == null ? null : path;
    }

    public void togglePlay() {
        execute("pause");
    }

    public boolean isProcessSpawned() {
        return mplayerProcess != null;
    }

    public Float getTimePosition() {
        return getPropertyAsFloat("time_pos");
    }

    public void setTimePosition(long seconds) {
        setProperty("time_pos", seconds);
    }

    public Float getTotalTime() {
        return getPropertyAsFloat("length");
    }

    public Float getVolume() {
        return getPropertyAsFloat("volume");
    }

    public void setVolume(float volume) {
        setProperty("volume", volume);
    }

    protected String getProperty(String name) {
        if (name == null || mplayerProcess == null) {
            return null;
        }
        String s = "ANS_" + name + "=";
        String x = execute("get_property " + name, "ANS_");
        if (x == null || x.startsWith("ANS_ERROR")) {
            return null;
        }
        if (!x.startsWith(s)) {
            return null;
        }
        return x.substring(s.indexOf('=') + 1);
    }

    protected Long getPropertyAsLong(String name) {
        String value = getProperty(name);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException exc) {
            }
        }
        return null;
    }

    protected Float getPropertyAsFloat(String name) {
        String value = getProperty(name);
        if (value != null) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException exc) {
            }
        }
        return null;
    }

    protected void setProperty(String name, String value) {
        execute("set_property " + name + " " + value);
    }

    protected void setProperty(String name, long value) {
        execute("set_property " + name + " " + value);
    }

    protected void setProperty(String name, float value) {
        execute("set_property " + name + " " + value);
    }

    private void execute(String command) {
        execute(command, null);
    }

    private String execute(String command, String expected) {
        if (mplayerProcess != null) {
            //System.out.println("   in: " + command + " and expecting " + (expected != null ? "\"" + expected + "\"" : "no answer"));
            if (expected == null) {
                mplayerProcess.print(command).print("\n").flush();
            } else {
                try {
                    String response = mplayerProcess.printAndWait(command + "\n", expected);
                    //System.out.println("  out: " + response);
                    return response;
                } catch (InterruptedException ex) {
                }
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        final JMPlayer jmPlayer = new JMPlayer("/usr/bin/mplayer", "-ao alsa", null);
        // open a video file
//        jmPlayer.open(new File("video.avi"));
        final String filename = "http://localhost:54702/test.mp3";
        //jmPlayer.open(new File(filename));
        jmPlayer.openResource(filename);
        // skip 2 minutes
        //jmPlayer.setTimePosition(120);
        // set volume to 90%
        //jmPlayer.setVolume(90);


        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    Thread.sleep(20 * 1000);
                    jmPlayer.close();
                } catch (Exception ex) {
                    Logger.getLogger(JMPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();

        t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    Thread.sleep(13 * 1000);
                    jmPlayer.openResource(filename);
                } catch (Exception ex) {
                    Logger.getLogger(JMPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();

        t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    int count = 0;
                    while (count++ < 20) {
                        Thread.sleep(1000);
                        String f = jmPlayer.getPlayingPath();
                        Float pos = jmPlayer.getTimePosition();
                        Float len = jmPlayer.getTotalTime();
                        String s = "#" + f + " " + (pos != null ? pos : "") + (len != null ? "/" + len : "");
                        System.out.println(s);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(JMPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();
    }
}
