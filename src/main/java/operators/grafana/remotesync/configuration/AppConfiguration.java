package operators.grafana.remotesync.configuration;

import operators.grafana.remotesync.controller.GrafanaDashboardReconciler;
import operators.grafana.remotesync.services.GrafanaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfiguration {
    @Bean
    public GrafanaService grafanaService(Environment environment) {
        var restClient = RestClient.builder()
                .baseUrl(environment.getRequiredProperty("grafana.url"))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + environment.getRequiredProperty("grafana.api-key"))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        return new GrafanaService(restClient);
    }

    @Bean
    public GrafanaDashboardReconciler grafanaDashboardReconciler(GrafanaService grafanaService) {
        return new GrafanaDashboardReconciler(grafanaService);
    }
}
