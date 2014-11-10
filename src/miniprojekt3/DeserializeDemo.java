package miniprojekt3;

import java.io.*;

public class DeserializeDemo {

    public static void main(String[] args) {
        GetRequest gr = null;
        try {
            FileInputStream fileIn = new FileInputStream("getRequest.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            gr = (GetRequest) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("GetRequest class not found");
            c.printStackTrace();
            return;
        }
        System.out.println("Deserialized GetRequest...");
        System.out.println("Key: " + gr.key);
        System.out.println("Value: " + gr.value);

        
        PutRequest pr = null;
        try {
            FileInputStream fileIn = new FileInputStream("PutRequest.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            pr = (PutRequest) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("PutRequest class not found");
            c.printStackTrace();
            return;
        }
        System.out.println("Deserialized PutRequest...");
        System.out.println("Key: " + pr.key);
        System.out.println("IP: " + pr.ip);
        System.out.println("Port: " + pr.port);
    }
}
