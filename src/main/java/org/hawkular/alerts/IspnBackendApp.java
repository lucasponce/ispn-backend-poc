package org.hawkular.alerts;

import static java.lang.System.currentTimeMillis;
import static org.hawkular.alerts.Common.TENANT;
import static org.hawkular.alerts.Common.TEST_ENTRIES_SIZE;

import java.io.IOException;
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
    static String ISPN_CONFIG_LOCAL = "/alerting-local.xml";
    static EmbeddedCacheManager cacheManager = null;
    static Cache<String, Alert> backend;
    static QueryFactory qf;
    static Query qTriggerId;

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
            backend.putAsync(id, a);
        }
    }

    static void queryAlertsByTrigger() {
        int results = qTriggerId.getResultSize();
        System.out.println("Filtered by ctime > 900000 and (trigger0 or trigger1): " + results);
    }

    public static void main( String[] args ) throws Exception {
        System.out.println( "Start ISPN Test" );
        long start = currentTimeMillis();
        init();
        loadData();
        System.out.println("Loaded:" + (currentTimeMillis() - start) + "ms");
        queryAlertsByTrigger();
        System.out.println("Queried:" + (currentTimeMillis() - start) + "ms");
    }
}
