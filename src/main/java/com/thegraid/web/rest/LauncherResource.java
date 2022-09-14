package com.thegraid.web.rest;

import com.thegraid.gamma.domain.GameInst;
import com.thegraid.gamma.domain.Player;
import com.thegraid.share.LobbyLauncher.LaunchInfo;
import com.thegraid.share.LobbyLauncher.LaunchResults;
import com.thegraid.share.domain.intf.IGameInstDTO;
import gamma.main.Launcher;
import gamma.main.Launcher.Game;
import gamma.main.Launcher.PlayerInfo;
import gamma.main.Launcher.PlayerInfo2;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * LauncherResource controller
 */
@RestController
@RequestMapping("/api/launcher")
public class LauncherResource {

    private static final Logger log = LoggerFactory.getLogger(LauncherResource.class);

    @Autowired
    private GameAndRole.Map garMap;

    @Autowired
    private GameInfo.Map giMap;

    public static class GameAndRole {

        public Launcher.Game game;
        public String role;

        GameAndRole(Launcher.Game game, String role) {
            this.game = game;
            this.role = role;
        }

        /** GameAndRole mapped by gpid */
        @Component
        public static class Map extends ConcurrentHashMap<Long, GameAndRole> {

            public Long gpidFromString(String gpid) {
                try {
                    return Long.parseLong(gpid);
                } catch (NumberFormatException ex) {
                    log.debug("invalid gpid: {}, {}", gpid, ex);
                    return null;
                }
            }
        }
    }

    public static class GameInfo {

        public Launcher.Game game;
        public GameInst gameInst;

        GameInfo(Launcher.Game game, GameInst gameInst) {
            this.game = game;
            this.gameInst = gameInst;
        }

        /** Launcher.Games [giid] that are run by this LaunchService.class instance */
        @Component
        public static class Map extends ConcurrentHashMap<Object, GameInfo> {}
    }

    @Value("${thegraid.lobbyUrl}")
    String lobbyUrl;

    @Value("${thegraid.gameCtlUrl}")
    String gameCtlUrl;

    @Value("${thegraid.gameWssUrl}")
    String gameWssUrl;

    // ticket=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGgiOiJST0xFX0FETUlOLFJPTEVfVVNFUiIsImV4cCI6MTY2MzExMjEyN30.0VGpW-EGujhhOb0GIP5yheafrQpDovPyOZxa1I2MJc6xaYPOB4fiBfcZ2Yx5Kq2hsOpvingYrLFr2XbYdscUWQ
    // curl -d@gi184.json https://game5.thegraid.com:8445/api/launcher/launch -H "Content-Type: application/json" -H "Authorization: Bearer $ticket" && echo
    /**
     * Create a new GameInst, based on the supplied LaunchInfo form.
     * IGameInstDTO: {
     * IGameClassDTO gameClass
     * gameProps: IGameInstProps
     * playerA: IPlayerDTO
     * playerB: IPlayerDTO
     * }
     * ResultTicket: JWT.toString()
     *
     * @param launchInfo as form in RequestBody
     * @return LaunchResults: { started, hostUrl, wssUrl }
     */
    @PostMapping(value = "launch", produces = "application/json")
    @ResponseBody
    public ResponseEntity<LaunchResults> launchPost(@RequestBody LaunchInfo launchInfo) {
        GameInst gameInstDTO = launchInfo.gameInst;
        String resultTicket = launchInfo.resultTicket;
        log.debug("\nlaunchPost: gameInst={} resultTicket={}", gameInstDTO, resultTicket);
        LaunchResults results = launch(gameInstDTO, resultTicket);
        return ResponseEntity.ok(results);
    }

