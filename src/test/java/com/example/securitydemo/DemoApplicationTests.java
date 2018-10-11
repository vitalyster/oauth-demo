package com.example.securitydemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DemoApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
	@Test
	public void contextLoads() throws Exception {
		mockMvc.perform(get("/"))
                .andExpect(status().isOk());
		mockMvc.perform(get("/me"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("http://localhost/login"));
		mockMvc.perform(post("/login")
                .param("username", "user")
                .param("password", "secret")
                .with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
	}
	@WithMockUser(username = "user")
    @Test
    public void logoutTest() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
	    mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
    @Test
    public void apiTest() throws Exception {
        String profile = "/api/me";
        mockMvc.perform(get(profile))
                .andExpect(status().isUnauthorized());
        MvcResult result = mockMvc.perform(post("/api/oauth/token")
                .with(httpBasic("client", "secret"))
                .param("username", "user")
                .param("password", "secret")
                .param("scope", "all")
                .param("grant_type", "password")).andExpect(status().isOk()).andReturn();
        JacksonJsonParser jsonParser = new JacksonJsonParser();
        String token = jsonParser.parseMap(result.getResponse().getContentAsString()).get("access_token").toString();
        mockMvc.perform(get(profile)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

}
