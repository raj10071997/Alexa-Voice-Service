package com.game.dhanraj.myownalexa.DirectivesandEvents;

/**
 * Created by Dhanraj on 01-06-2017.
 */

public class Event {

    Header header;
    Payload payload;

    public static class Header{
        String namespace;
        String name;
        String messageId;
        String dialogRequestId;

        public String getDialogRequestId() {
            return dialogRequestId;
        }

        public void setDialogRequestId(String dialogRequestId) {
            this.dialogRequestId = dialogRequestId;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }
    }

    public static class Payload{
        String token;
        String profile;
        String format;
        Boolean muted;
        Long volume;
        Long offsetInMilliseconds;

        public String getProfile() {
            return profile;
        }

        public String getFormat() {
            return format;
        }

    }

    public static class Builder{
        Event event = new Event();
        Header header = new Header();
        Payload payload = new Payload();
    }
}
