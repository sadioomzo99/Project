package de.uni_marburg.sp21.Model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Messages {

    private String content;
    private Date date;

    public Messages(String content, String date) throws ParseException {
        this.content = content;
        this.date =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(date);
    }

    //content
    public String getContent() {
        return content;
    }
    //Date
    public Date getDate() {
        return date;
    }
}

