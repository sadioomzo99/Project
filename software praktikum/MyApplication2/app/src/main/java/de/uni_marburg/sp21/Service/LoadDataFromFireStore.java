package de.uni_marburg.sp21.Service;



import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import de.uni_marburg.sp21.Activity.CompaniesActivity;
import de.uni_marburg.sp21.Model.Address;
import de.uni_marburg.sp21.Model.Company;
import de.uni_marburg.sp21.Model.Day;
import de.uni_marburg.sp21.Model.Images;
import de.uni_marburg.sp21.Model.Location;
import de.uni_marburg.sp21.Model.Messages;
import de.uni_marburg.sp21.Model.OpeningHours;
import de.uni_marburg.sp21.Model.Organizations;
import de.uni_marburg.sp21.Model.ProductGroups;
import de.uni_marburg.sp21.Model.TimeInterval;
import de.uni_marburg.sp21.Model.Types;


import static de.uni_marburg.sp21.Util.Constants.ID;
import static de.uni_marburg.sp21.Util.Constants.collectionReference;
import static de.uni_marburg.sp21.Util.Constants.db;


public class LoadDataFromFireStore {
    List<Company> dataList = new ArrayList<>();
    private static LoadDataFromFireStore instance;


    public LoadDataFromFireStore() {
        initialize();
    }

    public static LoadDataFromFireStore getInstance() {
        if (instance == null) {
            instance = new LoadDataFromFireStore();
        }
        return instance;
    }

