package me.isword.rvextraview;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView rv;

    View errorView;
    View progressView;
    View emptyView;

    final ArrayList<String> mItems = new ArrayList<>();
    ExtraViewAdapter mAdapter;

    AsyncTask<Void, Void, Void> mTask;

    int count = 0; // used for fake loading

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup the RecyclerView
        rv = (RecyclerView) findViewById(R.id.rv);

        // Inflate the views
        errorView = getLayoutInflater().inflate(R.layout.rv_error, rv, false);
        progressView = getLayoutInflater().inflate(R.layout.rv_progress, rv, false);
        emptyView = getLayoutInflater().inflate(R.layout.rv_empty, rv, false);

        // Setup the adapter
        mAdapter = new ExtraViewAdapter();
        rv.setAdapter(mAdapter);

        // Add fake content
        addContent();
    }

    /*
    * Adds fake content continuously every 5 seconds.
    */
    void addContent (){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mTask != null) mTask.cancel(true);

                mTask = new FakeLoading();
                mTask.execute();

                addContent();
            }
        }, 5000);
    }

    /*
    * Delete the clicked item (used to test emptyView)
    */
    public void onItemClicked(View view) {
        int index = (int) view.getTag();

        mItems.remove(index);
        mAdapter.notifyDataSetChanged();
    }

    class ExtraViewAdapter extends RecyclerView.Adapter<ExtraViewAdapter.VH> {
        View extraView = null;

        class VH extends RecyclerView.ViewHolder {
            private final TextView tv;

            VH(View itemView, TextView tv) {
                super(itemView);
                this.tv = tv;
            }
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 0) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.rv_item, parent, false);
                TextView tv = (TextView) view.findViewById(android.R.id.title);
                return new VH(view, tv);

            } else if (viewType == -1) {
                return new VH(emptyView, null);

            } else {
                return new VH(extraView, null);
            }
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            if (position < mItems.size() && mItems.size() > 0) { // or if (holder.tv != null)
                holder.tv.setTag(position);
                holder.tv.setText(mItems.get(position));
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0 && mItems.size() == 0 && extraView == null) return -1;// emptyView
            return position < mItems.size() ? 0 : extraView.hashCode(); // normal or extraView
        }

        @Override
        public int getItemCount() {
            int count = mItems.size();

            if (count == 0 && extraView == null) count++; // for emptyView

            return count + (extraView == null ? 0 : 1);
        }

        public void setExtraView(@Nullable View extraView) {
            this.extraView = extraView;
        }
    }

    class FakeLoading extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mAdapter.setExtraView(progressView);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (isCancelled()) return;

            // Clear progressView
            mAdapter.setExtraView(null);
            mAdapter.notifyDataSetChanged();

            // Used to demonstrate different extraViews
            if (count >= 3) {
                mAdapter.setExtraView(errorView);
                mAdapter.notifyDataSetChanged();
                return;
            } count++;

            // Add dummy content
            for (int i = 0; i < 2; i++) {
                mItems.add(String.format("Item %d", mItems.size()));
            }

            mAdapter.notifyDataSetChanged();
        }
    }
}
