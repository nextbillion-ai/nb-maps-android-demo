package ai.nextbillion.utils;

import android.content.Context;
import android.util.TypedValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import androidx.annotation.RawRes;

public class ResourceUtils {

    public static String readRawResource(Context context, @RawRes int rawResource) throws IOException {
        String json = "";
        if (context != null) {
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            try (InputStream is = context.getResources().openRawResource(rawResource)) {
                Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                int numRead;
                while ((numRead = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, numRead);
                }
            }
            json = writer.toString();
        }
        return json;
    }

    public static float convertDpToPx(Context context, float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}

