package all.in.pdf.Controllers;


import all.in.pdf.Security.CurrentUser;
import all.in.pdf.Services.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.NotAcceptableStatusException;
import all.in.pdf.Security.UserPrincipal;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@RestController
@PreAuthorize("hasRole('USER')")
@CrossOrigin(origins = "http://localhost:3000")
public class MainController {

    @Autowired
    MainService mainService;
    @Autowired
    ServletContext servletContext;

    @PostMapping(value = "upload")
    public String uploadFile(@RequestBody MultipartFile file) throws IOException {
        try {
            return mainService.addToSession(file);
        }catch(NotAcceptableStatusException ignored){
            return ignored.getMessage();
        }
    }

//    @GetMapping(value = "files")
//    public Map<String, String> getFilesFromSession(@CurrentUser UserPrincipal userPrincipal){
//        return mainService.getFilesFromSession(userPrincipal);
//    }

    @GetMapping(value = "init")
    public String initFiles(){
        return mainService.initFiles();
    }

    @GetMapping(value = "concat")
    public ResponseEntity<InputStreamResource> downloadFile() throws Exception {
        String path = mainService.concatPdfs();
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
    public String deleteFile(String code){

        return mainService.deleteFile(code);
    }


}
