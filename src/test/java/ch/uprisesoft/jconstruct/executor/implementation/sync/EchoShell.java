/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.jconstruct.executor.implementation.sync;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
public class EchoShell extends CommandExecutionHelper {
    public EchoShell() {
        super();
    }

    @Override
    protected boolean handleCommandLine(String command) throws Exception {
        OutputStream out = getOutputStream();
        out.write((command + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();

        return !"exit".equals(command);

    }
}
