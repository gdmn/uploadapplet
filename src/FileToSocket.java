
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author dmn
 */
public class FileToSocket implements Runnable {

    @Override
    public void run() {
        String filename = "/home/dmn/Pulpit/stromae - alors on danse.mp3";
        String mplayerexecutable = "/usr/bin/mplayer";
        try {
            ServerSocket server = new ServerSocket(8888);
            while (true) {
                Socket client = server.accept();
                System.out.println("got new client");
                BufferedOutputStream socketOut = new BufferedOutputStream(client.getOutputStream());
                BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(new File(filename)));
                int read;
                byte[] buf = new byte[4096];
                while ((read = fileIn.read(buf)) > 0) {
                    socketOut.write(buf, 0, read);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FileToSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

     public static void main(String[] args)  {
         Thread t = new Thread(new FileToSocket());
         t.start();
     }
}
