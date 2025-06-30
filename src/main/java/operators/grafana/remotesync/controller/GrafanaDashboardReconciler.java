package operators.grafana.remotesync.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;

import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import operators.grafana.remotesync.crds.RSGrafanaDashboardStatus;
import operators.grafana.remotesync.crds.Status;
import operators.grafana.remotesync.services.GrafanaService;
import operators.grafana.remotesync.crds.RSGrafanaDashboard;
import operators.grafana.remotesync.utils.ChecksumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@ControllerConfiguration
public class GrafanaDashboardReconciler implements Reconciler<RSGrafanaDashboard> {

    private static final Logger log = LoggerFactory.getLogger(GrafanaDashboardReconciler.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final GrafanaService grafanaService;

    public GrafanaDashboardReconciler(GrafanaService grafanaService) {
        this.grafanaService = grafanaService;
    }

    @Override
    public UpdateControl<RSGrafanaDashboard> reconcile(RSGrafanaDashboard resource, Context<RSGrafanaDashboard> context) throws Exception {
        String uid = resource.getMetadata().getUid();
        RSGrafanaDashboardStatus status = resource.getStatus();
        String dashboardJson = replaceUid(resource.getSpec().json(), uid);
        String checksum = ChecksumUtils.computeChecksum(dashboardJson);

        if (status != null && checksum.equals(status.getLastSyncedChecksum())) {
            log.info("Dashboard already synced, skipping.");
            return UpdateControl.noUpdate();
        }

        try {
            grafanaService.syncDashboard(dashboardJson);
            RSGrafanaDashboardStatus newStatus = new RSGrafanaDashboardStatus();
            newStatus.setUid(uid);
            newStatus.setLastSyncedChecksum(checksum);
            newStatus.setState(Status.SYNCED);
            newStatus.setMessage("Dashboard synced successfully");
            resource.setStatus(newStatus);
            return UpdateControl.patchStatus(resource);
        } catch (Exception e) {
            log.error("Failed to sync dashboard", e);
            RSGrafanaDashboardStatus failedStatus = new RSGrafanaDashboardStatus();
            failedStatus.setState(Status.ERROR);
            failedStatus.setMessage(e.getMessage());
            resource.setStatus(failedStatus);
            return UpdateControl.patchStatus(resource);
        }
    }

    private String replaceUid(String dashboardJson, String resourceUid) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(dashboardJson);
            if (root == null || !root.isObject()) {
                throw new IllegalArgumentException("Missing 'dashboard' field in JSON");
            }

            ObjectNode wrapper = OBJECT_MAPPER.createObjectNode();
            wrapper.set("dashboard", root);
            wrapper.put("overwrite", true);

            ObjectNode dashboardObj = (ObjectNode) root;
            dashboardObj.put("uid", resourceUid);
            wrapper.set("dashboard", dashboardObj);

            return OBJECT_MAPPER.writeValueAsString(wrapper);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to replace UID in dashboard JSON", e);
        }
    }


}
