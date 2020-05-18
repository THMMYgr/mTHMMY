package gr.thmmy.mthmmy.utils.io;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import timber.log.Timber;

public class AssetUtils {
    public static String readFileToText(Context context, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName)));
            String line;

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            Timber.e(e, "IO error reading file %s from assets.", fileName);
        } catch (Exception e) {
            Timber.e(e, "Error reading file %s from assets.", fileName);
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                Timber.e(e, "Error in AssetUtils (closing reader).");
            }
        }
        return null;
    }
}

