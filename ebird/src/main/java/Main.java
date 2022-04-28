import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wirefreethought.geodb.client.GeoDbApi;
import com.wirefreethought.geodb.client.model.GeoDbInstanceType;
import com.wirefreethought.geodb.client.model.RegionSummary;
import com.wirefreethought.geodb.client.model.RegionsResponse;
import com.wirefreethought.geodb.client.net.GeoDbApiClient;
import com.wirefreethought.geodb.client.request.FindRegionsRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Main {
    private static final Logger log = Logger.getLogger(Main.class.getName());
    private static final String birdApiToken = "jru82bsubc2g"; // my eBird API Token
    private static final String regionApiToken = "";
    private static String defaultCountry = "DE";
    private static String birdUrl = "https://api.ebird.org/v2/data/obs/%s/recent";
    private static String countryUrl = "https://wft-geo-db.p.rapidapi.com/v1/geo/countries/%s/regions";

    public static void main(String[] args) {
        boolean isRunning = true;
        while (isRunning) {
            System.out.print("Enter '-query' for querying birds. " +
                    "Enter '-regions' to get subregions of a specific country. " +
                    " Enter '-quit' to quit: ");
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            try {
                String command = input.readLine();
                BufferedReader userInput;
                switch (command.toLowerCase()) {
                    case "-query":
                        System.out.print("Please enter country code: ");
                        userInput = new BufferedReader(new InputStreamReader(System.in));
                        String countryCode = userInput.readLine();
                        countryCode = countryCode.replaceAll("\\s+", "");
                        executeBirdQuery(countryCode);
                        break;
                    case "-regions":
                        System.out.print("Please enter name of country to get subregions of: ");
                        userInput = new BufferedReader(new InputStreamReader(System.in));
                        String countryName = userInput.readLine();
                        List<String> regions = queryCountry(countryName);
                        for (String region : regions) {
                            System.out.println(region);
                        }
                        break;
                    case "-quit":
                        isRunning = false;
                        break;
                    default:
                        System.out.println("Unknown command. Please retry.");
                        break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Quitting");
    }

    /**
     * Executes query to get latest bird observation
     *
     * @param countryCode country code to query
     */
    private static void executeBirdQuery(String countryCode) {
        if (countryCode.isEmpty() || countryCode.isBlank()) {
            countryCode = defaultCountry;
        }
        birdUrl = String.format(birdUrl, countryCode);
        HttpURLConnection connection = buildBirdConnection(birdUrl, birdApiToken);
        String responseContent = getResponseContent(connection);
        List<ResponseObject> responseObjects = getAllResponseObjects(responseContent);
        System.out.println(String.format("Latest observation with at least 5 birds in %s: \n", countryCode)
                + getJsonOfResponseObject(responseObjects.get(0)));
    }

    /**
     * Querying regions in a country
     * @param countryCode code of country. e.g. US = United States of America, DE = Germany
     * @return list of regions in country
     */
    private static List<String> queryCountry(String countryCode) {
        GeoDbApiClient apiClient = new GeoDbApiClient(GeoDbInstanceType.FREE);
        GeoDbApi geoDbApi = new GeoDbApi(apiClient);
        RegionsResponse regionsResponse = geoDbApi.findRegions(
                FindRegionsRequest.builder().countryId(countryCode).build()
        );
        List<String> regions = new ArrayList<>();
        for (RegionSummary regionSummary : regionsResponse.getData()) {
            regions.add(regionSummary.getCountryCode() + "-" + regionSummary.getIsoCode());
        }
        return regions;
    }

    /**
     * Creates instance of {@link HttpURLConnection} to get response.
     *
     * @param urlString String representation of URL to query
     * @param apiToken  apiToken of eBird API
     * @return instance of {@link HttpURLConnection}
     */
    private static HttpURLConnection buildBirdConnection(String urlString, String apiToken) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("X-eBirdApiToken", apiToken);
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setConnectTimeout(15000); // 5000 milliseconds = 5 seconds
            conn.setReadTimeout(15000);
            return conn;
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets response status of {@link HttpURLConnection}
     *
     * @param connection instance of {@link HttpURLConnection}
     * @return HTTP status code
     */
    private static int getResponseStatus(HttpURLConnection connection) {
        try {
            return connection.getResponseCode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets content of response when calling eBird API via HTTP-GET
     *
     * @return response of eBird API
     */
    private static String getResponseContent(HttpURLConnection connection) {
        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();
        try {
            int responseCode = getResponseStatus(connection);
            if (responseCode >= 300) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }
            if (reader != null) {
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            }
            log.info("HTTP response code: " + responseCode);
            return responseContent.toString();
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Converts webserver response of querying birds into a list of {@link ResponseObject}.
     * @param responseContent json string response of webserver
     * @return {@link List} of {@link ResponseObject}
     */
    private static List<ResponseObject> getAllResponseObjects(String responseContent) {
        List<ResponseObject> responseObjectList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(responseContent);
        Gson gson = new GsonBuilder().create();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            ResponseObject responseObject = gson.fromJson(String.valueOf(jsonObject), ResponseObject.class);
            if (responseObject.howMany >= 5) {
                responseObjectList.add(responseObject);
            }
        }
        return responseObjectList;
    }

    /**
     * Gets JSON-String representation of an instance of {@link ResponseObject}.
     * @param responseObject responseObject to represent as JSON
     * @return JSON representation of responseObject
     */
    private static String getJsonOfResponseObject(ResponseObject responseObject) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(responseObject);
    }
}
