package com.martijnkoning.stockwatch;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class StockAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private List<Stock> stockList;
    private MainActivity mainAct;

    StockAdapter(List<Stock> stkList, MainActivity ma) {
        this.stockList = stkList;
        mainAct = ma;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_list_row, parent, false);

        itemView.setOnClickListener(mainAct);
        itemView.setOnLongClickListener(mainAct);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Stock stock = stockList.get(position);

        String priceChangeNeutral = String.format(Locale.US, "%.2f", stock.getPercentage()) + "%";
        String priceChangePositive = "\u25B2 " + String.format(Locale.US, "%.2f", stock.getPriceChange());
        String priceChangeNegative = "\u25BC " + String.format(Locale.US, "%.2f", stock.getPriceChange());
        String percentageChange = "(" + String.format(Locale.US, "%.2f", stock.getPercentage()) + "%)";

        holder.symbol.setText(stock.getSymbol());
        holder.company.setText(stock.getCompany());
        holder.price.setText(String.format(Locale.US, "%.2f", stock.getPrice()));
        holder.percentage.setText(percentageChange);

        if (stock.getPriceChange() > 0) {
            holder.symbol.setTextColor(Color.parseColor("#1cd400"));
            holder.company.setTextColor(Color.parseColor("#1cd400"));
            holder.price.setTextColor(Color.parseColor("#1cd400"));
            holder.priceChange.setText(priceChangePositive);
            holder.priceChange.setTextColor(Color.parseColor("#1cd400"));
            holder.percentage.setTextColor(Color.parseColor("#1cd400"));
        }
        else if (stock.getPriceChange() < 0) {
            holder.symbol.setTextColor(Color.parseColor("#e82300"));
            holder.company.setTextColor(Color.parseColor("#e82300"));
            holder.price.setTextColor(Color.parseColor("#e82300"));
            holder.priceChange.setText(priceChangeNegative);
            holder.priceChange.setTextColor(Color.parseColor("#e82300"));
            holder.percentage.setTextColor(Color.parseColor("#e82300"));
        }
        else
        {
            holder.symbol.setTextColor(Color.parseColor("#ABABAB"));
            holder.company.setTextColor(Color.parseColor("#ABABAB"));
            holder.price.setTextColor(Color.parseColor("#ABABAB"));
            holder.priceChange.setTextColor(Color.parseColor("#ABABAB"));
            holder.percentage.setTextColor(Color.parseColor("#ABABAB"));
            holder.priceChange.setText(priceChangeNeutral);
        }
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
}
