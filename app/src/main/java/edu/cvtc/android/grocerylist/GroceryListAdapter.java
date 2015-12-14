package edu.cvtc.android.grocerylist;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by Lance Matysik on 12/5/15.
 */

public class GroceryListAdapter extends RealmBaseAdapter<GroceryItem> implements ListAdapter {

    private static class GroceryView {
        TextView groceryItem;
    }

    public GroceryListAdapter(Context context,
                              int resId,
                              RealmResults<GroceryItem> realmResults,
                              boolean automaticUpdate) {

        super(context, realmResults, automaticUpdate);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        GroceryView groceryView;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_grocerylist_listview, parent, false);
            groceryView = new GroceryView();
            groceryView.groceryItem = (TextView) convertView.findViewById(R.id.groceryListTextView);
            convertView.setTag(groceryView);
        } else {
            groceryView = (GroceryView) convertView.getTag();
        }

        GroceryItem item = realmResults.get(position);
        groceryView.groceryItem.setText(item.getGroceryItem() + "\t\t(" + item.getGroceryCategory() + ")");
        return convertView;
    }

    public RealmResults<GroceryItem> getRealmResults() {
        return realmResults;
    }

}
