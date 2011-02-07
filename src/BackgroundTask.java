/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.Random;
import javax.swing.SwingWorker;

/**
 *
 * @author dmn
 */
public class BackgroundTask extends SwingWorker<String, Integer> {

    @Override
    protected String doInBackground() throws Exception {
        Random r = new Random();
        int v = 0, max = r.nextInt(300) + 50;
        for (int i = 0; i < max; i++) {
            v = r.nextInt(300);
            Thread.sleep(r.nextInt(60) + 3);
            //publish(v);
            if (isCancelled() || Thread.interrupted()) {
                break;
            }
            setProgress((int) ((float) (i + 1) * 100 / max));
        }
        setProgress(100);
        return "" + v;
    }

    @Override
    protected void done() {
        super.done();
    }

    public boolean isSuccessful() {
        return true;
    }
}
