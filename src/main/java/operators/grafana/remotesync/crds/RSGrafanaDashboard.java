package operators.grafana.remotesync.crds;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;


@Group("monitoring.com")
@Version("v1")
public class RSGrafanaDashboard
        extends CustomResource<RSGrafanaDashboardSpec, RSGrafanaDashboardStatus>
        implements Namespaced {}
