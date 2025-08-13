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
import de.uni_marburg.sp21.Model.Organizations;


public class OrganizationsAdapter extends RecyclerView.Adapter <OrganizationsAdapter.ViewHolder> {
        List<Organizations> organizations;
        detailsActivity detailsActivity;

        public OrganizationsAdapter(List<Organizations> organizations, detailsActivity detailsActivity) {
            this.organizations = organizations;
            this.detailsActivity = detailsActivity;
        }

        @NonNull
        @Override
        public de.uni_marburg.sp21.Adapter.OrganizationsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.org, parent, false);
            return new OrganizationsAdapter.ViewHolder(view);
        }



        @Override
        public void onBindViewHolder(@NonNull de.uni_marburg.sp21.Adapter.OrganizationsAdapter.ViewHolder holder, int position) {
            List<String> name=new ArrayList<>();
            List<String> url=new ArrayList<>();
            if (organizations.get(position).getName()!=null) {
                name.add(organizations.get(position).getName());
            }else {
                name.add("");
            }
            if (organizations.get(position).getUrl()!=null) {
                url.add(organizations.get(position).getUrl());
            }else {
                url.add("");
            }
            holder.name.setAdapter(new ArrayAdapter<>(detailsActivity, R.layout.org_item, R.id.itemName, name));
            holder.url.setAdapter(new ArrayAdapter<>(detailsActivity, R.layout.org_item, R.id.itemUrl, url));

        }


        @Override
        public int getItemCount() {
            return organizations.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ListView name;
            TextView nameText,urlText;
            ListView url;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                nameText = itemView.findViewById(R.id.nameText);
                urlText = itemView.findViewById(R.id.urlText);
                url  = itemView.findViewById(R.id.url);
            }

        }
    }

