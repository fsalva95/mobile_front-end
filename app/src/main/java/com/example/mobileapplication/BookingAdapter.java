package com.example.mobileapplication;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


//adapter for listview on activities fragment

public class BookingAdapter extends BaseAdapter {



    private LayoutInflater inflater;
    private ArrayList<Booking> objects;

    private class ViewHolder {
        TextView textView1;
        TextView textView2;
        TextView textView3;

    }

    public BookingAdapter(Context context, ArrayList<Booking> objects) {
        inflater = LayoutInflater.from(context);
        this.objects = objects;
    }


    public int getCount() {
        return objects.size();
    }

    public Booking getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.fragment_activity_list, null);
            holder.textView1 = (TextView) convertView.findViewById(R.id.activity_1);
            holder.textView2 = (TextView) convertView.findViewById(R.id.activity_2);
            holder.textView3 = (TextView) convertView.findViewById(R.id.activity_3);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }



        holder.textView3.setText("Car number: "+ objects.get(position).getCar().getId());
        holder.textView1.setText("Start activity: "+objects.get(position).getStart_date());
        holder.textView2.setText("End activity: "+objects.get(position).getEnd_date());

        return convertView;
    }

}