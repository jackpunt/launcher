package com.thegraid.share.domain.intf;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

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

    public Integer getScoreA();

    public Integer getScoreB();

    public Integer getTicks();

    public Instant getCreated();

    public Instant getStarted();

    public Instant getFinished();

    public void setId(Long id);

    public void setGameClass(IGameClassDTO gameClass);

    public void setPlayerA(IPlayerDTO playerA);

    public void setPlayerB(IPlayerDTO playerB);

    public void setGameName(String gameName);

    public void setHostUrl(String hostUrl);

    public void setPasscode(String passcode);

    public void setScoreA(Integer scoreA);

    public void setScoreB(Integer scoreB);

    public void setTicks(Integer ticks);

    public void setCreated(Instant created);

    public void setStarted(Instant started);

    public void setFinished(Instant finished);

    public static class Impl implements IGameInstDTO {

        private Long id;
        private IGameClassDTO gameClass;
        private IPlayerDTO playerA;
        private IPlayerDTO playerB;
        private String gameName;
        private String hostUrl;
        private String password;
        private Map<String, Object> propertyMap;
        private Integer scoreA;
        private Integer scoreB;
        private Integer ticks;
        private Instant created;
        private Instant started;
        private Instant finished;

        @Override
        public Long getId() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public IGameClassDTO getGameClass() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public IPlayerDTO getPlayerA() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public IPlayerDTO getPlayerB() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getGameName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getHostUrl() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getPasscode() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Map<String, Object> getPropertyMap() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Integer getScoreA() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Integer getScoreB() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Integer getTicks() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Instant getCreated() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Instant getStarted() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Instant getFinished() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setId(Long id) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setGameClass(IGameClassDTO gameClass) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setPlayerA(IPlayerDTO playerA) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setPlayerB(IPlayerDTO playerB) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setGameName(String gameName) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setHostUrl(String hostUrl) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setPasscode(String passcode) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setScoreA(Integer scoreA) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setScoreB(Integer scoreB) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setTicks(Integer ticks) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setCreated(Instant created) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setStarted(Instant started) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setFinished(Instant finished) {
            // TODO Auto-generated method stub

        }
    }
}
