package producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {

        String topic = "binance-price";
        String bootstrapServers = "192.168.100.35:9092";
        String apiUrl = "https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT";

        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        try {
            while (true) {
                String message = fetchApiResponse(apiUrl);

                System.out.println(LocalDateTime.now() + " Sending to Kafka: " + message);

                ProducerRecord<String, String> record =
                        new ProducerRecord<>(topic, "BTC", message);


                  producer.send(record, (metadata, exception) -> {
                      if (exception == null) {
                          System.out.println("Sent to topic " + metadata.topic() +
                              " partition " + metadata.partition() +
                              " offset " + metadata.offset());
                       } else {
                        System.err.println("Kafka send failed:");
                        exception.printStackTrace();
                       }
                  });

                producer.flush();

                Thread.sleep(5000); // wait 5 seconds
            }
        } catch (Exception e) {
            System.err.println("Application error:");
            e.printStackTrace();
        } finally {
            producer.close();
        }
    }

    private static String fetchApiResponse(String apiUrl) throws Exception {
        URI uri = URI.create(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();

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

        return response.toString();
    }
}
