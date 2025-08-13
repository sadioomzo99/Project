package de.uni_marburg.sp21.Model;

public class Address {
    private String street;
    private String zip;
    private String city;

    public Address(){

    }
    public Address(String street,String city ,String zip ){
        this.city=city;
        this.street=street;
        this.zip=zip;

    }

    public String getStreet() {
        return street;
    }



    public String getZip() {
        return zip;
    }



    public String getCity() {

        return city ;
    }
}
