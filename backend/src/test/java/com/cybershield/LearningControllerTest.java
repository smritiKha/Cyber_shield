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
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:learningtest;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class LearningControllerTest {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper mapper;
 @Autowired AccountRepository accounts;
 @Test void rolesAndPublishedCatalog() throws Exception {
  String email="student"+System.nanoTime()+"@example.com";
  String response=mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
   .content(mapper.writeValueAsString(new ApiController.Signup(email,"Student Tester","correct horse battery staple 88"))))
   .andExpect(status().isOk()).andExpect(jsonPath("$.account.role").value("STUDENT"))
   .andReturn().getResponse().getContentAsString();
  String token=mapper.readTree(response).path("token").asText();
  mvc.perform(get("/api/admin/content").header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
  mvc.perform(get("/api/student/dashboard").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.completed").value(0));
  mvc.perform(get("/api/catalog")).andExpect(status().isOk());
  mvc.perform(post("/api/admin/content").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON)
   .content("{\"type\":\"COURSE\",\"title\":\"Test course\",\"summary\":\"Summary\",\"body\":{\"text\":\"Lesson\"},\"published\":true}")).andExpect(status().isForbidden());
  mvc.perform(post("/api/student/enroll/99999").header("Authorization","Bearer "+token)).andExpect(status().isNotFound());
 }
 @Test void administratorCanPublishAndStudentsCannotSeeAnswerKeys() throws Exception {
  String email="admin"+System.nanoTime()+"@example.com";
  UserAccount admin=new UserAccount(email,"Admin Test",new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(12).encode("strong test password 12345"));
  admin.role="ADMIN";accounts.save(admin);
  String login=mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
   .content(mapper.writeValueAsString(new ApiController.Credentials(email,"strong test password 12345"))))
   .andExpect(status().isOk()).andExpect(jsonPath("$.account.role").value("ADMIN"))
   .andReturn().getResponse().getContentAsString();
  String token=mapper.readTree(login).path("token").asText();
  String payload="{\"type\":\"QUIZ\",\"title\":\"Test safe quiz\",\"summary\":\"Safe practice\",\"body\":{\"questions\":[{\"question\":\"Choose the safe response\",\"options\":[\"Verify independently\",\"Share password\"],\"correct\":0,\"explanation\":\"Never share passwords\"}]},\"published\":true}";
  mvc.perform(post("/api/admin/content").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content(payload))
   .andExpect(status().isOk()).andExpect(jsonPath("$.published").value(true));
  mvc.perform(get("/api/catalog")).andExpect(status().isOk())
   .andExpect(jsonPath("$[?(@.title=='Test safe quiz')].body.questions[0].correct").doesNotExist());
  mvc.perform(get("/api/admin/analytics").header("Authorization","Bearer "+token)).andExpect(status().isOk());
  mvc.perform(get("/api/student/dashboard").header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
 }

}
