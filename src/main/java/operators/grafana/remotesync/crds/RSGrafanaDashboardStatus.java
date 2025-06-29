package operators.grafana.remotesync.crds;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.javaoperatorsdk.operator.api.ObservedGenerationAwareStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RSGrafanaDashboardStatus extends ObservedGenerationAwareStatus {
    private String uid;
    private String lastSyncedChecksum;
    private String state;
    private String message;

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getLastSyncedChecksum() { return lastSyncedChecksum; }
    public void setLastSyncedChecksum(String lastSyncedChecksum) {
        this.lastSyncedChecksum = lastSyncedChecksum;
    }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
