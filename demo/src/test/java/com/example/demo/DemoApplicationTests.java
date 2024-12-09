package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.MOCK,
  classes = Application.class)
@AutoConfigureMockMvc
class DemoApplicationTests {
	@Autowired
    private MockMvc mvc;

	@Test
	void contextLoads() {

	}
	@Test
	void getPeers() {
	 mvc.perform(get("/api/peers")
      .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content()
      .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$[0].name", is("cypherco")))
      .andExpect(jsonPath("$[0].xp", !is(999999)));}
}
