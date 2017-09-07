package org.hawkular.alerts;

import static java.lang.System.currentTimeMillis;
import static org.hawkular.alerts.Common.TENANT;
import static org.hawkular.alerts.Common.TEST_ENTRIES_SIZE;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.Search;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class IspnBackendApp
{
    static String ISPN_CONFIG_LOCAL = "/alerting-debug.xml";
    static EmbeddedCacheManager cacheManager = null;
    static Cache<String, Object> backend;
    static QueryFactory qf;
    static Query qTriggerId;
    static Query qTags;

    static void init() throws IOException {
        cacheManager = new DefaultCacheManager(IspnBackendApp.class.getResourceAsStream(ISPN_CONFIG_LOCAL));
        backend = cacheManager.getCache("backend");
        qf = Search.getQueryFactory(backend);
        qTriggerId = qf.from(Alert.class)
                .having("ctime").gte(900000)
                .and(qf
                    .having("triggerId").equal("trigger0")
                    .or()
                    .having("triggerId").equal("trigger1"))
                .build();
    }

    static void loadData() {
        for (int i=0; i<TEST_ENTRIES_SIZE; i++) {
            String id = UUID.randomUUID().toString();
            String triggerId = "trigger" + (i % 10);
            String status = "status" + (i % 3);
            Alert a = new Alert(TENANT, id, triggerId, i, status);
            // a.addTag("test.uno.tag" + (i % 2), "value" + (i % 3));
            a.addTag("tag.1", "/tag;/uno;toledo cuenca");
            a.addTag("tag.2", "/tag;/dos;ciudad real");
            a.addTag("test_tag", "/t;hawkular/f;my-agent/r;Local%20DMR~~_Server Availability");
            System.out.println(a);
            backend.put(id, a); // Async works but query probably is not ready in low iterations
        }
    }

    static void loadConditions() {
        for (int i=0; i<TEST_ENTRIES_SIZE; i++) {
            String id = UUID.randomUUID().toString();
            String description = "description" + (i % 3);
            String threshold = "threshold" + ( i % 2);
            ThresholdCondition thresholdCondition = new ThresholdCondition(id, description, threshold);
            backend.put(id, (Condition) thresholdCondition);
        }
    }

    static void queryAlertsByTrigger() {
        int results = qTriggerId.getResultSize();
        System.out.println("Filtered by ctime > 900000 and (trigger0 or trigger1): " + results);
    }

    static void queryAlertsByTags() {
        // String sql = "from org.hawkular.alerts.Alert where (tags like 'tag0:value_' or tags like 'tag1:value_')";
        // String sql = "from org.hawkular.alerts.Alert where tenant = 'poc-tenant' and (tags : (/tag\\.1_\\/tag;\\/uno;/)) ";
        // String sql = "from org.hawkular.alerts.Alert where tenant = 'poc-tenant' and (tags : (/tag.1_\\/xxx;.*/)) ";
        // String sql = "from org.hawkular.alerts.Alert where tenant = 'poc-tenant' and (tags : (/tag.2_\\/tag;\\/uno;tol.*/)) ";
        // String sql = "from org.hawkular.alerts.Alert where tenant = 'poc-tenant' and (tags : (not ('tag.3' or 'tag.4' or 'tag.5'))) ";
        // String sql = "from org.hawkular.alerts.Alert where tenant = 'poc-tenant' and (tags like 'tag.1_/tag;/uno;toledo cuenca') ";
        String sql = "from org.hawkular.alerts.Alert where tenant = 'poc-tenant' and (tags : /test_tag_\\/t;hawkular\\/f;my-agent\\/r;Local%20DMR\\~\\~_Server Availability/) ";
        qTags = qf.create(sql);
        int results = qTags.getResultSize();
        System.out.println(sql + " = " + results);
        List<Alert> alerts = qTags.list();
        for (Alert a : alerts) {
            System.out.println(a);
        }
    }

    static void updateAlertTag() {
        backend.keySet().stream().forEach(key -> {
            Alert alert = (Alert) backend.get(key);
            alert.addTag("test.uno.tag0", "value");
            backend.put(key, alert);
            System.out.println("Updated " + alert);
        });
    }

    public static void main( String[] args ) throws Exception {
        System.setProperty("poc.test", "./target");
        System.out.println( "Start ISPN Test" );
        long start = currentTimeMillis();
        init();
        loadData();
        long now = currentTimeMillis();
        System.out.println("Loaded [" + (now - start) + " ms]");
        // start = now;
        // queryAlertsByTrigger();
        // now = currentTimeMillis();
        // System.out.println("Query [" + (now - start) + " ms]");
        start = now;
        queryAlertsByTags();
        System.out.println("Query [" + (currentTimeMillis() - start) + " ms]");
        // updateAlertTag();
        // queryAlertsByTags();
    }
}
