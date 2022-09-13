package com.thegraid.share.domain.intf;

public interface IAssetDTO {
    //static class AAssetDTO extends AssetDTO implements IAssetDTO {}

    public Long getId();

    public void setId(Long id);

    public Integer getVersion();

    public void setVersion(Integer version);

    public String getName();

    public void setName(String name);

    public Boolean getMain();

    public void setMain(Boolean main);

    public Boolean getAuto();

    public void setAuto(Boolean auto);

    public String getPath();

    public void setPath(String path);

    public String getInclude();

    public void setInclude(String include);

    public IUserDTO getUser();

    public void setUser(IUserDTO user);
}
