package com.sscctv.seeeyesonvif.Settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.util.List;

import static android.widget.Toast.makeText;

public class AdapterRangeList extends RecyclerView.Adapter<AdapterRangeList.ViewHolder> {
    private static final String TAG = AdapterRangeList.class.getSimpleName();

    private List<ItemRange> items;
    private Context context;
    private TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private Toast toast = null;
    private boolean chk;

    public AdapterRangeList(Context context, List<ItemRange> items) {
        super();
        this.context = context;
        this.items = items;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_eth_range, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ItemRange item = items.get(position);
        holder.num.setText(String.valueOf(item.getNum()));
        holder.use.setText(item.getResult());

//        Log.d(TAG, "ITEM: " + item.getResult());
        if(item.getResult().equals("use")) {
            holder.layout.setBackgroundColor(context.getResources().getColor(R.color.orange));
        } else {
            holder.layout.setBackgroundColor(context.getResources().getColor(R.color.DarkCyan));
        }
    }


    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout layout;
        private TextView use;
        private TextView num;

        ViewHolder(View view) {
            super(view);

            layout = view.findViewById(R.id.ran_layout);
            use = view.findViewById(R.id.ran_use);
            num = view.findViewById(R.id.ran_num);
//            view.setOnCreateContextMenuListener(this);
            chk = false;


        }


    }


    private void toastShow(String message) {
        if (toast == null) {
            toast = makeText(context, message, Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    public void onUpdate(int position) {
        notifyItemChanged(position);
//        notifyDataSetChanged();

    }

    public void onStatus(int position) {
        items.get(position).setResult("use");
    }
    public void offStatus(int position) {
        items.get(position).setResult("");
//        notifyDataSetChanged();

    }

}
