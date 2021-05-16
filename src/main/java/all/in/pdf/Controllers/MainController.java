package all.in.pdf.Controllers;


import all.in.pdf.Services.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.NotAcceptableStatusException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@RestController
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
public class MainController {

    private static final String DIRECTORY = "C:/Users/user/Desktop/MAIFP/tests";
    private static final String DEFAULT_FILE_NAME = "test.pdf";

    @Autowired
    MainService mainService;
    @Autowired
    ServletContext servletContext;

    @PostMapping(value = "upload")
    public String uploadFile(@RequestBody MultipartFile file, HttpSession session) throws IOException {
        try {
            return mainService.addToSession(file, session);
        }catch(NotAcceptableStatusException ignored){
            return ignored.getMessage();
        }
    }

    @GetMapping(value = "files")
    public Map<String, String> getFilesFromSession(HttpSession session){
        return mainService.getFilesFromSession(session);
    }

    @GetMapping(value = "concat")
    public ResponseEntity<InputStreamResource> downloadFile(HttpSession session) throws Exception {
        String path = mainService.concatPdfs(session);
        File file = new File(path);
        MediaType mediaType = MainService.getMediaTypeForFileName(this.servletContext, file.getName());

        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(mediaType);
        respHeaders.setContentLength(file.length());
        respHeaders.setContentDispositionFormData("attachment", file.getName());
        InputStreamResource isr = new InputStreamResource(new FileInputStream(file));
        return new ResponseEntity(isr, respHeaders, HttpStatus.OK);
    }

    @DeleteMapping
    public String deleteFile(String code, HttpSession session){

        return mainService.deleteFile(code, session);
    }


}
