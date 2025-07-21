package de.uni_marburg.sp21.Model;

public class Organizations {
    private double id;
    private String name,url;

    public Organizations(double id,String name,String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

    //id
    public void setId(double id) {
        this.id = id;
    }

    public double getId() {
        return id;
    }

    //name
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    //url

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