    public void initialize() {
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    public void addCompany(Company company) {
        dataList.add(company);
    }

    public List<Company> getCompanies() {
        return dataList;
    }

    public void getData(CompaniesActivity companiesActivity) {
        collectionReference.orderBy(ID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Company company = new Company(
                                ((double) document.get("id")),
                                ((String) document.get("name")),
                                ((String) document.get("owner")),
                                loadAddress((Map<String, String>) document.get("address")),
                                ((boolean) document.get("deliveryService")),
                                ((String) document.get("description")),
                                ((String) document.get("openingHoursComments")),
                                ((String) document.get("geoHash")),
                                ((String) document.get("url")),
                                ((String) document.get("productsDescription")),
                                ((String) document.get("mail")),
                                loadLocation((Map<String, Double>) document.get("location")),
                                loadMessages((ArrayList<Map<String, String>>) document.get("messages")),
                                loadOrganizations((ArrayList<Map<String, Object>>) document.get("organizations")),
                                loadTypes((ArrayList<String>) document.get("types")),
                                loadProductGroups((ArrayList<Map<String, Object>>) document.get("productGroups")),
                                loadImages((ArrayList<Images>) document.get("imagePaths")),
                                openingHours((Map<String, List<Map<String, String>>>)document.get("openingHours"))
                        );
                        addCompany(company);
                    }

                }
                companiesActivity.initRecyclerView();

            }
        });
    }

    //Loading Address
    private Address loadAddress(Map<String, String> addressMap) {
        return new Address(addressMap.get("street"), addressMap.get("city"), addressMap.get("zip"));
    }

    //Loading Location
    private Location loadLocation(Map<String, Double> locationMap) {
        return new Location(locationMap.get("lat"), locationMap.get("lon"));
    }

    //Loading Organizations
    private List<Organizations> loadOrganizations(ArrayList<Map<String, Object>> organizationMap) {
        List<Organizations> organisations = new ArrayList<>();
        for (Map<String, Object> map : organizationMap) {
            if (!map.isEmpty()) {
                Organizations org = new Organizations((double) map.get("id"), (String) map.get("name"), (String) map.get("url"));
                organisations.add(org);
            }
        }
        return organisations;
    }

    //Messages
    private List<Messages> loadMessages(ArrayList<Map<String, String>> messagesMapArrayList) {
        List<Messages> messages = new ArrayList<>();
        for (Map<String, String> map : messagesMapArrayList) {
            if (!map.isEmpty()) {
                try {
                    messages.add(new Messages(map.get("content"), map.get("date")));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return messages;
    }


    private List<ProductGroups> loadProductGroups(ArrayList<Map<String, Object>> productMaps) {
        List<ProductGroups> products = new ArrayList<>();
        for (Map<String, Object> map : productMaps) {
            if (!map.isEmpty()) {
                products.add(new ProductGroups(((String) map.get("category")), ((boolean) map.get("rawProduct")), ((Double) map.get("producer")).intValue(), (ArrayList<String>) map.get("productTags"), ((ArrayList<String>) map.get("seasons"))));
            }
        }
        return products;
    }


    private List<Types> loadTypes(ArrayList<String> typeList) {
        List<Types> Type = new ArrayList<>();
        for (String type : typeList) {
            switch (type) {
                case "producer":
                    Type.add(Types.PRODUCER);
                    break;
                case "shop":
                    Type.add(Types.SHOP);
                    break;
                case "restaurant":
                    Type.add(Types.RESTAURANT);
                    break;
                case "hotel":
                    Type.add(Types.HOTEL);
                    break;
                case "mart":
                    Type.add(Types.MART);
                    break;
            }
        }
        return Type;
    }

    private List<Images> loadImages(ArrayList<Images> imagePaths) {
        List<Images> image = new ArrayList<>();
        image.addAll(imagePaths);
        return image;
    }



    private List<OpeningHours> openingHours(Map<String, List<Map<String, String>>> openingHoursMap) {

        List<OpeningHours> openingHours = new ArrayList<>();
        if (openingHoursMap.get("monday") != null) {
            for (Map<String, String> map : openingHoursMap.get("monday")) {
                openingHours.add(new OpeningHours(Day.monday, new TimeInterval(map.get("start"), map.get("end"))));
            }
        } else {
            openingHours.add(new OpeningHours(Day.monday, new TimeInterval("closed","closed")));
    }
        if (openingHoursMap.get("tuesday") != null) {
            for (Map<String, String> map : openingHoursMap.get("tuesday")) {
                openingHours.add(new OpeningHours(Day.tuesday, (new TimeInterval(map.get("start"), map.get("end")))));
            }
        }else {
        openingHours.add(new OpeningHours(Day.tuesday, new TimeInterval("closed","closed")));
        }

        if (openingHoursMap.get("wednesday") != null) {
            for (Map<String, String> map : openingHoursMap.get("wednesday")) {
                openingHours.add(new OpeningHours(Day.wednesday, (new TimeInterval(map.get("start"), map.get("end")))));
            }
        }else {
            openingHours.add(new OpeningHours(Day.wednesday, new TimeInterval("closed","closed")));
        }

        if (openingHoursMap.get("thursday") != null) {
            for (Map<String, String> map : openingHoursMap.get("thursday")) {
                openingHours.add(new OpeningHours(Day.thursday, (new TimeInterval(map.get("start"), map.get("end")))));
            }

            }else {
                openingHours.add(new OpeningHours(Day.tuesday, new TimeInterval("closed","closed")));
        }
        if (openingHoursMap.get("friday") != null) {
            for (Map<String, String> map : openingHoursMap.get("friday")) {
                openingHours.add(new OpeningHours(Day.friday, (new TimeInterval(map.get("start"), map.get("end")))));
            }
        } else {
            openingHours.add(new OpeningHours(Day.friday, new TimeInterval("closed","closed")));
        }

            if (openingHoursMap.get("saturday") != null) {
                for (Map<String, String> map : openingHoursMap.get("saturday")) {
                    openingHours.add(new OpeningHours(Day.saturday, (new TimeInterval(map.get("start"), map.get("end")))));
                }
            }else{
                openingHours.add(new OpeningHours(Day.saturday, new TimeInterval("closed","closed")));
            }


            if (openingHoursMap.get("sunday") != null) {
                for (Map<String, String> map : openingHoursMap.get("sunday")) {
                    openingHours.add(new OpeningHours(Day.sunday, (new TimeInterval(map.get("start"), map.get("end")))));

                }
            } else {
                openingHours.add(new OpeningHours(Day.sunday, new TimeInterval("closed","closed")));
            }

    return openingHours;
    }
}


