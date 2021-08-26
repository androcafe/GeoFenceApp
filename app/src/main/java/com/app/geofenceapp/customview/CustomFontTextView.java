package com.app.geofenceapp.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import java.util.Locale;

/**
 * Created by pc2 on 7/9/2016.
 */
@SuppressLint("AppCompatCustomView")
public class CustomFontTextView extends AppCompatTextView {

    private static final String sScheme = "http://schemas.android.com/apk/res-auto";
    private static final String sAttribute = "customFont";

    enum CustomFont {
        FONT_NUNITO_REGULAR("fonts/nunito_regular.ttf"),
        FONT_NUNITO_BOLD("fonts/nunito_bold.ttf"),
        FONT_NUNITO_SEMIBOLD("fonts/nunito.semibold.ttf"),
        FONT_LATO_BOLD("fonts/nunito_bold.ttf"),
        FONT_LATO_ITALIC("fonts/lato_italic.ttf"),
        FONT_LATO_REGULAR("fonts/Lato-Regular.ttf");

        private final String fileName;

        CustomFont(String fileName) {
            this.fileName = fileName;
        }

        static CustomFont fromString(String fontName) {
            return CustomFont.valueOf(fontName.toUpperCase(Locale.US));
        }

        public Typeface asTypeface(Context context) {
            return Typeface.createFromAsset(context.getAssets(), fileName);
        }
    }

    public CustomFontTextView(Context context) {
        super(context);
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) {
            return;
        } else {
            final String fontName = attrs.getAttributeValue(sScheme, sAttribute);

            if (fontName == null) {
               // throw new IllegalArgumentException("You must provide \"" + sAttribute + "\" for your text view");
            } else {
                final Typeface customTypeface = CustomFont.fromString(fontName).asTypeface(context);
                setTypeface(customTypeface);
            }
        }
    }
}