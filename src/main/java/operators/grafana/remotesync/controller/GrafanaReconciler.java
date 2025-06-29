package operators.grafana.remotesync.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;

import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import operators.grafana.remotesync.crds.RSGrafanaDashboardStatus;
import operators.grafana.remotesync.services.GrafanaService;
import operators.grafana.remotesync.crds.RSGrafanaDashboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@ControllerConfiguration
public class GrafanaReconciler implements Reconciler<RSGrafanaDashboard> {

    private static final Logger log = LoggerFactory.getLogger(GrafanaReconciler.class);
    private final GrafanaService grafanaService;

    public GrafanaReconciler(GrafanaService grafanaService) {
        this.grafanaService = grafanaService;
    }

    @Override
    public UpdateControl<RSGrafanaDashboard> reconcile(RSGrafanaDashboard resource, Context<RSGrafanaDashboard> context) throws Exception {
        String dashboardJson = resource.getSpec().json();

        String uid = extractUid(dashboardJson, resource.getMetadata().getUid()); // Implement this method to parse UID
        String checksum = computeChecksum(dashboardJson); // MD5 or SHA-256

        // Check if this dashboard is already synced
        RSGrafanaDashboardStatus status = resource.getStatus();
        if (status != null && checksum.equals(status.getLastSyncedChecksum())) {
            log.info("Dashboard already synced, skipping.");
            return UpdateControl.noUpdate();
        }

        try {
            grafanaService.syncDashboard(dashboardJson);
            RSGrafanaDashboardStatus newStatus = new RSGrafanaDashboardStatus();
            newStatus.setUid(uid);
            newStatus.setLastSyncedChecksum(checksum);
            newStatus.setState("Synced");
            newStatus.setMessage("Dashboard synced successfully");

            resource.setStatus(newStatus);
            return UpdateControl.updateResource(resource);
        } catch (Exception e) {
            log.error("Failed to sync dashboard", e);
            RSGrafanaDashboardStatus failedStatus = new RSGrafanaDashboardStatus();
            failedStatus.setState("Error");
            failedStatus.setMessage(e.getMessage());
            resource.setStatus(failedStatus);
            return UpdateControl.updateResource(resource);
        }
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String extractUid(String dashboardJson, String defaultUid) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(dashboardJson);
            JsonNode uidNode = root.path("dashboard").path("uid");

            if (!uidNode.isMissingNode() && !uidNode.isNull() && uidNode.isTextual()) {
                return uidNode.asText();
            } else {
                // UID is not present in the dashboard JSON, fallback
                return defaultUid;
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON provided for dashboard: " + e.getMessage(), e);
        }
    }


    private String computeChecksum(String dashboardJson) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(dashboardJson.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
