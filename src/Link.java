import java.net.URL;

/**
 *
 * @author gorladam
 */
public class Link {

    private String name;
    private URL url;
    private boolean dir;

    public Link(String name, URL url, boolean isDirectory) {
        this.name = name;
        this.url = url;
        this.dir = isDirectory;
    }

    public String getName() {
        return name;
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean isDir() {
        return dir;
    }

    
}
