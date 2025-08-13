package de.uni_marburg.sp21.Activity;
/**
 * this class is reponsible of the filter function
 */

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.example.sp21.R;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.uni_marburg.sp21.Model.Address;
import de.uni_marburg.sp21.Model.Categories;
import de.uni_marburg.sp21.Model.Company;
import de.uni_marburg.sp21.Model.Messages;
import de.uni_marburg.sp21.Model.OpeningHours;
import de.uni_marburg.sp21.Model.Organizations;
import de.uni_marburg.sp21.Model.ProductGroups;
import de.uni_marburg.sp21.Util.Constants;

import static de.uni_marburg.sp21.Util.Constants.ADDRESS;
import static de.uni_marburg.sp21.Util.Constants.ADRESSE;
import static de.uni_marburg.sp21.Util.Constants.BESCHREIBUNG;
import static de.uni_marburg.sp21.Util.Constants.BESITZER;
import static de.uni_marburg.sp21.Util.Constants.DE;
import static de.uni_marburg.sp21.Util.Constants.DESCRIPTION;
import static de.uni_marburg.sp21.Util.Constants.EN;
import static de.uni_marburg.sp21.Util.Constants.MESSAGES;
import static de.uni_marburg.sp21.Util.Constants.NACHRICHTEN;
import static de.uni_marburg.sp21.Util.Constants.NAME;
import static de.uni_marburg.sp21.Util.Constants.OPENING_HOURS_COMMENTS;
import static de.uni_marburg.sp21.Util.Constants.ORGANISATIONEN;
import static de.uni_marburg.sp21.Util.Constants.ORGANIZATIONS;
import static de.uni_marburg.sp21.Util.Constants.OWNER;
import static de.uni_marburg.sp21.Util.Constants.PRODUCKTGRUPPE;
import static de.uni_marburg.sp21.Util.Constants.PRODUCTS_DESCRIPTION;
import static de.uni_marburg.sp21.Util.Constants.PRODUCT_GROUPS;
import static de.uni_marburg.sp21.Util.Constants.PRODUKTSBESCHREIBUNG;
import static de.uni_marburg.sp21.Util.Constants.TYPEN;
import static de.uni_marburg.sp21.Util.Constants.TYPES;

import static de.uni_marburg.sp21.Util.Constants.isFav;
import static de.uni_marburg.sp21.Util.Constants.isFavList;
import static de.uni_marburg.sp21.Util.Constants.ÖFFNUNGSZEITENKOMMENTARE;

public class filterActivity extends AppCompatActivity {
    private List<Company> dataList = new ArrayList<>();
    public static List<Company> filteredCompanies = new ArrayList<>();
    Switch SW1, openSwitch, timeSwitch, fav;
    Intent intent;
    Button button, rest;
    EditText hour, minute;
    String[] catProd, typProd, org;
    static String day;
    ArrayList<Integer> itemsSelected1 = new ArrayList<>();
    ArrayList<Integer> itemsSelected2 = new ArrayList<>();
    ArrayList<Integer> itemsSelected3 = new ArrayList<>();
    static ArrayList<String> SelectedCat = new ArrayList<>(); //final
    static ArrayList<String> SelectedTypes = new ArrayList<>();
    static ArrayList<String> SelectedOrg = new ArrayList<>();
    private boolean[] booleans1;
    private boolean[] booleans2;
    private boolean[] booleans3;
    public static boolean clicked = false;
    public static boolean rested = false;
    public static boolean checkedDelivery = false;
    public static boolean checkedOpen = false;
    public static boolean okIsClickedInOrg = false;
    public static boolean checkedOrg = false;
    public static boolean chosenTime = false;
    static boolean checkedTypes = false;
    static boolean okIsClickedInCat = false;
    static boolean switchDelivery = false;
    static boolean switchOpen;
    static boolean checkedCat = false;
    static boolean isFavoriteCheck = false;
    static boolean okIsClickedInType = false;
    static boolean switchTime = false;
    public static boolean favoriteChecked = false;
    String newSrText, newStrCat;
    private Spinner spinner1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter);

        catProd = getResources().getStringArray(R.array.productsGroups);
        typProd = getResources().getStringArray(R.array.Types);
        org = getResources().getStringArray(R.array.OrgName);
        booleans1 = new boolean[catProd.length];
        booleans2 = new boolean[typProd.length];
        booleans3 = new boolean[org.length];
        spinner1 = findViewById(R.id.daySpinner);

        button = findViewById(R.id.ok);
        rest = findViewById(R.id.reset);
        SW1 = findViewById(R.id.switch1);
        openSwitch = findViewById(R.id.isOpen);
        timeSwitch = findViewById(R.id.Time);
        fav = findViewById(R.id.fav);

        hour = findViewById(R.id.hourtext);
        minute = findViewById(R.id.minutetext);
        spinner1 = findViewById(R.id.daySpinner);
        intent = new Intent(filterActivity.this, CompaniesActivity.class);
        spinnerSet();

        /**
         * Switches for the filter's criteria
         */

        SW1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SW1.setChecked(isChecked);
                switchDelivery = true;
            }
        });
        openSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                openSwitch.setChecked(isChecked);
                switchOpen = true;
            }
        });
        timeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                timeSwitch.setChecked(isChecked);
                switchTime = true;
                String Hour = hour.getText().toString();
                String Min = minute.getText().toString();
                if (Hour.isEmpty() || Min.isEmpty()) {
                    if (Hour.isEmpty()) {
                        hour.setError("Enter Hour");
                        hour.requestFocus();
                    }
                    if (Min.isEmpty()) {
                        minute.setError("Enter Minute");
                        minute.requestFocus();
                    }
                }
            }
        });



                                                      fav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                          @Override
                                                          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                              fav.setChecked(isChecked);
                                                              isFavoriteCheck = true;
                                                          }
                                                      });
                                                      button.setOnClickListener(new View.OnClickListener() {
                                                          @Override
                                                          public void onClick(View v) {

                                                              clicked = true;
                                                              Intent intent = new Intent(filterActivity.this, CompaniesActivity.class);
                                                              startActivity(intent);
                                                          }
                                                      });

                                                      rest.setOnClickListener(new View.OnClickListener() {
                                                          @Override
                                                          public void onClick(View v) {
                                                              rested = true;
                                                              restAllFilter();
                                                          }
                                                      }); }

