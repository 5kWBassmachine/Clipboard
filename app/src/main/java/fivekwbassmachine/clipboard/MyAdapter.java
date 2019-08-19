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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<String> keys;
    private OnButtonListener myOnButtonListener;

    public MyAdapter(List<String> keys, OnButtonListener onButtonListener) {
        this.keys = keys;
        this.myOnButtonListener = onButtonListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(itemView, myOnButtonListener);
    }

    @Override
    public int getItemCount() {
        return keys.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.position.setText("" + (position + 1));
        String s = keys.get(position);
        holder.key.setText(s);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView position;
        public TextView key;
        public ImageButton copy;
        public ImageButton edit;
        public ImageButton delete;
        OnButtonListener onButtonListener;

        public ViewHolder(View itemView, OnButtonListener onButtonListener) {
            super(itemView);
            position = (TextView)itemView.findViewById(R.id.position);
            key = (TextView)itemView.findViewById(R.id.key);
            copy = (ImageButton)itemView.findViewById(R.id.copy);
            edit = (ImageButton)itemView.findViewById(R.id.edit);
            delete = (ImageButton)itemView.findViewById(R.id.delete);
            this.onButtonListener = onButtonListener;

            copy.setOnClickListener(this);
            edit.setOnClickListener(this);
            delete.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.copy:
                    onButtonListener.onButtonClick(getAdapterPosition(), Constants.ACTION_COPY);
                    break;
                case R.id.edit:
                    onButtonListener.onButtonClick(getAdapterPosition(), Constants.ACTION_EDIT);
                    break;
                case R.id.delete:
                    onButtonListener.onButtonClick(getAdapterPosition(), Constants.ACTION_DELETE);
                    break;
            }
        }
    }

    public interface OnButtonListener {
        void onButtonClick(int position, short action);
    }
}
