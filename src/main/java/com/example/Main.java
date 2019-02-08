package com.example;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final String DB = "C:\\Program Files\\Firebird\\Firebird_2_5\\examples\\empbuild\\EMPLOYEE.FDB";

    public static final void main(String... args) throws IOException {
        SystemOutTraceNotifier notifier = new SystemOutTraceNotifier(DB, "SYSDBA", "masterkey");

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(notifier);
        
        System.out.println("[*] ... hit any key ...");
        System.in.read();

        executorService.shutdownNow();        
    }
}
