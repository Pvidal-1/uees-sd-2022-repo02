package project.proyectosistemasdistribuidos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.util.concurrent.TimeoutException;
import org.json.simple.JSONObject;

public class Productor {

    String file;

    public Productor(String file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "Productor{" + "file=" + file + '}';
    }

    public String readFile(File file) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder stringBuilder = new StringBuilder();
        char[] buffer = new char[10];
        while (reader.read(buffer) != -1) {
            stringBuilder.append(new String(buffer));
            buffer = new char[10];
        }
        reader.close();

        String content = stringBuilder.toString();
        return content;
    }

    public ArrayList<JSONObject> listFiles(String path, List<String> filesList) throws NoSuchAlgorithmException, IOException {

        ArrayList<String> hashes = new ArrayList<>();
        ArrayList<JSONObject> arrayJSON = new ArrayList<>();
        File folder = new File(path);
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".txt")) {

                System.out.println("======================================================");

                String fileHash = hashFile(file);
                System.out.println("Analizando " + file.getName() + ".");
                System.out.println("Hash de este archivo: " + fileHash);

                if (!hashes.contains(fileHash)) {

                    //System.out.println("Analizando nuevo archivo.");

                    String[] words = readFile(file).replace("[\\n|\\t|\\r|,|.|:|;|!|#|'|?|$|%|&|/|)|(|=|¿|¡|1|2|3|4|5|6|7|8|9|0|\\u0000]", "").split(" ");
                    hashes.add(fileHash);

                    FileWriter createdJSONFile = new FileWriter(path + "/" + file.getName().replace(".txt", "") + ".json");
                    HashMap<String, HashMap<String, String>> mapMap = new HashMap<>();
                    HashMap<String, String> mapHash = new HashMap<>();
                    HashMap<String, String> mapCounter = new HashMap<>();


                    mapHash.put("Hash", fileHash);

                    for (String word : words) {

                        if (!mapCounter.containsKey(word)) {
                            mapCounter.put(word, "1");

                        } else {
                            mapCounter.put(word, String.valueOf(Integer.parseInt(mapCounter.get(word)) + 1));
                        }
                    }

                    mapMap.put("Frecuencia", mapCounter);
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.putAll(mapHash);
                    jsonObj.putAll(mapMap);
                    arrayJSON.add(jsonObj);
                    
                    try {

                        FileWriter fileWriter = new FileWriter(path + "/" + file.getName().replace(".txt", "") + ".json");
                        System.out.println("Escribiendo objeto JSON al archivo...");
                        //System.out.print(jsonObj);

                        fileWriter.write(jsonObj.toJSONString());
                        fileWriter.flush();
                        fileWriter.close();
                        System.out.println("Objeto escrito exitosamente.");

                    } catch (IOException e) {
                        System.out.println("ERROR:");
                        e.printStackTrace();
                    }

                }

                filesList.add(file.getName());

            } else if (file.isDirectory()) {
                ArrayList<JSONObject> temp = listFiles(file.getAbsolutePath(), filesList);

                for(JSONObject json : temp){
                    arrayJSON.add(json);
                }
                

            }
        }
        return arrayJSON;

    }

    public static String hashFile(File file) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        FileInputStream fis = new FileInputStream(file);
        byte[] dataBytes = new byte[1024];

        int nread = 0;
        while ((nread = fis.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, nread);
        }

        byte[] mdbytes = md.digest();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mdbytes.length; i++) {
            sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        fis.close();
        return sb.toString();
    } 
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, TimeoutException {
    
        Productor p = new Productor(args[0]);
        List<String> filesList = new ArrayList<>();
        ArrayList<JSONObject> arrayJSON  = p.listFiles(args[0], filesList);
        
        ConnectionFactory factory = new ConnectionFactory();
        //factory.setHost("181.199.42.240");
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
