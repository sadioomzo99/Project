package de.uni_marburg.sp21.Util;

import android.Manifest;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * this Class holds the different Constants
 */

public class Constants {
    public static  FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static final CollectionReference collectionReference = db.collection("companies");
    public static final String CREDENTIAL_SHARED_PREF = "log";
    public static final String FiNE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    public static final float DEFAULT_ZOOM = 6f;
    public static final String LAT= "lat";
    public static final String LON="lon";
    public static final String NAME="name";
    public static final String ID= "id";
    public static boolean isFav;
    public static int posFav;
    public static List<Double> isFavList=new ArrayList<Double>();
    public static final String LAN="lan";
    public static final String CHECK="check";
    public static final String POS="pos";
    public static final String ENTERTEXT="Enter Text";
    public static final String OPENING_HOURS_COMMENTS= "openingHoursComments";
    public static final String OWNER="owner";
    public static final String PRODUCTS_DESCRIPTION= "productsDescription";
    public static final String DESCRIPTION="description";
    public static final String ADDRESS = "address";
    public static final String TYPES= "types";
    public static final String MESSAGES="messages";
    public static final String PRODUCT_GROUPS= "productGroups";
    public static final String ORGANIZATIONS="organizations";
    public static final String DE="De";
    public static final String EN="En";
    public static final String ÖFFNUNGSZEITENKOMMENTARE ="öffnungszeitenKommentare";
    public static final String BESITZER= "besitzer";
    public static final String ADRESSE= "adresse";
    public static final String TYPEN="typen";
    public static final String ORGANISATIONEN= "organisationen";
    public static final String PRODUCKTGRUPPE= "produktGruppe";
    public static final String NACHRICHTEN=  "Nachrichten";
    public static final String PRODUKTSBESCHREIBUNG= "produktsBeschreibung";
    public static final String BESCHREIBUNG= "beschreibung";
    public static final String MY_LOCATION="you are here";
    public static final String  NO_CONNECTION= "NO Connection";
    public static final String  SEARCH_RECHECK= "\"Nothing found! \n please recheck your text\"";
}
