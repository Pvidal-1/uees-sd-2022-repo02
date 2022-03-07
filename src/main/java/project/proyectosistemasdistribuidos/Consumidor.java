package project.proyectosistemasdistribuidos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import com.rabbitmq.client.Delivery;
import com.rabbitmq.client.AMQP.Queue;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import java.util.concurrent.TimeUnit;

public class Consumidor {
    int id;
    String mensajeConsumidor;

    public Consumidor(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public void setMensajeConsumidor(String mensajeConsumidor) {
        this.mensajeConsumidor = mensajeConsumidor;
    }

    public String getMensajeConsumidor() {
        return mensajeConsumidor;
    }

    public static void writeToDictionary(String m, int id) throws IOException, NoSuchAlgorithmException,
            TimeoutException, ParseException, org.json.simple.parser.ParseException {

        JSONObject jsonMessage = (JSONObject) JSONValue.parse(m);
        JSONObject jsonDictionary = (JSONObject) JSONValue.parse(new FileReader("diccionario" + id + ".json"));
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
                    wordsDicc.replace(word, wordsDicc.get(word),
                            (Object) (Integer.parseInt(wordsDicc.get(word).toString())
                                    + Integer.parseInt(wordsMessage.get(word).toString())));
                }
            }

            FileWriter fileWriter = new FileWriter("diccionario" + id + ".json");
            fileWriter.write(jsonDictionary.toJSONString());
            fileWriter.flush();
            fileWriter.close();
            System.out.println("Diccionario escrito exitosamente ("
                    + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ").");
            System.out.println("Numero de archivos procesados >> " + jsonArray.size());

        }

    }

    public static void main(String[] args)
            throws IOException, NoSuchAlgorithmException, TimeoutException, ParseException,
            org.json.simple.parser.ParseException {
        ConnectionFactory factory = new ConnectionFactory();
        // factory.setHost("181.199.42.240");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare("myRabbitQueue", false, false, false, null);
        Queue.DeclareOk response = channel.queueDeclarePassive("myRabbitQueue");
        Consumidor consumer = new Consumidor(response.getConsumerCount());
        System.out.println("ID: " + consumer.getID());
      
        channel.basicConsume("myRabbitQueue", true, (consumerTag, message) -> {
            
            if (consumer.getID() == 1) {

                ///////////////////// SERVER 1 ///////////////
                ServerSocket server;
                InputStream is;
                Socket client;

                try {
                    server = new ServerSocket(9999);

                    client = server.accept();
                    System.out.println("New connection from " + client.getRemoteSocketAddress());

                    is = client.getInputStream();
                    InputStreamReader in = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(in);
                    String mess = br.readLine();
                    System.out.println(">>> MENSAJE RECIBIDO id1: ("
                            + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ").");
                    System.out.println("");
                    Consumidor.writeToDictionary(mess, consumer.getID());
                    client.close(); // close connection to client

                    server.close(); // (*@\serverBox{5)}@*)
                } catch (Throwable t) {
                    t.printStackTrace();
                }

            }

            String m = new String(message.getBody(), "UTF-8");
            consumer.setMensajeConsumidor(m);

            try {
                Consumidor.writeToDictionary(m, consumer.getID());
            } catch (NoSuchAlgorithmException | TimeoutException | ParseException
                    | org.json.simple.parser.ParseException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            try {
                TimeUnit.MILLISECONDS.sleep(Math.round(Math.floor((Math.random() * 3) + 3)) * 1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (consumer.getID() == 0) {

                ////////////////////// CLIENT 0 ///////////////////////////
                Socket client;
                OutputStream os;
                InetAddress ia;

                try {
                    ia = InetAddress.getByName("localhost");// get local host address

                    client = new Socket(ia, 9999); // create socket (*@\clientBox{1+2)}@*)

                    os = client.getOutputStream(); // get stream to write to
                    os.write(consumer.getMensajeConsumidor().getBytes()); // write one byte of value 1
                    // (*@\clientBox{3)}@*)
                    System.out.println("");
                    System.out.println(">>> MENSAJE ENVIADO id0: ("
                            + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ").");

                    client.close(); // close (*@\clientBox{4)}@*)
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                ///////////////////// SERVER 0 ///////////////
                ServerSocket server;
                InputStream is;

                try {
                    server = new ServerSocket(9998);

                    client = server.accept();
                    System.out.println("New connection from " + client.getRemoteSocketAddress());

                    is = client.getInputStream();
                    InputStreamReader in = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(in);
                    String mess = br.readLine();
                    System.out.println(">>> MENSAJE RECIBIDO id0: ("
                            + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ").");
                    System.out.println("");
                    Consumidor.writeToDictionary(mess, consumer.getID());
                    client.close(); // close connection to client

                    server.close(); // (*@\serverBox{5)}@*)
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else if (consumer.getID() == 1) {

                ////////////////////// CLIENT 1 ///////////////////////////
                Socket client;
                OutputStream os;
                InetAddress ia;

                try {
                    ia = InetAddress.getByName("localhost");// get local host address

                    client = new Socket(ia, 9998); // create socket (*@\clientBox{1+2)}@*)

                    os = client.getOutputStream(); // get stream to write to
                    os.write(consumer.getMensajeConsumidor().getBytes()); // write one byte of value 1
                    // (*@\clientBox{3)}@*)
                    System.out.println("");
                    System.out.println(">>> MENSAJE ENVIADO id1: ("
                            + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ").");

                    client.close(); // close (*@\clientBox{4)}@*)
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

        }, consumerTag -> {
        });

    }

}
