package com.thegraid.share;

import gamma.util.ArraySet;
import gamma.util.DelayQueue;
import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.FileSuffixFilter;
import util.Reflect;

/*  TODO:
	*SimpleGame/Base_Game protocol, with properties, classloader, etc. as called from GameControl.
		Launcher, Launcher.Game
	*rewrite GameCtrl to use FlexClient().getId() vs gpid.
	*construct handoff from GameControl to client's PlayerAI (avatar/personae)
	*work Spring-BlazeDS so avatar can be found/used by Flex client. [clientURL]

   use rmic so we can run UserAI in separate/local web server and RMI/RPC to ControllerRMI_PlayerAI
 */

/**
 * Interface[s] between [generic]LaunchService and [Gamma]Launcher.
 * Allows startup of any Game without polluting classpath of the LaunchService web app.
 *
 * We can 'cut' this several ways:
 * Implement LauncherIntf as a 'proxy' that execs a process,
 * and send/recv protocol over stdin/stdout as RPC
 * OR
 * Run Game instance 'InProc' and invoke methods of GammaLauncher directly.
 *
 * Only GammaLauncher (implements LauncherIntf) needs gamma.main on its classpath.
 * <br>[... this interface seems to be more between GameLauncher and the GameImpl.jar ?]
 * <p>
 * LauncherIntf makes no references to gamma-spi, gamma-p nor gamma-si;
 * LauncherIntf defines interfaces that are implemented by the other gamma modules.
 *
 * Each Game [class/jar] supplies an impl of LauncherIntf that will instantiate that game.
 *
 */
public interface LauncherGame {
    // presumably the 'root directory' for the Game resources; is where we found LauncherJar.
    // launcherPath is domain.GameClass.launcherPath: path_to_jar!main_class == gamma.main.GammaLanucher
    // NOW: this is included in IGameInstDTO.IGameClassDTO.launcherPath
    /** set path so Launcher can find the game jar(s). */
    void setLauncherPath(String launcherPath); // inject value from GameClass.launcherPath

    /**
     * Load the gameClass and instantiate it.
     *
     * @param gamePath from GameClass.gamePath: identify path and fqcn of SI.Game/PPI.game
     * @param gameProps property map for the game as negotiated by players
     */
    Game instantiateGameClass(String gamePath, Map<String, Object> gameProps);

    /** makes PlayerAI, stores it in PlayerInfo with other information.
     * PlayerAI load/initialization code runs in a separate thread.
     * @param playerInfo a settable PlayerInfo that will be filled with PlayerAI and other information.
     * @param path contains at least the .jar holding fqcn, possibly other jars or directories.
     * @param fqcn IPlayer class to load with PlayerClassLoader
     */
    PlayerInfo2 instantiatePlayerAI(PlayerInfo2 playerInfo, URL[] path, String fqcn);

    abstract static class Base implements LauncherGame {}

    /**
     * Restricted set of Game Methods, used by GameControl, as agent for DisplayClient.
     * @see Launcher.ClockCtrl
     */
    interface GameDS {
        boolean abort(String comment);
        String pause(String role, int round);
        String resume(String role);
        String setClockRate(String role, int rate);
    }

    /**
     * Methods looking from Launcher down into Game: [how Launcher initializes  a Game]
     */
    interface Game extends GameDS {
        /**
         * Initialize this Class<Game>.newInstance(), making it ready for game.start().
         * @param launcher [Games own Launcher] access to DS structures/methods ??
         * @param gameProps the player-agreed configuration for this Game instance.
         * [TODO: change to JSON string? assert -->Object is a simple Object]
         * @param PlayerInfo[] representing playerA and playerB; game may add NPC players.
         */
        Game initializeGame(LauncherGame launcher, Map<String, Object> gameProps, PlayerInfo... pInfos);

        /** create Game-specific instance of settable PlayerInfo */
        PlayerInfo2 newPlayerInfo(); // make a settable PlayerInfo suitable for this Game

        /** Read-only view of gameProps. */
        Map<String, Object> getProps(); // read-only Props

        /** GameLauncher starts game.
         * @return timestamp when game starts, or null if fails.
         */
        Instant start();

        PlayerInfo getPlayerInfoForRole(String role);
    }

