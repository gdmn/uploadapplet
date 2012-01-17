
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author dmn
 */
public class PlaylistForm extends JPanel {

    private JList playlist;
    private JPanel centerPanel = new JPanel();
    private JPanel bottomPanel = new JPanel();
    private JMPlayer player;
    private JLabel label = new JLabel("[0/0]");
    private JLabel timeLabel = new JLabel();
    private Thread monitorThread = new Thread(new MonitorThread());
    private int playingIndex = -1;
    private boolean pause = true;
    private JButton playButton;

    public PlaylistForm(String bin, String args, String proxy) {
        try {
            player = new JMPlayer(bin, args, proxy);
        } catch (IOException ex) {
            Logger.getLogger(PlaylistForm.class.getName()).log(Level.SEVERE, null, ex);
			throw new RuntimeException(ex);
        }

        this.setLayout(new BorderLayout());
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);
        centerPanel.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        topPanel.setBackground(Color.white);
        this.add(topPanel, BorderLayout.NORTH);
        topPanel.add(label, BorderLayout.CENTER);
        topPanel.add(timeLabel, BorderLayout.EAST);
        bottomPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        bottomPanel.setBackground(Color.white);

        playButton = new JButton(">");
        Dimension buttonDimension = new Dimension(60, 25);
        playButton.setPreferredSize(buttonDimension);
        playButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedIndices = playlist.getSelectedIndices();
                if (selectedIndices.length == 1) {
                    play(selectedIndices[0]);
                } else {
                    if (playlist.getModel().getSize() > 0) {
                        play(0);
                    }
                }
            }
        });
        bottomPanel.add(playButton);

        JButton pauseButton = new JButton("||");
        pauseButton.setPreferredSize(buttonDimension);
        pauseButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                togglePause();
            }
        });
        bottomPanel.add(pauseButton);

        JButton prevButton = new JButton("<<");
        prevButton.setPreferredSize(buttonDimension);
        prevButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                playPrev();
            }
        });
        bottomPanel.add(prevButton);

        JButton nextButton = new JButton(">>");
        nextButton.setPreferredSize(buttonDimension);
        nextButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                playNext();
            }
        });
        bottomPanel.add(nextButton);
    }

    private void togglePause() {
        if (player.getTimePosition() != null) {
            player.togglePlay();
            pause = !pause;
        }
    }

    private void playNext() {
        int id = playingIndex + 1;
        PlaylistListModel<Link> model = (PlaylistListModel<Link>) playlist.getModel();
        if (model.getSize() < 1) {
            playingIndex = -1;
            pause = true;
            return;
        }
        if (id >= model.getSize()) {
            id = 0;
        }
        play(id);
    }

    private void playPrev() {
        int id = playingIndex - 1;
        PlaylistListModel<Link> model = (PlaylistListModel<Link>) playlist.getModel();
        if (model.getSize() < 1) {
            playingIndex = -1;
            pause = true;
            return;
        }
        if (id < 0) {
            id = model.getSize() - 1;
        }
        play(id);
    }

    private void play(int id) {
        try {
            PlaylistListModel<Link> model = (PlaylistListModel<Link>) playlist.getModel();
            Link value = model.getElementAt(id);
            player.openResource(value.getUrl().toString());
            playingIndex = id;
            pause = false;
        } catch (IOException ex) {
            Logger.getLogger(PlaylistForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String toTime(Float value) {
        int v = value.intValue();
        int min = v / 60;
        int sec = v - min * 60;
        return min + ":" + (sec < 10 ? "0" + sec : sec);
    }

    class MonitorThread implements Runnable {

        @Override
        public void run() {
            String s = null, s2 = null;
            Float pos = null;
            String f;
            Float len;
            URLDecoder dec = new URLDecoder();
            try {
                int noPlayingCounter = 0;
                Thread.sleep(500);
                while (!Thread.interrupted()) {
                    Thread.sleep(200);
                    s2 = null;
                    if (player != null) {
                        if (player.isProcessSpawned()) {
                            if (!pause) {
                                f = player.getPlayingFilename();
                                f = f == null ? null : dec.decode(f, "UTF-8");
                                pos = player.getTimePosition();
                                len = player.getTotalTime();
                                s = "[" + (playingIndex + 1) + "/" + playlist.getModel().getSize() + "]"
                                        + ((f == null || "(null)".equals(f)) ? "" : " " + f);
                                s2 = (pos != null && pos >= 0 ? " " + toTime(pos) : "") + (len != null && len >= 0 ? "/" + toTime(len) : "");
                            } else {
                                s = "[" + (playingIndex + 1) + "/" + playlist.getModel().getSize() + "] "
                                        + "paused";
                            }
                            //System.out.println("# " + s);
                            if (pause) {
                                noPlayingCounter = 0;
                            } else if (pos == null) {
                                noPlayingCounter++;
                            } else {
                                noPlayingCounter = 0;
                            }
                            if (noPlayingCounter >= 3) {
                                playNext();
                                noPlayingCounter = 0;
                            }
                        } else {
                            s = "process died";
                        }
                    } else {
                        s = "null player";
                    }
                    label.setText(s);
                    timeLabel.setText(s2 == null ? "" : s2);
                }
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(PlaylistForm.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
            } finally {
            }
        }
    }

    public void showForm() {
        JFrame frame = new JFrame("player");
        frame.setSize(new Dimension(400, 200));
        frame.setContentPane(this);
        frame.setLocationRelativeTo(null);
        monitorThread.start();
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                monitorThread.interrupt();
                if (player != null) {
                    player.close();
                }
                //System.out.println("dead!");
            }
        });
    }

    public void append(List<Link> filelist) {
        ListModel model = new PlaylistListModel(filelist);
        playlist = new JList(model);
        final JScrollPane browserScrollPane = new JScrollPane(playlist);
        browserScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        browserScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        centerPanel.add(browserScrollPane, BorderLayout.CENTER);

        playlist.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2 && e.getSource() instanceof JList) {
                    playButton.doClick();
                }
            }
        });
        playlist.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getSource() instanceof JList) {
                    if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    playButton.doClick();
                    }
                }
            }
        });
    }
}
