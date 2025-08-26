package co.edu.escuelaing.arep.reflexionlab.controller;


import co.edu.escuelaing.arep.reflexionlab.Anotations.GetMapping;
import co.edu.escuelaing.arep.reflexionlab.Anotations.RequestParam;
import co.edu.escuelaing.arep.reflexionlab.Anotations.RestController;

import java.io.IOException;
import java.nio.file.Paths;

import static java.nio.file.Files.readAllBytes;

@RestController
public class Controller {
    @GetMapping("/hello")
    public String Hello(@RequestParam(value = "name", defaultValue = "word") String name){
        return "hello " + name;
    }

    @GetMapping("/pi")
    public String pi(){
        return "3,141516";
    }

    @GetMapping("/files")
    public String getFiles(@RequestParam("name") String fileName) {
        String basePath = "src/main/resources/public/";
        String filePath = basePath + fileName;

        try {
            byte[] fileBytes = readAllBytes(Paths.get(filePath));
            return new String(fileBytes);
        } catch (IOException e) {
            return "Error al leer el archivo: " + fileName;
        }
    }

}
