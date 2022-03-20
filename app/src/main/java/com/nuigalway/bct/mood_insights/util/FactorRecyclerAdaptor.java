package com.nuigalway.bct.mood_insights.util;

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

import java.util.ArrayList;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.O)
public class FactorRecyclerAdaptor extends RecyclerView.Adapter<FactorRecyclerAdaptor.MyViewHolder> {

    private final ArrayList<Factor> factorsList;
    private final FactoryRecyclerViewClickListener listener;
    private FactorPage fp;
    private Sleep currentSleepFactors;

    public FactorRecyclerAdaptor(ArrayList<Factor> factorsList, FactoryRecyclerViewClickListener listener, FactorPage fp){
        this.factorsList = factorsList;
        this.listener = listener;
        this.fp = fp;
        currentSleepFactors = fp.getCurrentSleep();
    }

    @NonNull
    @Override
    public FactorRecyclerAdaptor.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_factors, parent, false);
        //itemView.setBackgroundColor(parent.getContext().getResources().getColor(R.color.factorUnselected));
        return new MyViewHolder(itemView, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull FactorRecyclerAdaptor.MyViewHolder holder, int position) {
        String factor = factorsList.get(position).getFactorName();
        holder.factorNameText.setText(factor);
        if(currentSleepFactors.getSleepFactorValue(factor)){
            holder.getView().setBackgroundColor(holder.getParent().getContext().getResources().getColor(R.color.factorSelected));
            fp.addTextViewFactorToFactorViewListEnabled(holder.factorNameText);
        }else{
            holder.getView().setBackgroundColor(holder.getParent().getContext().getResources().getColor(R.color.factorUnselected));
        }
    }

    @Override
    public int getItemCount() {
        return factorsList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView factorNameText;
        private final ViewGroup parent;
        private final View view;

        public MyViewHolder(final View view, ViewGroup parent){
            super(view);
            this.parent = parent;
            this.view = view;
            factorNameText = view.findViewById(R.id.factorHolder);
            view.setOnClickListener(this);
        }

        public View getParent(){
            return parent;
        }

        public View getView(){
            return view;
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
