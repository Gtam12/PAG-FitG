package com.fitg.auth.controller;


import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.fitg.TestcontainersConfiguration;
import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthControllerTest {

    @Autowired
     MockMvc mvc;


    private static String uniqueEmail() {
        return "u" + UUID.randomUUID() + "@example.com";
    }



    private ResultActions register(String email, String password) throws Exception{

        return mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s","password":"%s","displayName":"Test"}
                        """.formatted(email, password))
        );
    }

    private String registerAndGetResponse(String email) throws Exception {
        return register(email, "password123")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }



    private ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password))
        );
    }


    private ResultActions me(String jwt) throws Exception {
        String token = "Bearer " + jwt;
        return mvc.perform(get("/api/me")
                .header("Authorization", token)
        );

    }


    @Test
    void register_validBody_returns201WithToken() throws Exception {
        register(uniqueEmail(), "password123")
                .andExpect(status().isCreated())
                .andExpect( jsonPath("$.token").isNotEmpty())
                .andExpect( jsonPath("$.expiresAt").exists());
    }

    @Test
    void register_duplicateEmailAnyCase_returns409() throws Exception {
        String email = uniqueEmail();
        register(email, "password").andExpect(status().isCreated());
        register(email.toUpperCase(Locale.ROOT), "Pass2345")
                .andExpect(status().isConflict());
    }

    @Test
    void login_wrongPasswordAndUnknownEmail_returnsSame401() throws Exception{
        String email = uniqueEmail();
        registerAndGetResponse(email);

        String wrongPw = login(email, "wrongpass1")
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        String unknown = login(uniqueEmail(), "wrongpass1")
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertThat(unknown).isEqualTo(wrongPw);
    }

    @Test
    void register_invalidBody_returns400() throws Exception {
        register("abc", "1")
                .andExpect(status().isBadRequest());
    }

    @Test
    void me_withToken_returnsProfileWithoutPasswordHash() throws Exception {
        String email = uniqueEmail();
        String response = registerAndGetResponse(email);
        String token = JsonPath.read(response, "$.token");


            me(token).andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(email))
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }



    @Test
    void me_withoutToken_returns401() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }











}
