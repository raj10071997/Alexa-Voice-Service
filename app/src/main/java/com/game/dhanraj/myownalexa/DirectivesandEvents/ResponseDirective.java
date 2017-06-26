package com.game.dhanraj.myownalexa.DirectivesandEvents;

/**
 * Created by Dhanraj on 06-06-2017.
 */

public class ResponseDirective {
    private Directive directive;

    public Directive getDirective() {
        return directive;
    }

    public void setDirective(Directive directive) {
        this.directive = directive;
    }

   public static class Directive {
        private Header header;
        private Payload payload;

        public Header getHeader() {
            return header;
        }

        public void setHeader(Header header) {
            this.header = header;
        }

        public Payload getPayload() {
            return payload;
        }

        public void setPayload(Payload payload) {
            this.payload = payload;
        }

       public static class Header {
            private String namespace;
            private String name;
            private String messageId;
            private String dialogRequestId;

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

            public String getDialogRequestId() {
                return dialogRequestId;
            }

            public void setDialogRequestId(String dialogRequestId) {
                this.dialogRequestId = dialogRequestId;
            }
        }

        public static class Payload {
            private String url;
            private String format;
            private String token;
            private long timeoutInMilliseconds;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getFormat() {
                return format;
            }

            public void setFormat(String format) {
                this.format = format;
            }

            public String getToken() {
                return token;
            }

            public void setToken(String token) {
                this.token = token;
            }

            public long getTimeoutInMilliseconds() {
                return timeoutInMilliseconds;
            }

            public void setTimeoutInMilliseconds(int timeoutInMilliseconds) {
                this.timeoutInMilliseconds = timeoutInMilliseconds;
            }
        }

    }
}



