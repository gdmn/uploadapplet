
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

/**
 *
 * @author gorladam
 */
public class UploadTask extends BackgroundTask implements SpeedCounter {

    private File source;
    private URL destination;
    private int bytesPerSecond, overallBytesPerSecond;
    private String name;
    private boolean ok = false;

    public UploadTask(File source, URL destination) {
        this.source = source;
        this.destination = destination;
        this.name = source.getName();//.replace(" ", "%20");
    }

    @Override
    public int getBytesPerSecond() {
        return bytesPerSecond;
    }

    @Override
    public int getOverallBytesPerSecond() {
        return overallBytesPerSecond;
    }

    private static void sendUTF8(BufferedOutputStream output, String text) throws IOException {
        String tmp = text + "\r\n";
        output.write(tmp.getBytes("UTF-8"));
        //System.out.println(tmp.replace("\n", "\n<<< "));
    }
    private static final String BR = "\r\n";

    private static String randomizeBoundary() {
        Random r = new Random();
        StringBuilder result = new StringBuilder(15);
        for (int i = 0; i < 15; i++) {
            result.append(String.valueOf(r.nextInt(10)));
        }
        return result.toString();
    }

    @Override
    protected String doInBackground() throws Exception {
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        Socket socket = null;
        URLConnection urlConnection = null;
        BufferedInputStream answer = null;
        long done = 0;
        try {
            Properties sp = System.getProperties();
            String proxyHost = sp.getProperty("http.proxyHost", null);
            Integer proxyPort = Integer.valueOf(sp.getProperty("http.proxyPort", "0"));
            String boundary = randomizeBoundary();
            long contentLength = source.length();
            String encodedName = EncodingTools.urlEncodeUTF(name);
            if (proxyHost == null) {
                socket = new Socket(destination.getHost(), destination.getPort() > 0 ? destination.getPort() : 80);
                out = new BufferedOutputStream(socket.getOutputStream());
            } else {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                urlConnection = destination.openConnection(proxy);
                HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
                httpConnection.setDoInput(true);
                httpConnection.setAllowUserInteraction(true);
                httpConnection.setDefaultUseCaches(false);
                httpConnection.setDoOutput(true);
                httpConnection.setFixedLengthStreamingMode((int) (contentLength + 44 + 2 +
                        38 + 4 + 13 + encodedName.length() + 1 + 2 +
                        38 + 2 + 2 +
                        2 + 46 + 2));
                httpConnection.setRequestMethod("POST");
                httpConnection.setUseCaches(false);

                //answer = new BufferedInputStream(urlConnection.getInputStream());
                httpConnection.addRequestProperty("Content-Type", "multipart/form-data; boundary=---------------------------" + boundary);
//                httpConnection.addRequestProperty("Content-Length", String.valueOf(contentLength + 44 + 2 +
//                        38 + 4 + 13 + name.length() + 1 + 2 +
//                        38 + 2 + 2 +
//                        2 + 46 + 2));
                out = new BufferedOutputStream(urlConnection.getOutputStream());
            }

            //System.out.println("" + contentLength + " " + connection.getContentType() + " " + destination + " " + name + " -> " + newDestination);
            in = new BufferedInputStream(new FileInputStream(source));
            if (proxyHost == null) {
                sendUTF8(out, "POST / HTTP/1.1" + BR +
                        //> Host: localhost:8080
                        //> Connection: keep-alive
                        //> Referer: http://localhost:8080/upload/
                        "Content-Type: multipart/form-data; boundary=---------------------------" + boundary + BR +
                        "Content-Length: " + (contentLength + 44 + 2 +
                        38 + 4 + 13 + encodedName.length() + 1 + 2 +
                        38 + 2 + 2 +
                        2 + 46 + 2) + BR);
            }
            sendUTF8(out, "-----------------------------" + boundary + BR + // 44+2
                    "Content-Disposition: form-data; name=\"name\"; filename=\"" + encodedName + "\"" + BR + //38+4(name)+13+x(file)+1+2
                    "Content-Type: application/octet-stream" + BR); //38+2   +2

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

            sendUTF8(out, BR + "-----------------------------" + boundary + "--"); // 2+46   +2
            out.flush();
            double diff = 0.000000001d * (t3 - t1);
            overallBytesPerSecond = (int) ((double) done / diff);
            setProgress(100);
            if (urlConnection != null) {
                String result = null;
                if (urlConnection instanceof HttpURLConnection) {
                    int code = ((HttpURLConnection) urlConnection).getResponseCode();
                    if (code > 0) {
                        ok = code == 200;
                        result = ((HttpURLConnection) urlConnection).getResponseMessage() + " (" + code + ")";
                    }
                }
                answer = new BufferedInputStream(urlConnection.getInputStream());
                while ((read = answer.read(buffer)) > -1) {
                    System.out.println(new String(Arrays.copyOf(buffer, read)));
                }
                if (result != null) {
                    return result;
                }
            }
            ok = true;
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        } finally {
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
            if (in != null) {
                in.close();
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
