
import java.net.URL;
import java.util.List;
import javax.swing.AbstractListModel;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dmn
 */
public class PlaylistListModel<T> extends AbstractListModel {

    private List<T> content;

    public PlaylistListModel(List<T> content) {
        this.content = content;
    }

    public List<T> getContent() {
        return content;
    }

    @Override
    public int getSize() {
        return content.size();
    }

    @Override
    public T getElementAt(int index) {
        return content.get(index);
    }

    public void setContent(List<T> content) {
        this.content = content;
        fireContentsChanged(this, 0, Integer.MAX_VALUE);
    }
}