    private LaunchResults launch(GameInst gameInst, String resultTicket) {
        String giid = gameInst.getId().toString();
        Launcher.Static.logClassLoader(log, "web-app-root", this.getClass().getClassLoader(), false); // use true for internal details
        log.info("Try launch(giid={}, props={})", giid, "?"); // gameInst.getPropertyMap());
        log.info("Launch(giid={}): gameInst={}, propertyMap={}", giid, gameInst);
        // initialize results from gameInst info:
        LaunchResults.Impl results = new LaunchResults.Impl(gameInst);
        Instant started = gameInst.getStarted(); // presumably: null

        if (started != null) {
            // gameInstController/database knows (starttime != null); must be resetGiid()
            // where client connects and auths to GameInst: [proto//host:port/client]
            String hostUrl = gameInst.getHostUrl();
            results.setWssURL(gameWssUrl + giid);
            // Try find Game [may have started on a different hostUrl!]
            Launcher.Game game = giMap.get(giid).game; // (gameInst.getId())
            if (game != null) results.setHostURL(lobbyUrl + "results/" + giid); // indicate that we have the game
            log.warn("Dubious launch: game started: {} @ {}", game, started, hostUrl);
        } else {
            // gameInst.hostUrl = "https://game5.thegraid.com:8445/gamma-web/GameControl/148"
            Launcher.Game game = makeGameInstance(gameInst); // make Game and PlayerAI/PlayerInfo
            String resultUrl = lobbyUrl + "results/" + giid, wssUrl;
            log.info("\ngame={}, lobbyUrl={}, giid={}, gameCtlUrl={}", game, lobbyUrl, giid, gameCtlUrl);
            log.info("\ngameCtlUrl2={} resultUrl={}, giid={}", gameCtlUrl, resultUrl, giid);
            results.setHostURL(gameCtlUrl + giid);
            results.setWssURL(gameWssUrl + giid);
            started = game.start().toInstant(); // TODO: fix when game.start() is Instant
            results.setStarted(game.start().toInstant());
            log.warn("\nNew launch: game started: {} @ {} on {} wss:{}", game, started, gameCtlUrl, gameWssUrl);
            log.info("\nresults={}", results);
            if (started == null) {
                log.error("New launch: game failed: {}", game);
            }
            // Inform Lobby what happened:
            if (!updateGameInfo(resultTicket, results)) log.error("Failed to updateGameInfo giid: {}", giid);
            giMap.put(giid, new GameInfo(game, gameInst));
            recordGPID(gameInst, IGameInstDTO.Role_A, game);
            recordGPID(gameInst, IGameInstDTO.Role_B, game);
            // TODO: remove when game is done;
        }
        return results;
    }

    private Launcher.Game makeGameInstance(GameInst gameInst) {
        return new GameImpl();
    }

    private boolean updateGameInfo(String resultsTicket, LaunchResults results) {
        return true; // TODO: WebClient.builder()
    }

    /**
     * TODO: GameInstDTO or IPlayerDTO should include the [globally unique] gpid of each Player.
     * For now, we synthesize a token number. [unique for this gameInst]
     */
    private Long recordGPID(GameInst gameInst, String role, Game game) {
        Player player = (role == IGameInstDTO.Role_A ? gameInst.getPlayerA() : gameInst.getPlayerB());
        Long gpid = (gameInst.getId() * 1000 + player.getId()) * 10 + (role == IGameInstDTO.Role_A ? 1L : 2L);
        //player.setGpid(gpid); // for the duration of using this IPlayerDTO!
        garMap.put(gpid, new GameAndRole(game, role));
        return gpid;
    }

    /** Game impl as returned from GammaLauncher. */
    static class GameImpl implements Game {

        @Override
        public boolean abort(String comment) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public String pause(String role, int round) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String resume(String role) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String setClockRate(String role, int rate) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Game initializeGame(Launcher launcher, Map<String, Object> gameProps, PlayerInfo... pInfos) {
            // TODO Auto-generated method stub
            // set gameProps and pInfos
            return null;
        }

        @Override
        public PlayerInfo2 newPlayerInfo() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Map<String, Object> getProps() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Date start() {
            // TODO exec the game
            return new Date();
        }

        @Override
        public PlayerInfo getPlayerInfoForRole(String role) {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
