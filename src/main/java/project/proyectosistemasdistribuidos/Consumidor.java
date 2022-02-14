package project.proyectosistemasdistribuidos;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Consumidor {
    public static void main(String[] args)
            throws IOException, NoSuchAlgorithmException, TimeoutException, ParseException,
            org.json.simple.parser.ParseException {

        ConnectionFactory factory = new ConnectionFactory();

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare("myRabbitQueue", false, false, false, null);

        channel.basicConsume("myRabbitQueue", true, (consumerTag, message) -> {

            String m = new String(message.getBody(), "UTF-8");

            JSONObject jsonMessage = (JSONObject) JSONValue.parse(m);
            JSONObject jsonDictionary = (JSONObject) JSONValue.parse(new FileReader("diccionario.json"));
            org.json.simple.JSONObject wordsMessage = (org.json.simple.JSONObject) jsonMessage.get("Frecuencia");
            org.json.simple.JSONObject wordsDicc = (org.json.simple.JSONObject) jsonDictionary.get("Frecuencia");
            org.json.simple.JSONArray jsonArray = (org.json.simple.JSONArray) jsonDictionary.get("Hashes");

            if (!jsonArray.contains(jsonMessage.get("Hash"))) {

                System.out.println("Escribiendo nuevo diccionario...");
                
                jsonArray.add(jsonMessage.get("Hash"));

                

                for (Object word : wordsMessage.keySet()) {
                    if (!wordsDicc.keySet().contains(word)) {

                        HashMap<String, String> new_word = new HashMap();
                        new_word.put(word.toString(), wordsMessage.get(word).toString());
                        wordsDicc.putAll(new_word);

                    } else {

                        wordsDicc.replace(word, wordsDicc.get(word), (Object) (Integer.parseInt(wordsDicc.get(word).toString()) + Integer.parseInt(wordsMessage.get(word).toString()))  );

                    }
                }

                FileWriter fileWriter = new FileWriter("diccionario.json");
                fileWriter.write(jsonDictionary.toJSONString());
                fileWriter.flush();
                fileWriter.close();
                System.out.println("Diccionario escrito exitosamente (" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ").");

            }
            System.out.println("Numero de archivos procesados >> " + jsonArray.size());

        }, consumerTag -> {
        });

    }
}
