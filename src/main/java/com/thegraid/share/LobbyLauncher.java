package com.thegraid.share;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thegraid.gamma.domain.GameInst;
//import com.thegraid.share.auth.TicketService.Ticket;
import com.thegraid.share.domain.intf.IGameInstDTO;
import java.time.Instant;

public interface LobbyLauncher {
    /** RequestBody received from Lobby: GameInst and Ticket */
    public static class LaunchInfo {

        static ObjectMapper mapper = new ObjectMapper().setDefaultPropertyInclusion(Include.NON_NULL);

        public String toString() {
            try {
                return mapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON conversion failed", e);
            }
        }

        public GameInst gameInst;
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

            Impl() {}

            public Impl(GameInst gi) {
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
            // are these needed? or will Spring provide with jackson?
            // public String toJson() {
            // 	return toJson(false);
            // }
            // public String toJson(boolean includeClass) {
            // 	JSONSerializer jsrl = new JSONSerializer();
            // 	if (!includeClass)
            // 		jsrl.exclude("class");
            // 	// jsrl.include("started", "hostURL");
            // 	return jsrl.serialize(this);
            // }
            // public static Impl fromJson(String json) {
            // 	Impl impl = new JSONDeserializer<Impl>().deserialize(json);
            // 	return impl;
            // }
            //public String toString() { return toJson(); }
        }
    }
}
