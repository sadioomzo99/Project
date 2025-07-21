package de.uni_marburg.sp21.Activity;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.denzcoskun.imageslider.ImageSlider;

import de.uni_marburg.sp21.Adapter.OrganizationsAdapter;
import de.uni_marburg.sp21.Model.Company;

import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.sp21.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
//import de.uni_marburg.sp21.Adapter.detailsAdapter;
import de.uni_marburg.sp21.Model.Day;
import de.uni_marburg.sp21.Adapter.ProductGroupAdapter;
import de.uni_marburg.sp21.Service.LoadDataFromFireStore;
//import de.uni_marburg.sp21.LoadData;


import java.util.ArrayList;
import java.util.List;

import static de.uni_marburg.sp21.Activity.filterActivity.checkedCat;
import static de.uni_marburg.sp21.Activity.filterActivity.checkedDelivery;
import static de.uni_marburg.sp21.Activity.filterActivity.checkedOpen;
import static de.uni_marburg.sp21.Activity.filterActivity.checkedOrg;
import static de.uni_marburg.sp21.Activity.filterActivity.checkedTypes;
import static de.uni_marburg.sp21.Activity.filterActivity.chosenTime;
import static de.uni_marburg.sp21.Activity.filterActivity.favoriteChecked;
import static de.uni_marburg.sp21.Activity.filterActivity.filteredCompanies;
import static de.uni_marburg.sp21.Activity.filterActivity.isFavoriteCheck;
import static de.uni_marburg.sp21.Util.Constants.LAN;
import static de.uni_marburg.sp21.Util.Constants.LAT;
import static de.uni_marburg.sp21.Util.Constants.LON;
import static de.uni_marburg.sp21.Util.Constants.NAME;
import static de.uni_marburg.sp21.Util.Constants.POS;

/**
 * @author Abdulmallek Ali
 * this class is used to show the details for each Company
 */
public class  detailsActivity extends AppCompatActivity {
    ImageButton btn;
    TextView owner, street, zip, city, name, deliverService;
    TextView url, openingHoursComments, geoHash, productsDescription, organizationsName, organizationsUrl;
    TextView mail, description, types, category, raw, productTags, seasons, openingHours;
    ImageSlider slider;
    RecyclerView recyclerView,recyclerView2;

    List<Company> dataList = LoadDataFromFireStore.getInstance().getCompanies();
    List<SlideModel> slideModels = new ArrayList<>();
    StorageReference storageReference;
    FirebaseStorage database;
    StorageReference s;
    String lan;
    int pos;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cardviewdetails);
        recyclerView = findViewById(R.id.recProd);
        recyclerView2=findViewById(R.id.recOrg);

        Intent prevIntent = getIntent();
        pos = prevIntent.getIntExtra(POS, -1);
        lan=prevIntent.getStringExtra(LAN);
        btn = findViewById(R.id.btn2);

        slider = findViewById(R.id.slideimage);
        name = findViewById(R.id.Name);
        owner = findViewById(R.id.owner);
        //Address
        street = findViewById(R.id.street);
        zip = findViewById(R.id.zip);
        city = findViewById(R.id.city);

        //DeliveryService
        deliverService = findViewById(R.id.deliveryService);

        //description
        description = findViewById(R.id.description);
        //mail
        mail = findViewById(R.id.mail);
        //url
        url = findViewById(R.id.url);
        //productsDescription
        productsDescription = findViewById(R.id.productDescription);
        //geoHash

        // openingHoursComments
        openingHoursComments = findViewById(R.id.openingHoursComments);
        //organizations
//        organizationsName = findViewById(R.id.OrganizationsName);
//        organizationsUrl = findViewById(R.id.OrganizationsUrl);
        //types
        types = findViewById(R.id.types);

        //Image
        slider = findViewById(R.id.slideimage);

        //OpeningsHours
        openingHours = findViewById(R.id.openingHours);
        loadData();
        getAdapter();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(detailsActivity.this, MapsActivity.class);
                intent.putExtra(LAT, getDataList().get(pos).getLocation().getLat());
                intent.putExtra(LON, getDataList().get(pos).getLocation().getLon());
                intent.putExtra(NAME, getDataList().get(pos).getName());
                startActivity(intent);
            }
        });

    }
    private List<Company> getDataList(){
        filterActivity filterActivity=new filterActivity();

        List<Company> newCompany=new ArrayList<>();
        if(checkedDelivery||checkedCat||checkedTypes||checkedOpen||checkedOrg||chosenTime||favoriteChecked) {
           newCompany=filteredCompanies;
        }else {
                newCompany= dataList;
            }

        return newCompany;
    }



