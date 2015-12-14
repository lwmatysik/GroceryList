package edu.cvtc.android.grocerylist;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Lance Matysik on 12/5/15.
 */

public class GroceryItem extends RealmObject {

    @PrimaryKey
    private String groceryItem;
    private String groceryCategory;

    public String getGroceryItem() {
        return groceryItem;
    }

    public void setGroceryItem(String groceryItem) {
        this.groceryItem = groceryItem;
    }

    public String getGroceryCategory() {
        return groceryCategory;
    }

    public void setGroceryCategory(String groceryCategory) {
        this.groceryCategory = groceryCategory;
    }

}
