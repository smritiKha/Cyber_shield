package com.cybershield;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:cybershieldtest;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class ApiControllerTest {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper mapper;
 @Test void signupLoginAndProgress() throws Exception {
  String email="test"+System.nanoTime()+"@example.com";
  String signup=mapper.writeValueAsString(new ApiController.Signup(email,"Test Learner","correct horse battery staple 99"));
  String response=mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(signup))
    .andExpect(status().isOk()).andExpect(jsonPath("$.account.email").value(email))
    .andReturn().getResponse().getContentAsString();
  String token=mapper.readTree(response).path("token").asText();
  mvc.perform(get("/api/progress").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.progress").value(org.hamcrest.Matchers.nullValue()));
  mvc.perform(put("/api/progress").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON)
    .content("{\"progress\":{\"completedLessons\":[\"phishing\"],\"correctAnswers\":1,\"attemptedAnswers\":1}}"))
    .andExpect(status().isOk()).andExpect(jsonPath("$.progress.completedLessons[0]").value("phishing"));
  mvc.perform(get("/api/progress").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.progress.correctAnswers").value(1));
  mvc.perform(get("/api/progress")).andExpect(status().isUnauthorized());
  mvc.perform(post("/api/auth/logout").header("Authorization","Bearer "+token)).andExpect(status().isNoContent());
  mvc.perform(get("/api/progress").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
 }
}
