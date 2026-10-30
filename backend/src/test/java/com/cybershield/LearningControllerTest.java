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
}
