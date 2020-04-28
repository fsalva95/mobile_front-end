package com.example.mobileapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


//adapter for listview on selectcars fragment

public class CarAdapter extends BaseAdapter {



    private LayoutInflater inflater;
    private ArrayList<Car> objects;

    String address="";
    String state="";


    private class ViewHolder {
        TextView textView1;
        TextView textView2;
        TextView textView3;
        TextView textView4;

    }

    public CarAdapter(Context context, ArrayList<Car> objects) {
        inflater = LayoutInflater.from(context);
        this.objects = objects;
    }


    public int getCount() {
        return objects.size();
    }

    public Car getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.fragment_element_list, null);
            holder.textView1 = (TextView) convertView.findViewById(R.id.list_1);
            holder.textView2 = (TextView) convertView.findViewById(R.id.list_2);
            holder.textView3 = (TextView) convertView.findViewById(R.id.list_3);
            holder.textView4 = (TextView) convertView.findViewById(R.id.list_4);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        address="";
        state="";


        holder.textView3.setText("Car number: "+ objects.get(position).getId());




        holder.textView1.setText(objects.get(position).getAddress());
        holder.textView2.setText(objects.get(position).getState());


        if (objects.get(position).getBusy().equals("false")){
            holder.textView4.setText("Available");
            holder.textView4.setTypeface(null, Typeface.BOLD);
        }else{
            holder.textView4.setText("Busy");
            holder.textView4.setTypeface(null, Typeface.BOLD);

        }


        return convertView;
    }


}