    ////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////

    // TODO: change to Long when we can upgrade service APIs
    // These may have been exposed through Flex,
    // Consider: GameControl/ClockControl is managed by Http
    // and flex app can bridge to Http as necessary.
    /** Remote methods for GameControl, on Game from client.
     * Client/Session may multiplex many GamePlayers, so include gpid.
     * @see Launcher.GameDS
     */
    interface ClockCtrl {
        /** Player quits.  */
        String resign(String gpid, String comment);
        /** pause the game engine now. */
        String pause(String gpid);
        /** pause the game engine at round.*/
        String pause2(String gpid, int round);
        /** resume the game engine; re-balance queues as necessary. */
        String resume(String gpid);
        /** encourage game engine to run faster or slower. */
        String setClockRate(String gpid, int rate);
    }

    @SuppressWarnings("serial")
    static class FailedException extends IllegalStateException {

        public FailedException(final String msg) {
            super(msg);
        }

        public FailedException(final String msg, final Throwable cause) {
            super(msg, cause);
        }
    }

    // domain.GameInst implements gamma.main.GameResults
    interface GameResults {
        boolean getAborted();
        long getId();
        Instant getFinished();
        int getScoreA();
        int getScoreB();
        int getTicks();

        // TODO access restrictions (or: reimplement as ImmutableMap) [is maybe internal only]
        static class Impl implements GameResults {

            public Impl() {}

            public Impl(Long id) {
                this.id = id;
            }

            long id;

            @Override
            public long getId() {
                return this.id;
            }

            public void setId(long id) {
                this.id = id;
            }

            boolean aborted;

            @Override
            public boolean getAborted() {
                return this.aborted;
            }

            public void setAborted(boolean aborted) {
                this.aborted = aborted;
            }

            Instant finished;

            @Override
            public Instant getFinished() {
                return this.finished;
            }

            public void setFinished(Instant finished) {
                this.finished = finished;
            }

            int scoreA;

            @Override
            public int getScoreA() {
                return this.scoreA;
            }

            public void setScoreA(int scoreA) {
                this.scoreA = scoreA;
            }

            int scoreB;

            @Override
            public int getScoreB() {
                return this.scoreB;
            }

            public void setScoreB(int scoreB) {
                this.scoreB = scoreB;
            }

            int ticks;

            @Override
            public int getTicks() {
                return this.ticks;
            }

            public void setTicks(int ticks) {
                this.ticks = ticks;
            }
        }
    }

    /** @see gammaDS/.../domain/GameClass; */
    // currently unused; split and parse getGamePath() and getLauncherPath() where needed.
    interface GameClassX {
        String getJarPath(); // URL for URLClassLoader to find jar
        String getClassName(); // class name within jar
    }

    /** Generic PPI.Player implementation for Player or NPC. */
    interface PlayerAI {
        /**
         * Allows PlayerAI to specify the methods to be visible to the remote service.
         * May be null for no remote interface (for NPC or full-auto).
         *
         * For Websocket, this would be 'supported protocol'...
         *
         */
        ServiceDefinition getClientServiceDefinition();
    }

    // TODO: delete or adapt for webservice and/or protobuf-based-rpc
    // TODO: gamma-main is loaded in both gammaDS and Game Server, this code should move to gamma-web.
    // TODO: Do we need something that spans Game Server & DisplayClient (SPI is GameServer & PlayerAI)
    /** Describes a PlayerAI's RemoteService interface.
     * <p>
     * Typically, the ServiceName is the name of the interface that defines the Methods.
     * <p>
     * <b>Note:</b> methods may not be overloaded; only one Method with any given name.
     * [BlazeDS will not allow; but maybe someday!]
     * <p>
     * <b>Note:</b> the PlayerAI class is typically only defined in the PlayerClassLoader,
     * and we do not want to interrogate that class from the Launcher, so PlayerAI reduces
     * the necessary information to Strings in this structure.
     */
    interface ServiceDefinition {
        String getServiceName();
        String[] getMethodNames();

        /** An implementation of ServiceDefinition */
        public static class Impl implements ServiceDefinition {

            private String serviceName;

            public String getServiceName() {
                return this.serviceName;
            }

            public void setServiceName(String serviceName) {
                this.serviceName = serviceName;
            }

