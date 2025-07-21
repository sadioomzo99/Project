package de.uni_marburg.sp21.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sp21.R;

import java.util.ArrayList;
import java.util.List;

import de.uni_marburg.sp21.Activity.detailsActivity;
import de.uni_marburg.sp21.Model.ProductGroups;

public class ProductGroupAdapter extends RecyclerView.Adapter <ProductGroupAdapter.ViewHolder> {
    List<ProductGroups> productGroups;
    detailsActivity detailsActivity;

    public ProductGroupAdapter(List<ProductGroups> productGroups, detailsActivity detailsActivity) {
        this.productGroups = productGroups;
        this.detailsActivity = detailsActivity;
    }

    @NonNull
    @Override
    public ProductGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product, parent, false);
        return new ProductGroupAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductGroupAdapter.ViewHolder holder, int position) {
        if (productGroups.get(position).isRawProduct()) {
            holder.rawProduct.setText(R.string.raw);
        } else {
            holder.rawProduct.setText(R.string.notRaw);
        }

        List<String> cat = new ArrayList<>();
        cat.add(productGroups.get(position).getCategoryToString());
        List<String> Tag = new ArrayList<>(productGroups.get(position).getProductTags());
        List<String> season = new ArrayList<>(productGroups.get(position).getSeasons());
        holder.seasons.setAdapter(new ArrayAdapter<>(detailsActivity, R.layout.enum_item, R.id.itemSeason, season));
        holder.producerTags.setAdapter(new ArrayAdapter<>(detailsActivity, R.layout.enum_item, R.id.itemProducerTags, Tag));
        holder.productCategory.setAdapter(new ArrayAdapter<>(detailsActivity, R.layout.enum_item, R.id.itemProductCategory, cat));
    }


    @Override
    public int getItemCount() {
        return productGroups.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
       ListView productCategory;
        TextView rawProduct;
        TextView producerID;
        ListView producerTags;
        ListView seasons;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productCategory = itemView.findViewById(R.id.name);
            rawProduct = itemView.findViewById(R.id.Raw1);
            producerTags = itemView.findViewById(R.id.url);
            seasons = itemView.findViewById(R.id.season1);
        }

    }
}
