package harper.mapsmeproject.handle;

import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import harper.mapsmeproject.interfaces.OnMapObjectsUpdate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;

public class EntityDataHandler {

    private Thread taskThread;

    private boolean running;
    private NodeList nodes;

    private OnMapObjectsUpdate.Callback callback;

    public EntityDataHandler(final Context context) {
        taskThread = new Thread(new Runnable() {

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                if (nodes == null) {
                    try (InputStream is = context.getAssets().open("row.xml")){
                        final Document doc = new ResourceHandler().new XML()
                                .get(is);

                        nodes = doc.getElementsByTagName("timestep");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                Looper.prepare();
                startUpdates();
            }

            private void startUpdates() {
                running = true;
                for (int i = 0; i < nodes.getLength() && running; i = (i + 1) % nodes.getLength()) {
                    if (nodes.item(i) instanceof Element) {
                        callback.onUpdate((Element)nodes.item(i));
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    public void startUpdates(OnMapObjectsUpdate.Callback callback) {
        if (taskThread != null) {
            this.callback = callback;
            taskThread.start();
        }
    }

    public void stopUpdates() {
        running = false;
    }
}
