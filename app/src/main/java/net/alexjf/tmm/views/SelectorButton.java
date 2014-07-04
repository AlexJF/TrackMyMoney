/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.views;

import net.alexjf.tmm.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class SelectorButton extends RelativeLayout {
    private ImageView selectionIcon;
    private Button selectionButton;
    private ImageView errorIcon;
    private TypedArray attributeArray;

    public SelectorButton(Context context) {
        super(context);
        initialize(context, null);
    }

    public SelectorButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public SelectorButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    protected void initialize(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater)
               context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_selector_button, this);

        if (attrs != null) {
            attributeArray = getContext().obtainStyledAttributes(attrs,
                     R.styleable.SelectorButton);
        }
    }

    public void setOnClickListener(OnClickListener listener) {
        selectionButton.setOnClickListener(listener);
    }

    public String getText() {
        return selectionButton.getText().toString();
    }

    public void setText(CharSequence text) {
        selectionButton.setText(text);
    }

    public void setText(int textResourceId) {
        selectionButton.setText(getResources().getText(textResourceId));
    }

    public void setDrawableId(int drawableId) {
        if (drawableId == 0) {
            selectionIcon.setVisibility(View.GONE);
        } else {
            selectionIcon.setImageResource(drawableId);
            selectionIcon.setVisibility(View.VISIBLE);
        }
    }

    public void setError(boolean error) {
        if (error) {
            errorIcon.setVisibility(View.VISIBLE);
        } else {
            errorIcon.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onFinishInflate() {
        selectionIcon = (ImageView) findViewById(R.id.selection_icon);
        selectionButton = (Button) findViewById(R.id.selection_button);
        errorIcon = (ImageView) findViewById(R.id.selection_error);
        selectionButton.setText(attributeArray.getString(
                    R.styleable.SelectorButton_android_text));
        super.onFinishInflate();
    }
}
