package all.in.pdf.Services;

import all.in.pdf.Security.UserPrincipal;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.NotAcceptableStatusException;

import java.io.File;
import java.io.IOException;
import java.util.*;


@Service
public class MainService {

    public static String FILE_INDEX = "FILE_INDEX";
    public static List<String> IMAGE_EXTENSIONS = Arrays.asList("jpg","png","jpeg","JPG","JPEG","PNG");
    public static List<String> PDF_EXTENSIONS = Arrays.asList("pdf", "PDF");
    public static int indexFile = 0;
    public static Map<String, Map<String, String>> filesData = new HashMap<>();

    @Autowired
    FileLocalUtils fileLocalUtils;

    public String addToSession(MultipartFile file) throws IOException,NotAcceptableStatusException {

        String key = "file_" + indexFile;
        String pathFile = fileLocalUtils.saveToDisk(file);
        String extension = fileLocalUtils.getExtension(file.getOriginalFilename());
        validerExtension(extension);
        Map<String, String> fileData = new HashMap<>();
        fileData.put("file",pathFile);
        fileData.put("ext", extension);
        filesData.put(key,fileData);
        increaseFileIndexSession();

        return key;
    }

//    public Map<String, String> getFilesFromSession(UserPrincipal userPrincipal){
//        Map<String, String> files = new HashMap<>();
//        Enumeration<String> en = session.getAttributeNames();
//        while (en.hasMoreElements()){
//            String name = en.nextElement();
//            if (name.startsWith("file")) {
//                Map<String, String> file = (Map<String, String>) userPrincipal.fileData;
//                files.put(name, file.get("file"));
//            }
//        }
//
//        return files;
//    }


    private static void increaseFileIndexSession(){
        indexFile += 1;
    }


    public static MediaType getMediaTypeForFileName(ServletContext servletContext, String fileName) {

        String mineType = servletContext.getMimeType(fileName);
        try {
            MediaType mediaType = MediaType.parseMediaType(mineType);
            return mediaType;
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    public static String concatPdfs() throws Exception {
        List<byte[]> FileList = new ArrayList<>();
        Map<String, Map<String, String>> data = filesData;
        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            //String name = entry.getKey();
            Map<String, String> file = entry.getValue();
            if (IMAGE_EXTENSIONS.contains(file.get("ext"))) {
                byte[] image = getDocumentFromTempFile(file.get("file"));
                FileList.add(FileLocalUtils.imageToPdf(image).toByteArray());
            } else if (PDF_EXTENSIONS.contains(file.get("ext"))) {
                File f = new File(file.get("file"));
                byte[] test = FileUtils.readFileToByteArray(f);
                FileList.add(test);
            }

        }
        filesData =new HashMap<>();
        indexFile = 0;

        return FileLocalUtils.pdfsTopdf(FileList);

    }

    protected static byte[] getDocumentFromTempFile(String path) throws IOException{
        if (path != null) {
            return FileLocalUtils.getImagefromTempFile(path);
        } else {
            throw new IOException("Temp file path unfound for $path");
        }
    }

    public String deleteFile(String code){
        try {
            filesData.remove(code);
            return "ok";

        }catch (Exception e){
            return e.getMessage();
        }

    }

    public static String deleteAllAttribute(HttpSession s){

        try {
            filesData = new HashMap<>();
            return "ok";
        }catch (Exception e){
            return e.getMessage();
        }
    }

    private void validerExtension(String extension) throws NotAcceptableStatusException{
        if(!IMAGE_EXTENSIONS.contains(extension) && !PDF_EXTENSIONS.contains(extension)){
            throw new NotAcceptableStatusException("this Extension is not acceptable!");
        }
    }

    public static String initFiles() {
        indexFile = 0;
        return "success";
    }
}
