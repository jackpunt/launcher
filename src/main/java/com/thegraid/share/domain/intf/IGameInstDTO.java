package com.thegraid.share.domain.intf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

@JsonIgnoreProperties({ "version" })
@JsonDeserialize(as = IGameInstDTO.Impl.class)
public interface IGameInstDTO extends Serializable {
    public static String Role_A = "A";
    public static String Role_B = "B";

    public Long getId();

    public IGameClassDTO getGameClass();

    public IPlayerDTO getPlayerA();

    public IPlayerDTO getPlayerB();

    public String getGameName();

    public String getHostUrl();

    public String getPasscode();

    // findGameInstProps(this.getId()).parseJSON().asMap()
    public Map<String, Object> getPropertyMap();

    public IGameInstPropsDTO getProps();

    public Integer getScoreA();

    public Integer getScoreB();

    public Integer getTicks();

    public Instant getCreated();

    public Instant getStarted();

    public Instant getUpdated();

    public Instant getFinished();

    public void setId(Long id);

    public void setGameClass(IGameClassDTO gameClass);

    public void setPlayerA(IPlayerDTO playerA);

    public void setPlayerB(IPlayerDTO playerB);

    public void setGameName(String gameName);

    public void setHostUrl(String hostUrl);

    public void setPasscode(String passcode);

    public void setProperyMap(Map<String, Object> propertyMap);

    public void setProps(IGameInstPropsDTO props);

    public void setScoreA(Integer scoreA);

    public void setScoreB(Integer scoreB);

    public void setTicks(Integer ticks);

    public void setCreated(Instant created);

    public void setStarted(Instant started);

    public void setUpdated(Instant updated);

    public void setFinished(Instant finished);

    public static class Impl implements IGameInstDTO {

        private Long id;
        private IGameClassDTO gameClass;

        @JsonIgnoreProperties(value = { "gameClass", "user" }, allowSetters = true)
        private IPlayerDTO playerA;

        @JsonIgnoreProperties(value = { "gameClass", "user" }, allowSetters = true)
        private IPlayerDTO playerB;

        private String gameName;
        private String hostUrl;
        private String passcode;
        private Map<String, Object> propertyMap;

        @JsonIgnoreProperties(value = { "gameInst" }, allowSetters = true)
        private IGameInstPropsDTO props;

        private Integer scoreA;
        private Integer scoreB;
        private Integer ticks;
        private Instant created;
        private Instant started;
        private Instant updated;
        private Instant finished;

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public IGameClassDTO getGameClass() {
            return gameClass;
        }

        @Override
        public IPlayerDTO getPlayerA() {
            return playerA;
        }

        @Override
        public IPlayerDTO getPlayerB() {
            return playerB;
        }

        @Override
        public String getGameName() {
            return gameName;
        }

        @Override
        public String getHostUrl() {
            return hostUrl;
        }

        @Override
        public String getPasscode() {
            return passcode;
        }

        @Override
        public Map<String, Object> getPropertyMap() {
            return propertyMap;
        }

        @Override
        public IGameInstPropsDTO getProps() {
            return props;
        }

        @Override
        public Integer getScoreA() {
            return scoreA;
        }

        @Override
        public Integer getScoreB() {
            return scoreB;
        }

        @Override
        public Integer getTicks() {
            return ticks;
        }

        @Override
        public Instant getCreated() {
            return created;
        }

        @Override
        public Instant getStarted() {
            return started;
        }

        @Override
        public Instant getUpdated() {
            return updated;
        }

        @Override
        public Instant getFinished() {
            return finished;
        }

        @Override
        public void setId(Long id) {
            this.id = id;
        }

        @Override
        public void setGameClass(IGameClassDTO gameClass) {
            this.gameClass = gameClass;
        }

        @Override
        public void setPlayerA(IPlayerDTO playerA) {
            this.playerA = playerA;
        }

        @Override
        public void setPlayerB(IPlayerDTO playerB) {
            this.playerB = playerB;
        }

        @Override
        public void setGameName(String gameName) {
            this.gameName = gameName;
        }

        @Override
        public void setHostUrl(String hostUrl) {
            this.hostUrl = hostUrl;
        }

        @Override
        public void setPasscode(String passcode) {
            this.passcode = passcode;
        }

        @Override
        public void setProperyMap(Map<String, Object> propertyMap) {
            this.propertyMap = propertyMap;
        }

        @Override
        public void setProps(IGameInstPropsDTO props) {
            this.props = props;
        }

        @Override
        public void setScoreA(Integer scoreA) {
            this.scoreA = scoreA;
        }

        @Override
        public void setScoreB(Integer scoreB) {
            this.scoreB = scoreB;
        }

        @Override
        public void setTicks(Integer ticks) {
            this.ticks = ticks;
        }

        @Override
        public void setCreated(Instant created) {
            this.created = created;
        }

        @Override
        public void setStarted(Instant started) {
            this.started = started;
        }

        @Override
        public void setUpdated(Instant updated) {
            this.updated = updated;
        }

        @Override
        public void setFinished(Instant finished) {
            this.finished = finished;
        }

        static ObjectMapper mapper = JsonMapper
            .builder()
            .addModule(new JavaTimeModule())
            .build()
            .setDefaultPropertyInclusion(Include.NON_NULL);

        public String toString() {
            try {
                return mapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON conversion failed", e);
            }
        }
    }
}
