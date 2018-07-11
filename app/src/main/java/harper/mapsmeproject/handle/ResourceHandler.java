package harper.mapsmeproject.handle;

import android.content.res.AssetManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Xml;
import harper.mapsmeproject.models.MapQueue;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.Buffer;

public class ResourceHandler {

    private static final String serverAdress = "http://localhost:8000";

    public class JSON {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Nullable
        public MapQueue get(InputStream is) throws IOException {

            if (is == null) {
                throw new IOException("Unable to get stream");
            }

            try {
                StringBuilder sb = new StringBuilder();

                try (BufferedReader bf = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    while ((line = bf.readLine()) != null) {
                        sb.append(line);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                final JSONObject json = new JSONObject(sb.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return null;
        }
    }

    //For a while
    public class XML {
        @RequiresApi(Build.VERSION_CODES.KITKAT)
        @Nullable
        public Document get(InputStream is) throws IOException {
            if (is == null) {
                throw new IOException("Unable to get stream");
            }

            try {
                DocumentBuilderFactory db = DocumentBuilderFactory.newInstance();
                return db.newDocumentBuilder().parse(is);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return null;
        }
    }
}
