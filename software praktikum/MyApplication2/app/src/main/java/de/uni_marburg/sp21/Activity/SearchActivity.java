//package de.uni_marburg.sp21.Activity;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.widget.Toast;
//
//import de.uni_marburg.sp21.Adapter.CompanyAdapter;
//import de.uni_marburg.sp21.Model.Company;
//import de.uni_marburg.sp21.Service.LoadDataFromFireStore;
//
//import com.example.sp21.R;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.firestore.DocumentSnapshot;
//
//import com.google.firebase.firestore.QuerySnapshot;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
//import static de.uni_marburg.sp21.Util.Constants.ADDRESS;
//import static de.uni_marburg.sp21.Util.Constants.ADRESSE;
//import static de.uni_marburg.sp21.Util.Constants.BESCHREIBUNG;
//import static de.uni_marburg.sp21.Util.Constants.BESITZER;
//import static de.uni_marburg.sp21.Util.Constants.DE;
//import static de.uni_marburg.sp21.Util.Constants.DESCRIPTION;
//import static de.uni_marburg.sp21.Util.Constants.EN;
//import static de.uni_marburg.sp21.Util.Constants.ID;
//import static de.uni_marburg.sp21.Util.Constants.LAN;
//import static de.uni_marburg.sp21.Util.Constants.NAME;
//import static de.uni_marburg.sp21.Util.Constants.OPENING_HOURS_COMMENTS;
//import static de.uni_marburg.sp21.Util.Constants.ORGANISATIONEN;
//import static de.uni_marburg.sp21.Util.Constants.ORGANIZATIONS;
//import static de.uni_marburg.sp21.Util.Constants.OWNER;
//import static de.uni_marburg.sp21.Util.Constants.PRODUCKTGRUPPE;
//import static de.uni_marburg.sp21.Util.Constants.PRODUCTS_DESCRIPTION;
//import static de.uni_marburg.sp21.Util.Constants.PRODUCT_GROUPS;
//import static de.uni_marburg.sp21.Util.Constants.PRODUKTEBESCHREIBUNG;
//import static de.uni_marburg.sp21.Util.Constants.SEARCHCATOGORY;
//import static de.uni_marburg.sp21.Util.Constants.SEARCHEDTEXT;
//import static de.uni_marburg.sp21.Util.Constants.SEARCH_RECHECK;
//import static de.uni_marburg.sp21.Util.Constants.TYPEN;
//import static de.uni_marburg.sp21.Util.Constants.TYPES;
//import static de.uni_marburg.sp21.Util.Constants.collectionReference;
//import static de.uni_marburg.sp21.Util.Constants.CREDENTIAL_SHARED_PREF;
//import static de.uni_marburg.sp21.Util.Constants.ÖFFNUNGSZEITENKOMMENTARE;
//
///**
// * @author  Oumar Sadio
// * This Class is responsibale for retriving the Data form firebase and the Names of the companies
// */
//
//public class SearchActivity extends AppCompatActivity {
//    List<Company> dataList = LoadDataFromFireStore.getInstance().getCompanies();
//    RecyclerView searchview;
//    CompanyAdapter adapter;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_search);
//        SharedPreferences data = getSharedPreferences(CREDENTIAL_SHARED_PREF, Context.MODE_PRIVATE);
//        String searched = data.getString(SEARCHEDTEXT, null);
//        String searchCategory = data.getString(SEARCHCATOGORY, null);
//        searchview = findViewById(R.id.searchCompanyView);
//
//        initRecyclerView();
////        getItemPosition();
//        getFoundData(searchCategory, searched);
//    }
//
//    /**
//     * This methode for setting the RecycleView with Adapter
//     */
//    private void initRecyclerView() {
//        searchview.setLayoutManager(new LinearLayoutManager(this));
//        adapter = new CompanyAdapter(dataList);
//        searchview.setAdapter(adapter);
//    }
//
//    /**
//     * This methode is responsible for showing the details of a company when we click on it and saving the id for further use in the detailsActivity
//     */
//    private void getItemPosition() {
//        adapter.setOnItemClickListener(new CompanyAdapter.OnItemClickListener() {
//            @Override
//            public void onIemClick(int position) {
//                String id = dataList.get(position).getCompanies();
//                Intent intent = new Intent(SearchActivity.this, detailsActivity.class);
//                intent.putExtra(ID, id);
//                startActivity(intent);
//            }
//        });
//    }
//
//    /**
//     * this method is responsible for transforming the given Category in lowercase and eliminates the space in the Category
//     * @param srCat Category
//     * @return Category without space and with only lower Letters
//     */
//    private String lower(String srCat) {
//        String lower = "";
//        if (srCat.contains(" ")) {
//            char[] c = srCat.toCharArray();
//            c[0] = Character.toLowerCase(c[0]);
//            String x = String.valueOf(c);
//            String[] newSrCat = x.split(" ");
//            lower = newSrCat[0] + newSrCat[1];
//        } else {
//            lower = srCat.toLowerCase();
//        }
//        return lower;
//    }
//
//    /**
//     * this method is responsible for transforming the given searched Text in lowercase and eliminates the space in the searched Text
//     * @param srText searched Text
//     * @return   searched Text only lower Letters
//     */
//    private String lowerSearchText(String srText) {
//        String lower = "";
//        lower=srText.toLowerCase();
//        return lower;
//    }
//    /**
//     * it is responsible for loading the corresponding data when the user enters something to search
//     *
//     * @param srCat  chosen Category
//     * @param srText inputed Text to search
//     */
//
//
//    public void getFoundData(String srCat, String srText) {
//        SharedPreferences sharedPreferences = getSharedPreferences("", Context.MODE_PRIVATE);
//        String lang = sharedPreferences.getString(LAN, null);
//        final String newSrText=lowerSearchText(srText);
//        final String newStrCat = lower(srCat);
//        switch (lang) {
//            case EN:
//                switch (newStrCat) {
//                    case OPENING_HOURS_COMMENTS:
//                    case NAME:
//                    case OWNER:
//                    case PRODUCTS_DESCRIPTION:
//                    case DESCRIPTION:
//                    case ADDRESS:
//                    case TYPES:
//
//
//                        for (int i =0;i<dataList.get(pos))
//                                        adapter.notifyDataSetChanged();
//                                    }
//
//                                });
//                        break;
////                    case MESSAGES:
////                        collectionReference
////                                .get()
////                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
////                                    @Override
////                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
////                                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
////                                        for (DocumentSnapshot d : list) {
////                                            Company company = d.toObject(Company.class);
////                                            String fs = String.valueOf(company.getContentDate()).toLowerCase();
////                                            if (fs.contains(newSrText)) {
////                                                String companiesId = d.getId();
////                                                company.setCompanies(companiesId);
////                                                dataList.add(company);
////
////                                            }
////                                        }
////                                            if (dataList.size() == 0) {
////                                                Toast.makeText(SearchActivity.this, SEARCH_RECHECK, Toast.LENGTH_SHORT).show();
////                                            }
////                                            adapter.notifyDataSetChanged();
////                                        }
////
////                                });
////                        break;
//                    case PRODUCT_GROUPS:
//                        collectionReference
//                                .get()
//                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
//                                        for (DocumentSnapshot d : list) {
//                                            Company company = d.toObject(Company.class);
//                                            String fs = String.valueOf(company.getStringProductGroups()).toLowerCase();
//                                            if (fs.contains(newSrText)) {
//                                                String companiesId = d.getId();
//                                                company.setCompanies(companiesId);
//                                                dataList.add(company);
//
//                                            }
//                                        }
//                                            if(dataList.size()==0){
//                                                Toast.makeText(SearchActivity.this,SEARCH_RECHECK,Toast.LENGTH_SHORT).show();
//                                            }
//                                        adapter.notifyDataSetChanged();
//                                    }
//
//
//                                });
//                        break;
//                    case ORGANIZATIONS:
//                        collectionReference
//                                .get()
//                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
//                                        for (DocumentSnapshot d : list) {
//                                            Company company = d.toObject(Company.class);
//                                            String fs = String.valueOf(company.getOrganizationsUrl()).toLowerCase();
//                                            String fss = String.valueOf(company.getOrganizationsName()).toLowerCase();
//                                            if (fs.contains(newSrText)) {
//                                                String companiesId = d.getId();
//                                                company.setCompanies(companiesId);
//                                                dataList.add(company);
//                                            } else if (fss.contains(newSrText)) {
//                                                String companiesId = d.getId();
//                                                company.setCompanies(companiesId);
//                                                dataList.add(company);
//
//                                        }
//                                        } if(dataList.size()==0){
//                                            Toast.makeText(SearchActivity.this,SEARCH_RECHECK,Toast.LENGTH_SHORT).show();
//                                        }
//                                        adapter.notifyDataSetChanged();
//                                    }
//
//                                });
//                        break;
//                }
//
//
//            case DE:
//                switch (newStrCat) {
//                    case ÖFFNUNGSZEITENKOMMENTARE:
//
//                        collectionReference
//                                .get()
//                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
//                                        for (DocumentSnapshot d : list) {
//                                            Company company = d.toObject(Company.class);
//                                            String fs = String.valueOf(company.getOpeningHoursComments()).toLowerCase();
//
//                                            if (fs.contains(newSrText)) {
//                                                String companiesId = d.getId();
//                                                company.setCompanies(companiesId);
//                                                dataList.add(company);
//
//                                        }
//                                        } if(dataList.size()==0){
//                                            Toast.makeText(SearchActivity.this,SEARCH_RECHECK,Toast.LENGTH_SHORT).show();
//                                        }
//                                        adapter.notifyDataSetChanged();
//                                    }
//
//                                });
//                        break;
//                    case NAME:
//                        collectionReference
//                            .get()
//                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                @Override
//                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
//                                    for (DocumentSnapshot d : list) {
//                                        Company company = d.toObject(Company.class);
//                                        String fs = String.valueOf(company.getName()).toLowerCase();
//
//                                        if (fs.contains(newSrText)) {
//                                            String companiesId = d.getId();
//                                            company.setCompanies(companiesId);
//                                            dataList.add(company);
//                                        }
//                                    }
//                                    if (dataList.size() == 0) {
//                                        Toast.makeText(SearchActivity.this, SEARCH_RECHECK, Toast.LENGTH_SHORT).show();
//                                    }
//
//                                    adapter.notifyDataSetChanged();
//                                }
//
//                            });
//                        break;
//
//                    case BESITZER:
//                        collectionReference
//                                .get()
//                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots){
//                                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
//                                        for (DocumentSnapshot d : list) {
//                                            Company company = d.toObject(Company.class);
//                                            String fs = String.valueOf(company.getOwner()).toLowerCase();
//
//                                            if (fs.contains(newSrText)) {
//                                                String companiesId = d.getId();
//                                                company.setCompanies(companiesId);
//                                                dataList.add(company);
//                                            }
//                                        }
//                                        if (dataList.size() == 0) {
//                                            Toast.makeText(SearchActivity.this, SEARCH_RECHECK, Toast.LENGTH_SHORT).show();
//                                        }
//                                        adapter.notifyDataSetChanged();
//
//                                    }
//
//                                });
//                        break;
//                    case PRODUKTEBESCHREIBUNG:
//                        collectionReference
//                                .get()
//                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
//                                        for (DocumentSnapshot d : list) {
//                                            Company company = d.toObject(Company.class);
//                                            String fs = String.valueOf(company.getProductsDescription()).toLowerCase();
//
//                                            if (fs.contains(newSrText)) {
//                                                String companiesId = d.getId();
//                                                company.setCompanies(companiesId);
//                                            }
//                                        } if(dataList.size()==0){
//                                            Toast.makeText(SearchActivity.this,SEARCH_RECHECK,Toast.LENGTH_SHORT).show();
//                                        }
//
//                                        adapter.notifyDataSetChanged();
//                                    }
//
//                                });
//                        break;
//                    case BESCHREIBUNG:
//                        collectionReference
//                                .get()
//                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
//                                        for (DocumentSnapshot d : list) {
//                                            Company company = d.toObject(Company.class);
//                                            String fs = String.valueOf(company.getDescription()).toLowerCase();
//
//                                            if (fs.contains(newSrText)) {
//                                                String companiesId = d.getId();
//                                                company.setCompanies(companiesId);
//                                                dataList.add(company);
//
//                                        }
//                                        } if(dataList.size()==0){
//                                            Toast.makeText(SearchActivity.this,SEARCH_RECHECK,Toast.LENGTH_SHORT).show();
//                                        }
//                                        adapter.notifyDataSetChanged();
//                                    }
//
//                                });
//                        break;
//                    case ADRESSE:
//                        collectionReference
//                                .get()
//                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
//                                        for (DocumentSnapshot d : list) {
//                                            Company company = d.toObject(Company.class);
//                                            String fs = String.valueOf(company.getAddress()).toLowerCase();
//                                            if (fs.contains(newSrText)) {
//                                                String companiesId = d.getId();
//                                                company.setCompanies(companiesId);
//                                                dataList.add(company);
//
//                                            }
//                                        } if(dataList.size()==0){
//                                            Toast.makeText(SearchActivity.this,SEARCH_RECHECK,Toast.LENGTH_SHORT).show();
//                                        }
//                                        adapter.notifyDataSetChanged();
//                                    }
//
//                                });
//                        break;
//                    case TYPEN:
//                        collectionReference
//                                .get()
//                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
//                                        for (DocumentSnapshot d : list) {
//                                            Company company = d.toObject(Company.class);
//                                            String fs = String.valueOf(company.getTypes()).toLowerCase();
//                                            if (fs.contains(newSrText)) {
//                                                String companiesId = d.getId();
//                                                company.setCompanies(companiesId);
//                                                dataList.add(company);
//
//                                        }
//                                        } if(dataList.size()==0){
//                                            Toast.makeText(SearchActivity.this,SEARCH_RECHECK,Toast.LENGTH_SHORT).show();
//                                        }
//                                        adapter.notifyDataSetChanged();
//                                    }
//
//                                });
//                        break;
//
////                    case NACHRICHTEN:
////                        collectionReference
////                                .get()
////                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
////                                    @Override
////                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
////                                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
////                                        for (DocumentSnapshot d : list) {
////                                            Company company = d.toObject(Company.class);
////                                            String fs = String.valueOf(company.getContentDate()).toLowerCase();
////                                            if (fs.contains(newSrText)) {
////                                                String companiesId = d.getId();
////                                                company.setCompanies(companiesId);
////                                                dataList.add(company);
////                                        }
////                                        } if(dataList.size()==0){
////                                            Toast.makeText(SearchActivity.this,SEARCH_RECHECK,Toast.LENGTH_SHORT).show();
////                                        }
////                                        adapter.notifyDataSetChanged();
////                                    }
////
////                                });
////                        break;
//
//                    case PRODUCKTGRUPPE:
//                        collectionReference
//                                .get()
//                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
//                                        for (DocumentSnapshot d : list) {
//                                            Company company = d.toObject(Company.class);
//                                            String fs = String.valueOf(company.getStringProductGroups()).toLowerCase();
//                                            if (fs.contains(newSrText)) {
//                                                String companiesId = d.getId();
//                                                company.setCompanies(companiesId);
//                                                dataList.add(company);
//
//                                        }
//                                        } if(dataList.size()==0){
//                                            Toast.makeText(SearchActivity.this,SEARCH_RECHECK,Toast.LENGTH_SHORT).show();
//                                        }
//                                        adapter.notifyDataSetChanged();
//                                    }
//
//                                });
//                        break;
//                    case ORGANISATIONEN:
//                        collectionReference
//                                .get()
//                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
//                                        for (DocumentSnapshot d : list) {
//                                            Company company = d.toObject(Company.class);
//                                            String fs = String.valueOf(company.getOrganizationsUrl()).toLowerCase();
//                                            String fss = String.valueOf(company.getOrganizationsName()).toLowerCase();
//                                            if (fs.contains(newSrText)) {
//                                                String companiesId = d.getId();
//                                                company.setCompanies(companiesId);
//
//                                                dataList.add(company);
//                                            } else if (fss.contains(newSrText)) {
//                                                String companiesId = d.getId();
//                                                company.setCompanies(companiesId);
//                                                dataList.add(company);
//
//                                        }
//                                        } if(dataList.size()==0){
//                                            Toast.makeText(SearchActivity.this,SEARCH_RECHECK,Toast.LENGTH_SHORT).show();
//                                        }
//                                        adapter.notifyDataSetChanged();
//                                    }
//
//                                });
//                        break;
//                }
//        }
//    }
//}
