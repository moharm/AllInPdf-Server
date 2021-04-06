package all.in.pdf.Controllers;


import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


@SpringBootTest
@AutoConfigureMockMvc
public class testMainController {

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    MainController mainController;

    @Test
    public void controllerLoads()throws Exception{
        assertThat(mainController).isNotNull();
    }

    @Test
    public void uploadfile_ShouldReturnTheNameOfTheFile() throws Exception{
        MockMultipartFile file
                = new MockMultipartFile(
                "file",
                "image.jpg",
                MediaType.TEXT_PLAIN_VALUE,
                FileUtils.readFileToByteArray(new File("C:/Users/user/Desktop/MAIFP/tests/image.jpg"))
        );
        this.mockMvc.perform(multipart("/upload").file(file)).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("file_0")));

    }

    @Test
    public void getFiles_ShouldReturnListOfStrings() throws Exception {
        MockMultipartFile file
                = new MockMultipartFile(
                "file",
                "image.jpg",
                MediaType.TEXT_PLAIN_VALUE,
                FileUtils.readFileToByteArray(new File("C:/Users/user/Desktop/MAIFP/tests/image.jpg"))
        );
        MockHttpSession session = new MockHttpSession();
        this.mockMvc.perform(multipart("/upload").file(file).session(session));
        this.mockMvc.perform(multipart("/upload").file(file).session(session));

        this.mockMvc.perform(get("/files").session(session)).andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    public void downloadFile_SouldReturnfileconcat() throws Exception {

        MockMultipartFile file
                = new MockMultipartFile(
                "file",
                "image.jpg",
                MediaType.TEXT_PLAIN_VALUE,
                FileUtils.readFileToByteArray(new File("C:/Users/user/Desktop/MAIFP/tests/image.jpg"))
        );
        MockHttpSession session = new MockHttpSession();
        this.mockMvc.perform(multipart("/upload").file(file).session(session));
        this.mockMvc.perform(multipart("/upload").file(file).session(session));

        MvcResult result = this.mockMvc.perform(get("/concat").session(session)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andReturn();

        File testFile= new File("C:/desktop/projects/AlInPdf/server/tests/test.pdf");
        byte[] resultFileBytes = result.getResponse().getContentAsByteArray();
        FileUtils.writeByteArrayToFile(testFile, resultFileBytes);

        assertThat(testFile.isFile());

    }

    @Test
    public void deleteFile_SouldDeleteFile() throws Exception {
        MockMultipartFile file
                = new MockMultipartFile(
                "file",
                "image.jpg",
                MediaType.TEXT_PLAIN_VALUE,
                FileUtils.readFileToByteArray(new File("C:/Users/user/Desktop/MAIFP/tests/image.jpg"))
        );
        MockHttpSession session = new MockHttpSession();
        this.mockMvc.perform(multipart("/upload").file(file).session(session));
        this.mockMvc.perform(multipart("/upload").file(file).session(session));

        this.mockMvc.perform(delete("/?code=file_0").session(session));
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            assertThat(name).isNotEqualTo("file_0");
        }
    }

}
