package es.eina.geolocalization;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import es.eina.RestApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Geolocalizer {

    public static final String DEFAULT_COUNTRY = "O1";
    private static Logger logger = LoggerFactory.getLogger("Geolocalizer");
    private static Geolocalizer instance = null;


    private DatabaseReader reader = null;

    public Geolocalizer(String f) {
        // This creates the DatabaseReader object. To improve performance, reuse
        // the object across lookups. The object is thread-safe.
        try {
            ClassLoader loader = RestApp.class.getClassLoader();
            InputStream stream = loader.getResourceAsStream(f);
            if(stream == null){
                throw new RuntimeException("Cannot read inner file: " + f);
            }
            reader = new DatabaseReader.Builder(stream).build();
            logger.info("Read GeoIP database from " + f);
        } catch (IOException e) {
            logger.error("Cannot read database file, all requests will return O1 ISO country code.");
        }
    }

    public String getCountryCode(String ip) {
        try {
            InetAddress parsedIp = InetAddress.getByName(ip);
            return getCountryCode(parsedIp);
        } catch (UnknownHostException e) {
            logger.error("Cannot parse " + ip + " as a valid IP, country will be "+DEFAULT_COUNTRY+".");
        }
        return DEFAULT_COUNTRY;
    }

    public String getCountryCode(InetAddress ip) {
        if(reader != null) {
            try {
                // Get Country from a parsed IP
                CountryResponse response = reader.country(ip);
                return response.getCountry().getIsoCode();
            } catch (IOException e) {
                logger.error("Cannot parse " + ip.getHostAddress() + " as a valid IP, country will be "+DEFAULT_COUNTRY+".");
            } catch (GeoIp2Exception e) {
                logger.error("Cannot geolocalize " + ip.getHostAddress() + ", country will be "+DEFAULT_COUNTRY+".");

            }
        }
        return DEFAULT_COUNTRY;
    }

    public static Geolocalizer build(String f){
        if(instance == null){
            instance = new Geolocalizer(f);
        }

        return instance;
    }

    public static Geolocalizer getInstance(){
        return instance;
    }
}
