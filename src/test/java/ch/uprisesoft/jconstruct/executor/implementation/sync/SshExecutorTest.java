/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.jconstruct.executor.implementation.sync;

import ch.uprisesoft.jconstruct.executor.ConsoleOutputObserver;
import ch.uprisesoft.jconstruct.executor.Executor;
import ch.uprisesoft.jconstruct.target.Target;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;

public class SshExecutorTest {

//    SshServer server;
    AbstractSshServer server;
    Target target;
    Executor executor;

    public SshExecutorTest() {
    }

//    @Test
    public void testConnect() throws IOException {
        
        Logger.getRootLogger().setLevel(Level.ALL);
        
        this.server = new SshMockServer();
        server.start();

        if (server.isStarted()) {
            System.out.println("===================");
            System.out.println("Started");
            System.out.println("===================");
        }

//        Logger minaLogger = Logger.getLogger("org.apache.mina");
//
//        minaLogger.setLevel(Level.DEBUG);
//
//        Logger.getRootLogger().setLevel(Level.DEBUG);

        target = new Target.Builder()
                .withHost("127.0.0.1")
                .withUsername("user")
                .withPassword("password")
                .withPort(3333)
                .build();

        executor = new SshExecutor.Builder()
                .withTarget(target)
                .withCommand("ls")
                .register(new ConsoleOutputObserver(true, true))
                .build();
        executor.runCommands();
        
        server.stop();

    }
    
    @Test
    public void testUpload() throws IOException {
        
        Logger.getRootLogger().setLevel(Level.ALL);
        
        this.server = new ScpMockServer();
        server.start();

        if (server.isStarted()) {
            System.out.println("===================");
            System.out.println("Started");
            System.out.println("===================");
        }

        Logger minaLogger = Logger.getLogger("org.apache.mina");

        minaLogger.setLevel(Level.DEBUG);

        Logger.getRootLogger().setLevel(Level.DEBUG);

        target = new Target.Builder()
                .withHost("127.0.0.1")
                .withUsername("user")
                .withPassword("password")
                .withPort(3333)
                .build();

        executor = new SshExecutor.Builder()
                .withTarget(target)
                .withUpload("Blablubb", "")
                .register(new ConsoleOutputObserver(true, true))
                .build();
        executor.runCommands();
        
        server.stop();

    }

}
