package org.hawkular.alerts;

import java.io.Serializable;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@Indexed(index = "condition")
public class Condition implements Serializable {

    @Field(store = Store.YES, analyze = Analyze.NO)
    private String conditionId;

    @Field(store = Store.YES, analyze = Analyze.NO)
    private String description;

    public Condition() {
    }

    public Condition(String conditionId, String description) {
        this.conditionId = conditionId;
        this.description = description;
    }

    public String getConditionId() {
        return conditionId;
    }

    public void setConditionId(String conditionId) {
        this.conditionId = conditionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Condition condition = (Condition) o;

        if (conditionId != null ? !conditionId.equals(condition.conditionId) : condition.conditionId != null)
            return false;
        return description != null ? description.equals(condition.description) : condition.description == null;
    }

    @Override
    public int hashCode() {
        int result = conditionId != null ? conditionId.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Condition{" +
                "conditionId='" + conditionId + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
