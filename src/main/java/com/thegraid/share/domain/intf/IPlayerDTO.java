package com.thegraid.share.domain.intf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;

@JsonIgnoreProperties({ "version", "id" })
@JsonDeserialize(as = IPlayerDTO.Impl.class)
/** service.dto.PlayerDTO implements IPlayerDTO */
public interface IPlayerDTO {
    //static class APlayerPDTO extends PlayerDTO implements IPlayerDTO {}
    public Long getGpid(); // id from GamePlayer; distinct from PlayerId.

    public void setGpid(Long gpid);

    public Long getId();

    public void setId(Long id);

    public Integer getVersion();

    public void setVersion(Integer version);

    public String getName();

    public void setName(String name);

    public Integer getRank();

    public void setRank(Integer rank);

    public Integer getScore();

    public void setScore(Integer score);

    public Instant getScoreTime();

    public void setScoreTime(Instant scoreTime);

    public Instant getRankTime();

    public void setRankTime(Instant rankTime);

    public String getDisplayClient();

    public void setDisplayClient(String displayClient);

    // public IGameClassDTO getGameClass();
    // public void setGameClass(IGameClassDTO gameClass);
    // public IAssetDTO getMainJar();
    // public void setMainJar(IAssetDTO mainJar);
    // public IUserDTO getUser();
    // public void setUser(IUserDTO user);

    public static class Impl implements IPlayerDTO {

        private Long id;
        private Long gpid;
        private Integer version;
        private String name;
        private Integer rank;
        private Integer score;
        private Instant scoreTime;
        private Instant rankTime;
        private String displayClient;

        @Override
        public Long getGpid() {
            return gpid;
        }

        @Override
        public void setGpid(Long gpid) {
            this.gpid = gpid;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public void setId(Long id) {
            this.id = id;
        }

        @Override
        public Integer getVersion() {
            return version;
        }

        @Override
        public void setVersion(Integer version) {
            this.version = version;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public Integer getRank() {
            return rank;
        }

        @Override
        public void setRank(Integer rank) {
            this.rank = rank;
        }

        @Override
        public Integer getScore() {
            return score;
        }

        @Override
        public void setScore(Integer score) {
            this.score = score;
        }

        @Override
        public Instant getScoreTime() {
            return scoreTime;
        }

        @Override
        public void setScoreTime(Instant scoreTime) {
            this.scoreTime = scoreTime;
        }

        @Override
        public Instant getRankTime() {
            return rankTime;
        }

        @Override
        public void setRankTime(Instant rankTime) {
            this.rankTime = rankTime;
        }

        @Override
        public String getDisplayClient() {
            return displayClient;
        }

        @Override
        public void setDisplayClient(String displayClient) {
            this.displayClient = displayClient;
        }
    }
}
