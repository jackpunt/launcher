package com.thegraid.share.domain.intf;

public interface IGamePlayerDTO {
    public Long getId();

    public void setId(Long id);

    public Integer getVersion();

    public void setVersion(Integer version);

    public String getRole();

    public void setRole(String role);

    public Integer getReady();

    public void setReady(Integer ready);

    public IGameInstDTO getGameInst();

    public void setGameInst(IGameInstDTO gameInst);

    public IPlayerDTO getPlayer();

    public void setPlayer(IPlayerDTO player);
}
