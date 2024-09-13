package com.indium.backend_assignment.service;

import com.indium.backend_assignment.entity.*;
import com.indium.backend_assignment.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CricketService {

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



    @Transactional
    @CacheEvict(value = {"matchesByPlayer", "scoresByDate"}, allEntries = true)
    public String uploadJsonFile(MultipartFile file) throws IOException {
        Map<String, Object> jsonData = objectMapper.readValue(file.getInputStream(), Map.class);

        // Extract match info
        Map<String, Object> info = (Map<String, Object>) jsonData.get("info");
        if (info == null) {
            return "Match info is missing in JSON data";
        }

        // Check if the match already exists
        Match existingMatch = findExistingMatch(info);
        if (existingMatch != null) {
            return "This match data has already been uploaded.";
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
                return "Innings data is missing in JSON data";
            }
            createInningsOversDeliveries(match, inningsData);

            return "File uploaded successfully";
        } catch (Exception e) {
            return "Error occurred while processing the file: " + e.getMessage();
        }
    }

    private Match findExistingMatch(Map<String, Object> info) {
        String city = (String) info.get("city");
        String venue = (String) info.get("venue");
        List<?> dates = (List<?>) info.get("dates");
        if (dates == null || dates.isEmpty()) {
            return null; // Handle this case in the calling method
        }
        LocalDate matchDate = LocalDate.parse((String) dates.get(0));

        return matchRepository.findByCityAndVenueAndMatchDate(city, venue, matchDate);
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
            Team team = teamRepository.findByTeamName(teamName);
            if (team == null) {
                team = new Team();
                team.setTeamName(teamName);
            }
            team.setMatch(match);  // Set the match for the team
            team = teamRepository.save(team);

            List<String> playerNames = playersMap.get(teamName);
            if (playerNames != null) {
                for (String playerName : playerNames) {
                    Player player = playerRepository.findByPlayerName(playerName);
                    if (player == null) {
                        player = new Player();
                        player.setPlayerName(playerName);
                        player.setTeam(team);
                        player.setTotalRuns(0);
                        playerRepository.save(player);
                    }
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

    // Other methods remain the same
    @Cacheable(value = "matchesByPlayer", key = "#playerName")
    public String getMatchesPlayedByPlayer(String playerName) {
        long matchCount = deliveryRepository.findByBatter(playerName)
                .stream()
                .filter(delivery -> delivery.getOver() != null && delivery.getOver().getInnings() != null)
                .map(delivery -> delivery.getOver().getInnings().getMatch().getMatchId())
                .distinct()
                .count();
        return playerName + " has played in " + matchCount + " match(es).";
    }
    @Cacheable(value = "cumulativeScore", key = "#playerName")
    public int getCumulativeScoreOfPlayer(String playerName) {
        Player player = playerRepository.findByPlayerName(playerName);
        if (player == null) {
            return 0;
        }
        return player.getTotalRuns();
    }
    @Cacheable(value = "topBatsmen", key = "{#pageable.pageNumber, #pageable.pageSize}")
    public String getTopBatsmenPaginated(Pageable pageable) {
        Page<Player> topBatsmen = playerRepository.findAllByOrderByTotalRunsDesc(pageable);
        return topBatsmen.getContent().stream()
                .map(player -> player.getPlayerName() + " (" + player.getTeam().getTeamName() + "): " + player.getTotalRuns() + " runs")
                .collect(Collectors.joining("\n"));
    }
    @Cacheable(value = "scoresByDate", key = "#date")
    public String getMatchScoresByDate(LocalDate date) {
        List<Match> matches = matchRepository.findByMatchDate(date);
        if (matches.isEmpty()) {
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

        return result.toString();
    }
}
