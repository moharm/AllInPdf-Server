package all.in.pdf.Services;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileLocalUtils {

    public static final char EXTENSION_SEPARATOR = '.';


    public String saveToDisk(MultipartFile file) throws IOException {

        String extension = getExtension(file.getOriginalFilename());
        File temp = File.createTempFile("tmpPJ_", '.' + extension);
        temp.deleteOnExit();

        file.transferTo(temp);

        return temp.getPath();
    }

    public String getExtension(String filename){
        if (filename == null) {
            return null;
        }
        int index = filename.lastIndexOf(EXTENSION_SEPARATOR);
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

    static byte[] getImagefromTempFile(String path) throws IOException {
        File file = new File(path);
        return FileUtils.readFileToByteArray(file);
    }

    static Document createDoc(OutputStream pdf) throws DocumentException {
        Document document = new Document();
        document.setMargins(0, 0, 0, 0);
        PdfWriter writer = PdfWriter.getInstance(document, pdf);
        writer.setStrictImageSequence(true);
        return document;
    }

    static ByteArrayOutputStream imageToPdf(byte[] image) throws Exception {
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        Document document = createDoc(pdf);

        document.open();

        Image pdfImage = Image.getInstance(image);
        Rectangle pageSize = new Rectangle(pdfImage.getWidth(), pdfImage.getHeight());
        document.setPageSize(pageSize);
        document.newPage();
        document.add(pdfImage);

        document.close();

        return pdf;
    }

    static String pdfsTopdf(List<byte[]> pdfs) throws IOException {

        List<InputStream> InputStreams = new ArrayList<>();

        pdfs.stream().forEach(pdf ->{
            InputStream myInputStream = new ByteArrayInputStream(pdf);
            InputStreams.add(myInputStream);
        });

        PDFMergerUtility PDFmerger = new PDFMergerUtility();
        PDFmerger.addSources(InputStreams);

        File tempFile = File.createTempFile("tmpFile", ".pdf");
        tempFile.deleteOnExit();
        String path = tempFile.getPath();

        PDFmerger.setDestinationFileName(path);
        PDFmerger.mergeDocuments();

        return path;
    }
}
