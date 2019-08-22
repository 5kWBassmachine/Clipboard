/*
 * MIT License
 *
 * Copyright (c) 2019 5kWBassMachine
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package fivekwbassmachine.clipboard;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class MainActivity extends AppCompatActivity implements MyAdapter.OnButtonListener {
    private static final String TAG = "MainActivity";
    private List<String> listKey = new ArrayList<>();
    private List<String> listValue = new ArrayList<>();
    //unused-0 private View parentLayout;
    private Context context;
    private Utils.FileHandler list;
    private MyAdapter myAdapter;
    private String version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //unused-0 this.parentLayout = findViewById(android.R.id.content);
        this.context = this;
        this.version = BuildConfig.VERSION_NAME;

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add();
            }
        });

        RecyclerView recyclerView = findViewById(R.id.list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        myAdapter = new MyAdapter(listKey, this);
        recyclerView.setAdapter(myAdapter);

        getLists();

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target) {
                return current.getItemViewType() == target.getItemViewType();
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int positionDragged = viewHolder.getAdapterPosition();
                int positionTarget = target.getAdapterPosition();
                Collections.swap(listKey, positionDragged, positionTarget);
                Collections.swap(listValue, positionDragged, positionTarget);
                return true;
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                // Action finished
                applyChanges();
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });
        helper.attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_version).setTitle(getString(R.string.action_version) + ' ' + version);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch(item.getItemId()) {
            case R.id.action_export_import:
                final TextView textView = new TextView(context);
                textView.setText(R.string.alert_export_import_example);
                new AlertDialog.Builder(context)
                        .setTitle(getString(R.string.alert_export_import_title))
                        .setView(textView)
                        .setPositiveButton(getString(R.string.alert_confirm_import), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String value;
                                ClipboardManager clipboardManager = (ClipboardManager)context.getSystemService(CLIPBOARD_SERVICE);
                                if (clipboardManager.hasPrimaryClip()) {
                                    if (clipboardManager.getPrimaryClip().getItemAt(0).getText().toString() != "") {
                                        value = clipboardManager.getPrimaryClip().getItemAt(0).getText().toString();
                                        if (Utils.JSON.isJSONValid(value, Utils.JSON.JSON_ARRAY)) {
                                            list.write(Utils.FileHandler.WRITE_REPLACE, value);
                                            recreate();
                                        } else {
                                            Log.w(TAG, "onClick: cant import data from clipboard: invalid data");
                                            Toast.makeText(context, R.string.feedback_error_import_invalid_clipboard, Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Log.e(TAG, "onClick: cant import data from clipboard: invalid mime type");
                                        Toast.makeText(context, R.string.feedback_error_import_invalid_clipboard, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.e(TAG, "onClick: cant import data from clipboard: no primaryclip");
                                    Toast.makeText(context, R.string.feedback_error_import_invalid_clipboard, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNeutralButton(getString(R.string.alert_confirm_export), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ClipboardManager clipboardManager = (ClipboardManager)context.getSystemService(CLIPBOARD_SERVICE);
                                ClipData clipData = ClipData.newPlainText("input", list.read());
                                if (clipboardManager != null) {
                                    clipboardManager.setPrimaryClip(clipData);
                                    Toast.makeText(getApplicationContext(), R.string.feedback_copied, Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.wtf(TAG, "onButtonClick: unexpected error: clipboardManager is null");
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.alert_abort), null)
                        .show();
                break;
            case R.id.action_update:
                Toast.makeText(context, R.string.feedback_error_not_implemented, Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_fork:
                // open clipboard on github in browser
                String url = getString(R.string.internal_fork);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            case R.id.action_version:
                // do nothing, only info
                //@TODO prevent menu closing
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onButtonClick(final int position, short action) {
        Log.d(TAG, "onButtonClick: pos: " + position + "; action: " + action);
        Log.d(TAG, "Creating new Intent");
        switch(action) {
            case Constants.ACTION_COPY:
                String value = listValue.get(position);
                ClipboardManager clipboardManager = (ClipboardManager)this.getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("input", value);
                if (clipboardManager != null) {
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(getApplicationContext(), R.string.feedback_copied, Toast.LENGTH_SHORT).show();
                } else {
                    Log.wtf(TAG, "onButtonClick: unexpected error: clipboardManager is null");
                }
                break;
            case Constants.ACTION_EDIT:
                edit(position);
                break;
            case Constants.ACTION_DELETE:
                //ask before deleting
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.alert_delete_title))
                        .setMessage(getString(R.string.alert_delete_message))
                        .setPositiveButton(getString(R.string.alert_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                delete(position);
                            }
                        })
                        .setNegativeButton(getString(R.string.alert_no), null)
                        .show();
                break;

        }
    }

    private void getLists() {
        this.list = new Utils.FileHandler(context, Constants.FILE_LIST, true);
        list.resetCurrentTry();
        String tmpFile = list.read();
        if (tmpFile.length() > 0) {
            try {
                JSONArray tmp = new JSONArray(tmpFile);
                for (int i = 0; i < tmp.length(); i++) {
                    listKey.add(tmp.getJSONObject(i).getString("key"));
                    listValue.add(tmp.getJSONObject(i).getString("value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, R.string.feedback_empty_file, Toast.LENGTH_SHORT).show();
        }
    }

    private void delete(int position) {
        Log.d(TAG, "delete: " + position);
        listKey.remove(position);
        listValue.remove(position);
        applyChanges(R.string.feedback_deleted);
    }

    private void edit(final int position) {
        Log.d(TAG, "edit: " + position);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText key = new EditText(context);
        key.setHint(getString(R.string.alert_edittext_key));
        key.setText(listKey.get(position));
        layout.addView(key);
        final EditText value = new EditText(context);
        value.setHint(getString(R.string.alert_edittext_value));
        value.setText(listValue.get(position));
        layout.addView(value);

        new AlertDialog.Builder(context)
                .setTitle(getString(R.string.alert_edit_title))
                .setView(layout)
                .setPositiveButton(getString(R.string.alert_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listKey.set(position, key.getText().toString());
                        listValue.set(position, value.getText().toString());
                        applyChanges(R.string.feedback_edited);
                    }
                })
                .setNegativeButton(getString(R.string.alert_abort), null)
                .show();
    }

    private void add() {
        final int position = listKey.size() + 1;
        Log.d(TAG, "add: " + position);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText key = new EditText(context);
        key.setHint(getString(R.string.alert_edittext_key));
        layout.addView(key);
        final EditText value = new EditText(context);
        value.setHint(getString(R.string.alert_edittext_value));
        layout.addView(value);

        new AlertDialog.Builder(context)
                .setTitle(getString(R.string.alert_add_title))
                .setView(layout)
                .setPositiveButton(getString(R.string.alert_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listKey.add(key.getText().toString());
                        listValue.add(value.getText().toString());
                        applyChanges(R.string.feedback_added);
                    }
                })
                .setNegativeButton(getString(R.string.alert_abort), null)
                .show();
    }


    private void applyChanges(int toastMessage) {
        myAdapter.notifyDataSetChanged();
        try {
            JSONArray tmpArray = new JSONArray();
            for(int i = 0; i < listKey.size(); i++) {
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("key", listKey.get(i));
                tmpObject.put("value", listValue.get(i));
                tmpArray.put(i, tmpObject);
            }
            list.resetCurrentTry();
            list.write(Utils.FileHandler.WRITE_REPLACE, tmpArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
    }
    private void applyChanges() {
        myAdapter.notifyDataSetChanged();
        try {
            JSONArray tmpArray = new JSONArray();
            for(int i = 0; i < listKey.size(); i++) {
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("key", listKey.get(i));
                tmpObject.put("value", listValue.get(i));
                tmpArray.put(i, tmpObject);
            }
            list.resetCurrentTry();
            list.write(Utils.FileHandler.WRITE_REPLACE, tmpArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