//    /**
//     * This methode for setting the RecycleView with Adapter
//     */


    /**
     * load the data from CompanyActivity
     */
    private void loadData() {
        //name
        name.setText(String.format("%s: %s", "Name", getDataList().get(pos).getName()));

        //address
        String City = city.getContext().getString(R.string.City);
        City += ": " + getDataList().get(pos).getAddress().getCity();
        city.setText(City);

        String Zip = zip.getContext().getString(R.string.Zip);
        Zip += ": " + getDataList().get(pos).getAddress().getZip();
        zip.setText(Zip);

        String Street = street.getContext().getString(R.string.Street);
        Street += ": " + getDataList().get(pos).getAddress().getStreet();
        street.setText(Street);

        //DeliveryService
        String Delivery = deliverService.getContext().getString(R.string.DeliveryService);
        Delivery += ": " + getDataList().get(pos).getDeliveryServices();
        deliverService.setText(Delivery);

        //Owner
        String Owner = owner.getContext().getString(R.string.Owner);
        Owner += ": " + getDataList().get(pos).getOwner();
        owner.setText(Owner);

        //description
        String Description = description.getContext().getString(R.string.Description);
        Description += ": " + getDataList().get(pos).getDescription();
        description.setText(Description);

        //mail
        String Mail = mail.getContext().getString(R.string.Mail);
        Mail += ": " + getDataList().get(pos).getMail();
        mail.setText(Mail);

        //url
        String Url = url.getContext().getString(R.string.Url);
        Url += ": " + getDataList().get(pos).getUrl();
        url.setText(Url);

        //productsDescription
        String ProductsDescription = productsDescription.getContext().getString(R.string.ProductDescription);
        ProductsDescription += ": " + getDataList().get(pos).getProductsDescription();
        productsDescription.setText(ProductsDescription);

        //geoHash


        //openingHoursComments
        String OpeningHoursComments = openingHoursComments.getContext().getString(R.string.OpeningHoursComments);
        OpeningHoursComments += ": " + getDataList().get(pos).getOpeningHoursComments();
        openingHoursComments.setText(OpeningHoursComments);

        //organizations
//        String OrganizationsName=organizationsName.getContext().getString(R.string.Name);
//        for (int j = 0; j < getDataList().get(pos).getOrganizations().size(); j++) {
//            OrganizationsName += ": " + getDataList().get(pos).getOrganizations().get(j).getName().replace("[", "").replace("]", "")+"|";
//            organizationsName.setText(OrganizationsName);
//
//            String OrganizationsUrl = organizationsUrl.getContext().getString(R.string.Url);
//            OrganizationsUrl += ": " + getDataList().get(pos).getOrganizations().get(j).getUrl().replace("[", "").replace("]", "")+"|";
//            organizationsUrl.setText(OrganizationsUrl);
//        }
        //types
        String Types = types.getContext().getString(R.string.Types);
        Types += ": " + getDataList().get(pos).getTypesToString().toString().replace("[", "").replace("]", "");
        types.setText(Types);

        //messages

        //images
        String path = String.valueOf(getDataList().get(pos).getImages()).replace("[", "").replace("]", "").replace(" ", "");
        database = FirebaseStorage.getInstance();
        storageReference = database.getReference();
        String[] part = path.split(",");
        String data = "";
        if (!path.isEmpty()) {
            for (int i = 0; i < getDataList().get(pos).getImages().size(); i++) {
                data = part[i];
                s = storageReference.child(data);
                s.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        slideModels.add(new SlideModel(uri.toString(), ScaleTypes.FIT));
                        slider.setImageList(slideModels, ScaleTypes.FIT);
                    }
                });
            }
        }

        //OpeningHours

        openingHours.setText(getOpeningHours().toString().replace("[", "").replace("]", "").replace(",", "\n") + "\n");


    }

    private List<String> getOpeningHours() {
        List<String> newDay = new ArrayList<>();
        List<String> day = new ArrayList<>();
        String closed = getString(R.string.Closed);
            for (int i = 0; i < getDataList().get(pos).getOpeningHours().size(); i++) {
                String x=getDataList().get(pos).getOpeningHours().get(i).getDay();

                if (!day.contains(x)){
                    day.add(getDataList().get(pos).getOpeningHours().get(i).getDay());
                    if (getDataList().get(pos).getOpeningHours().get(i).isOpen()) {
                        newDay.add(getDataList().get(pos).getOpeningHours().get(i).getDay()+ ": " + getDataList().get(pos).getOpeningHours().get(i).getTimeIntervals().getTime());
                    } else {
                         newDay.add(getDataList().get(pos).getOpeningHours().get(i).getDay()+ ": " + closed);
                    }
                } else {
                    if (getDataList().get(pos).getOpeningHours().get(i).isOpen()) {
                        newDay.add(getDataList().get(pos).getOpeningHours().get(i).getTimeIntervals().getTime());
                    } else {
                        newDay.add(getDataList().get(pos).getOpeningHours().get(i).getDay()+ ": " + closed);
                    }

                }
            }

        return newDay;
    }

    private void getAdapter(){
        LinearLayoutManager manager = new LinearLayoutManager(this);
        LinearLayoutManager manager1 = new LinearLayoutManager(this);

        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView2.addItemDecoration(new DividerItemDecoration(recyclerView2.getContext(), DividerItemDecoration.VERTICAL));
        OrganizationsAdapter orgAdapter=new OrganizationsAdapter(dataList.get(pos).getOrganizations(),this);
        ProductGroupAdapter adapter = new ProductGroupAdapter(dataList.get(pos).getProductGroups(), this);
        recyclerView2.setAdapter(orgAdapter);
        recyclerView2.setLayoutManager(manager1);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);
    }
}













