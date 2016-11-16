package com.example.dzj.theweather;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


/**
 * Created by dzj on 2016/11/7.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<String> mtime,mtmp,mwind,mtxt,city,ntmp;
    private List<Integer> micon;

    private LayoutInflater mInflater;
    public enum ITEM_TYPE {
        ITEM1,
        ITEM2
    }

    public RecyclerViewAdapter(Context context, List<String> time,List<String>tmp,List<Integer>icon,List<String>wind,List<String>txt,List<String> Ntmp,List<String> mcity)
    {
        mInflater = LayoutInflater.from(context);
        mtime = time;
        mtmp=tmp;
        micon=icon;
        mwind=wind;
        mtxt=txt;
        city=mcity;
        ntmp=Ntmp;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        if (viewType == ITEM_TYPE.ITEM2.ordinal()) {
            return new Item2ViewHolder(mInflater.inflate(R.layout.today,parent,false));
        }
        else{
            return new Item1ViewHolder(mInflater.inflate(R.layout.item_home,parent,false));
        }
    }
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position){
        if(holder instanceof Item1ViewHolder){
            ((Item1ViewHolder)holder).time.setText(mtime.get(position));
            ((Item1ViewHolder)holder).weather_text.setText(mtxt.get(position));
            ((Item1ViewHolder)holder).tmp.setText(mtmp.get(position));
            ((Item1ViewHolder)holder).wind.setText(mwind.get(position));
            ((Item1ViewHolder)holder).icon.setImageResource(micon.get(position));
        }else if(holder instanceof Item2ViewHolder){
            ((Item2ViewHolder)holder).time.setText(mtime.get(position));
            ((Item2ViewHolder)holder).weather_text.setText(mtxt.get(position));
            ((Item2ViewHolder)holder).tmp.setText(mtmp.get(position));
            ((Item2ViewHolder)holder).wind.setText(mwind.get(position));
            ((Item2ViewHolder)holder).icon.setImageResource(micon.get(position));
            ((Item2ViewHolder)holder).city.setText(city.get(0));
            ((Item2ViewHolder)holder).ntmp.setText(ntmp.get(0));
        }
    }
    public int getItemViewType(int positon){
        if(positon==0){
            return ITEM_TYPE.ITEM2.ordinal();
        }else{
            return ITEM_TYPE.ITEM1.ordinal();
        }

    }
    public int getItemCount(){
        return mtime.size();
    }
    //future message
    public static class Item1ViewHolder extends RecyclerView.ViewHolder{
        TextView time,weather_text,tmp,wind;
        ImageView icon;
        public Item1ViewHolder(View itemView){
            super(itemView);
            time=(TextView)itemView.findViewById(R.id.time);
            weather_text=(TextView) itemView.findViewById(R.id.weather_text);
            tmp=(TextView) itemView.findViewById(R.id.tmp);
            wind=(TextView)itemView.findViewById(R.id.wind);
            icon=(ImageView)itemView.findViewById(R.id.icon);
        }
    }
    //today message
    public static class Item2ViewHolder extends RecyclerView.ViewHolder{
        TextView city,time,ntmp,weather_text,tmp,wind;
        ImageView icon;
        public Item2ViewHolder(View itemView){
            super(itemView);
            city=(TextView)itemView.findViewById(R.id.city);
            time=(TextView)itemView.findViewById(R.id.time);
            ntmp=(TextView)itemView.findViewById(R.id.ntmp);
            weather_text=(TextView)itemView.findViewById(R.id.weather_text);
            tmp=(TextView)itemView.findViewById(R.id.tmp);
            wind=(TextView)itemView.findViewById(R.id.wind);
            icon=(ImageView)itemView.findViewById(R.id.icon);
        }
    }
}
