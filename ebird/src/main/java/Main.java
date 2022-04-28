import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());
    private static final String apiToken = "jru82bsubc2g";
    private static String defaultCountry = "DE";
    private static String url = "https://api.ebird.org/v2/data/obs/%s/recent";

    public static void main(String[] args) {
        boolean isRunning = true;
        while (isRunning) {
            System.out.print("Enter '-query' for querying birds. " +
                    "Enter '-regions' to get subregions of a specific country. " +
                    " Enter '-quit' to quit: ");
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            try {
                String command = input.readLine();
                switch (command.toLowerCase()) {
                    case "-query":
                        System.out.print("Please enter country code: ");
                        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
                        String countryCode = userInput.readLine();
                        countryCode = countryCode.replaceAll("\\s+", "");
                        executeBirdQuery(countryCode);
                        break;
                    case "-regions":
                        System.out.println("Working on getting regions of country");
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
        url = String.format(url, countryCode);
        HttpURLConnection connection = buildConnection(url, apiToken);
        String responseContent = getResponseContent(connection);
        List<ResponseObject> responseObjects = getAllResponseObjects(responseContent);
        System.out.println(String.format("Latest observation with at least 5 birds in %s: \n", countryCode)
                + getJsonOfResponseObject(responseObjects.get(0)));
    }

    /**
     * Creates instance of {@link HttpURLConnection} to get response.
     *
     * @param urlString String representation of URL to query
     * @param apiToken  apiToken of eBird API
     * @return instance of {@link HttpURLConnection}
     */
    private static HttpURLConnection buildConnection(String urlString, String apiToken) {
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
