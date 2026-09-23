package com.dongnguyen248.add2num.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dongnguyen248.add2num.web.Add2NumWebApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = Add2NumWebApplication.class)
@AutoConfigureMockMvc
class AdditionApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsAdditionJobAndExposesItsStatus() throws Exception {
        String response = mockMvc.perform(post("/api/additions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstNumber\":\"1234\",\"secondNumber\":\"897\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String jobId = response.replaceFirst(".*\"jobId\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/additions/{jobId}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(jobId));
    }

    @Test
    void rejectsInvalidRequests() throws Exception {
        mockMvc.perform(post("/api/additions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstNumber\":\"12x\",\"secondNumber\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstNumber").isNotEmpty())
                .andExpect(jsonPath("$.secondNumber").isNotEmpty());
    }

    @Test
    void returnsNotFoundForUnknownJobs() throws Exception {
        mockMvc.perform(get("/api/additions/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Addition job not found."));
    }

    @Test
    void rendersTheApplicationPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }
}