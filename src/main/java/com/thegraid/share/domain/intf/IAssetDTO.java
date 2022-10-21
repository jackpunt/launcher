package com.thegraid.share.domain.intf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties({ "version", "id", "user" })
@JsonDeserialize(as = IAssetDTO.Impl.class)
public interface IAssetDTO {
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

    public static class Impl implements IAssetDTO {

        private Long id;
        private Integer version;
        private String name;
        private boolean main;
        private boolean auto;
        private String path;
        private String include;
        private IUserDTO user;

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
        public Boolean getMain() {
            return main;
        }

        @Override
        public void setMain(Boolean main) {
            this.main = main;
        }

        @Override
        public Boolean getAuto() {
            return auto;
        }

        @Override
        public void setAuto(Boolean auto) {
            this.auto = auto;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public void setPath(String path) {
            this.path = path;
        }

        @Override
        public String getInclude() {
            return include;
        }

        @Override
        public void setInclude(String include) {
            this.include = include;
        }

        @Override
        public IUserDTO getUser() {
            return user;
        }

        @Override
        public void setUser(IUserDTO user) {
            this.user = user;
        }
    }
}
