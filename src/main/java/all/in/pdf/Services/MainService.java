package all.in.pdf.Services;

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

    HttpSession session;

    @Autowired
    FileLocalUtils fileLocalUtils;

    public String addToSession(MultipartFile file, HttpSession s) throws IOException,NotAcceptableStatusException {
        session = s;
        initSession(session);
        String key = "file_" + session.getAttribute(FILE_INDEX);
        String pathFile = fileLocalUtils.saveToDisk(file);
        String extension = fileLocalUtils.getExtension(file.getOriginalFilename());
        validerExtension(extension);
        Map<String, String> fileData = new HashMap<>();
        fileData.put("file",pathFile);
        fileData.put("ext", extension);
        session.setAttribute(key,fileData);
        increaseFileIndexSession();

        return key;
    }

    public Map<String, String> getFilesFromSession(@NotNull HttpSession session){
        Map<String, String> files = new HashMap<>();
        Enumeration<String> en = session.getAttributeNames();
        while (en.hasMoreElements()){
            String name = en.nextElement();
            if (name.startsWith("file")) {
                Map<String, String> file = (Map<String, String>) session.getAttribute(name);
                files.put(name, file.get("file"));
            }
        }

        return files;
    }

    private void initSession(HttpSession session){
        if(session.getAttribute(FILE_INDEX) == null){
            session.setAttribute(FILE_INDEX, 0);
        }
    }

    private void increaseFileIndexSession(){
        int index = (int) session.getAttribute(FILE_INDEX);
        int newValut = index + 1;
        session.setAttribute(FILE_INDEX, newValut);
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

    public String concatPdfs(HttpSession s) throws Exception {
        session = s;
        Enumeration<String> names = session.getAttributeNames();
        List<byte[]> FileList = new ArrayList<>();

        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name.startsWith("file")) {
                Map<String, String> file = (Map<String, String>) session.getAttribute(name);
                if (IMAGE_EXTENSIONS.contains(file.get("ext"))) {
                    FileList.add(FileLocalUtils.imageToPdf(getDocumentFromTempFile(file.get("file"))).toByteArray());
                }else if (PDF_EXTENSIONS.contains(file.get("ext")))  {
                    File f = new File(file.get("file"));
                    boolean exist = f.exists();
                    byte[] test = FileUtils.readFileToByteArray(f);
                    FileList.add(test);
                }
            }
            session.removeAttribute(name);
            session.removeAttribute(FILE_INDEX);


        }
        return FileLocalUtils.pdfsTopdf(FileList);

    }

    protected byte[] getDocumentFromTempFile(String path) throws IOException{
        if (path != null) {
            return FileLocalUtils.getImagefromTempFile(path);
        } else {
            throw new IOException("Temp file path unfound for $path");
        }
    }

    public String deleteFile(String code, HttpSession s){
        try {
            session = s;
            session.removeAttribute(code);
            return "ok";

        }catch (Exception e){
            return e.getMessage();
        }

    }

    public String deleteAllAttribute(HttpSession s){
        session = s;
        try {
            Enumeration<String> names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                session.removeAttribute(name);

            }
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

}
