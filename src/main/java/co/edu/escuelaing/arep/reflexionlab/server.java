package co.edu.escuelaing.arep.reflexionlab;

import java.io.IOException;

public class server {
    public static void main(String[] args) throws IOException {
        Framework.loadComponents(args);
        Framework.start();
    }
}
