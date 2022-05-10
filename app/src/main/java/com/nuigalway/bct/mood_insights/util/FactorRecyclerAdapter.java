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

/**
 * FactorRecyclerAdapter class handles the recycler adapter to display the factor elements.
 *
 * Gets data from the FactorPage instance to refer to how each factor should be colour coded if the
 * user has already selected Factors for that day
 *
 * Class extends RecyclerView.Adapter
 *
 * @author Karl Gordon
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class FactorRecyclerAdapter extends RecyclerView.Adapter<FactorRecyclerAdapter.CustomViewHolder> {
    // Private final fields
    private final ArrayList<Factor> factorsList;
    private final FactoryRecyclerViewClickListener listener;
    private final FactorPage fp;
    private final Sleep currentSleepFactors;

    /**
     * Constructor method to initialise the FactorRecyclerAdaptor, gets the factor list, the
     * Recycler click listener and a reference to the factor page
     *
     * @param factorsList - List of Factors, read in from the internal strings.xml file
     * @param listener - FactoryRecyclerViewClickListener, listens to user clicks on a recycler view
     * @param fp - FactorPage, reference to get data from the user profile
     */
    public FactorRecyclerAdapter(ArrayList<Factor> factorsList, FactoryRecyclerViewClickListener listener, FactorPage fp){
        this.factorsList = factorsList;
        this.listener = listener;
        this.fp = fp;
        currentSleepFactors = fp.getCurrentSleep();
    }

    /**
     * Method creates the View Holder to hold the Child View types
     *
     * @param parent - The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType - The view type of the new View
     * @return The Custom View holder for the TextView
     */
    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_factors, parent, false);
        return new CustomViewHolder(itemView, parent);
    }

    /**
     * Method handles the binding of Views to the CustomViewHolder based on the position parameter
     *
     * @param holder - CustomViewHolder to update and represent the contents of the item at the
     *                 given position in the data set.
     * @param position – The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        String factor = factorsList.get(position).getFactorName();
        holder.factorNameText.setText(factor);

        // Change the colour of the View if the factor was already selected from a prior session
        if(currentSleepFactors != null && currentSleepFactors.getSleepFactorValue(factor)){
            holder.getView().setBackgroundColor(holder.getParent().getContext()
                    .getResources().getColor(R.color.factorSelected));
            fp.addTextViewFactorToFactorViewEnabledList(holder.factorNameText);
        }else{
            holder.getView().setBackgroundColor(holder.getParent().getContext()
                    .getResources().getColor(R.color.factorUnselected));
        }
    }

    /**
     * Getter method gets the number of items currently placed in the RecyclerAdapter
     *
     * @return int, size of the factors ArrayList
     */
    @Override
    public int getItemCount() {
        return factorsList.size();
    }


    /**
     * Protected inner-class CustomViewHolder, extends RecyclerView.ViewHolder, and implements the
     * OnClickListener, passed in the FactorRecyclerAdapter constructor
     */
    protected class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Private final fields
        private final TextView factorNameText;
        private final ViewGroup parent;
        private final View view;

        /**
         * Constructor, calls its super method, and holds the Inflated view that will hold the
         * TextView factors, ViewGroup parent and sets the onClickListener
         *
         * @param view - View, the inflated view to hold the factors
         * @param parent - ViewGroup, holds the view that holds the Factor, retains the application
         *                 context necessary to change the view colour.
         */
        public CustomViewHolder(final View view, ViewGroup parent){
            super(view);
            this.parent = parent;
            this.view = view;
            factorNameText = view.findViewById(R.id.factorHolder);
            view.setOnClickListener(this);
        }

        /**
         * Getter method returns the ViewGroup parent
         *
         * @return The parent ViewGroup
         */
        public View getParent(){
            return parent;
        }

        /**
         * Getter method returns the inflated View for the TextView of factors
         *
         * @return the inflated View for the TextView of factors
         */
        public View getView(){
            return view;
        }

        /**
         * Method called when the user clicks the RecyclerAdapter, starts the listener, by passing
         * in the View that was clicked on, as well as its position in the RecyclerView
         *
         * @param v View, (TextView) that was clicked on
         */
        @Override
        public void onClick(View v) {
            listener.onClick(v, getAdapterPosition());
        }
    }


    /**
     * Interface used in FactorPage to carry the functionality of a user click on a RecyclerView
     * element
     */
    public interface FactoryRecyclerViewClickListener{
        void onClick(View v, int position);
    }
}
