package com.thegraid.share.domain.intf;

import java.time.Instant;

/** service.dto.PlayerDTO implements IPlayerDTO */
public interface IPlayerDTO {
    //static class APlayerPDTO extends PlayerDTO implements IPlayerDTO {}
    public Long getGpid();

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

    public IGameClassDTO getGameClass();

    public void setGameClass(IGameClassDTO gameClass);

    public IAssetDTO getMainJar();

    public void setMainJar(IAssetDTO mainJar);

    public IUserDTO getUser();

    public void setUser(IUserDTO user);
}
