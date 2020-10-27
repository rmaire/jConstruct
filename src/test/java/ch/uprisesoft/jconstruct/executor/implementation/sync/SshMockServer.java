/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.jconstruct.executor.implementation.sync;

import java.io.IOException;
import org.apache.sshd.server.shell.ProcessShellCommandFactory;

/**
 *
 * @author rmaire
 */
public class SshMockServer extends AbstractSshServer {

    public SshMockServer() throws IOException {
    }

    public SshMockServer(int port, String user, String password, String host) throws IOException {
        super(port, user, password, host);
    }
    
    

    @Override
    protected void setActualCommandFactory() {
        server.setCommandFactory(new ProcessShellCommandFactory());
    }
    
}
