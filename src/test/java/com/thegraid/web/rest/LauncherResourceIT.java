package com.thegraid.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thegraid.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Test class for the LauncherResource REST controller.
 *
 * @see LauncherResource
 */
@IntegrationTest
class LauncherResourceIT {

    private MockMvc restMockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        LauncherResource launcherResource = new LauncherResource();
        restMockMvc = MockMvcBuilders.standaloneSetup(launcherResource).build();
    }

    /**
     * Test launch
     */
    @Test
    void testLaunch() throws Exception {
        restMockMvc.perform(post("/api/launcher/launch")).andExpect(status().isOk());
    }
}