//    public void onBackPressed () {
//        if (count>=2){
//            super.onBackPressed();
//            Log.d("count",String.valueOf(count));
//        }
//
//    }

    /**
     * this method is responsible of resetting the chosen filter's options
     */
    private void restAllFilter() {
        updateList(dataList).clear();

        fav.setChecked(false);
        timeSwitch.setChecked(false);
        openSwitch.setChecked(false);
        SW1.setChecked(false);
        SelectedCat.clear();
        hour.setText("");
        minute.setText("");
        okIsClickedInCat = false;
        okIsClickedInOrg = false;
        okIsClickedInType = false;
        for (int i = 0; i < booleans1.length; i++) {
            booleans1[i] = false;
        }
        itemsSelected1.clear();
        for (int i = 0; i < booleans2.length; i++) {
            booleans2[i] = false;
        }
        itemsSelected2.clear();
        for (int i = 0; i < booleans3.length; i++) {
            booleans3[i] = false;
        }
        itemsSelected3.clear();
        SelectedCat.clear();
        SelectedOrg.clear();
        SelectedTypes.clear();
        spinner1.setSelection(0);
    }

    /**
     * shows options for the different products Groups
     * @param view
     */
    public void showPopup1(View view) {
        int id = view.getId();
        if (id == R.id.Categ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.SelectCategory));
            builder.setMultiChoiceItems(catProd, booleans1,
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int selectedItemId,
                                            boolean isSelected) {
                            if (isSelected) {

                                itemsSelected1.add(selectedItemId);
                            } else if (itemsSelected1.contains(selectedItemId)) {

                                itemsSelected1.remove(Integer.valueOf(selectedItemId));
                            }
                        }
                    }).

                    setPositiveButton(getString(R.string.Ok), new DialogInterface.OnClickListener() {
                        final StringBuilder stringBuilder = new StringBuilder();

                        public void onClick(DialogInterface dialog, int id) {
                            okIsClickedInCat = true;
                            for (int i = 0; i < itemsSelected1.size(); i++) {
                                SelectedCat.add(catProd[itemsSelected1.get(i)]);
                                if (i != itemsSelected1.size() - 1) {
                                    stringBuilder.append(", ");
                                }
                            }
                            //Your logic when OK button is clicked
                        }
                    })
                    .setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            for (int i = 0; i < booleans1.length; i++) {
                                booleans1[i] = false;
                            }
                            itemsSelected1.clear();
                        }

                    });

            for (int i = 0; i < itemsSelected1.size(); i++) {
                SelectedCat.add(catProd[itemsSelected1.get(i)]);
            }
            builder.show();

        }
    }

    /**
     * shows options for the different Organizations
     * @param view
     */

    public void showPopupOrg(View view) {
        int id = view.getId();
        if (id == R.id.OrgButton) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(getString(R.string.SelectedOrganizationsName));
            builder.setMultiChoiceItems(org, booleans3,
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int selectedItemId,
                                            boolean isSelected) {
                            if (isSelected) {

                                itemsSelected3.add(selectedItemId);
                            } else if (itemsSelected3.contains(selectedItemId)) {

                                itemsSelected3.remove(Integer.valueOf(selectedItemId));
                            }
                        }
                    }).

                    setPositiveButton(getString(R.string.Ok), new DialogInterface.OnClickListener() {
                        final StringBuilder stringBuilder = new StringBuilder();

                        public void onClick(DialogInterface dialog, int id) {
                            okIsClickedInOrg = true;
                            for (int i = 0; i < itemsSelected3.size(); i++) {
                                SelectedOrg.add(org[itemsSelected3.get(i)]);
                                if (i != itemsSelected3.size() - 1) {
                                    stringBuilder.append(", ");
                                }
                            }
                            //Your logic when OK button is clicked
                        }
                    })
                    .setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            for (int i = 0; i < booleans3.length; i++) {
                                booleans3[i] = false;
                            }
                            itemsSelected3.clear();
                        }

                    });

            for (int i = 0; i < itemsSelected3.size(); i++) {
                SelectedOrg.add(org[itemsSelected3.get(i)]);
            }
            builder.show();

        }
    }

    /**
     * shows options for the different types
     * @param view
     */
    public void showPopup2(View view) {
        int id = view.getId();
        if (id == R.id.Comp_types) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.SelectTypes));
            builder.setMultiChoiceItems(typProd, booleans2,
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int selectedItemId,
                                            boolean isSelected) {
                            if (isSelected) {

                                itemsSelected2.add(selectedItemId);
                            } else if (itemsSelected2.contains(selectedItemId)) {

                                itemsSelected2.remove(Integer.valueOf(selectedItemId));
                            }
                        }
                    }).

                    setPositiveButton(getString(R.string.Ok), new DialogInterface.OnClickListener() {
                        final StringBuilder stringBuilder = new StringBuilder();

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            okIsClickedInType = true;

                            for (int i = 0; i < itemsSelected2.size(); i++) {
                                SelectedTypes.add(typProd[itemsSelected2.get(i)]);
                                if (i != itemsSelected2.size() - 1) {
                                    stringBuilder.append(", ");
                                }
                            }
                        }
                    })
                    .setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            for (int i = 0; i < itemsSelected2.size(); i++) {
                                ((AlertDialog) dialog).getListView().setItemChecked(itemsSelected2.get(i), false);
                            }
                            itemsSelected2.clear();
                        }
                    });

            for (int i = 0; i < itemsSelected2.size(); i++) {
                SelectedTypes.add(typProd[itemsSelected2.get(i)]);
            }
            builder.show();
        }
    }

    /**
     * this method gives us the companies depending on the chosen Organisations
     * @param companies
     * @param OrgBoolean
     * @return
     */
    public List<Company> getOrg(List<Company> companies, boolean OrgBoolean) {

        List<Company> filterOrg = new LinkedList<Company>();

        if (OrgBoolean) {
            for (int i = 0; i < dataList.size(); i++) {
                List<Organizations> ty = dataList.get(i).getOrganizations();
                for (int j = 0; j < ty.size(); j++) {

                    if (SelectedOrg.contains(ty.get(j).getName())) {
                        filterOrg.add(dataList.get(i));
                        checkedOrg = true;
                    }
                }
            }

        } else {
            filterOrg = companies;

        }
        return filterOrg;

    }

    /**
     * this method gives us the companies marked as Favorites
     * @param company
     */
    public void addFavorite(Company company) {
        company.setIsFavorite(isFav);
    }
    /**
     * this method remove the companies marked as Favorites
     * @param company
     */
    public void removeFavorite(Company company) {
        company.setIsFavorite(isFav);

    }

    /**
     * this method gives us the companies marked as Favorites
     * @param companies
     * @param fav
     * @return companies
     */
    public List<Company> getFavoriteFilter(List<Company> companies, boolean fav) {
        List<Company> favoriteFilter = new ArrayList<>();
        if (fav) {
            for (int i = 0; i < dataList.size(); i++) {
                for (int j = 0; j < isFavList.size(); j++) {
                    if (dataList.get(i).getId() == isFavList.get(j)) {
                        dataList.get(i).setIsFavorite(true);
                        if (dataList.get(i).isFavorite()) {
                            favoriteFilter.add(dataList.get(i));
                            favoriteChecked = true;
                        }
                    }
                }
            }
        } else {
            favoriteFilter = companies;
        }
            return favoriteFilter;

    }

    public List<Company> getFilter(List<Company> companiesList) {

        return getDeliveryServiceCompany(getProductCategoryCompany(getTypes(getOpen(setChosenTime(getOrg(getFavoriteFilter(updateList(companiesList), isFavoriteCheck), okIsClickedInOrg), switchTime), switchOpen), okIsClickedInType), okIsClickedInCat), switchDelivery);

    }

    public List<Company> updateList(List<Company> updatedList) {
        if (!filteredCompanies.isEmpty()) {
            updatedList = filteredCompanies;
        } else {
            dataList = updatedList;
        }
        return updatedList;
    }

    /**
     * this method gives us the companies that have a Delivery Service
     * @param companies
     * @param bo
     * @return
     */

    public List<Company> getDeliveryServiceCompany(List<Company> companies, boolean bo) {
        List<Company> filterDelivery = new LinkedList<Company>();
        if (bo) {
            for (int i = 0; i < dataList.size(); i++) {
                if (dataList.get(i).isDeliveryService()) {
                    filterDelivery.add(dataList.get(i));
                    checkedDelivery = true;
                }
            }
        } else {
            filterDelivery = companies;
        }
        return filterDelivery;
    }

    /**
     * this method gives us the companies depending on the chosen Categories
     * @param companies
     * @param l
     * @return
     */
    public List<Company> getProductCategoryCompany(List<Company> companies, boolean l) {
        List<Company> filterProductCategory = new LinkedList<Company>();
        if (l) {
            for (int i = 0; i < dataList.size(); i++) {
                List<ProductGroups> pd = dataList.get(i).getProductGroups();
                for (int j = 0; j < pd.size(); j++) {
                    if (SelectedCat.contains(pd.get(j).getCategoryToString())) {
                        filterProductCategory.add(dataList.get(i));
                        checkedCat = true;
                    }
                }
            }
        } else {
            filterProductCategory = companies;
        }
        return filterProductCategory;
    }

    /**
     * this method gives us the companies depending on the chosen Types
     * @param companies
     * @param typesBool
     * @return
     */
    public List<Company> getTypes(List<Company> companies, boolean typesBool) {

        List<Company> filterType = new LinkedList<Company>();
        if (typesBool) {
            filteredCompanies.clear();
            for (int i = 0; i < dataList.size(); i++) {
                List<String> ty = dataList.get(i).getTypesToString();
                for (int j = 0; j < ty.size(); j++) {
                    if (SelectedTypes.contains(ty.get(j))) {
                        filterType.add(dataList.get(i));
                        checkedTypes = true;
                    }
                }
            }

        } else {
            filterType = companies;
        }
        return filterType;

    }

    /**
     * this method gives us the companies that are open now
     * @param companies
     * @param openBool
     * @return
     */
    public List<Company> getOpen(List<Company> companies, boolean openBool) {
        List<Company> filterOpen = new ArrayList<>();
        LocalDateTime localDate = LocalDateTime.now();
        if (openBool) {
            for (int i = 0; i < dataList.size(); i++) {
                List<OpeningHours> openingHoursList = dataList.get(i).getOpeningHours();
                for (int j = 0; j < openingHoursList.size(); j++) {
                    if (openingHoursList.get(j).isOpen()) {
                        if (dataList.get(i).getdayOfWeek(localDate.getDayOfWeek().name(), localDate.getHour(), localDate.getMinute())) {
                            filterOpen.add(dataList.get(i));
                            checkedOpen = true;
                        }
                    }

                }
            }
        } else {
            filterOpen = companies;
        }

        return filterOpen;
    }

    /**
     * //     * this method is responsible for transforming the given Category in lowercase and eliminates the space in the Category
     * //     * @param srCat Category
     * //     * @return Category without space and with only lower Letters
     * //
     */
    private String lower(String srCat) {
        String lower = "";
        if (srCat.contains(" ")) {
            char[] c = srCat.toCharArray();
            c[0] = Character.toLowerCase(c[0]);
            String x = String.valueOf(c);
            String[] newSrCat = x.split(" ");
            lower = newSrCat[0] + newSrCat[1];
        } else {
            lower = srCat.toLowerCase();
        }
        return lower;
    }

    /**
     * this method is responsible for transforming the given searched Text in lowercase and eliminates the space in the searched Text
     *
     * @param srText searched Text
     * @return searched Text only lower Letters
     */
    private String lowerSearchText(String srText) {
        String lower = "";

        lower = srText.replace(" ", "").toLowerCase();

        return lower;
    }

    /**
     * it is responsible for loading the corresponding data when the user enters something to search
     */


    public List<Company> getFoundData(String searched, String searchCategory, String lang) {
        List<Company> searchList = new ArrayList<>();
        newStrCat = lower(searchCategory);
        newSrText = lowerSearchText(searched);

        switch (lang) {
            case EN:
                for (int i = 0; i < dataList.size(); i++) {
                    switch (newStrCat) {
                        case NAME:
                            String name = dataList.get(i).getName().toLowerCase();
                            if (name.contains(newSrText)) {
                                searchList.add(dataList.get(i));

                            }
                            break;
                        case OWNER:
                            String owner = dataList.get(i).getOwner().toLowerCase();
                            if (owner.contains(newSrText)) {
                                searchList.add(dataList.get(i));

                            }
                            break;
                        case PRODUCTS_DESCRIPTION:
                            String productsDescription = dataList.get(i).getProductsDescription().toLowerCase();
                            if (productsDescription.contains(newSrText)) {
                                searchList.add(dataList.get(i));

                            }
                            break;
                        case DESCRIPTION:
                            String description = dataList.get(i).getDescription().toLowerCase();
                            if (description.contains(newSrText)) {
                                searchList.add(dataList.get(i));

                            }
                            break;
                        case ADDRESS:
                            Address address = dataList.get(i).getAddress();
                            if ((address.getCity()).toLowerCase().contains(newSrText) || (address.getStreet().toLowerCase().contains(newSrText)) || (address.getZip().toLowerCase().contains(newSrText))) {
                                searchList.add(dataList.get(i));
                                break;
                            }
                        case ORGANIZATIONS:
                                List<Organizations> ty = dataList.get(i).getOrganizations();
                                for (int j = 0; j < ty.size(); j++) {
                                    if ((ty.get(j).getName()).toLowerCase().contains(newSrText) || ((ty.get(j).getUrl()).toLowerCase().contains(newSrText))) {
                                        searchList.add(dataList.get(i));
                                    }
                                }

                        case PRODUCT_GROUPS:
                                List<ProductGroups> pd = dataList.get(i).getProductGroups();
                                for (int j = 0; j < pd.size(); j++) {
                                    if ((pd.get(j).getSeasons()).toString().toLowerCase().contains(newSrText) || (pd.get(j).getProductTags()).toString().toLowerCase().contains(newSrText) || (pd.get(j).getRawProduct()).toLowerCase().contains(newSrText) || (pd.get(j).getCategoryToString().toLowerCase().contains(newSrText))) {
                                        searchList.add(dataList.get(i));
                                    }
                                }

                        case MESSAGES:
                                List<Messages> messages=dataList.get(i).getMessages();
                            for (int j = 0; j < messages.size(); j++) {
                                if ((messages.get(j).getContent()).toLowerCase().contains(newSrText)||(messages.get(j).getDate()).toString().contains(newSrText)) {
                                    searchList.add(dataList.get(i));
                                }
                            }
                        case TYPES:
                                List<String> types = dataList.get(i).getTypesToString();
                                for (int j = 0; j < types.size(); j++) {
                                    if ((types.get(j)).toLowerCase().contains(newSrText)) {
                                        searchList.add(dataList.get(i));
                                    }
                            }
                        case OPENING_HOURS_COMMENTS:
                            String openingHoursComments = dataList.get(i).getOpeningHoursComments().toLowerCase();
                            if (openingHoursComments.contains(newSrText)) {
                                searchList.add(dataList.get(i));
                            }
                            break;
                    }
                }

                case DE:
                            for (int i = 0; i < dataList.size(); i++) {
                                switch (newStrCat) {
                                    case NAME:
                                        String name = dataList.get(i).getName().toLowerCase();
                                        if (name.contains(newSrText)) {
                                            searchList.add(dataList.get(i));

                                        }
                                        break;
                                    case BESITZER:
                                        String owner = dataList.get(i).getOwner().toLowerCase();
                                        if (owner.contains(newSrText)) {
                                            searchList.add(dataList.get(i));

                                        }
                                        break;
                                    case PRODUKTSBESCHREIBUNG:
                                        String productsDescription = dataList.get(i).getProductsDescription().toLowerCase();
                                        if (productsDescription.contains(newSrText)) {
                                            searchList.add(dataList.get(i));

                                        }
                                        break;
                                    case BESCHREIBUNG:
                                        String description = dataList.get(i).getDescription().toLowerCase();
                                        if (description.contains(newSrText)) {
                                            searchList.add(dataList.get(i));
                                        }
                                        break;
                                    case ADRESSE:
                                        Address address = dataList.get(i).getAddress();
                                        if ((address.getCity()).toLowerCase().contains(newSrText) || (address.getStreet().toLowerCase().contains(newSrText)) || (address.getZip().toLowerCase().contains(newSrText))) {
                                            searchList.add(dataList.get(i));
                                        }
                                        break;

                                    case TYPEN:
                                        List<String> types = dataList.get(i).getTypesToString();
                                        for (int j = 0; j < types.size(); j++) {
                                            if ((types.get(j)).toLowerCase().contains(newSrText)) {
                                                searchList.add(dataList.get(i));

                                            }
                                        }
                                    case NACHRICHTEN:
                                        List<Messages> messages=dataList.get(i).getMessages();
                                        for (int j = 0; j < messages.size(); j++) {
                                            if ((messages.get(j).getContent()).toLowerCase().contains(newSrText)||(messages.get(j).getDate()).toString().contains(newSrText)) {
                                                searchList.add(dataList.get(i));
                                            }
                                        }

                                    case PRODUCKTGRUPPE:
                                        for (int j = 0; j < dataList.size(); j++) {
                                            List<ProductGroups> pd = dataList.get(j).getProductGroups();
                                            for (int y = 0; y < pd.size(); y++) {
                                                if ((pd.get(y).getSeasons()).toString().toLowerCase().contains(newSrText) || (pd.get(y).getProductTags()).toString().toLowerCase().contains(newSrText) || (pd.get(y).getRawProduct()).toLowerCase().contains(newSrText) || (pd.get(y).getCategoryToString().toLowerCase().contains(newSrText))) {
                                                    searchList.add(dataList.get(j));
                                                }
                                            }
                                        }

                                    case ORGANISATIONEN:
                                        for (int j = 0; i < dataList.size(); i++) {
                                            List<Organizations> ty = dataList.get(j).getOrganizations();
                                            for (int y = 0; y < ty.size(); y++) {
                                                if ((ty.get(y).getName()).toLowerCase().contains(newSrText) || ((ty.get(y).getUrl()).toLowerCase().contains(newSrText))) {
                                                    searchList.add(dataList.get(j));
                                                }
                                            }
                                        }

                                    case ÖFFNUNGSZEITENKOMMENTARE:
                                        String openingHoursComments = dataList.get(i).getOpeningHoursComments().toLowerCase();
                                        if (openingHoursComments.contains(newSrText)) {
                                            searchList.add(dataList.get(i));
                                        }
                                        break;

                                }

                            }
                    }

                    return searchList;
                }

    /**
     * this method sets the spinner of DAys
     */
                private void spinnerSet () {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Days,
                        android.R.layout.simple_list_item_checked);

                spinner1.setAdapter(adapter);
                spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        day = parent.getItemAtPosition(position).toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

            }

    /**
     * this method gives us the companies depending on the chosen Time
     * @param companies
     * @param openBool
     * @return
     */
            public List<Company> setChosenTime (List < Company > companies,boolean openBool){
                List<Company> filterChosenTime = new LinkedList<Company>();
                if (openBool) {
                    int h = Integer.parseInt(hour.getText().toString());
                    int  m = Integer.parseInt(minute.getText().toString());

                    for (int i = 0; i < dataList.size(); i++) {
                        List<OpeningHours> openingHoursList = dataList.get(i).getOpeningHours();
                        for (int j = 0; j < openingHoursList.size(); j++) {
                            if (openingHoursList.get(j).isOpen()) {
                                if (dataList.get(i).getdayOfWeek(day.toUpperCase(), h, m)) {
                                    filterChosenTime.add(dataList.get(i));
                                    chosenTime = true;
                                    break;
                                }
                            }

                        }
                    }

                } else {
                    filterChosenTime = companies;
                }
                return filterChosenTime;
            }
    }


