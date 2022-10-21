package com.thegraid.share;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
//import com.thegraid.share.auth.TicketService.Ticket;
import com.thegraid.share.domain.intf.IGameInstDTO;
import com.thegraid.share.domain.intf.IGameInstPropsDTO;
import java.time.Instant;

public interface LobbyLauncher {
    /** RequestBody received from Lobby: GameInst and Ticket */
    public static class LaunchInfo {

        // https://www.baeldung.com/spring-boot-customize-jackson-objectmapper#1-objectmapper

        public IGameInstDTO.Impl gameInst;
        public IGameInstPropsDTO gameProps;
        public Long gpidA;
        public Long gpidB;
        public String resultTicket;
    }

    /** a simple multi-value return with the GameControl URL and the StartTime */
    public interface LaunchResults {
        /** official game start time. */
        Instant getStarted();
        /** where client can contact the launched game instance gameControl. */
        String getHostURL();
        /** where client can connect to the ClientPlayer command/event queues. */
        String getWssURL();

        @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
        public static class Impl implements LaunchResults {

            public Impl() {}

            public Impl(IGameInstDTO gi) {
                this.started = gi.getStarted();
                this.hostUrl = gi.getHostUrl();
            }

            Instant started;
            String hostUrl;
            String wssUrl;

            public Instant getStarted() {
                return this.started;
            }

            public void setStarted(Instant started) {
                this.started = started;
            }

            public String getHostURL() {
                return this.hostUrl;
            }

            public void setHostURL(String hostUrl) {
                this.hostUrl = hostUrl;
            }

            public String getWssURL() {
                return this.wssUrl;
            }

            public void setWssURL(String wssUrl) {
                this.wssUrl = wssUrl;
            }
        }
    }

    public interface GameResults {
        boolean getAborted(); // true IFF game ended without a Winner
        long getId();
        Instant getFinished(); // when Game play stopped.
        Integer getScoreA();
        Integer getScoreB();
        Integer getTicks();

        // TODO access restrictions (or: reimplement as ImmutableMap) [is maybe internal only]
        /** DTO to send GameResults from Launcher to Lobby for recording. */
        static class Impl implements GameResults {

            public Impl() {}

            public Impl(Long id) {
                this.id = id;
            }

            public Impl(Long id, boolean aborted, Instant finished, Integer scoreA, Integer scoreB, Integer ticks) {
                this.id = id;
                this.aborted = aborted;
                this.finished = finished;
                this.scoreA = scoreA;
                this.scoreB = scoreB;
                this.ticks = ticks;
            }

            long id;

            @Override
            public long getId() {
                return this.id;
            }

            public void setId(long id) {
                this.id = id;
            }

            boolean aborted = true;

            @Override
            public boolean getAborted() {
                return this.aborted;
            }

            public void setAborted(boolean aborted) {
                this.aborted = aborted;
            }

            Instant finished;

            @Override
            public Instant getFinished() {
                return this.finished;
            }

            public void setFinished(Instant finished) {
                this.finished = finished;
            }

            Integer scoreA;

            @Override
            public Integer getScoreA() {
                return this.scoreA;
            }

            public void setScoreA(int scoreA) {
                this.scoreA = scoreA;
            }

            Integer scoreB;

            @Override
            public Integer getScoreB() {
                return this.scoreB;
            }

            public void setScoreB(int scoreB) {
                this.scoreB = scoreB;
            }

            Integer ticks;

            @Override
            public Integer getTicks() {
                return this.ticks;
            }

            public void setTicks(int ticks) {
                this.ticks = ticks;
            }
        }
    }
}
