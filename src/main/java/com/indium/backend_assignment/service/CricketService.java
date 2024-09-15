package com.indium.backend_assignment.service;

import com.indium.backend_assignment.entity.*;
import com.indium.backend_assignment.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CricketService {

    private static final Logger log = LoggerFactory.getLogger(CricketService.class);
    private static final String TOPIC = "match-logs-topic";

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private InningsRepository inningsRepository;

    @Autowired
    private OverRepository overRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @CacheEvict(value = {"matchesByPlayer", "cumulativeScore", "topBatsmen", "scoresByDate"}, allEntries = true)
    @Transactional
    public String uploadJsonFile(MultipartFile file) throws IOException {
        log.info("Received file upload request");
        if (file.isEmpty()) {
            log.error("Uploaded file is empty");
            sendLogToKafka("uploadJsonFile", "fileName", file.getOriginalFilename());
            return "Uploaded file is empty";
        }

        Map<String, Object> jsonData;
        try {
            jsonData = objectMapper.readValue(file.getInputStream(), Map.class);
        } catch (IOException e) {
            log.error("Error parsing JSON file: " + e.getMessage(), e);
            sendLogToKafka("uploadJsonFile", "fileName", file.getOriginalFilename());
            throw new IOException("Error parsing JSON file: " + e.getMessage(), e);
        }

        // Extract match info
        Map<String, Object> info = (Map<String, Object>) jsonData.get("info");
        if (info == null) {
            log.error("Match info is missing in JSON data");
            sendLogToKafka("uploadJsonFile", "fileName", file.getOriginalFilename());
            return "Match info is missing in JSON data";
        }

        // Check if the match already exists
        Optional<Match> existingMatchOpt = findExistingMatch(info);
        if (existingMatchOpt.isPresent()) {
            log.warn("Match already exists in the database");
            sendLogToKafka("uploadJsonFile", "fileName", file.getOriginalFilename());
            return "Already exists";
        }

        try {
            // If the match doesn't exist, proceed with the upload
            Match match = createMatchFromJson(info);
            match = matchRepository.save(match);

            // Update Teams and Players
            updateTeamsAndPlayers(match, info);

            // Create and save Innings, Overs, and Deliveries
            List<Map<String, Object>> inningsData = (List<Map<String, Object>>) jsonData.get("innings");
            if (inningsData == null) {
                log.error("Innings data is missing in JSON data");
                sendLogToKafka("uploadJsonFile", "fileName", file.getOriginalFilename());
                return "Innings data is missing in JSON data";
            }
            createInningsOversDeliveries(match, inningsData);

            log.info("File processing completed successfully. Clearing caches...");
            log.info("Caches cleared after file upload");
            sendLogToKafka("uploadJsonFile", "fileName", file.getOriginalFilename());
            return "File uploaded successfully";
        } catch (Exception e) {
            log.error("Error occurred while processing the file: " + e.getMessage(), e);
            sendLogToKafka("uploadJsonFile", "fileName", file.getOriginalFilename());
            return "Error occurred while processing the file: " + e.getMessage();
        }
    }


    private Optional<Match> findExistingMatch(Map<String, Object> info) {
        String city = (String) info.get("city");
        String venue = (String) info.get("venue");
        List<?> dates = (List<?>) info.get("dates");
        if (dates == null || dates.isEmpty()) {
            return Optional.empty();
        }
        LocalDate matchDate = LocalDate.parse((String) dates.get(0));

        return Optional.ofNullable(matchRepository.findByCityAndVenueAndMatchDate(city, venue, matchDate));
    }

    private Match createMatchFromJson(Map<String, Object> info) {
        Match match = new Match();
        match.setCity((String) info.get("city"));
        match.setVenue((String) info.get("venue"));

        List<?> dates = (List<?>) info.get("dates");
        if (dates == null || dates.isEmpty()) {
            throw new RuntimeException("Match date is missing in JSON data");
        }
        match.setMatchDate(LocalDate.parse((String) dates.get(0)));
        return match;
    }

    private void updateTeamsAndPlayers(Match match, Map<String, Object> info) {
        Map<String, List<String>> playersMap = (Map<String, List<String>>) info.get("players");
        if (playersMap == null) {
            throw new RuntimeException("Players data is missing in JSON data");
        }

        List<String> teamNames = (List<String>) info.get("teams");
        if (teamNames == null) {
            throw new RuntimeException("Teams data is missing in JSON data");
        }

        for (String teamName : teamNames) {
            // Create a new team for this specific match
            Team team = new Team();
            team.setTeamName(teamName);
            team.setMatches(new ArrayList<>());
            team.getMatches().add(match);
            team = teamRepository.save(team);

            List<String> playerNames = playersMap.get(teamName);
            if (playerNames != null) {
                for (String playerName : playerNames) {
                    Player player = playerRepository.findByPlayerName(playerName);
                    if (player == null) {
                        player = new Player();
                        player.setPlayerName(playerName);
                        player.setTotalRuns(0);
                    }
                    player.setTeam(team);
                    playerRepository.save(player);
                }
            }
        }
    }


    private void createInningsOversDeliveries(Match match, List<Map<String, Object>> inningsData) {
        int inningsCount = 0;
        for (Map<String, Object> inningData : inningsData) {
            inningsCount++;
            Team team = teamRepository.findByTeamNameAndMatch((String) inningData.get("team"), match);
            if (team == null) {
                throw new RuntimeException("Team not found for this match");
            }

            Innings innings = new Innings();
            innings.setMatch(match);
            innings.setTeam(team);
            inningsRepository.save(innings);

            List<Map<String, Object>> overs = (List<Map<String, Object>>) inningData.get("overs");
            if (overs != null) {
                for (int overNumber = 0; overNumber < overs.size(); overNumber++) {
                    Map<String, Object> overData = overs.get(overNumber);

                    Over over = new Over();
                    over.setOverNumber(overNumber + 1);
                    over.setInnings(innings);
                    overRepository.save(over);

                    List<Map<String, Object>> deliveries = (List<Map<String, Object>>) overData.get("deliveries");
                    if (deliveries != null) {
                        for (Map<String, Object> deliveryData : deliveries) {
                            Delivery delivery = new Delivery();
                            delivery.setBatter((String) deliveryData.get("batter"));
                            delivery.setBowler((String) deliveryData.get("bowler"));
                            delivery.setOver(over);

                            Map<String, Object> runs = (Map<String, Object>) deliveryData.get("runs");
                            if (runs != null) {
                                Integer runsScored = (Integer) runs.get("batter");
                                if (runsScored != null) {
                                    delivery.setRuns(runsScored);

                                    // Update player's total runs
                                    Player player = playerRepository.findByPlayerName(delivery.getBatter());
                                    if (player == null) {
                                        throw new RuntimeException("Player not found");
                                    }
                                    player.setTotalRuns(player.getTotalRuns() + runsScored);
                                    playerRepository.save(player);

                                    // Check for wicket
                                    delivery.setWicket(deliveryData.containsKey("wicket"));

                                    deliveryRepository.save(delivery);
                                } else {
                                    throw new RuntimeException("Runs scored data is missing");
                                }
                            } else {
                                throw new RuntimeException("Runs data is missing");
                            }
                        }
                    }
                }
            }
        }
    }

    @Cacheable(value = "matchesByPlayer", key = "#playerName")
    public String getMatchesPlayedByPlayer(String playerName) {
        log.info("Fetching matches played by player: {}", playerName);
        long matchCount = deliveryRepository.findByBatter(playerName)
                .stream()
                .filter(delivery -> delivery.getOver() != null && delivery.getOver().getInnings() != null)
                .map(delivery -> delivery.getOver().getInnings().getMatch().getMatchId())
                .distinct()
                .count();
        log.info("Matches played by player {}: {}", playerName, matchCount);
        sendLogToKafka("getMatchesPlayedByPlayer", "playerName", playerName);
        return playerName + " has played in " + matchCount + " match(es).";
    }

    @Cacheable(value = "cumulativeScore", key = "#playerName")
    public int getCumulativeScoreOfPlayer(String playerName) {
        log.info("Fetching cumulative score for player: {}", playerName);
        Player player = playerRepository.findByPlayerName(playerName);
        if (player == null) {
            sendLogToKafka("getCumulativeScoreOfPlayer", "playerName", playerName);
            return 0;
        }
        log.info("Cumulative score for player {}: {}", playerName, player.getTotalRuns());
        sendLogToKafka("getCumulativeScoreOfPlayer", "playerName", playerName);
        return player.getTotalRuns();
    }

    @Cacheable(value = "topBatsmen", key = "{#pageable.pageNumber, #pageable.pageSize}")
    public String getTopBatsmenPaginated(Pageable pageable) {log.info("Fetching top batsmen with pagination");
        log.info("Fetching top batsmen with pagination");
        Page<Player> topBatsmen = playerRepository.findAllByOrderByTotalRunsDesc(pageable);
        String result = topBatsmen.getContent().stream()
                .limit(5)
                .map(player -> player.getPlayerName() + " (" + player.getTeam().getTeamName() + "): " + player.getTotalRuns() + " runs")
                .collect(Collectors.joining("\n"));
        log.info("Top 5 batsmen are: {} ", result);
        sendLogToKafka("getTopBatsmenPaginated", "pageNumber", String.valueOf(pageable.getPageNumber()));
        return result;

    }

    @Cacheable(value = "scoresByDate", key = "#date")
    public String getMatchScoresByDate(LocalDate date) {
        log.info("Fetching match scores for date: {}", date);
        List<Match> matches = matchRepository.findByMatchDate(date);
        if (matches.isEmpty()) {
            sendLogToKafka("getMatchScoresByDate", "date", date.toString());
            return "No matches found on " + date;
        }

        StringBuilder result = new StringBuilder("Scores for matches on " + date + ":\n");

        for (Match match : matches) {
            result.append("Match at ").append(match.getVenue()).append(" between teams: ");
            for (Team team : match.getTeams()) {
                result.append(team.getTeamName()).append(" ");
            }
            result.append("\n");

            List<Innings> inningsList = inningsRepository.findByMatch(match);
            for (Innings innings : inningsList) {
                result.append("Team: ").append(innings.getTeam().getTeamName()).append(" scored ");

                int totalRuns = 0;
                List<Over> overs = overRepository.findByInnings(innings);
                for (Over over : overs) {
                    List<Delivery> deliveries = deliveryRepository.findByOver(over);
                    for (Delivery delivery : deliveries) {
                        totalRuns += delivery.getRuns();
                    }
                }
                result.append(totalRuns).append(" runs\n");
            }
        }
        log.info("match scores for date: {} are: {} ", date, result);
        sendLogToKafka("getMatchScoresByDate", "date", date.toString());
        return result.toString();
    }
    private void sendLogToKafka(String methodName, String paramKey, String paramValue) {
        try {
            Map<String, Object> logMessage = new HashMap<>();
            logMessage.put("method", methodName);
            logMessage.put("timestamp", LocalDateTime.now().toString());
            logMessage.put("params", Map.of(paramKey, paramValue));

            String jsonLog = objectMapper.writeValueAsString(logMessage);

            // Send log to Kafka
            kafkaTemplate.send(new ProducerRecord<>(TOPIC, jsonLog));
            log.info("Log sent to Kafka: {}", jsonLog);

        } catch (Exception e) {
            log.error("Failed to send log to Kafka", e);
        }
    }


    }