            private String[] methodNames;

            public String[] getMethodNames() {
                return this.methodNames;
            }

            public void setMethodNames(String[] methodNames) {
                this.methodNames = methodNames;
            }

            public Impl(Class<?> clazz) {
                serviceName = clazz.getSimpleName();
                ArrayList<String> meths = new ArrayList<String>();
                if (clazz != null) {
                    for (java.lang.reflect.Method meth : clazz.getMethods()) {
                        meths.add(meth.getName());
                    }
                }
                methodNames = meths.toArray(new String[meths.size()]);
            }
        }
    }

    /**
     * How a Player controls (or queries) the Game.
     * The useful methods are in gamma.spi.PPI
     */
    interface Controller {
        // PPI defines all the really good stuff.
    }

    /** Simple struct for multi-value return from PlayerRunnable that loads PlayerAI.
     * Cache everything that SI/Game wants to know about Player without going back to Player Thread.
     *
     */
    interface PlayerInfo {
        boolean isDone(); // is the PlayerRunnable that creates the PlayerAI done?
        PlayerAI getPlayerAI(); // from PlayerRunnable
        Controller getController(); // the associated Controller (== SI_Player)
        MessageSender getResponder();
        ClassLoader getPlayerClassLoader();
        ServiceDefinition getServiceDefinition();
        ClientServiceFactory getClientServiceFactory();
        Map<String, Object> getProps(); // Player Props
        DelayQueue<Object> getCmdDelayQueue();
        DelayQueue<Serializable> getRspDelayQueue();
        Long getGamePlayerId();
        String getRole(); // "A" or "B" or maybe "Gaia" or "Troll"
    }

    // extend to add setter methods. maybe use a Builder instead?
    /** mutable view of PlayerInfo. */
    interface PlayerInfo2 extends PlayerInfo {
        void setPlayerAI(PlayerAI playerAI);
        void setController(Controller controller);
        void setResponder(MessageSender sender);
        void setPlayerClassLoader(ClassLoader playerClassLoader);
        void setServiceDefinition(ServiceDefinition serviceDefinition);
        void setClientServiceFactory(ClientServiceFactory clientServiceFactory);

        void setProps(Map<String, Object> props);
        void setCmdDelayQueue(DelayQueue<Object> cmdDelayQueue);

        void setRspDelayQueue(DelayQueue<Serializable> rspDelayQueue);
        void setGamePlayerId(Long gpid);
        void setRole(String role); // record the role of this PlayerInfo

        static class Impl implements PlayerInfo2 {

            private boolean done; // is the PlayerRunnable that creates the PlayerAI done?

            public boolean isDone() {
                return this.done;
            }

            public void setDone(boolean done) {
                this.done = done;
            }

            private PlayerAI playerAI; // from PlayerRunnable

            public PlayerAI getPlayerAI() {
                return this.playerAI;
            }

            public void setPlayerAI(PlayerAI playerAI) {
                this.playerAI = playerAI;
            }

            private Controller controller; // the associated Controller (== SI_Player)

            public Controller getController() {
                return this.controller;
            }

            public void setController(Controller controller) {
                this.controller = controller;
            }

            private MessageSender responder;

            public MessageSender getResponder() {
                return this.responder;
            }

            public void setResponder(MessageSender responder) {
                this.responder = responder;
            }

            private ClassLoader playerClassLoader; //

            public ClassLoader getPlayerClassLoader() {
                return this.playerClassLoader;
            }

            public void setPlayerClassLoader(ClassLoader playerClassLoader) {
                this.playerClassLoader = playerClassLoader;
            }

            private ServiceDefinition serviceDefinition; //

            public ServiceDefinition getServiceDefinition() {
                return this.serviceDefinition;
            }

            public void setServiceDefinition(ServiceDefinition serviceDefinition) {
                this.serviceDefinition = serviceDefinition;
            }

            private ClientServiceFactory clientServiceFactory;

            public ClientServiceFactory getClientServiceFactory() {
                return this.clientServiceFactory;
            }

            public void setClientServiceFactory(ClientServiceFactory clientServiceFactory) {
                this.clientServiceFactory = clientServiceFactory;
            }

            private Map<String, Object> props; // Player Props

