{{- define "rs-grafana-operator.name" -}}
{{ .Chart.Name }}
{{- end }}

{{- define "rs-grafana-operator.fullname" -}}
{{ .Release.Name }}-{{ .Chart.Name }}
{{- end }}

{{- define "rs-grafana-operator.serviceAccountName" -}}
{{- if .Values.serviceAccount.name }}
{{- .Values.serviceAccount.name }}
{{- else }}
{{ include "rs-grafana-operator.fullname" . }}
{{- end }}
{{- end }}

{{- define "rs-grafana-operator.labels" -}}
app.kubernetes.io/name: {{ include "rs-grafana-operator.name" . }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
{{- end }}
