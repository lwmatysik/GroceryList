package edu.cvtc.android.grocerylist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Lance Matysik on 12/5/15.
 */

public class GroceryListActivity extends SherlockFragmentActivity {

    private EditText groceryEditText;
    private TextView groceryTotal;
    private Button groceryButton;
    private ListView groceryListView;

    private int selected_position;

    private Realm realm;
    private RealmResults<GroceryItem> groceryItems;

    private static final String SAVED_EDIT_TEXT = "groceryEditText";

    private Menu groceryMainMenu;

    private ActionMode actionMode;
    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            actionMode = mode;
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.actionmenu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_remove:
                    removeGroceryItem();
                    mode.finish();
                    break;
                default:
                    break;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_grocerylist);

        initLayout();
        initListeners();

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        groceryEditText.setText(preferences.getString(SAVED_EDIT_TEXT, ""));

        realm = Realm.getInstance(this);
        groceryItems = realm.where(GroceryItem.class).findAll();

        groceryItems.sort("groceryCategory", Sort.ASCENDING, "groceryItem", Sort.ASCENDING);

        final GroceryListAdapter adapter = new GroceryListAdapter(this, R.id.groceryListViewGroup, groceryItems, true);
        groceryListView.setAdapter(adapter);
        groceryTotal.setText("Total: " + groceryItems.size() + " Items");

        hideSoftKeyboard();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        groceryMainMenu = menu;
        inflater.inflate(R.menu.mainmenu, groceryMainMenu);
        return super.onCreateOptionsMenu(groceryMainMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_empty) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Confirm");
            builder.setMessage("Are you sure you want to empty your grocery list?");
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    removeAll();
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.show();

        }

        if (item.getItemId() == R.id.menu_email) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Confirm");
            builder.setMessage("Are you sure you want to email your grocery list?");
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    RealmQuery<GroceryItem> query = realm.where(GroceryItem.class);
                    RealmResults<GroceryItem> result = query.findAll();
                    groceryItems.sort("groceryCategory", Sort.ASCENDING, "groceryItem", Sort.ASCENDING);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy");
                    String currentDateAndTime = simpleDateFormat.format(new Date());

                    StringBuilder stringBuilder = new StringBuilder("Hello,\n\nHere is today's grocery list:\n\n");

                    for (GroceryItem item : result) {
                        stringBuilder.append(item.getGroceryItem() + "  [" + item.getGroceryCategory() + "]\n");
                    }

                    stringBuilder.append("\nTotal items: " + groceryItems.size() + "\n\nThank you");

                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setType("message/rfc822");
                    intent.setData(Uri.parse("mailto:"));
                    intent.putExtra(Intent.EXTRA_EMAIL, "");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Grocery List: " + currentDateAndTime);
                    intent.putExtra(Intent.EXTRA_TEXT, "" + stringBuilder);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    dialog.dismiss();

                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }

                }
            });

            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void initLayout() {
        groceryEditText = (EditText) findViewById(R.id.newGroceryEditText);
        groceryButton = (Button) findViewById(R.id.addGroceryButton);
        groceryListView = (ListView) findViewById(R.id.groceryListViewGroup);
        groceryTotal = (TextView) findViewById(R.id.totalGroceriesTextView);

        groceryListView.setClickable(true);
        groceryListView.setLongClickable(true);
        groceryListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                GroceryListActivity.this.selected_position = position;
                actionMode = GroceryListActivity.this.startActionMode(actionModeCallback);
                view.setSelected(true);
                return true;
            }
        });

    }

    private void initListeners() {

        groceryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!groceryEditText.getText().toString().isEmpty()) {
                    addGroceryFromEditText();
                } else {
                    Toast.makeText(GroceryListActivity.this, "There is no grocery item to add.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager methodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            methodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addGroceryFromEditText() {

        final CharSequence categories[] = new CharSequence[] {
                "Beverages",
                "Baked",
                "Canned",
                "Dairy",
                "Dry",
                "Frozen",
                "Meat",
                "Produce",
                "Other"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a category");
        builder.setItems(categories, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final String groceryItem = groceryEditText.getText().toString();
                final String groceryCategory = categories[which].toString();

                if (groceryItem != null && !groceryItem.isEmpty()) {
                    groceryEditText.setText("");
                    addGroceryItem(groceryItem, groceryCategory);
                }

            }
        });
        builder.show();
    }

    private void addGroceryItem(final String item, final String category) {
        realm = Realm.getInstance(this);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                GroceryItem groceryItem = realm.createObject(GroceryItem.class);
                groceryItem.setGroceryItem(item);
                groceryItem.setGroceryCategory(category);
            }
        });

        groceryTotal.setText("Total: " + groceryItems.size() + " Items");
        hideSoftKeyboard();
    }

    private void removeGroceryItem() {
        realm = Realm.getInstance(this);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                GroceryItem groceryItem = (GroceryItem) groceryListView.getItemAtPosition(selected_position);
                RealmQuery<GroceryItem> query = realm.where(GroceryItem.class);
                query.equalTo("groceryItem", groceryItem.getGroceryItem());
                RealmResults<GroceryItem> result = query.findAll();
                result.remove(0);
            }
        });
        groceryTotal.setText("Total: " + groceryItems.size() + " Items");
    }
    
    private void removeAll() {
        realm = Realm.getInstance(this);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<GroceryItem> results = realm.where(GroceryItem.class).findAll();
                results.clear();
            }
        });
        groceryTotal.setText("Total: " + groceryItems.size() + " Items");
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        preferences.edit().putString(SAVED_EDIT_TEXT, groceryEditText.getText().toString()).commit();
    }

}