/*
 * --downbtn y --upbtn y --upchange n --upsrv "http://127.0.0.1:11380" --mplayer "c:\progs\mplayer\mplayer.exe"
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author dmn
 */
public class SendForm extends JPanel {

	private File outputDirectory = FileSystemView.getFileSystemView().getHomeDirectory();
	private String preveriousDownloadUrl = null;
	private String preveriousUploadUrl = null;
	private File preveriousUploadDir = FileSystemView.getFileSystemView().getHomeDirectory();
	private JList browser;
//	private String proxyHost = null;
//	private int proxyPort = 0;

	{
		Authenticator.setDefault(MyAuthenticator.getAuthenticator());
	}

	public SendForm(boolean downloadButton, boolean uploadButton, String initialOutputDirectory, boolean canChangeOutputDirectory,
			String initialUploadServer, final boolean canChangeUploadServer, String initialInputDirectory,
			final String mplayer_bin, final String mplayer_args,
			final String proxyHost, final int proxyPort) {

//		this.proxyHost = proxyHost;
//		this.proxyPort = proxyPort;

		if (initialOutputDirectory != null) {
			outputDirectory = new File(initialOutputDirectory);
		}
		if (initialUploadServer != null) {
			preveriousUploadUrl = initialUploadServer;
			if (!preveriousUploadUrl.endsWith("/")) {
				preveriousUploadUrl = preveriousUploadUrl + '/';
			}
		}
		if (initialInputDirectory != null) {
			preveriousUploadDir = new File(initialInputDirectory);
		}
		setLayout(new BorderLayout());

		final JPanel browserPanel = new JPanel();
		browserPanel.setLayout(new BorderLayout());
		browser = null;
		if (initialUploadServer != null) {
			try {
				browser = new JList(new MyBrowserListModel(preveriousUploadUrl));
				browser.setSelectedIndex(0);
				browser.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2 && e.getSource() instanceof JList && ((JList) e.getSource()).getModel() instanceof MyBrowserListModel) {
							JList list = (JList) e.getSource();
							MyBrowserListModel model = (MyBrowserListModel) list.getModel();
							Object[] selectedValues = list.getSelectedValues();
							if (selectedValues.length == 1) {
								Link selected = (Link) selectedValues[0];
								//System.out.println("selected " + selected.getUrl() + " " + (selected.isDir() ? "dir" : "file"));
								try {
									list.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
									if (selected.isDir()) {
										model.setURL(selected.getUrl());
									}
									list.setSelectedIndex(0);
								} finally {
									list.setCursor(Cursor.getDefaultCursor());
								}
							}
						}
					}
				});
				browser.addKeyListener(new KeyAdapter() {

					@Override
					public void keyTyped(KeyEvent e) {
						if (e.getSource() instanceof JList && ((JList) e.getSource()).getModel() instanceof MyBrowserListModel) {
							if (e.getKeyChar() == KeyEvent.VK_ENTER) {
								JList list = (JList) e.getSource();
								MyBrowserListModel model = (MyBrowserListModel) list.getModel();
								Object[] selectedValues = list.getSelectedValues();
								if (selectedValues.length == 1) {
									Link selected = (Link) selectedValues[0];
									//System.out.println("selected " + selected.getUrl() + " " + (selected.isDir() ? "dir" : "file"));
									try {
										list.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
										if (selected.isDir()) {
											model.setURL(selected.getUrl());
										}
										list.setSelectedIndex(0);
									} finally {
										list.setCursor(Cursor.getDefaultCursor());
									}
								}
							} else {
								super.keyTyped(e);
							}
						} else {
							super.keyTyped(e);
						}
					}
				});
			} catch (MalformedURLException ex) {
				Logger.getLogger(SendForm.class.getName()).log(Level.SEVERE, null, ex);
			}

			final JScrollPane browserScrollPane = new JScrollPane(browser);
			browserScrollPane.getVerticalScrollBar().setUnitIncrement(10);
			browserScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
			browserPanel.add(browserScrollPane);
		}

		final JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		final JScrollPane contentScrollPane = new JScrollPane(content);
		contentScrollPane.getVerticalScrollBar().setUnitIncrement(10);
		contentScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

		if (browser != null) {
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, browserPanel, contentScrollPane);
			splitPane.setResizeWeight(0.5);
			splitPane.setOneTouchExpandable(true);
			splitPane.setContinuousLayout(true);
			this.add(splitPane, BorderLayout.CENTER);
		} else {
			this.add(content, BorderLayout.CENTER);
		}
		content.add(Box.createVerticalGlue());

		JButton downloadUrl = new JButton("Download URL...");
		downloadUrl.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String link = JOptionPane.showInputDialog(SendForm.this, "URL?", "Download", JOptionPane.QUESTION_MESSAGE);
				URL url;
				if (link != null) {
					try {
						url = new URL(link);
						BackgroundTask bt = new BackgroundTask();
						bt = new DownloadTask(url, outputDirectory);
						SendControl sc = new SendControl(bt);
						content.add(sc, content.getComponentCount() - 1);
						content.validate();
						contentScrollPane.getViewport().setViewPosition(new Point(0, Integer.MAX_VALUE));
						contentScrollPane.validate();
						bt.execute();
					} catch (MalformedURLException ex) {
						JOptionPane.showMessageDialog(SendForm.this, "URL is malformed.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		JButton downloadSelectedBtn = new JButton("Download");
		downloadSelectedBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] selectedValues = browser.getSelectedValues();
				if (!(selectedValues.length == 1 && ((Link) selectedValues[0]).getName().equals("..")) && !(selectedValues.length == 0)) {
					for (Object selected : selectedValues) {
						Link link = (Link) selected;
						if (link.getName().equals("..")) {
							continue;
						}
						BackgroundTask bt = new BackgroundTask();
						bt = new DownloadTask(link.getUrl(), outputDirectory);
						SendControl sc = new SendControl(bt);
						content.add(sc, content.getComponentCount() - 1);
						bt.execute();
					}
					content.validate();
					contentScrollPane.getViewport().setViewPosition(new Point(0, Integer.MAX_VALUE));
					contentScrollPane.validate();
				}
			}
		});

		JButton playlistBtn = new JButton("Make playlist...");
		playlistBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(outputDirectory);
				int returnVal = fc.showSaveDialog(SendForm.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					Object[] selectedValues = browser.getSelectedValues();
					FileWriter writer = null;
					try {
						writer = new FileWriter(fc.getSelectedFile());
						if ((selectedValues.length == 1 && ((Link) selectedValues[0]).getName().equals("..")) || selectedValues.length == 0) {
							MyBrowserListModel model = (MyBrowserListModel) browser.getModel();
							for (int i = 0; i < model.getSize(); i++) {
								Link link = (Link) model.getElementAt(i);
								if (!link.getName().equals("..")) {
									writer.append(link.getUrl().toString() + "\r\n");
								}
							}
						} else {
							for (Object selected : selectedValues) {
								Link link = (Link) selected;
								if (!link.getName().equals("..")) {
									writer.append(link.getUrl().toString() + "\r\n");
								}
							}
						}
					} catch (IOException ex) {
						Logger.getLogger(SendForm.class.getName()).log(Level.SEVERE, null, ex);
					} finally {
						try {
							if (writer != null) {
								writer.close();
							}
						} catch (IOException ex) {
							Logger.getLogger(SendForm.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
				}
			}
		});

		JButton headButton = new JButton("Head");
		headButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] selectedValues = browser.getSelectedValues();
				try {
					StringBuilder builder = new StringBuilder();
					if ((selectedValues.length == 1 && ((Link) selectedValues[0]).getName().equals("..")) || selectedValues.length == 0) {
						MyBrowserListModel model = (MyBrowserListModel) browser.getModel();
						for (int i = 0; i < model.getSize(); i++) {
							Link link = (Link) model.getElementAt(i);
							if (!link.getName().equals("..")) {
								builder.append(link.getUrl().toString()).append("\r\n");
								builder.append(Browser.head(link.getUrl()));
								builder.append("\r\n");
							}
						}
					} else {
						for (Object selected : selectedValues) {
							Link link = (Link) selected;
							if (!link.getName().equals("..")) {
								builder.append(link.getUrl().toString()).append("\r\n");
								builder.append(Browser.head(link.getUrl()));
								builder.append("\r\n");
							}
						}
					}
					JOptionPane.showMessageDialog(content, builder.toString());
				} finally {
				}
			}
		});

		JButton playBtn = new JButton("Play");
		if (mplayer_bin != null) {
			playBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					Object[] selectedValues = browser.getSelectedValues();
					ArrayList<Link> list = new ArrayList<Link>(selectedValues.length);
					try {
						if ((selectedValues.length == 1 && ((Link) selectedValues[0]).getName().equals("..")) || selectedValues.length == 0) {
							MyBrowserListModel model = (MyBrowserListModel) browser.getModel();
							for (int i = 0; i < model.getSize(); i++) {
								Link link = (Link) model.getElementAt(i);
								if (!link.getName().equals("..")) {
									list.add(link);
								}
							}
						} else {
							for (Object selected : selectedValues) {
								Link link = (Link) selected;
								if (!link.getName().equals("..")) {
									list.add(link);
								}
							}
						}
						PlaylistForm playlistForm = new PlaylistForm(mplayer_bin, mplayer_args, proxyHost == null ? null : proxyHost + ":" + proxyPort);
						playlistForm.append(list);
						playlistForm.showForm();
					} finally {
					}
				}
			});
		}

		JButton uploadBtn = new JButton("Upload...");
		uploadBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(preveriousUploadDir);
				fc.setMultiSelectionEnabled(true);
				int returnVal = fc.showOpenDialog(SendForm.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					preveriousUploadDir = fc.getCurrentDirectory();
					String link;
					if (canChangeUploadServer) {
						link = JOptionPane.showInputDialog(SendForm.this, "URL?", preveriousUploadUrl);
					} else {
						link = preveriousUploadUrl;
					}
					URL url;
					if (link != null) {
						try {
							url = new URL(link);
							preveriousUploadUrl = link.toString();
							for (File f : fc.getSelectedFiles()) {
								BackgroundTask bt = new BackgroundTask();
								bt = new UploadTask(f, url);
								SendControl sc = new SendControl(bt);
								content.add(sc, content.getComponentCount() - 1);
								content.validate();
								contentScrollPane.getViewport().setViewPosition(new Point(0, Integer.MAX_VALUE));
								contentScrollPane.validate();
								bt.execute();
							}
						} catch (MalformedURLException ex) {
							JOptionPane.showMessageDialog(SendForm.this, "URL is malformed.", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		});

		JButton outputBtn = new JButton("Output... (currently: " + outputDirectory.getName() + ")");
		outputBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setSelectedFile(outputDirectory);
				int returnVal = fc.showOpenDialog(SendForm.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					SendForm.this.outputDirectory = file;
					((JButton) e.getSource()).setText("Output... (currently: " + file.getName() + ")");
				}
			}
		});

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		bottomPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		bottomPanel.setBackground(Color.white);
		if (browser != null) {
			bottomPanel.add(downloadSelectedBtn);
		}
		if (browser != null) {
			bottomPanel.add(headButton);
			if (mplayer_bin != null) {
				bottomPanel.add(playBtn);
			}
		}
		if (downloadButton) {
			bottomPanel.add(downloadUrl);
		}
		if (downloadButton && canChangeOutputDirectory) {
			bottomPanel.add(outputBtn);
		}
		if (uploadButton && (canChangeUploadServer || browser != null)) {
			bottomPanel.add(Box.createHorizontalStrut(8));
			bottomPanel.add(uploadBtn);
		}
		this.add(bottomPanel, BorderLayout.PAGE_END);
	}

	public static void showHelp() {
		final String[] help = new String[]{
			"UploadManager",
			"Example usage:",
			"   java -jar [archivename].jar -d y -u y -z n -s \"http://192.168.1.10:8080\"",
			"Parameters:",
			"  -h, --help        - short help",
			"  -d, --downbtn     - show download button",
			"  -u, --upbtn       - show upload button",
			"  -s, --upsrv       - upload server (full url)",
			"  -z, --upchange    - user can choose upload server",
			"  -p, --proxy       - proxy (server:port)",
			"  -m, --mplayer     - full path and parameters to mplayer (space separated)"
		};
		for (String s : help) {
			System.out.println(s);
		}
	}

	private static class Initializer implements Runnable {

		String[] args;

		public Initializer(String[] args) {
			this.args = args;
		}

		@Override
		public void run() {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ex) {
			}

			String prev_arg = null;
			boolean helpDisplayed = false;
			boolean downbtn = true, upbtn = true, upchange = true;
			String upsrv = null;
			String mplayer_bin = null, mplayer_args = null;
			String proxyHost = null;
			int proxyPort = 0;

//                System.out.print("Params:");
//                for (String arg : argtemp2) {
//                    System.out.print(" " + arg);
//                }
			System.out.println();
			for (String arg : args) {
				if (arg.charAt(0) == '-') {
					if ("-h".equals(arg) || "--help".equals(arg)) {
						showHelp();
						System.exit(0);
						helpDisplayed = true;
					} else {
						prev_arg = arg.toLowerCase();
					}
				} else if (prev_arg != null) {
					if ("-d".equals(prev_arg) || "--downbtn".equals(prev_arg)) {
						if (arg.equals("n")) {
							downbtn = false;
						} else if (arg.equals("y")) {
							downbtn = true;
						} else {
							showHelp();
							helpDisplayed = true;
						}
					} else if ("-u".equals(prev_arg) || "--upbtn".equals(prev_arg)) {
						if (arg.equals("n")) {
							upbtn = false;
						} else if (arg.equals("y")) {
							upbtn = true;
						} else {
							showHelp();
							helpDisplayed = true;
						}
					} else if ("-z".equals(prev_arg) || "--upchange".equals(prev_arg)) {
						if (arg.equals("n")) {
							upchange = false;
						} else if (arg.equals("y")) {
							upchange = true;
						} else {
							showHelp();
							helpDisplayed = true;
						}
					} else if ("-s".equals(prev_arg) || "--upsrv".equals(prev_arg)) {
						upsrv = arg;
					} else if ("-m".equals(prev_arg) || "--mplayer".equals(prev_arg)) {
						int second = arg.startsWith("\"") ? arg.indexOf('\"', 1) : (arg.startsWith("'") ? arg.indexOf('\'', 1) : -1);
						if (second > 1) {
							mplayer_bin = arg.substring(1, second);
							if (arg.length() > second + 1) {
								mplayer_args = arg.substring(second + 2);
							}
						} else {
							second = arg.indexOf(' ');
							if (second > 1) {
								mplayer_bin = arg.substring(0, second);
								mplayer_args = arg.substring(second + 1);
							} else {
								mplayer_bin = arg;
								mplayer_args = null;
							}
						}
					} else if ("-p".equals(prev_arg) || "--proxy".equals(prev_arg)) {
						int p = arg.lastIndexOf(':');
						if (p > 1) {
							proxyHost = arg.substring(0, p);
							String sPort = arg.substring(p + 1);
							proxyPort = Integer.valueOf(sPort);
						} else {
							showHelp();
							helpDisplayed = true;
						}
					} else {
						System.err.println("Unknown parameter: " + prev_arg);
						System.exit(-2);
					}
					prev_arg = null;
				} else {
					System.err.println("Unknown parameter: " + arg);
					System.exit(-2);
				}
			}
			if (prev_arg != null) {
				System.err.println("Unknown parameter or inproper usage: " + prev_arg);
				System.exit(-2);
			}
			if (helpDisplayed) {
				System.exit(-3);
			}

			Properties sp = System.getProperties();
			if (proxyHost != null && proxyPort > 0) {
				sp.setProperty("http.proxyHost", proxyHost);
				sp.setProperty("http.proxyPort", "" + proxyPort);
				sp.setProperty("ftp.proxyHost", proxyHost);
				sp.setProperty("ftp.proxyPort", "" + proxyPort);
				sp.setProperty("https.proxyHost", proxyHost);
				sp.setProperty("https.proxyPort", "" + proxyPort);
			}
			final JFrame frame = new JFrame("upload manager");
			frame.setLayout(new BorderLayout());
			frame.add(new SendForm(downbtn, upbtn, null, true, upsrv, upchange, null, mplayer_bin, mplayer_args, proxyHost, proxyPort), BorderLayout.CENTER);
			frame.setMinimumSize(new Dimension(600, 300));
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
		}
	}

	public static void main(final String args[]) {
		final String[] argtemp = new String[]{
			//-p localhost:8080 -z n -s http://localhost -d y
			"-p", "localhost:8080",
			"-z", "n", //"-d", "n",
			//"-u", "n",
			"-s", "http://localhost/tmp"
		};
		final String[] argtemp2 = new String[]{
			"-z", "n",
			//"-d", "n",
			//"-u", "n",
			"-s", "http://localhost:8080"
		};
		final String[] argtemp3 = new String[]{
			//-z n -s http://localhost:11380
			"-z", "n",
			"-s", "http://localhost:8080/"
		};
		SwingUtilities.invokeLater(new Initializer(args));
	}
}
