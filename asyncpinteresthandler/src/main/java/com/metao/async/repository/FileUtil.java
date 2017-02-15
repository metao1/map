package com.metao.async.repository;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by metao on 2/12/2017.
 */
public class FileUtil {

    public static String readFile(Context context, String fileName) {
        BufferedReader reader = null;
        String data = "";
        String mLine = "";
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getResources().getAssets().open(fileName)));
            while ((mLine = reader.readLine()) != null) {
                data += mLine;
            }
        } catch (IOException e) {
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    return data;
                } catch (IOException e) {
                    return null;
                }
            }
            return null;
        }
    }
}
