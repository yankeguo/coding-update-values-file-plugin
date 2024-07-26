package com.yankeguo.jenkins.plugins.updateremotefile.providers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CodingHybridResponse {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Error {

            @JsonProperty("Code")
            private String code;

            @JsonProperty("Message")
            private String message;

            public String getCode() {
                return code;
            }

            public void setCode(String code) {
                this.code = code;
            }

            public String getMessage() {
                return message;
            }

            public void setMessage(String message) {
                this.message = message;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class GitFile {
            @JsonProperty("Encoding")
            private String encoding;

            @JsonProperty("Content")
            private String content;

            public String getEncoding() {
                return encoding;
            }

            public void setEncoding(String encoding) {
                this.encoding = encoding;
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Commit {
            @JsonProperty("Sha")
            private String sha;

            public String getSha() {
                return sha;
            }

            public void setSha(String sha) {
                this.sha = sha;
            }
        }

        @JsonProperty("Error")
        private Error error;

        public Error getError() {
            return error;
        }

        public void setError(Error error) {
            this.error = error;
        }

        @JsonProperty("GitFile")
        private GitFile gitFile;

        public GitFile getGitFile() {
            return gitFile;
        }

        public void setGitFile(GitFile gitFile) {
            this.gitFile = gitFile;
        }

        @JsonProperty("Commits")
        private List<Commit> commits;

        public List<Commit> getCommits() {
            return commits;
        }

        public void setCommits(List<Commit> commits) {
            this.commits = commits;
        }

        @JsonProperty("GitCommit")
        private Commit GitCommit;

        public Commit getGitCommit() {
            return GitCommit;
        }

        public void setGitCommit(Commit gitCommit) {
            GitCommit = gitCommit;
        }
    }

    @JsonProperty("Response")
    private Response response;

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
