package com.martijnkoning.stockwatch;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

class MyViewHolder extends RecyclerView.ViewHolder {
    TextView symbol;
    TextView company;
    TextView price;
    TextView priceChange;
    TextView percentage;

    MyViewHolder(View view) {
        super(view);
        symbol = view.findViewById(R.id.symbol);
        company = view.findViewById(R.id.company);
        price = view.findViewById(R.id.price);
        priceChange = view.findViewById(R.id.priceChange);
        percentage = view.findViewById(R.id.percentage);
    }
}
