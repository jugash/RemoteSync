package operators.grafana.remotesync.services;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

public class GrafanaService {

    private static final Logger logger = LoggerFactory.getLogger(GrafanaService.class);

    private final RestClient restClient;

    public GrafanaService(String grafanaUrl, String apiKey) {
        Objects.requireNonNull(grafanaUrl, "GRAFANA_URL must be set");
        Objects.requireNonNull(apiKey, "GRAFANA_API_KEY must be set");

        this.restClient = RestClient.builder()
                .baseUrl(grafanaUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    GrafanaService(RestClient restClient) {
        this.restClient = restClient;
    }

    public void syncDashboard(String dashboardJson) {
        try {
            String response = restClient.post()
                .uri("/api/dashboards/db")
                .body(dashboardJson)
                .retrieve()
                .body(String.class);
            logger.info("Dashboard synced successfully: {}", response != null ? response : "no response body");

        } catch (RestClientResponseException e) {
            logger.error("Failed to sync dashboard: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to sync dashboard: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error syncing dashboard to Grafana", e);
            throw new RuntimeException("Failed to sync dashboard: " + e.getMessage());
        }
    }

    public void syncAlertRule(String alertRuleJson) {
        try {
            String response = restClient.post()
                    .uri("/api/v1/provisioning/alert-rules")
                    .body(alertRuleJson)
                    .retrieve()
                    .body(String.class);

            logger.info("Alert rule synced successfully: {}", response != null ? response : "no response body");

        } catch (RestClientResponseException e) {
            logger.error("Failed to sync alert rule: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Error syncing alert rule to Grafana", e);
        }
    }

    public void deleteDashboard(String uid) {
        try {
            restClient.delete()
                    .uri("/api/dashboards/uid/{uid}", uid)
                    .retrieve();

            logger.info("Dashboard deleted successfully: {}", uid);

        } catch (RestClientResponseException e) {
            logger.error("Failed to delete dashboard {}: {} {}", uid, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Error deleting dashboard {} from Grafana", uid, e);
        }
    }

    public void deleteAlertRule(String uid) {
        try {
            restClient.delete()
                    .uri("/api/v1/provisioning/alert-rules/{uid}", uid)
                    .retrieve();

            logger.info("Alert rule deleted successfully: {}", uid);

        } catch (RestClientResponseException e) {
            logger.error("Failed to delete alert rule {}: {} {}", uid, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Error deleting alert rule {} from Grafana", uid, e);
        }
    }
}
