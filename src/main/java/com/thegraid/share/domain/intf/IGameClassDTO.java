package com.thegraid.share.domain.intf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;

@JsonIgnoreProperties({ "version" })
@JsonDeserialize(as = IGameClassDTO.Impl.class)
public interface IGameClassDTO {
    public Long getId();

    // public Integer getVersion();

    public String getName();

    public String getRevision();

    public String getDocsPath();

    public String getLauncherPath();

    public String getGamePath();

    public String getPropNames();

    public Instant getUpdated();

    public void setId(Long id);

    // public void setVersion(Integer version);

    public void setName(String name);

    public void setRevision(String revision);

    public void setDocsPath(String docsPath);

    public void setLauncherPath(String launcherPath);

    public void setGamePath(String gamePath);

    public void setPropNames(String propNames);

    public void setUpdated(Instant updated);

    public static class Impl implements IGameClassDTO {

        private Long id;
        private String name;
        private String revision;
        private String docsPath;
        private String gamePath;
        private String launcherPath;
        private String propsNames;
        private Instant updated;

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getRevision() {
            return revision;
        }

        @Override
        public String getDocsPath() {
            return docsPath;
        }

        @Override
        public String getLauncherPath() {
            return launcherPath;
        }

        @Override
        public String getGamePath() {
            return gamePath;
        }

        @Override
        public String getPropNames() {
            return propsNames;
        }

        @Override
        public Instant getUpdated() {
            return updated;
        }

        @Override
        public void setId(Long id) {
            this.id = id;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public void setRevision(String revision) {
            this.revision = revision;
        }

        @Override
        public void setDocsPath(String docsPath) {
            this.docsPath = docsPath;
        }

        @Override
        public void setLauncherPath(String launcherPath) {
            this.launcherPath = launcherPath;
        }

        @Override
        public void setGamePath(String gamePath) {
            this.gamePath = gamePath;
        }

        @Override
        public void setPropNames(String propNames) {
            this.propsNames = propNames;
        }

        @Override
        public void setUpdated(Instant updated) {
            this.updated = updated;
        }
    }
}
