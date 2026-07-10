package com.voiceanalyser.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BackendApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void testHealthEndpoint() throws Exception {
		mockMvc.perform(get("/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ok"));
	}

	@Test
	void testEvaluateEndpointValidation() throws Exception {
		// Empty request body
		mockMvc.perform(post("/evaluate")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.error").exists());
	}

	@Test
	void testEvaluateFallbackEndpoint() throws Exception {
		String validPayload = "{"
				+ "\"transcript\": \"I have 3 years of experience building Python and React applications, deploying Docker containers, and managing CI/CD pipelines.\","
				+ "\"job_description\": \"Looking for a Software Engineer with experience in Python, React, Docker, and CI/CD pipelines.\","
				+ "\"question\": \"Describe your experience.\""
				+ "}";

		mockMvc.perform(post("/evaluate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(validPayload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.final_score").exists())
				.andExpect(jsonPath("$.grade").exists())
				.andExpect(jsonPath("$.sentiment.label").exists())
				.andExpect(jsonPath("$.keywords.matched").isArray());
	}
}

