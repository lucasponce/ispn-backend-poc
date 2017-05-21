package org.hawkular.alerts;

import static java.lang.System.currentTimeMillis;
import static org.hawkular.alerts.Common.TENANT;
import static org.hawkular.alerts.Common.TEST_ENTRIES_SIZE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Futures;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class CassBackendApp {

    static final String HOST = "127.0.0.1";
    static final String PORT = "9042";
    static final String MAX_QUEUE = "9182";

    static PreparedStatement iAlert;
    static PreparedStatement iTrigger;
    static PreparedStatement iCtime;
    static PreparedStatement iStatus;
    static PreparedStatement fCtime;
    static PreparedStatement fTrigger;
    static PreparedStatement qAlertsById;
    static PreparedStatement qAlerts;

    static ObjectMapper mapper;

    static Cluster cluster = null;
    static Session session = null;

    static void init() {
        Cluster.Builder clusterBuilder = new Cluster.Builder()
                .addContactPoints(HOST)
                .withPort(new Integer(PORT))
                .withPoolingOptions(new PoolingOptions().setMaxQueueSize(Integer.valueOf(MAX_QUEUE)))
                .withProtocolVersion(ProtocolVersion.V3)
                .withQueryOptions(new QueryOptions().setRefreshSchemaIntervalMillis(0));
        cluster = clusterBuilder.build();
        session = cluster.connect();
        mapper = new ObjectMapper();
    }

    static void initScheme() {
        session.execute("DROP KEYSPACE IF EXISTS cass_backend_poc");
        session.execute("CREATE KEYSPACE IF NOT EXISTS cass_backend_poc WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}");
        session.execute("USE cass_backend_poc");
        session.execute("CREATE TABLE alerts (tenant text, id text, payload text, PRIMARY KEY (tenant, id))");
        session.execute("CREATE TABLE alerts_triggers (tenant text, id text, triggerId text, PRIMARY KEY (tenant, triggerId, id))");
        session.execute("CREATE TABLE alerts_ctimes (tenant text, id text, ctime bigint, PRIMARY KEY (tenant, ctime, id))");
        session.execute("CREATE TABLE alerts_statuses (tenant text, id text, status text, PRIMARY KEY (tenant, status, id))");
        iAlert = session.prepare("INSERT INTO cass_backend_poc.alerts (tenant, id, payload) VALUES (?, ?, ?)");
        iTrigger = session.prepare("INSERT INTO cass_backend_poc.alerts_triggers (tenant, id, triggerId) VALUES (?, ?, ?)");
        iCtime = session.prepare("INSERT INTO cass_backend_poc.alerts_ctimes (tenant, id, ctime) VALUES (?, ?, ?)");
        iStatus = session.prepare("INSERT INTO cass_backend_poc.alerts_statuses (tenant, id, status) VALUES (?, ?, ?)");
        fCtime = session.prepare("SELECT id FROM cass_backend_poc.alerts_ctimes WHERE tenant = ? and ctime >= ?");
        fTrigger = session.prepare("SELECT id FROM cass_backend_poc.alerts_triggers WHERE tenant = ? and triggerId = ?");
        qAlertsById = session.prepare("SELECT payload FROM cass_backend_poc.alerts WHERE tenant = ? and id = ?");
        qAlerts = session.prepare("SELECT id, payload FROM cass_backend_poc.alerts WHERE tenant = ?");
    }

    static void loadData() throws Exception {
        for (int i=0; i<TEST_ENTRIES_SIZE; i++) {
            List<ResultSetFuture> futures = new ArrayList<ResultSetFuture>();
            String id = UUID.randomUUID().toString();
            String triggerId = "trigger" + (i % 10);
            String status = "status" + (i % 3);
            Alert a = new Alert(TENANT, id, triggerId, i, status);
            futures.add(session.executeAsync(iAlert.bind(TENANT, id, mapper.writeValueAsString(a))));
            futures.add(session.executeAsync(iTrigger.bind(TENANT, id, triggerId)));
            futures.add(session.executeAsync(iCtime.bind(TENANT, id, new Long(i))));
            futures.add(session.executeAsync(iStatus.bind(TENANT, id, status)));
            Futures.allAsList(futures).get();
        }
    }

    static void queryAlertsByTrigger() throws Exception {
        // Filter by ctime
        ResultSet rsAlertsCtimes = session.execute(fCtime.bind(TENANT, 900000l));
        Set<String> ctimesFilter = new HashSet<String>();
        for (Row r : rsAlertsCtimes) {
            ctimesFilter.add(r.getString("id"));
        }
        // Filtered by trigger
        List<ResultSet> rsAlertIdsByTriggerIds;
        List<ResultSetFuture> futures = new ArrayList<ResultSetFuture>();
        Set<String> triggersFilter = new HashSet<String>();
        futures.add(session.executeAsync(fTrigger.bind(TENANT, "trigger0")));
        futures.add(session.executeAsync(fTrigger.bind(TENANT, "trigger1")));
        rsAlertIdsByTriggerIds = Futures.allAsList(futures).get();
        for (ResultSet rs : rsAlertIdsByTriggerIds) {
            for (Row r : rs) {
                triggersFilter.add(r.getString("id"));
            }
        }

        // Merging
        ctimesFilter.retainAll(triggersFilter);

        // Final query
        /*
            This query strategy has a limit when # of ids is greater than max connections in queue.
            So at the end, for big queries, all data should be fetched and filtered on client.

            List<ResultSet> rsAlerts;
            futures = new ArrayList<ResultSetFuture>();
            for (String id : ctimesFilter) {
                futures.add(session.executeAsync(qAlerts.bind(TENANT, id)));
            }
            rsAlerts = Futures.allAsList(futures).get();
        */
        ResultSet rsAlerts = session.execute(qAlerts.bind(TENANT));
        Set<Alert> finalFilter = new HashSet<Alert>();
        for (Row r : rsAlerts) {
            String id = r.getString("id");
            if (ctimesFilter.contains(id)) {
                finalFilter.add(mapper.readValue(r.getString("payload"), Alert.class));
            }
        }
        System.out.println("Filtered by ctime > 900000 and (trigger0 or trigger1): " + finalFilter.size());
    }

    public static void main(String[] args) throws Exception {
        System.out.println( "Start Cassandra Test" );
        long start = currentTimeMillis();
        init();
        initScheme();
        loadData();
        System.out.println("Loaded:" + (currentTimeMillis() - start) + "ms");
        queryAlertsByTrigger();
        System.out.println("Queried:" + (currentTimeMillis() - start) + "ms");
        session.close();
        cluster.close();
    }
}