            public Map<String, Object> getProps() {
                return this.props;
            }

            public void setProps(Map<String, Object> props) {
                this.props = props;
            }

            private DelayQueue<Object> cmdDelayQueue; //

            public DelayQueue<Object> getCmdDelayQueue() {
                return this.cmdDelayQueue;
            }

            public void setCmdDelayQueue(DelayQueue<Object> cmdDelayQueue) {
                this.cmdDelayQueue = cmdDelayQueue;
            }

            private DelayQueue<Serializable> rspDelayQueue; //

            public DelayQueue<Serializable> getRspDelayQueue() {
                return this.rspDelayQueue;
            }

            public void setRspDelayQueue(DelayQueue<Serializable> rspDelayQueue) {
                this.rspDelayQueue = rspDelayQueue;
            }

            private Long gpid;

            public Long getGamePlayerId() {
                return this.gpid;
            }

            public void setGamePlayerId(Long gpid) {
                this.gpid = gpid;
            }

            private String role; // "A" or "B" or maybe "Gaia" or "Troll"

            public String getRole() {
                return this.role;
            }

            public void setRole(String role) {
                if (this.role != null) throw new IllegalStateException("Role has been set and is immutable: " + this.role);
                this.role = role;
            }

            public String toString() {
                return String.format("%s[%s]", super.toString(), getRole());
            }
        }
    }

    // TODO: maybe refactor, higher abstraction, consoliInstant with ServiceDefinition & MessageSender
    interface ClientServiceFactory {
        void createClientService(PlayerInfo2 playerInfo);
    }

    // TODO: gamma-main is loaded in both gammaDS and Game Server, this code should move to gamma-web.
    /** the responseBody sent to a responder. */
    interface EventBody extends Serializable {
        String getId();
        Serializable getData();

        static class Impl implements EventBody {

            private static final long serialVersionUID = 1L;

            public Impl(String eventId, Serializable data) {
                this.id = eventId;
                this.data = data;
            }

            public String id;

            public String getId() {
                return this.id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public Serializable data;

            public Serializable getData() {
                return this.data;
            }

            public void setData(Serializable data) {
                this.data = data;
            }

            public String toString() {
                return super.toString() + "[" + getId() + ":" + data.getClass().getSimpleName() + "]";
            }
        }
    }

    // TODO: redo to ensure DelayQueue is inaccessible
    // TODO: redo to work with WebSocket (and protobuf messaging)
    // give Player a MessageSender, so they can send back to the displayClient
    // This allows us to isolate the Spring Template or Flex MessageService;
    // keeping it out of the gamma
    // reappears as SPI.Responder - XXXXX
    // Note: gamma MessageSender assumes there is a DelayQueue between PlayerAI and DisplayClient.
    // Note: the Player.MessageSender can be _used_ [through Base_Player] but is not directly accessible [?]
    // Player must *not* be allowed to getDelayQueue()
    /**
     * MessageSender is the interface that PlayerAI sees, allowing PlayerAI to send message to its client.
     * Implies a working ClientService Connection and a ServiceDefinition.
     */
    interface MessageSender {
        void sendResponseData(String eventId, Serializable data);
        void sendResponseBody(Serializable data);

        static class Adapter implements MessageSender {

            @Override
            public void sendResponseData(String eventId, Serializable data) {}

            @Override
            public void sendResponseBody(Serializable data) {}
        }

        /** used by privileged code? */
        interface MessageSender_DQ extends MessageSender {
            /** the RspDelayQueue */
            public DelayQueue<Serializable> getDelayQueue();

            public void setDelayQueue(DelayQueue<Serializable> delayQueue);
        }

        /** This implementation sends iff hasDelayQueue(). */
        static class Adapter_DQ implements MessageSender.MessageSender_DQ {

            @Override
            public void sendResponseData(String eventId, Serializable data) {
                sendResponseBody(new LauncherGame.EventBody.Impl(eventId, data));
            }

            @Override
            public void sendResponseBody(Serializable body) {
                if (hasDelayQueue()) getDelayQueue().add(body);
            }

            @Override
            public DelayQueue<Serializable> getDelayQueue() {
                return delayQueue;
            }

