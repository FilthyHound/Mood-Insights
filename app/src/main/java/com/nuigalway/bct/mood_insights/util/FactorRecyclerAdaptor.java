package com.nuigalway.bct.mood_insights.util;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.nuigalway.bct.mood_insights.FactorPage;
import com.nuigalway.bct.mood_insights.R;
import com.nuigalway.bct.mood_insights.data.Factor;
import com.nuigalway.bct.mood_insights.data.Sleep;
import com.nuigalway.bct.mood_insights.user.User;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class FactorRecyclerAdaptor extends RecyclerView.Adapter<FactorRecyclerAdaptor.MyViewHolder> {

    private ArrayList<Factor> factorsList;
    private FactoryRecyclerViewClickListener listener;

    public FactorRecyclerAdaptor(ArrayList<Factor> factorsList, FactoryRecyclerViewClickListener listener){
        this.factorsList = factorsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FactorRecyclerAdaptor.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_factors, parent, false);
        itemView.setBackgroundColor(parent.getContext().getResources().getColor(R.color.factorUnselected));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FactorRecyclerAdaptor.MyViewHolder holder, int position) {
        String factor = factorsList.get(position).getFactorName();
        holder.factorNameText.setText(factor);
    }

    @Override
    public int getItemCount() {
        return factorsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView factorNameText;

        public MyViewHolder(final View view){
            super(view);
            factorNameText = view.findViewById(R.id.factorHolder);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onClick(v, getAdapterPosition());
        }
    }

    public interface FactoryRecyclerViewClickListener{
        void onClick(View v, int position);
    }
}
