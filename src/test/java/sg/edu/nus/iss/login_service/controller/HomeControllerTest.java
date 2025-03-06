package sg.edu.nus.iss.login_service.controller;

import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import sg.edu.nus.iss.login_service.config.SecurityConfig;

@WebMvcTest(HomeController.class)
@Import(SecurityConfig.class)
public class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome to Shopsmart Login Service"));
    }

    @Test
    public void testHomeWithEmptyPath() throws Exception {
        mockMvc.perform(get(""))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome to Shopsmart Login Service"));
    }
}