            @Override
            public void setDelayQueue(DelayQueue<Serializable> delayQueue) {
                this.delayQueue = delayQueue;
            }

            protected boolean hasDelayQueue() {
                return delayQueue != null;
            }

            private DelayQueue<Serializable> delayQueue; // response DelayQueue
        }
    }

    /** Wrap some handy utility Methods. */
    static class Static {

        private static Logger log = LoggerFactory.getLogger(Static.class);

        ////////////////////////////////////////////////////////////
        // ClassLoader:
        ////////////////////////////////////////////////////////////

        /** Write classloader (and classpath) info to a Logger. */
        public static void logClassLoader(String name, ClassLoader cl) {
            logClassLoader(null, name, cl, true);
        }

        /** Write classloader (and classpath) info to a Logger. */
        public static void logClassLoader(Logger log, String name, ClassLoader cl) {
            logClassLoader(log, name, cl, true);
        }

        /** Write classloader (and classpath) info to a Logger.
         * @param log a Logger (or null, use Static.log)
         * @param name informational String to include in each log message line
         * @param cl the ClassLoader to be itemized
         * @param logPath if true, then also show internal element of a URLClassLoader
         */
        public static void logClassLoader(Logger log, String name, ClassLoader cl, boolean logPath) {
            if (log == null) log = Static.log;
            log.info("{}: ClassLoader={}, parent={}", name, cl, cl.getParent());
            if (
                logPath && (cl instanceof URLClassLoader)
            ) //	log.info("{}: classpath={}", name, Stringify.array("[\n ",((URLClassLoader)cl).getURLs(),",\n ","]",new StringBuilder()));
            for (URL url : ((URLClassLoader) cl).getURLs()) log.info("{}: classpath={}", name, url.toString());
        }

        public static void printClassLoader(String name, ClassLoader cl, boolean logPath) {
            if (logPath && (cl instanceof URLClassLoader)) {
                //System.out.printf("%s: classpath=%s", name, Stringify.array("[\n ",((URLClassLoader)cl).getURLs(),",\n ","]",new StringBuilder()));
                for (URL url : ((URLClassLoader) cl).getURLs()) {
                    System.out.printf("%s: classpath=%s%n", name, url.toString());
                }
            }
        }

        @CheckForNull
        public static URL makeURL(String url, String msg) {
            try {
                return new URL(url);
            } catch (MalformedURLException ex) {
                log.error(msg + "(" + url + ") ", ex);/* ignore until NPE */
            }
            return null;
        }

        // technique to resolve version conflicts, keeping each [dependent] jar in it own classloader
        // http://onjava.com/pub/a/onjava/2005/04/13/dependencies.html?page=2
        // Downloads/jar-dependencies.jar
        // or http://classworlds.codehaus.org/
        // or use OSGI

        // URLClassLoader with proper paths:
        /**
         * expand paths[] to URL[] using new URL(base,path) and inject into new URLClassLoader.
         */
        // 		static public URLClassLoader newURLClassLoader(URL baseURL, String[] paths, ClassLoader parent) {
        // 			URL[] urls = new URL[paths.length];
        // 			for (int i = 0 ; i < urls.length; i++) {
        // 				urls[i] = newURL(baseURL, paths[i]);
        // 			}
        // 			return new URLClassLoader(urls, parent);
        // 		}
        /** return new URL(base,path); logging any MalformedURLException */
        private static URL newURL(URL base, String path) {
            try {
                return new URL(base, path);
            } catch (MalformedURLException ex) {
                log.error("Bad URL: {}", ex);
                throw new IllegalArgumentException("MalformedURL", ex);
            }
        }

        private static URL fileURL(File file) {
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException ex) {
                log.error("Bad URL: {}", ex);
                throw new IllegalArgumentException("Bad URL", ex);
            }
        }

        /** extend baseURL with each of the given paths */
        public static URL[] expandPathToURLs(URL baseURL, String... paths) {
            URL[] urls = new URL[paths.length];
            for (int i = 0; i < urls.length; i++) {
                urls[i] = newURL(baseURL, paths[i]);
            }
            return urls;
        }

