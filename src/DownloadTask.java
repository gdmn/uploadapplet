
import java.io.*;
import java.net.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author gorladam
 */
public class DownloadTask extends BackgroundTask implements SpeedCounter {

    private URL source;
    private File destination;
    private int bytesPerSecond, overallBytesPerSecond;
    private String name;
    private boolean ok = false;

    public DownloadTask(URL source, File destination) {
        this.source = source;
        this.destination = destination;
        this.name = source.getPath() == null ? null : EncodingTools.urlDecodeUTF(new File(source.getPath()).getName());
    }

    @Override
    public int getBytesPerSecond() {
        return bytesPerSecond;
    }

    @Override
    public int getOverallBytesPerSecond() {
        return overallBytesPerSecond;
    }

    @Override
    protected String doInBackground() throws Exception {
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        long done = 0;
        File outputFile = null;
        try {
            URLConnection connection = source.openConnection();
            long contentLength = connection.getContentLength();
            outputFile = null;
            if (!destination.isDirectory()) {
                outputFile = destination;
            } else if (name == null && destination.isDirectory()) {
                outputFile = new File(destination + (destination.toString().endsWith(File.separator) ? "" : File.separator) + "unknown_name");
            } else if (name != null && destination.isDirectory()) {
                outputFile = new File(destination + (destination.toString().endsWith(File.separator) ? "" : File.separator) + name);
            }
            if (!outputFile.isDirectory() && outputFile.exists()) {
                int i = 1;
                String fileName = outputFile.getAbsolutePath();
                int p = fileName.lastIndexOf(".");
                String k = null;
                do {
                    k = (p > 0) ? fileName + "." + i : fileName.substring(0, p) + i + fileName.substring(p);
                    outputFile = new File(k);
                    i++;
                } while (outputFile.exists());
            }
            //System.out.println("" + contentLength + " " + connection.getContentType() + " " + destination + " " + name + " -> " + newDestination);
            if (outputFile == null) {
                throw new IOException("Destination not given");
            }

            in = new BufferedInputStream(connection.getInputStream());
            out = new BufferedOutputStream(new FileOutputStream(outputFile));
            byte[] buffer = new byte[1024 * 128];
            int read = -1;
            long t1, t2, t3;
            t1 = System.nanoTime();
            t2 = t1;
            t3 = t1;
            while ((read = in.read(buffer)) > -1) {
                if (isCancelled() || Thread.interrupted()) {
                    return "terminated";
                }
                out.write(buffer, 0, read);
                done += read;
                t3 = System.nanoTime();
                double diff = 0.000000001d * (t3 - t1);
                t2 = t3;
                bytesPerSecond = (int) ((double) done / diff);
                if (contentLength > 0) {
                    setProgress((int) ((float) 100 * done / contentLength));
                }
            }
            double diff = 0.000000001d * (t3 - t1);
            overallBytesPerSecond = (int) ((double) done / diff);
            setProgress(100);
            ok = true;
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
                if (!ok) {
                    boolean deleted = outputFile.delete();
                    if (!deleted) {
                        outputFile.deleteOnExit();
                        System.err.println("Can not delete " + outputFile + ", deleting on exit");
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean isSuccessful() {
        return ok;
    }

    @Override
    public String toString() {
        return name != null ? name : source.toString();
    }
}
