package com.metao.async.download.appConstants;

import java.util.UUID;

/**
 * Created by metao on 1/28/2017.
 */
public class Helper {

    public static String createNewId() {
        return UUID.randomUUID().toString().substring(0, 5).replace("-", "");
    }
}
