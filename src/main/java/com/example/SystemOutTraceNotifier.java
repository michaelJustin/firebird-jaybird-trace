package com.example;

import java.io.IOException;
import java.io.OutputStream;
import org.firebirdsql.management.TraceManager;
import org.firebirdsql.management.FBTraceManager;
import org.firebirdsql.gds.impl.GDSType;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.Callable;

public class SystemOutTraceNotifier implements Callable<Void> {

    // see https://firebirdsql.org/rlsnotesh/rnfb25-trace.html
    private static final String TRACE_CFG = "<database>\n"
            + "	enabled                true\n"
            // + "	include_filter         %(SELECT|INSERT|UPDATE|DELETE)%\n"
            + "	log_statement_finish   true\n"
            /* + "	log_procedure_finish   true\n"
            + "	log_trigger_finish     true\n" */
            + "	print_plan             true\n"
            /* + "	print_perf             true\n"*/
            + "	time_threshold         0\n"
            + "</database>";

    private final TraceManager traceManager;

    private final String sessionName = UUID.randomUUID().toString();

    private final OutputStream out = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
           System.out.print((char) b);
        }
    };
    
    public SystemOutTraceNotifier(String database, String user, String pass) {
        traceManager = new FBTraceManager(GDSType.getType("PURE_JAVA"));
        traceManager.setHost("localhost");
        traceManager.setDatabase(database);
        traceManager.setUser(user);
        traceManager.setPassword(pass);
    }

    @Override
    public Void call() throws Exception {
        try {
            traceManager.setLogger(out);
            
            System.out.println("[*] Start trace session " + sessionName);
            traceManager.startTraceSession(sessionName, TRACE_CFG);

            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ex) {
                System.out.println("[*] Trace was interrupted");
                Thread.currentThread().interrupt();  // set interrupt flag
            }
            
            Integer sessionId = traceManager.getSessionId(sessionName);
            if (sessionId != null) {
                System.out.println("[*] Trace is stopping");
                traceManager.stopTraceSession(sessionId); 
            } else {
                System.out.println("[*] Could not stop (session id is null)");
            }            
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
        }
        return null;
    }
}
