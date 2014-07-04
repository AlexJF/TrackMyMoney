/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import android.app.Application;
import android.content.res.Resources;

/**
 * This class handles resolution of drawables from their names to their ids,
 * backed up by a local cache for faster execution.
 *
 * Cache of actual drawable objects is not needed since the android system
 * appears to do it by default: 
 * http://stackoverflow.com/questions/10135216/is-there-any-reason-in-preloading-drawables-form-resources
 */
public class DrawableResolver {
    private static DrawableResolver instance = null;

    private Resources res;
    private String packageName;
    private Map<String, Integer> map;

    public static final void initialize(Application app) {
        if (instance == null) {
            instance = new DrawableResolver(app);
        }
    }

    public static final DrawableResolver getInstance() {
        return instance;
    }

    private DrawableResolver(Application app) {
        res = app.getResources();
        packageName = app.getPackageName();
        map = new LinkedHashMap<String, Integer>();
    }

    /**
     * Performs a mapping from drawable name to drawable identifier.
     *
     * @param name The name of the drawable
     * @return The id of the drawable (or 0 in case of error/non-existance)
     */
    public int getDrawableId(String name) {
        if (name == null) {
            return 0;
        }

        Integer drawableId = map.get(name);

        if (drawableId == null) {
            drawableId = res.getIdentifier(name, "drawable", packageName);
            map.put(name, drawableId);
        }

        return drawableId;
    }
}
