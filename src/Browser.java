
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

public class Browser {

    private URL url;
    private String urlString;
    private boolean listPrepared = false;
    private ArrayList<Link> list;
    private final Object listLock = new Object();

    public Browser(URL url) {
        setURL(url);
    }

    public Browser(String url) throws MalformedURLException {
        this(new URL(url));
    }

    public ArrayList<Link> getList() {
        if (!listPrepared) {
            ls(); // never happen
        }
        return list;
    }

    public void setURL(URL url) {
        URL prevURL = this.url;
        String prevURLStr = this.urlString;
        try {
            synchronized (listLock) {
                this.url = url;
                this.urlString = url.toString();
                listPrepared = false;
                if (!ls()) {
                    this.url = prevURL;
                    this.urlString = prevURLStr;
                }
            }
        } catch (Exception e) {
            this.url = prevURL;
            this.urlString = prevURLStr;
        }
    }

    public int getSize() {
        return getList().size();
    }

    private static ArrayList<Link> convertToList(String url, String buf) {
        ArrayList<Link> result = new ArrayList<Link>();
        try {
            try {
                //URI r = new URI(url + "..").normalize();
                URL r = new URL(new URL(url), "..");
                if (r.toString().indexOf("..") == -1) {
                    result.add(new Link("..", r, true));
                } else {
                }
            } catch (Exception ex) {
                //System.err.println(ex.toString());
            }
            for (String s : buf.split("\n")) {
                int i_dir = s.indexOf("<a class=\"dir\"");
                int i_file = s.indexOf("<a class=\"file\"");
                if (i_dir > -1 || i_file > -1) {
                    //outBuf.append(s + "\r\n");
                    int i_href = s.indexOf("href=\"", i_dir > -1 ? i_dir : i_file);
                    int i_name = s.indexOf("\">", i_href);
                    int i_a = s.indexOf("</a>", i_name);
                    int i_span = s.indexOf("<span class=", i_name);
                    if (i_href > -1 && i_name > -1 && i_a > -1) {
                        String href = s.substring(i_href + 6, i_name);
                        if (href.indexOf(":/") == -1) {
                            href = url + href;
                        }
                        href = href.replace(" ", "%20");
                        String name = (i_span == -1 ? s.substring(i_name + 2, i_a) : s.substring(i_name + 2, i_span)).trim();
                        //System.out.println("> " + s + " " + name + " " + href);
                        result.add(new Link(name, new URL(href), i_dir > -1));
                        //outBuf.append(href + "|" + name + "\r\n");
                    }
                }
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private boolean ls() {
        listPrepared = false;
        list = ls(url);
        listPrepared = list != null;
        return listPrepared;
    }

    public static ArrayList<Link> ls(URL url) {
        ArrayList<Link> result = new ArrayList<Link>();
        BufferedInputStream in = null;
        StringBuilder outBuf = new StringBuilder();
        //long done = 0;
        try {
            try {
                URLConnection connection = url.openConnection();
                //long contentLength = connection.getContentLength();
                in = new BufferedInputStream(connection.getInputStream());
                byte[] buffer = new byte[1024 * 128];
                int read = -1;
                //long t1, t3;
                //t1 = System.nanoTime();
                //t3 = t1;
                while ((read = in.read(buffer)) > -1) {
                    if (buffer.length == read) {
                        outBuf.append(new String(buffer));
                    } else {
                        outBuf.append(new String(Arrays.copyOf(buffer, read)));
                    }
                    //done += read;
                }
                //t3 = System.nanoTime();
                //double diff = 0.000000001d * (t3 - t1);
                //int overallBytesPerSecond = (int) ((double) done / diff);
                //System.out.println(String.format("processing time: %.6f s", new Double(diff)) + ", bytes per second = " + overallBytesPerSecond);
                result = convertToList(url.toString(), outBuf.toString());
                return result;
            } catch (IOException e) {
                throw e;
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
