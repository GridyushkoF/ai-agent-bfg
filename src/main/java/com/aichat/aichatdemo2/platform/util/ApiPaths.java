package com.aichat.aichatdemo2.platform.util;

public final class ApiPaths {

    private ApiPaths() {
    }

    public interface Auth {
        String BASE = "/api/v1/auth";
        String REGISTER = "/register";
        String LOGIN = "/login";

        static String register() {
            return BASE + REGISTER;
        }

        static String login() {
            return BASE + LOGIN;
        }
    }

    public interface Users {
        String BASE = "/api/v1/users";
        String ME = "/me";

        static String buildProfilePath() {
            return BASE + ME;
        }
    }

    public interface Chats {
        String BASE = "/api/v1/chats";
        String BY_ID = "/{chatId}";
        String MESSAGES = "/{chatId}/messages";

        static String buildChatPath(String chatId) {
            return (BASE + BY_ID).replace("{chatId}", chatId);
        }

        static String buildMessagesPath(String chatId) {
            return (BASE + MESSAGES).replace("{chatId}", chatId);
        }
    }

    public interface Docs {
        String SWAGGER_UI = "/swagger-ui.html";
        String SWAGGER = "/swagger-ui/**";
        String API_DOCS = "/v3/api-docs";
        String API_DOCS_ALL = "/v3/api-docs/**";
    }
}
