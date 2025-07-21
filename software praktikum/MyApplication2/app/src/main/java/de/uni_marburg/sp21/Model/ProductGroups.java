package de.uni_marburg.sp21.Model;

import com.example.sp21.R;

import java.util.ArrayList;
import java.util.List;

import de.uni_marburg.sp21.Activity.CompaniesActivity;
import de.uni_marburg.sp21.Activity.detailsActivity;

public class ProductGroups {
    private Categories category;
    private boolean rawProduct;
    private int producer;
    Seasons season;
    private List<String> productTags;
    private List<Seasons> seasons;
    public ProductGroups(String category, boolean rawProduct, int producer, List<String>productTags,List<String>seasons){
        this.rawProduct=rawProduct;
        this.producer=producer;
        this.productTags=productTags;
        switch (category) {
            case "vegetables":
                this.category = Categories.VEGETABLES;
                break;
            case "fruits":
                this.category = Categories.FRUITS;
                break;
            case "meat":
                this.category = Categories.MEAT;
                break;
            case "meatproducts":
                this.category = Categories.MEATPRODUCTS;
                break;
            case "cereals":
                this.category = Categories.CEREALS;
                break;
            case "milk":
                this.category = Categories.MILK;
                break;
            case "milkproducts":
                this.category = Categories.MILKPRODUCTS;
                break;
            case "eggs":
                this.category = Categories.EGGS;
                break;
            case "honey":
                this.category = Categories.HONEY;
                break;
            case "beverages":
                this.category = Categories.BEVERAGES;
                break;
            case "bakedgoods":
                this.category = Categories.BAKEDGOODS;
                break;
            case "pasta":
                this.category = Categories.PASTA;
                break;

        }



        List<Seasons> seasonsArrayList = new ArrayList<>();
        for (String season : seasons) {
            switch (season) {
                case "spring": seasonsArrayList.add(Seasons.Spring); break;
                case "summer": seasonsArrayList.add(Seasons.Summer); break;
                case "autumn": seasonsArrayList.add(Seasons.Autumn); break;
                case "winter": seasonsArrayList.add(Seasons.Winter); break;
            }
        }
        this.seasons=seasonsArrayList;
    }


    public Categories getCategory() {
        return category;
    }

    public String getCategoryToString(){
        String cat="";
        switch(category) {
            case VEGETABLES:
               cat= CompaniesActivity.getRes().getString(R.string.Vegetables);
               break;
            case FRUITS:
                cat=CompaniesActivity.getRes().getString(R.string.Fruits);
                break;
            case MEAT:
                cat= CompaniesActivity.getRes().getString(R.string.Meat);
                break;
            case MEATPRODUCTS:
                cat= CompaniesActivity.getRes().getString(R.string.Meatproducts);
                break;
            case CEREALS:
                cat= CompaniesActivity.getRes().getString(R.string.Cereals);
                break;
            case MILK:
                cat= CompaniesActivity.getRes().getString(R.string.Milk);
                break;
            case MILKPRODUCTS:
                cat=CompaniesActivity.getRes().getString(R.string.Milkproducts);
                break;
            case EGGS:
                cat= CompaniesActivity.getRes().getString(R.string.Eggs);
                break;
            case HONEY:
                cat= CompaniesActivity.getRes().getString(R.string.Honey);
                break;
            case BEVERAGES:
                cat= CompaniesActivity.getRes().getString(R.string.Beverages);
                break;
            case BAKEDGOODS:
                cat= CompaniesActivity.getRes().getString(R.string.Bakedgoods);
                break;
            case PASTA:
                cat= CompaniesActivity.getRes().getString(R.string.Pasta);
                break;
        }
        return cat;
    }


    public boolean isRawProduct() {
        return rawProduct;
    }
    public String getRawProduct(){
        String raw="";
        if(isRawProduct()){
            raw=CompaniesActivity.getRes().getString(R.string.raw);
        }else{
            raw=CompaniesActivity.getRes().getString(R.string.notRaw);
        }
        return raw;
    }

    public int getProducer() {
        return producer;
    }

    public List<String> getSeasons() {
        List<String> seasonsList = new ArrayList<>();
        for (Seasons season : seasons) {
            switch (season) {
                case Autumn:
                    seasonsList.add(CompaniesActivity.getRes().getString(R.string.Autumn));
                    break;
                case Spring:
                    seasonsList.add(CompaniesActivity.getRes().getString(R.string.Spring));
                    break;
                case Summer:
                    seasonsList.add(CompaniesActivity.getRes().getString(R.string.Summer));
                    break;
                case Winter:
                    seasonsList.add(CompaniesActivity.getRes().getString(R.string.Winter));
                    break;
            }

        }
        return seasonsList;
    }

    public List<String> getProductTags() {
        return productTags;
    }



}
