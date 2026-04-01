package producer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

public class Main {
    public static void main(String[] args) {
        String apiUrl = "https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT";

        try {
            URI uri = URI.create(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            System.out.println("HTTP response code: " + responseCode);

            if (responseCode != 200) {
                throw new RuntimeException("Failed to call API. HTTP code: " + responseCode);
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            connection.disconnect();

            System.out.println("API response:");
            System.out.println(response);

        } catch (Exception e) {
            System.err.println("Error while calling Binance API:");
            e.printStackTrace();
        }
    }
}
