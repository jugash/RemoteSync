package operators.grafana.remotesync.configuration;

import operators.grafana.remotesync.controller.GrafanaDashboardReconciler;
import operators.grafana.remotesync.services.GrafanaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class AppConfiguration {
    @Bean
    public GrafanaService grafanaService(Environment environment) {
        return new GrafanaService(environment.getRequiredProperty("grafana.url"), environment.getRequiredProperty("grafana.api-key"));
    }

    @Bean
    public GrafanaDashboardReconciler grafanaDashboardReconciler(GrafanaService grafanaService) {
        return new GrafanaDashboardReconciler(grafanaService);
    }
}
