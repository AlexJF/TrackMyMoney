/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.preferences;

import net.alexjf.tmm.R;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;

public class URLPreference extends Preference {
    private String url;
    private TypedArray attributeArray;

    public URLPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(attrs);
    }

    public URLPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(attrs);
    }

    public URLPreference(Context context) {
        super(context);
    }

    protected void initialize(AttributeSet attrs) {
        if (attrs != null) {
            attributeArray = getContext().obtainStyledAttributes(attrs,
                     R.styleable.URLPreference);
            url = attributeArray.getString(R.styleable.URLPreference_url);
        }
       
        setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference pref) {
                if (url != null) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                        Uri.parse(url));
                    getContext().startActivity(browserIntent);
                }
                return true;
            };
        });
    }
}
