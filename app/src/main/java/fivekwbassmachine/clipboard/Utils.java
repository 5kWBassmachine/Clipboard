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

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public interface Utils {
    class JSON {
        public static final short JSON_OBJECT = 0;
        public static final short JSON_ARRAY = 1;
        public static boolean isJSONValid(String json) {
            try {
                new JSONObject(json);
            } catch (JSONException ex) {
                // edited, to include @Arthur's comment
                // e.g. in case JSONArray is valid as well...
                try {
                    new JSONArray(json);
                } catch (JSONException ex1) {
                    return false;
                }
            }
            return true;
        }
        public static boolean isJSONValid(String json, short type) {
            if (type == 0) {
                try {
                    new JSONObject(json);
                } catch (JSONException e) {
                    return false;
                }
            } else if (type == 1) {
                try {
                    new JSONArray(json);
                } catch (JSONException e) {
                    return false;
                }
            } else {
                return false;
            }
            return true;
        }
    }
    class FileHandler {
        static final short WRITE_PREPEND = 0;
        static final short WRITE_APPEND = 1;
        public static final short WRITE_REPLACE = 2;
        private final Context context;
        private final String filename;
        private final boolean create;
        private short currentTry = 0;
        private final short maximalTry = 2;

        private final static String TAG = "Utils.FileHandler";

        public FileHandler(Context context, String filename, boolean create) {
            this.context = context;
            this.filename = filename;
            this.create = create;
        }

        public void resetCurrentTry() {
            this.currentTry = 0;
        }

        /**
         * read from selected file
         *
         * @return          content of the file as String
         */
        public String read() {
            if (this.currentTry < maximalTry) {
                try {
                    FileInputStream fis = context.openFileInput(this.filename);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader bufferedReader = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    Log.d(TAG, "read: " + this.filename + ": read file");
                    return sb.toString();
                } catch (FileNotFoundException e) {
                    this.currentTry++;
                    if (this.create) {
                        Log.i(TAG, "read: " + this.filename + ": file does not exist, creating and retrying to read");
                        return read();
                    } else {
                        Log.e(TAG, "read: " + this.filename + ": cant read from file: " + e.getMessage());
                        return "";
                    }
                } catch (IOException e) {
                    this.currentTry++;
                    Log.e(TAG, "read: " + this.filename + ": cant read from file: " + e.getMessage());
                    return "";
                }
            } else {
                Log.e(TAG, "read: " + this.filename + ": cant read from file: maximum trys (" + this.maximalTry + ") reached");
                return "";
            }
        }
        /**
         * write to selected file
         *
         * @param action    action
         *                  <hint>
         *                  Utils.FileHandler.WRITE_PREPEND
         *                  Utils.FileHandler.WRITE_APPEND
         *                  Utils.FileHandler.WRITE_REPLACE
         *                  </hint>
         * @param content   content to write as String
         */
        public void write(short action, String content) {
            if (this.currentTry < maximalTry) {
                try {
                    switch(action) {
                        case WRITE_PREPEND:
                            content += read();
                            break;
                        case WRITE_APPEND:
                            content = read() + content;
                            break;
                        /*case WRITE_REPLACE:
                            content = content;
                            break;*/
                    }
                    FileOutputStream fos = context.openFileOutput(this.filename, Context.MODE_PRIVATE);
                    OutputStreamWriter outputWriter=new OutputStreamWriter(fos);
                    outputWriter.write(content);
                    outputWriter.close();
                    Log.d(TAG, "write: " + this.filename + ": file written");
                } catch (FileNotFoundException e) {
                    this.currentTry++;
                    if (this.create) {
                        Log.i(TAG, "write: " + this.filename + ": file does not exist, creating and retrying to write");
                    } else {
                        Log.e(TAG, "write: " + this.filename + ": cant write from file: " + e.getMessage());
                    }
                } catch (IOException e) {
                    this.currentTry++;
                    Log.e(TAG, "write: " + this.filename + ": cant write from file: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "write: " + this.filename + ": cant write to file: maximum trys (" + this.maximalTry + ") reached");
            }
        }
    }
}
