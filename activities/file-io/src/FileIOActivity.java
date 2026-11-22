import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileIOActivity {
    public static void main(String[] args) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("SD1.txt"))) {
            bw.write("File was successfully created.");
            System.out.println("SD1.txt has been created.");
        }
        catch (IOException e) {
            System.out.println("An error occurred!");
        }
    }
}