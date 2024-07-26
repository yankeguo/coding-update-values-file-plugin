package com.yankeguo.jenkins.plugins.modstate.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yankeguo.jenkins.plugins.modstate.Provider;
import com.yankeguo.jenkins.plugins.modstate.ProviderException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.Base64;
import java.util.List;
import java.util.Map;

public class CodingRepositoryProvider implements Provider {
    private final String username;
    private final String password;
    private final String tenant;
    private final String project;
    private final String repository;
    private final String branch;
    private final String path;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getTenant() {
        return tenant;
    }

    public String getProject() {
        return project;
    }

    public String getRepository() {
        return repository;
    }

    public String getBranch() {
        return branch;
    }

    public String getPath() {
        return path;
    }

    private CodingHybridResponse.Response invokeAPI(String action, Map<String, Object> data) throws ProviderException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            ObjectMapper objectMapper = new ObjectMapper();

            HttpPost httpPost = new HttpPost("https://e.coding.net/open-api/" + action + "?Action=" + action);
            httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(data)));
            httpPost.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((getUsername() + ":" + getPassword()).getBytes()));
            httpPost.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {

                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    throw new ProviderException("HTTP failed: no response body");
                }
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new ProviderException("HTTP failed: " + response.getStatusLine().getStatusCode());
                }
                String responseString = EntityUtils.toString(entity);

                CodingHybridResponse hr = objectMapper.readValue(responseString, CodingHybridResponse.class);
                if (hr == null) {
                    throw new ProviderException("HTTP failed: response is null");
                }
                CodingHybridResponse.Response hrr = hr.getResponse();
                if (hrr.getError() != null) {
                    throw new ProviderException("HTTP failed: " + hrr.getError().getCode() + ": " + hrr.getError().getMessage());
                }
                return hrr;
            } catch (Exception e) {
                throw new ProviderException("HTTP failed", e);
            }

        } catch (Exception e) {
            throw new ProviderException("HTTP failed", e);
        }
    }

    /**
     * Constructor
     *
     * @param username username
     * @param password password
     * @param target   target, in format of "company/project/repository@branch:path/to/file.json"
     * @throws ProviderException ProviderException
     */
    public CodingRepositoryProvider(String username, String password, String target) throws ProviderException {
        this.username = username;
        this.password = password;

        // decode target
        String[] split = target.split("@", 2);
        if (split.length != 2) {
            throw new ProviderException("Target malformed: " + target);
        }

        String[] split2 = split[0].split("/", 3);
        if (split2.length != 3) {
            throw new ProviderException("Target malformed: " + target);
        }
        this.tenant = split2[0];
        this.project = split2[1];
        this.repository = split2[2];

        String[] split3 = split[1].split(":", 2);
        if (split3.length != 2) {
            throw new ProviderException("Target malformed: " + target);
        }
        this.branch = split3[0];
        this.path = split3[1];
    }

    @Override
    public Object fetch() throws ProviderException {
        CodingHybridResponse.Response res = invokeAPI("DescribeGitFile", Map.of("DepotPath", getTenant() + "/" + getProject() + "/" + getRepository(), "Ref", getBranch(), "Path", getPath()));
        CodingHybridResponse.Response.GitFile gf = res.getGitFile();

        if (gf == null) {
            throw new ProviderException("'GitFile' is missing");
        }
        if (!"base64".equalsIgnoreCase(gf.getEncoding())) {
            throw new ProviderException("Encoding not supported: " + gf.getEncoding());
        }
        String content = gf.getContent();
        if (content == null || content.isBlank()) {
            throw new ProviderException("'Content' is missing");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(Base64.getDecoder().decode(content), Object.class);
        } catch (Exception e) {
            throw new ProviderException("Failed to unmarshal value", e);
        }
    }

    @Override
    public void update(Object data) throws ProviderException {
        ObjectMapper objectMapper = new ObjectMapper();
        String content;
        try {
            content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        } catch (Exception e) {
            throw new ProviderException("Failed to marshal value", e);
        }

        CodingHybridResponse.Response response = invokeAPI("DescribeGitCommitInfos", Map.of("DepotPath", getTenant() + "/" + getProject() + "/" + getRepository(), "Ref", getBranch(), "PageNumber", 1, "PageSize", 1));
        List<CodingHybridResponse.Response.Commit> commits = response.getCommits();
        if (commits == null || commits.isEmpty()) {
            throw new ProviderException("No commits found in branch: " + getBranch());
        }
        String sha = commits.get(0).getSha();
        if (sha == null || sha.isBlank()) {
            throw new ProviderException("No commit hash found for branch: " + getBranch());
        }
        CodingHybridResponse.Response response1 = invokeAPI("ModifyGitFiles", Map.of("DepotPath", getTenant() + "/" + getProject() + "/" + getRepository(), "Ref", getBranch(), "LastCommitSha", sha, "Message", "ci: update " + getPath(), "GitFiles", List.of(Map.of("Path", getPath(), "Content", content))));
        CodingHybridResponse.Response.Commit gitCommit = response1.getGitCommit();
        if (gitCommit == null || gitCommit.getSha() == null || gitCommit.getSha().isBlank()) {
            throw new ProviderException("Failed to create commit");
        }
    }
}
