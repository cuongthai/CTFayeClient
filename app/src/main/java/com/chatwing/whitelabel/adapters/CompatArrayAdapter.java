package com.chatwing.whitelabel.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.widget.ArrayAdapter;

import java.util.Collection;
import java.util.List;

/**
 * Author: Huy Nguyen
 * Date: 7/25/13
 * Time: 4:08 PM
 */
public class CompatArrayAdapter<T> extends ArrayAdapter<T> {
    public CompatArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public CompatArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public CompatArrayAdapter(Context context, int textViewResourceId, T[] objects) {
        super(context, textViewResourceId, objects);
    }

    public CompatArrayAdapter(Context context, int resource, int textViewResourceId, T[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public CompatArrayAdapter(Context context, int textViewResourceId, List<T> objects) {
        super(context, textViewResourceId, objects);
    }

    public CompatArrayAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @TargetApi(11)
    public void addAllData(Collection<? extends T> collection) {
        if (collection == null || collection.size() == 0) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 11) {
            super.addAll(collection);
        } else {
            for (T item : collection) {
                super.add(item);
            }
        }
    }

    @Override
    @TargetApi(11)
    public void addAll(T... items) {
        if (Build.VERSION.SDK_INT >= 11) {
            super.addAll(items);
        } else {
            for (T item : items) {
                super.add(item);
            }
        }
    }
}
