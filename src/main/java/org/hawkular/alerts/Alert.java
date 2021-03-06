package org.hawkular.alerts;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.Store;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@Indexed(index = "alert")
public class Alert implements Serializable {
    @Field(store = Store.YES, analyze = Analyze.NO)
    String tenant;

    @Field(store = Store.YES, analyze = Analyze.NO)
    String id;

    @Field(store = Store.YES, analyze = Analyze.NO)
    String triggerId;

    @Field(store = Store.YES, analyze = Analyze.NO)
    @SortableField
    long ctime;

    @Field(store = Store.YES, analyze = Analyze.NO)
    String status;

    @Field(store = Store.YES, analyze = Analyze.YES)
    @FieldBridge(impl = TagsBridge.class)
    @Analyzer(impl = TagsBridge.TagsAnalyzer.class)
    Map<String, String> tags = new HashMap<>();

    public Alert() {
    }

    public Alert(String tenant, String id, String triggerId, long ctime, String status) {
        this.tenant = tenant;
        this.id = id;
        this.triggerId = triggerId;
        this.ctime = ctime;
        this.status = status;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public long getCtime() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public void addTag(String name, String value) {
        if (null == name || null == value) {
            throw new IllegalArgumentException("Tag must have non-null name and value");
        }
        tags.put(name, value);
    }

    public void removeTag(String name) {
        if (null == name) {
            throw new IllegalArgumentException("Tag must have non-null name");
        }
        tags.remove(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Alert alert = (Alert) o;

        if (ctime != alert.ctime) return false;
        if (tenant != null ? !tenant.equals(alert.tenant) : alert.tenant != null) return false;
        if (id != null ? !id.equals(alert.id) : alert.id != null) return false;
        if (triggerId != null ? !triggerId.equals(alert.triggerId) : alert.triggerId != null) return false;
        if (status != null ? !status.equals(alert.status) : alert.status != null) return false;
        return tags != null ? tags.equals(alert.tags) : alert.tags == null;
    }

    @Override
    public int hashCode() {
        int result = tenant != null ? tenant.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (triggerId != null ? triggerId.hashCode() : 0);
        result = 31 * result + (int) (ctime ^ (ctime >>> 32));
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "tenant='" + tenant + '\'' +
                ", id='" + id + '\'' +
                ", triggerId='" + triggerId + '\'' +
                ", ctime=" + ctime +
                ", status='" + status + '\'' +
                ", tags='" + tags + '\'' +
                '}';
    }
}
