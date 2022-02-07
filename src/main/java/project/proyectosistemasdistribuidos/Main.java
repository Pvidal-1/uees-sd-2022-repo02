package project.proyectosistemasdistribuidos;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.json.simple.JSONObject;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;


public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, TimeoutException {

        Productor p = new Productor("Archivos");
        List<String> filesList = new ArrayList<>();
        ArrayList<JSONObject> arrayJSON  = p.listFiles("Archivos", filesList);
        
        
        ConnectionFactory factory = new ConnectionFactory();

        try (Connection connection = factory.newConnection()){
            Channel channel = connection.createChannel();
            channel.queueDeclare("myRabbitQueue", false, false, false, null);

            for(JSONObject file: arrayJSON){
                channel.basicPublish("", "myRabbitQueue", false, null, file.toJSONString().getBytes());
            }

            System.out.println("Mensaje enviado :)");
        }
    }
}
