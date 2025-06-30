package operators.grafana.remotesync.controller;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import operators.grafana.remotesync.crds.RSGrafanaDashboard;
import operators.grafana.remotesync.crds.RSGrafanaDashboardSpec;
import operators.grafana.remotesync.crds.RSGrafanaDashboardStatus;
import operators.grafana.remotesync.crds.Status;
import operators.grafana.remotesync.services.GrafanaService;
import operators.grafana.remotesync.utils.ChecksumUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GrafanaDashboardReconcilerTest {

    private GrafanaService grafanaService;
    private GrafanaDashboardReconciler reconciler;

    @BeforeEach
    public void setup() {
        grafanaService = mock(GrafanaService.class);
        reconciler = new GrafanaDashboardReconciler(grafanaService);
    }

    @Test
    public void testSyncDashboardSuccessfully() throws Exception {
        RSGrafanaDashboard resource = createDashboard("test-uid", "{}");

        UpdateControl<RSGrafanaDashboard> result = reconciler.reconcile(resource, mock(Context.class));

        assertNotNull(result);
        assertNotNull(resource.getStatus());
        assertEquals(Status.SYNCED, resource.getStatus().getState());
        assertEquals("Dashboard synced successfully", resource.getStatus().getMessage());

        verify(grafanaService, times(1)).syncDashboard(any());
    }

    @Test
    public void testSkipIfAlreadySynced() throws Exception {
        RSGrafanaDashboard resource = createDashboard("test-uid", "{}");
        String checksum = ChecksumUtils.computeChecksum("{\"dashboard\":{\"uid\":\"test-uid\"},\"overwrite\":true}");

        RSGrafanaDashboardStatus status = new RSGrafanaDashboardStatus();
        status.setLastSyncedChecksum(checksum);
        resource.setStatus(status);

        UpdateControl<RSGrafanaDashboard> result = reconciler.reconcile(resource, mock(Context.class));

        assertNotNull(result);

        verify(grafanaService, never()).syncDashboard(any());
    }

    @Test
    public void testSyncFailureUpdatesStatus() throws Exception {
        RSGrafanaDashboard resource = createDashboard("test-uid", "{}");

        doThrow(new RuntimeException("Boom")).when(grafanaService).syncDashboard(any());

        reconciler.reconcile(resource, mock(Context.class));

        assertEquals(Status.ERROR, resource.getStatus().getState());
        assertTrue(resource.getStatus().getMessage().contains("Boom"));
    }


    private RSGrafanaDashboard createDashboard(String uid, String json) {
        RSGrafanaDashboard resource = new RSGrafanaDashboard();
        resource.getMetadata().setUid(uid);
        RSGrafanaDashboardSpec spec = new RSGrafanaDashboardSpec(json);
        resource.setSpec(spec);
        return resource;
    }

}
