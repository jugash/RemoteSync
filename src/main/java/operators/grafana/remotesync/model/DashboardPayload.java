package operators.grafana.remotesync.model;

public record DashboardPayload(String dashboard, boolean overwrite, int folderId) {}
