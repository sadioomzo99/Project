package de.uni_marburg.sp21.Activity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import de.uni_marburg.sp21.Adapter.CompanyAdapter;
import de.uni_marburg.sp21.Model.Categories;
import de.uni_marburg.sp21.Model.Company;
import de.uni_marburg.sp21.Service.LoadDataFromFireStore;


import com.example.sp21.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static de.uni_marburg.sp21.Activity.filterActivity.clicked;
import static de.uni_marburg.sp21.Activity.filterActivity.filteredCompanies;
import static de.uni_marburg.sp21.Activity.filterActivity.rested;
import static de.uni_marburg.sp21.Util.Constants.CHECK;
import static de.uni_marburg.sp21.Util.Constants.CREDENTIAL_SHARED_PREF;
import static de.uni_marburg.sp21.Util.Constants.DE;
import static de.uni_marburg.sp21.Util.Constants.EN;

import static de.uni_marburg.sp21.Util.Constants.ENTERTEXT;
import static de.uni_marburg.sp21.Util.Constants.ID;
import static de.uni_marburg.sp21.Util.Constants.LAN;
import static de.uni_marburg.sp21.Util.Constants.POS;


/**
 * @author  Abdulmallek Ali and Oumar Sadio
 * This Class is responsibale for retriving the Data form firebase and the Names of the companies
 */

public class CompaniesActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    RecyclerView recview;
    CompanyAdapter adapter;
    TextView idTextView,textView;
    ImageButton searchButton,filter;
    EditText searchText;
    Button button;
    Spinner spinner, spinner2;
   static String mCurrentLocale;
    int pos;
    private   List<Company> input =new ArrayList<>();
    private static Resources res;
    boolean checked;
    String searchedText,searchCategory;
    boolean searchClicked=false;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoadDataFromFireStore i = LoadDataFromFireStore.getInstance();
        i.getData(this);
        setContentView(R.layout.rec_companyies_view);
        Intent intent=getIntent();
        res = getResources();
        idTextView = findViewById(R.id.id);
        textView=findViewById(R.id.textView);
        checked=intent.getBooleanExtra("true",false);
        searchText = findViewById(R.id.search_text);
        searchButton = findViewById(R.id.Search);
        filter=findViewById(R.id.Filter);
        spinner = findViewById(R.id.spinner1);
        spinner2 = findViewById(R.id.spinner2);
        spinnerLangeSet();
        spinnerSet();

            filter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CompaniesActivity.this, filterActivity.class);
                    input.clear();
                    startActivity(intent);

                    }
            });


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 searchedText = searchText.getText().toString();
                if (searchedText.isEmpty()) {
                    searchText.setError(ENTERTEXT);
                    searchText.requestFocus();
                } else {
                    searchClicked=true;
                    initRecyclerView();
                }
            }
        });
    }
    public static Resources getRes() {
        return res;
    }


    /**
     * This methode is for setting the spinner
     */
    private void spinnerSet() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.categories,
                android.R.layout.simple_list_item_checked);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    /**
     * This methode is for setting the spinner
     */
    public void spinnerLangeSet() {
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.lang,
                android.R.layout.simple_list_item_checked);
        spinner2.setAdapter(adapter1);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sharedPreferences = getSharedPreferences("", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                boolean isRefreshed= sharedPreferences.getBoolean(CHECK,false);
                String selectedLange="";
                if(isRefreshed) {
                    editor.remove(CHECK).apply();
                    if (pos != position) {
                        spinner2.setSelection(pos);
                        selectedLange = parent.getItemAtPosition(pos).toString();
                    }

                }else {
                    spinner2.setSelection(position);
                    selectedLange = parent.getItemAtPosition(position).toString();
                }

                String newSelectedLange = sharedPreferences.getString(LAN, null);
                switch (selectedLange) {
                    case EN:
                        setLocale(EN.toLowerCase());
                        editor.putString(LAN, selectedLange);
                        editor.putInt(POS, 0);
                        editor.apply();
                        if (!selectedLange.equals(newSelectedLange)) {
                            onRefresh();
                        }
                        break;

                    case DE:
                        setLocale(DE.toLowerCase());
                        editor.putString(LAN, selectedLange);
                        editor.putInt(POS, 1);
                        editor.apply();
                        if (!selectedLange.equals(newSelectedLange)) {
                            onRefresh();
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    /**
     * @param lang is a language from type String, it can be "en" or "de"
     * change the language of the view to the user's preferred language
     */
    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    /**
     * @return getting the selected language from SharedPreference
     */
    public String getLocale() {
        SharedPreferences sharedPreferences = getSharedPreferences("", Context.MODE_PRIVATE);
        return sharedPreferences.getString(LAN, null);
    }
    /**
     *  getting the pos of selected language from SharedPreference
     */

    private int getLocalePos() {
        SharedPreferences sharedPreferences = getSharedPreferences("", Context.MODE_PRIVATE);

        return sharedPreferences.getInt(POS, -1);
    }


    /**
     * This methode for setting the RecycleView with Adapter
     */
    public void initRecyclerView() {
        recview = findViewById(R.id.recCompanyView);
        recview.setLayoutManager(new LinearLayoutManager(this));
        if (input.isEmpty()) {
            input = LoadDataFromFireStore.getInstance().getCompanies();
        }
        filterActivity filterA=new filterActivity();

        if(clicked|| searchClicked) {
            if (clicked) {
                if (!(filterA.getFilter(input)).isEmpty()) {
                    adapter = new CompanyAdapter(filterA.getFilter(input),this);

                } else {
                    adapter = new CompanyAdapter(filterA.getFilter(input),this);
                    Toast.makeText(this, getString(R.string.NothingFound), Toast.LENGTH_LONG).show();
                }

            } else {

                adapter = new CompanyAdapter(filterA.getFoundData(searchedText, searchCategory, mCurrentLocale),this);
            }
            }else {

            adapter=new CompanyAdapter(input,this);
        }
        recview.setAdapter(adapter);
        getItemPosition();
    }

    /**
     * This methode is responsible for showing the details of a company when we click on it and saving the id for further use in the detailsActivity
     */
    private void getItemPosition() {
        adapter.setOnItemClickListener(new CompanyAdapter.OnItemClickListener() {
            @Override
            public void onIemClick(int position) {
                LoadDataFromFireStore loadData = LoadDataFromFireStore.getInstance();
                String id = loadData.getCompanies().get(position).getCompanies();
                Intent intent = new Intent(CompaniesActivity.this, detailsActivity.class);
                intent.putExtra(ID, id);
                intent.putExtra(LAN,mCurrentLocale);
                intent.putExtra(POS, position);
                intent.putExtra("check",checked);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCurrentLocale = getLocale();
        pos=getLocalePos();

    }
    /**
     * @param parent
     * @param view
     * @param position  its the  position in data list of the Company
     * @param id   its the id of the Company
     */

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        searchCategory = parent.getItemAtPosition(position).toString();
//        String s = getLocale();
//        switch (s) {
//            case EN:
//                textView.setText("Selected Category: " + searchCategory);
//                break;
//            case DE:
//                textView.setText("ausgewählte kategorien: " + searchCategory);
//                break;
//        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }




    private void onRefresh() {
        SharedPreferences sharedPreferences = getSharedPreferences("", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Intent refresh = new Intent(this, CompaniesActivity.class);
        String locale = getLocale();
        int newPos =getLocalePos();

        if (!locale.equals(mCurrentLocale)) {
            mCurrentLocale = locale;
            pos=newPos;
            editor.putBoolean(CHECK,true);
            editor.apply();
            startActivity(refresh);

        }

        }
    }





