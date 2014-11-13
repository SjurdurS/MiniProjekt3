package miniprojekt3;

import java.io.*;

public class SerializeDemo {

    public static void main(String[] args) {
        GetRequest gr = new GetRequest(10, "Test");
        gr.key = 1026;
        gr.value = "This is a get request";
        try {
            FileOutputStream fileOut
                    = new FileOutputStream("getRequest.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(gr);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in getRequest.ser");
        } catch (IOException i) {
            i.printStackTrace();
        }
        
        PutRequest pr = new PutRequest(10, "Test");
        pr.key = 1025;
        pr.ip = "localhost";
        pr.port = 717;
        try {
            FileOutputStream fileOut
                    = new FileOutputStream("putRequest.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(pr);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in putRequest.ser");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
}
