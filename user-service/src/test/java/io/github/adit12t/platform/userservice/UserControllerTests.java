package io.github.adit12t.platform.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"eureka.client.enabled=false", "spring.cloud.config.enabled=false", "management.tracing.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:users;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa", "spring.datasource.password="})
@AutoConfigureMockMvc
class UserControllerTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void returnsUsersWithHypermediaLinks() throws Exception {
        mvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ada"))
                .andExpect(jsonPath("$._links.self.href").value(endsWith("/api/v1/users/1")))
                .andExpect(jsonPath("$._links.posts.href").value(endsWith("/api/v1/users/1/posts")));
    }

    @Test
    void createsUsersAndPostsAndValidatesInput() throws Exception {
        mvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Grace\",\"birthDate\":\"1992-03-04\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
        mvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"birthDate\":\"2999-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.birthDate").exists());
        mvc.perform(post("/api/v1/users/2/posts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"First post\"}"))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/v1/users/2/posts"))
                .andExpect(jsonPath("$._embedded.postViewList[0].description").value("First post"));
    }

    @Test
    void deletingAUserRemovesTheirPosts() throws Exception {
        String location = mvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Temp\",\"birthDate\":\"2000-01-01\"}"))
                .andReturn().getResponse().getHeader("Location");
        mvc.perform(post(location + "/posts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"to be removed\"}"))
                .andExpect(status().isCreated());

        mvc.perform(delete(location)).andExpect(status().isNoContent());
        mvc.perform(get(location)).andExpect(status().isNotFound());
    }
}
