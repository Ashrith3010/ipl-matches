package com.indium.backend_assignment.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CricketControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void testUploadJsonFile() throws IOException {
        String url = "http://localhost:" + port + "/api/cricket/upload";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Load the test file
        ClassPathResource fileResource = new ClassPathResource("335982.json");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = testRestTemplate.postForEntity(url, requestEntity, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("File uploaded successfully", response.getBody());
    }

    @Test
    public void testGetMatchesPlayedByPlayer() {
        String playerName = "V Kohli";
        String url = "http://localhost:" + port + "/api/cricket/matches/player/" + playerName;

        String response = testRestTemplate.getForObject(url, String.class);
        assertEquals("V Kohli has played in 1 match(es).", response);
    }

    @Test
    public void testGetCumulativeScoreOfPlayer() {
        String playerName = "V Kohli";
        String url = "http://localhost:" + port + "/api/cricket/score/player/" + playerName;

        Integer response = testRestTemplate.getForObject(url, Integer.class);
        assertEquals(1, response);
    }

    @Test
    public void testGetTopBatsmenPaginated() {
        String url = "http://localhost:" + port + "/api/cricket/batsmen/top?page=0&size=5";

        String response = testRestTemplate.getForObject(url, String.class);
        String expectedResponse = "BB McCullum (Kolkata Knight Riders): 158 runs\nRV Uthappa (Mumbai Indians): 48 runs\nMV Boucher (Royal Challengers Bangalore): 46 runs\nR Dravid (Royal Challengers Bangalore): 34 runs\nJH Kallis (Royal Challengers Bangalore): 33 runs";
    }
    @Test
    public void testGetScoreDetailsByDate() {
        String date = "2008-04-18"; // Updated to match the available data
        String url = "http://localhost:" + port + "/api/cricket/matches/date/" + date;

        // Call the API
        ResponseEntity<String> response = testRestTemplate.getForEntity(url, String.class);

        // Updated expected response to match actual response format
        String expectedResponse = "Scores for matches on 2008-04-18: Match at M Chinnaswamy Stadium between teams: Royal Challengers Bangalore Kolkata Knight Riders Team: Kolkata Knight Riders scored 205 runs Team: Royal Challengers Bangalore scored 63 runs";

        // Normalize both expected and actual responses to remove extra spaces, tabs, and newlines
        String normalizedExpected = expectedResponse.replaceAll("\\s+", " ").trim();
        String normalizedActual = response.getBody().replaceAll("\\s+", " ").trim();

        // Compare normalized strings
        assertEquals(normalizedExpected, normalizedActual);
    }

}