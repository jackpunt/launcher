package com.thegraid.share.domain.intf;

import java.time.Instant;

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
}
