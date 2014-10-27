package com.chatwing.whitelabel.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Cheng Wei
 */
public class CompatibleGridView extends GridView {
    int selectedPosition;

    public CompatibleGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedPosition = i;
            }
        });
    }


    @TargetApi(11)
    public int getSupportCheckedItemPosition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return getCheckedItemPosition();
        } else {
            return selectedPosition;
        }
    }
}