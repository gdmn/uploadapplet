
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gorladam
 */
public class Player {
        String filename = "/home/dmn/test.mp3";
        //"c:\\Program Files\\SMPlayer\\mplayer\\mplayer.exe"
        String mplayerexecutable = "/usr/bin/mplayer";
        //"d:\\music\\test.mp3"
	private void test() throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder(mplayerexecutable, "-cache-min 1 -");
		Process process = processBuilder.start();
		BufferedInputStream inputFile = new BufferedInputStream(new FileInputStream(new File(filename)));
		BufferedOutputStream output = new BufferedOutputStream(process.getOutputStream());
		BufferedInputStream input = new BufferedInputStream(process.getInputStream());


                int read;
                byte[] buf = new byte[4096];
                while ((read = inputFile.read(buf)) > 0) {
                    output.write(buf, 0, read);
                }

	}

    public static void main(final String args[]) throws IOException {
		Player p = new Player();
		p.test();
	}
}
