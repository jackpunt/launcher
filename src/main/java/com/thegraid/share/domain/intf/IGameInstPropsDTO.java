package com.thegraid.share.domain.intf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;

@JsonIgnoreProperties({ "version", "id", "gameInst" })
@JsonDeserialize(as = IGameInstPropsDTO.Impl.class)
public interface IGameInstPropsDTO {
    public Long getId();

    public Integer getVersion();

    public void setVersion(Integer version);

    public Long getSeed();

    public void setSeed(Long seed);

    public String getMapName();

    public void setMapName(String mapName);

    public Integer getMapSize();

    public void setMapSize(Integer mapSize);

    public Integer getNpcCount();

    public void setNpcCount(Integer npcCount);

    public String getJsonProps();

    public void setJsonProps(String jsonProps);

    public Instant getUpdated();

    public void setUpdated(Instant updated);

    public IGameInstDTO getGameInst();

    public void setGameInst(IGameInstDTO gameInst);

    public static class Impl implements IGameInstPropsDTO {

        private Long id;
        private Integer version;
        private Long seed;
        private String mapName;
        private Integer mapSize;
        private Integer npcCount;
        private String jsonProps;
        private Instant updated;
        private IGameInstDTO gameInst;

        @Override
        public Long getId() {
            return id;
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
        public Long getSeed() {
            return seed;
        }

        @Override
        public void setSeed(Long seed) {
            this.seed = seed;
        }

        @Override
        public String getMapName() {
            return mapName;
        }

        @Override
        public void setMapName(String mapName) {
            this.mapName = mapName;
        }

        @Override
        public Integer getMapSize() {
            return mapSize;
        }

        @Override
        public void setMapSize(Integer mapSize) {
            this.mapSize = mapSize;
        }

        @Override
        public Integer getNpcCount() {
            return npcCount;
        }

        @Override
        public void setNpcCount(Integer npcCount) {
            this.npcCount = npcCount;
        }

        @Override
        public String getJsonProps() {
            return jsonProps;
        }

        @Override
        public void setJsonProps(String jsonProps) {
            this.jsonProps = jsonProps;
        }

        @Override
        public Instant getUpdated() {
            return updated;
        }

        @Override
        public void setUpdated(Instant updated) {
            this.updated = updated;
        }

        @Override
        public IGameInstDTO getGameInst() {
            return gameInst;
        }

        @Override
        public void setGameInst(IGameInstDTO gameInst) {
            this.gameInst = gameInst;
        }
    }
}
