package com.thegraid.web.rest;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.thegraid.share.LobbyLauncher.LaunchInfo;
import com.thegraid.share.LobbyLauncher.LaunchResults;
import com.thegraid.share.domain.intf.IGameClassDTO;
import com.thegraid.share.domain.intf.IGameInstDTO;
import com.thegraid.share.domain.intf.IPlayerDTO;
import gamma.main.Launcher;
import gamma.main.Launcher.Game;
import gamma.main.Launcher.PlayerInfo;
import gamma.main.Launcher.PlayerInfo2;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    static ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build().setDefaultPropertyInclusion(Include.NON_NULL);

    private String jsonify(Object obj) {
        // https://www.baeldung.com/spring-boot-customize-jackson-objectmapper#1-objectmapper
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON conversion failed", e);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(LauncherResource.class);

    @Autowired
    /** gpid -> {IGameInstDTO, Role} */
    private GameAndRole.Map garMap;

    @Autowired
    /** giid -> IGameInstDTO */
    private GameInfo.Map giMap;

    /** paths to find AIGS common assets, relative to assetBase. */
    // expandPathToFileURLs will create a "file:" URL
    @Value("${thegraid.commonLibs}")
    private List<String> commonLibs;

    @Value("${thegraid.assetBase}")
    private String assetBase;

    // @Autowired
    // InfoService gameInstInfoService;

    public static class GameAndRole {

        public Launcher.Game game;
        public String role;

        GameAndRole(Launcher.Game game, String role) {
            this.game = game;
            this.role = role;
        }

        /** GameAndRole mapped by gpid --> {game, role} */
        @Component
        public static class Map extends ConcurrentHashMap<Long, GameAndRole> {}
    }

    public static class GameInfo {

        public Launcher.Game game;
        public IGameInstDTO gameInst;

        GameInfo(Launcher.Game game, IGameInstDTO gameInst) {
            this.game = game;
            this.gameInst = gameInst;
        }

        /** Launcher.Games [giid] that are run by this LaunchService.class instance */
        @Component
        public static class Map extends ConcurrentHashMap<Long, GameInfo> {}
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
        IGameInstDTO gameInstDTO = launchInfo.gameInst;
        // IGameInstPropsDTO props = launchInfo.gameProps;
        // gameInstDTO.setProps(props);  // TODO: do this in Lobby.
        String resultTicket = launchInfo.resultTicket;
        log.info("launchPost: launchInfo={}", jsonify(launchInfo));
        LaunchResults results = launch(gameInstDTO, launchInfo.gpidA, launchInfo.gpidB, resultTicket);
        return ResponseEntity.ok(results);
    }

    private LaunchResults launch(IGameInstDTO gameInst, Long gpidA, Long gpidB, String resultTicket) {
        Long giid = gameInst.getId();
        Launcher.Static.logClassLoader(log, "web-app-root", this.getClass().getClassLoader(), false); // use true for internal details
        log.info("Try launch(giid={}, props={})", giid, gameInst.getProps());
        log.info("Try Launch(giid={}): gameInst={}", giid, gameInst);
        // initialize results from gameInst info:
        LaunchResults.Impl results = new LaunchResults.Impl(gameInst);
        Instant started = gameInst.getStarted(); // presumably: null

        if (started != null) {
            // gameInstController/database knows (starttime != null); must be resetGiid()
            // where client connects and auths to GameInst: [proto://host:port/client]
            String hostUrl = gameInst.getHostUrl();
            // Try find Game [may have started on a different hostUrl!]
            Launcher.Game game = giMap.get(giid).game;
            if (game != null) {
                results.setHostURL(gameCtlUrl + giid); // indicate that we have the game
                results.setWssURL(gameWssUrl + giid);
            }
            log.warn("Dubious launch: game started: {} @ {} on {}", game, started, hostUrl);
        } else {
            Launcher.Game game = makeGameInstance(gameInst); // make Game and PlayerAI/PlayerInfo
            String resultUrl = lobbyUrl + "results/" + giid;
            // log.info("\ngame={}, lobbyUrl={}, giid={}, gameCtlUrl={}", game, lobbyUrl, giid, gameCtlUrl);
            // log.info("\ngameCtlUrl2={} resultUrl={}, giid={}", gameCtlUrl, resultUrl, giid);
            started = game.start(); // TODO: fix when game.start() is Instant
            results.setStarted(started);
            results.setHostURL(gameCtlUrl + giid);
            results.setWssURL(gameWssUrl + giid);
            log.warn("\nlaunch new game: {} at {} to {}", game, started, resultUrl);
            log.info("\nlaunch results = {}", jsonify(results));
            if (started == null) {
                log.error("New launch: game failed: {}", game);
                return results; // with started == null
            }
            // Inform Lobby what happened:
            if (!updateGameInfo(resultTicket, results)) log.error("Failed to updateGameInfo giid: {}", giid);
            giMap.put(giid, new GameInfo(game, gameInst));
            garMap.put(gpidA, new GameAndRole(game, IGameInstDTO.Role_A));
            garMap.put(gpidB, new GameAndRole(game, IGameInstDTO.Role_B));
            // TODO: remove when game is done;
        }
        return results;
    }

    /**
     * Given GameInst (with propertyMap)
     * get GameClass (from GameInst) and associated [Game]Launcher
     * game = launcher.instantiateGameClass(gamePath, gameProps)
     * find PlayerA and PlayerB
     * game.initializeGame(launcher, props, piA, piB)
     */
    private Launcher.Game makeGameInstance(IGameInstDTO gameInst) {
        IGameClassDTO gameClass = gameInst.getGameClass(); // GameClass.findGameClass(gameClass.getId());
        Map<String, Object> gameProps = gameInst.getPropertyMap();

        Launcher launcher = getLauncher(gameClass);
        String gamePath = gameClass.getGamePath();
        // load game class and makeInstance:
        Launcher.Game game = launcher.instantiateGameClass(gamePath, gameProps);
        // someone may check gameProps and maybe setClockRate();

        IPlayerDTO playerA = gameInst.getPlayerA();
        IPlayerDTO playerB = gameInst.getPlayerB();

        // Load PlayerAI and store PlayerInfo for later use:
        Launcher.PlayerInfo piA = makePlayerInfo(playerA, launcher, game, IGameInstDTO.Role_A);
        Launcher.PlayerInfo piB = makePlayerInfo(playerB, launcher, game, IGameInstDTO.Role_B);
        game.initializeGame(launcher, gameProps, piA, piB);
        return game;
    }

    /** use Factory to find/create Launcher instance for given GameClass. */
    private Launcher getLauncher(IGameClassDTO gameClass) {
        String[] launcherPaths = gameClass.getLauncherPath().split("!");
        log.info("getLauncher: raw launcherPath={}, Paths={}", gameClass.getLauncherPath(), launcherPaths);
        String launcherPath = launcherPaths[0]; // relative to BASEURL [TODO: inject BASEURL from GAMEBASE]
        String launcherFQCN = launcherPaths[1]; // gamma.main.GammaLauncher
        Launcher launcher = getLauncher(launcherPath, launcherFQCN);
        launcher.setBasePath(assetBase); // for paths without "scheme:/.../"
        return launcher;
    }

    // get your Launcher here; so Launcher.Impl is not exported...?
    // given {giid} lookup GameInst: gameClass & launcherPath (launcherPath+launcherName)
    // expect launcher class is in jar with the game (Game and Launcher both "plugin")
    /**
     * get Launcher (with its ClassLoader) for the given GameClass
     * @param givenPath derived from database: GameClass.getLauncherPath();
     * if a relative path, it descends from setBasePath(assetBase);
     * @param fqcn derived from database: GameClass.getLauncherPath()
     */
    public Launcher getLauncher(String givenPath, String fqcn) {
        String launcherPath = givenPath.contains(":") ? givenPath : (assetBase + givenPath);
        URL[] urls = new URL[0];
        String[] otherLibs = new String[0]; // commonLibs.toArray()?
        if (otherLibs.length > 0) {
            URL baseURL = Launcher.Static.makeURL(launcherPath, "LaunchService.getLauncher: Bad GameClass URL: " + launcherPath);
            // Note: if no paths to expand, then baseURL is not used! (for ex: if fails above and is null)
            // If|When we have otherLibs that are not in the 'acl' loader paths,
            // then fix the database entry, or prepend above with a URL Schema://root-path/
            urls = Launcher.Static.expandPathToURLs(baseURL, otherLibs);
        }
        ClassLoader acl = Launcher.class.getClassLoader(); // probably appServer ClassLoader.
        ClassLoader lcl = new URLClassLoader(urls, acl); // Launcher ClassLoader
        try {
            Class<?> launcherClass = Launcher.Static.getClass(fqcn, true, lcl);
            Launcher launcher = (Launcher) launcherClass.getDeclaredConstructor().newInstance();
            return launcher;
        } catch (Exception ex) {
            log.error("Class.forName or .newInstance(): FAILED {}", ex);
            return null;
        }
        // try {
        //     Launcher launcher = (Launcher) Static.newInstance(className, true, lcl); // GammaLauncher?
        //     return launcher; // launcher.getClass().getClassLoader() = lcl!
        // } catch (Exception ex) {
        //     log.error("Failed: {}", ex);
        //     return null;
        // }
    }

    /**
     * Initialize PlayerAI for the game.
     * Setup paths and then give it to Game's Launcher to instantiate and initialize the PlayerAI
     * @param player Player object indicating Role, and the mainJar for PlayerAI;
     * @param launcher able to instantiate the PlayerAI
     * @param game Launcher.Game that knows how to create a PlayerInfo for the given GameClass
     * @return PlayerInfo struct of useful information
     */
    private Launcher.PlayerInfo makePlayerInfo(IPlayerDTO player, Launcher launcher, Launcher.Game game, String role) {
        final Launcher.PlayerInfo2 playerInfo = game.newPlayerInfo();

        String[] paths = player.getMainJar().getPath().split("!"); // path!fqcn to IPlayer
        String playerJar = paths[0]; // groupPath/artifact/artifact-version/
        String fqcn = paths[1]; // class implementing PlayerAI
        URL[] urls = getPathURLs(playerJar, player);

        // load player class, instantiate, and inject Controller, Responder
        launcher.instantiatePlayerAI(playerInfo, urls, fqcn); // Note: NPCs, Gaia are loaded in SimpleGame

        playerInfo.setGamePlayerId(player.getGpid()); // getGamePlayerId()
        playerInfo.setRole(role); // mark Players for later reference; see: setClockRate()
        playerInfo.setProps(new HashMap<String, Object>());

        // We believe LaunchService can synthesize these if/when needed, not generated or used by gammaDS
        //		String destId = gamePlayer.getDestId();			// Destination where client gets events. [aka getToken()]
        //		String uniqueId = gamePlayer.getGameInst().getId().toString(); // where client puts cmds: cmdDelayQueue
        //		csu.addClientServiceFactory(playerInfo, destId, uniqueId);
        // defer addClientService(playerInfo, destId, uniqueId);
        // so player can come back later to make/register a connection to/from remote client

        // playerInfo should now have all the useful stuff.
        return playerInfo;
    }

    private URL[] getPathURLs(String playerJar, IPlayerDTO gamePlayer) {
        log.debug("getPathURLs: playerJar=%s, commonLibs=", playerJar, commonLibs);
        /// expand playerBase+pathDir[/main.jar,/,/*], commonLibs for 'pcl'
        int ndx2 = Math.max(0, playerJar.lastIndexOf('/')); // TODO: use File.parent()?
        String playerDir = playerJar.substring(0, ndx2); // trim /main.jar, leaving ".../{memberId}"
        List<String> pathList = new ArrayList<String>();
        pathList.add(playerJar); // .../target/classes/  [FooPlayer.jar] (whatever given from DB)
        pathList.add(playerDir + "/"); // .../target/classes/  (aids in finding my/pkg/Foo.class or Resources
        pathList.add(playerDir + "/*"); // .../target/classes/* [*.jar]	(other jars in [lib?] directory)
        pathList.addAll(commonLibs); // .../target/classes/  (distinct from p)_paths/gamma-p ?)
        addAssets(gamePlayer, pathList);
        String[] paths = pathList.toArray(new String[pathList.size()]);

        URL[] urls = Launcher.Static.expandPathToFileURLs(assetBase, paths); // ...ToFileURLs()
        return urls;
    }

    private void addAssets(IPlayerDTO gamePlayer, List<String> pathList) {
        // If playerJar was an OSGI bundle, it would be easy; but we suppose there are other jars
        // and in dev mode, we want to get all the target/class resources
        // TODO, decode asset.include and inject additional classpath elements
    }

    private boolean updateGameInfo(String resultsTicket, LaunchResults results) {
        return true; // TODO: WebClient.builder()
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
        public Instant start() {
            // TODO exec the game
            return Instant.now();
        }

        @Override
        public PlayerInfo getPlayerInfoForRole(String role) {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
