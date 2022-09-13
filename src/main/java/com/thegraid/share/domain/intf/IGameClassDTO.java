package com.thegraid.share.domain.intf;

public interface IGameClassDTO {
    public Long getId();

    // public Integer getVersion();
    public String getName();

    public String getRevision();

    public String getLauncherPath();

    public String getGamePath();

    // public String getDocsPath();
    public String getPropNames();

    public void setId(Long id);

    // public void setVersion(Integer version);
    public void setName(String name);

    public void setRevision(String revision);

    public void setLauncherPath(String launcherPath);

    public void setGamePath(String gamePath);

    // public void setDocsPath(String docsPath);
    public void setPropNames(String propNames);
}
