
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.AbstractListModel;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author gorladam
 */
public class MyBrowserListModel extends AbstractListModel {

    private String server;
    private Browser browser;

    public MyBrowserListModel(String server) throws MalformedURLException {
        this.server = server;
        this.browser = new Browser(server);
    }

    @Override
    public int getSize() {
        return browser.getSize();
    }

    @Override
    public Object getElementAt(int index) {
        return browser.getList().get(index);
    }

    public void setURL(URL url) {
        browser.setURL(url);
        fireContentsChanged(this, 0, Integer.MAX_VALUE);
    }
}
