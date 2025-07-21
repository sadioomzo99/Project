package de.uni_marburg.sp21.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.sp21.R;

import java.util.List;

import de.uni_marburg.sp21.Activity.filterActivity;
import de.uni_marburg.sp21.Model.Company;

import static de.uni_marburg.sp21.Util.Constants.isFav;
import static de.uni_marburg.sp21.Util.Constants.isFavList;
import static de.uni_marburg.sp21.Util.Constants.posFav;
//import static de.uni_marburg.sp21.Util.Constants.isFav;
//import static de.uni_marburg.sp21.Util.Constants.isNotFav;

public class CompanyAdapter extends RecyclerView.Adapter<CompanyAdapter.myViewholder>{
    private  List<Company> dataList;
    private OnItemClickListener mListener;
    Context context;

    public interface OnItemClickListener{
        void onIemClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        mListener=listener;
    }

    public CompanyAdapter(List<Company> dataList,Context context) {
        this.dataList = dataList;
        this.context=context;

    }


    @NonNull
    @Override
    public myViewholder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_company,parent,false);
        return new myViewholder(view,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull  CompanyAdapter.myViewholder holder, int position) {
        holder.company.setText(dataList.get(position).getName());
        holder.fav.setChecked(dataList.get(position).isFavorite());
        holder.fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterActivity filterActivity=new filterActivity();

                if(holder.fav.isChecked()) {
                    isFavList.add(dataList.get(position).getId());
                    filterActivity.addFavorite(dataList.get(position));

                } else {
                    isFavList.add(dataList.get(position).getId());
                    filterActivity.removeFavorite(dataList.get(position));


                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class myViewholder extends RecyclerView.ViewHolder {
        TextView company;
        ToggleButton fav;
        public myViewholder(View itemView, OnItemClickListener listener){
            super(itemView);
            company=itemView.findViewById(R.id.Company_card);
            fav = itemView.findViewById(R.id.favorite);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener !=null){
                        int position=getAdapterPosition();
                        if(position!=RecyclerView.NO_POSITION){
                            listener.onIemClick(position);
                        }
                    }
                }
            });
        }
    }
}

