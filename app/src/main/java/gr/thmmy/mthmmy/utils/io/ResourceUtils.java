package gr.thmmy.mthmmy.utils.io;

import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import timber.log.Timber;

public class ResourceUtils {
    public static String readJSONResourceToString(Resources resources, int id) {
        InputStream inputStream = resources.openRawResource(id);
        return readStream(inputStream);
    }

    public static String readJSONResourceToString(InputStream inputStream) {
        return readStream(inputStream);
    }

    private static String readStream(InputStream inputStream) {
        Writer writer = new StringWriter();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line = reader.readLine();
            while (line != null) {
                writer.write(line);
                line = reader.readLine();
            }
        } catch (Exception e) {
            Timber.e(e, "Unhandled exception while using readJSONFromResource");
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                Timber.e(e, "Unhandled exception while using readJSONFromResource");
            }
        }

        return writer.toString();
    }
}

