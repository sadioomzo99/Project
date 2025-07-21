package de.uni_marburg.sp21.Model;

import com.example.sp21.R;

import java.util.ArrayList;
import java.util.List;

//import de.uni_marburg.sp21.Model.CompanyMessage;

import de.uni_marburg.sp21.Activity.CompaniesActivity;


public class Company {
    private String companies;
    private String name;
    private String description;
    private String owner;
    private String mail;
    private String url;
    private String productsDescription;
    private String openingHoursComments;
    private String geoHash;
    private double id;
    private Address address;
    private Location location;
    private List<Types> types;
    private List<ProductGroups>productGroups;
    private List<Organizations> organizations;
    private boolean deliveryService;
    private List<Messages> messages;
    private List<Images> images;
    private List<OpeningHours>openingHours;
    private boolean isFavorite = false;


    public Company(double id,String name, String owner, Address address, boolean deliveryService, String description,
                   String openingHoursComments, String geoHash, String url, String productsDescription, String mail, Location location,
                   List<Messages> messages, List<Organizations> organizations, List<Types> types, List<ProductGroups> productGroups, List<Images> images,List<OpeningHours> openingHours) {
        this.name = name;
        this.id=id;
        this.address = address;
        this.owner = owner;
        this.description = description;
        this.deliveryService = deliveryService;
        this.mail = mail;
        this.openingHoursComments = openingHoursComments;
        this.geoHash = geoHash;
        this.url = url;
        this.productsDescription = productsDescription;
        this.location = location;
        this.organizations = organizations;
        this.messages = messages;
        this.types=types;
        this.productGroups=productGroups;
        this.images=images;
        this.openingHours=openingHours;
    }




    //Companies
    public void setCompanies(String companies) {
        this.companies = companies;
    }

    public String getCompanies() {
        return companies;
    }

    //Id
    public void setId(double id) {
        this.id = id;
    }


    public double getId() {
        return id;
    }


    //Name
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    //Address:
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }


    //Owner
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        String newOwner = "";
        if (owner == null) {
            newOwner = "Not Available";
        } else {
            newOwner = owner;
        }
        return newOwner;
    }

    public boolean isDeliveryService() {
        return deliveryService;
    }

    public void setDeliveryService(boolean deliveryService) {
        this.deliveryService = deliveryService;
    }

    //DeliveryServices
    public String getDeliveryServices() {
        String delivery = "";
        if (deliveryService) {
            delivery = CompaniesActivity.getRes().getString(R.string.available);
        } else {
            delivery = CompaniesActivity.getRes().getString(R.string.notavailable);
        }
        return delivery;
    }

    //description
    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    //mail
    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getMail() {
        return mail;
    }

    //url
    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    //geoHash
    public void setGeoHash(String geoHash) {
        this.geoHash = geoHash;
    }

    public String getGeoHash() {
        return geoHash;
    }

    //productsDescription
    public void setProductsDescription(String productsDescription) {
        this.productsDescription = productsDescription;
    }

    public String getProductsDescription() {
        return productsDescription;
    }

    //openingHoursComments
    public void setOpeningHoursComments(String openingHoursComments) {
        this.openingHoursComments = openingHoursComments;
    }

    public String getOpeningHoursComments() {
        return openingHoursComments;
    }

    //location
    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    //Organizations
    public void setOrganizations(List<Organizations> organizations) {
        this.organizations = organizations;
    }

    public List<Organizations> getOrganizations() {
        return organizations;

    }
    public  String getOrganizationsToString(){
        String org="";
        for (Organizations organizations1 :organizations){
               org=organizations1.getName();

        }
        return org;
    }

    //messages
    public void setMessages(List<Messages> messages) {
        this.messages = messages;
    }

    public List<Messages> getMessages() {
        return messages;
    }

    //productGroups
    public void setProductGroups(List<ProductGroups> productGroups) {
        this.productGroups = productGroups;
    }

    public List<ProductGroups> getProductGroups() {
        return productGroups;
    }

    //types
    public void setTypes(List<Types> types) {
        this.types = types;
    }

    public List<Types> getTypes() {
        return types;
    }
    public  List<String> getTypesToString() {
        List<String> Types= new ArrayList<>();
        for (Types type : types) {
            switch (type) {
                case MART:
                   Types.add(CompaniesActivity.getRes().getString(R.string.Mart));
                    break;
                case SHOP:
                    Types.add(CompaniesActivity.getRes().getString(R.string.Shop));
                    break;
                case HOTEL:
                    Types.add(CompaniesActivity.getRes().getString(R.string.Hotel));
                    break;
                case PRODUCER:
                    Types.add(CompaniesActivity.getRes().getString(R.string.Producer));
                    break;
                case RESTAURANT:
                    Types.add(CompaniesActivity.getRes().getString(R.string.Restaurant));
                    break;
            }
        }
        return Types;
    }

    //Images
    public void setImages(List<Images> images) {
        this.images = images;
    }

    public List<Images> getImages() {
        return images;
    }
    //OpeningHours


    public List<OpeningHours> getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(List<OpeningHours> openingHours) {
        this.openingHours = openingHours;
    }
    public boolean getdayOfWeek(String days,int hour,int min){
        boolean b=false;
        List<String> day = new ArrayList<>();
        String closed =CompaniesActivity.getRes().getString(R.string.Closed);
        for (int i = 0; i < getOpeningHours().size(); i++) {
            String x = (getOpeningHours().get(i).getDay()).toUpperCase();

            day.add(getOpeningHours().get(i).getDay());
            if (days.equals(x)) {
                if (openingHours.get(i).getTimeIntervals().isBetween(hour, min)) {
                    b = true;

                }
            }
        }

        return b;
    }

    //Fav
    public boolean setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
        return isFavorite;
    }

    public boolean isFavorite() {
        return isFavorite;
    }
}