        /**
         * Convert String paths[] to (File) URL[], prepend root to relative path, expand /* into /*.jar.
         * @param base if non-null, prepend to each relative path
         * @param path include in returned path list, if endsWith("/*") include all "*.jar" files instead
         * @throws IllegalArgumentException if MalformedURLException
         */
        public static URL[] expandPathToFileURLs(String base, String... paths) {
            List<URL> pathURLs = new ArraySet<URL>();
            FilenameFilter jarFilter = new FileSuffixFilter(".jar");
            for (String path : paths) {
                if ((base != null) && !path.startsWith("/")) path = base + path;
                File file = new File(path);
                if (path.endsWith("/*")) { // or "*.jar" ??
                    file = new File(path.substring(0, path.length() - 1));
                    if (!file.exists()) {
                        log.warn("Skipping path: {}", file);
                        continue; // no directory, no jars
                    }
                    File[] jars = file.listFiles(jarFilter);
                    if (jars != null) {
                        for (File jar : jars) {
                            pathURLs.add(fileURL(jar));
                        }
                    }
                } else {
                    if (!file.exists()) {
                        log.warn("Skipping path: {}", file);
                        continue; // no file, not a jar
                    }
                    pathURLs.add(fileURL(file));
                }
            }
            return pathURLs.toArray(new URL[pathURLs.size()]);
        }

        ////////////////////////////////////////////////////////////
        // Reflection
        ////////////////////////////////////////////////////////////

        //
        // use Reflection to find and invoke named Class from specified ClassLoader:
        // (for classes that are not on this.getClass().getClassLoader()'s classpath)
        //
        /** Class.forName(name, resolve, cl).newInstance() throws IllegalArgumentException. */
        public static Class<?> getClass(String name, boolean resolve, ClassLoader cl) {
            try {
                return Reflect.getClass(name, resolve, cl);
            } catch (IllegalArgumentException ex1) {
                log.error(ex1.getMessage() + " {}", ex1);
                throw ex1;
            }
        }

        @SuppressWarnings({ "unused" })
        private static Method getMethod(Class<?> clazz, String methName, Object[] args) {
            try {
                Class<?>[] argClassAry = Reflect.objClassArray(args);
                return Reflect.lookupMethod(true, clazz, methName, argClassAry);
            } catch (NoSuchMethodException ex1) {
                log.error("invoke failure: {}", ex1);
                throw new IllegalArgumentException("invoke failure", ex1);
            }
        }

        /** clazz.newInstance() throws IllegalArgumentException */
        public static <T> T newInstance(Class<T> clazz) {
            try {
                return Reflect.newInstance(clazz); // using clazz.getClassLoader()
            } catch (IllegalArgumentException ex) {
                log.error(ex.getMessage(), ex);
                throw ex;
            }
        }

        /** Class.forName().newInstance() throws IllegalArgumentException */
        public static Object newInstance(String name, boolean resolve, ClassLoader cl) {
            try {
                return Reflect.newInstance(name, resolve, cl);
            } catch (IllegalArgumentException ex) {
                log.error(ex.getMessage(), ex.getCause());
                throw ex;
            }
        }

        // Invoke method on instance with args: Reflect.invoke(method, inst, args) uses setAccessibleIfAble()
        @SuppressWarnings("unused")
        private static Object invoke(Method meth, Object instance, Object[] args) {
            Throwable ex;
            try {
                return meth.invoke(instance, args);
            } catch (InvocationTargetException ex2) {
                ex = ex2;
                log.error("invoke exception: {}", ex2);
            } catch (IllegalAccessException ex3) {
                ex = ex3;
                log.error("bad access: {}", ex3);
            }
            throw new IllegalArgumentException("invoke failure", ex);
        }
    }

    /**
     * Games can callback through Launcher.TLS to create ClassLoader for Real/NPC players.
     */
    static class TLS {

        /** Identify the Client method currently being processed by this Thread;
         * which thread is typically the Dispatcher Thread of a DelayQueue.
         * gamma-web.ScopeAwareRemotingService deposits MethodId when processing a RPC call.
         * Perhaps could be better managed by AspectJ?
         */
        public static void setMethodId(String methodId) {
            TLS.methodId.set(methodId);
        }

        public static String getMethodId() {
            return methodId.get();
        }

        private static ThreadLocal<String> methodId = new ThreadLocal<String>();
    }
}
