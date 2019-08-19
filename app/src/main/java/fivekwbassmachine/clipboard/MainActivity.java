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
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MyAdapter.OnButtonListener {
    private static final String TAG = "MainActivity";
    private List<String> listKey = new ArrayList<>();
    private List<String> listValue = new ArrayList<>();
    //unused-0 private View parentLayout;
    private Context context;
    private Utils.FileInternal fileInternal;
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //unused-0 this.parentLayout = findViewById(android.R.id.content);
        this.context = this;

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
        this.fileInternal = new Utils.FileInternal(context,"list.json", true);
        fileInternal.resetCurrentTry();
        String tmpFile = fileInternal.read();
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
            fileInternal.resetCurrentTry();
            fileInternal.write(Utils.FileInternal.WRITE_REPLACE, tmpArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
    }
}
