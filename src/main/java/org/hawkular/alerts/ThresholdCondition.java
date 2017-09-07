package org.hawkular.alerts;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@Indexed(index = "condition")
public class ThresholdCondition extends Condition {

    @Field(store = Store.YES, analyze = Analyze.NO)
    private String threshold;

    public ThresholdCondition() {
    }

    public ThresholdCondition(String conditionId, String description, String threshold) {
        super(conditionId, description);
        this.threshold = threshold;
    }

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ThresholdCondition that = (ThresholdCondition) o;

        return threshold != null ? threshold.equals(that.threshold) : that.threshold == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (threshold != null ? threshold.hashCode() : 0);
        return result;
    }


}
