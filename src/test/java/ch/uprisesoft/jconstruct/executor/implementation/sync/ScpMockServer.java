package ch.uprisesoft.jconstruct.executor.implementation.sync;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.session.SessionContext;
import org.apache.sshd.server.scp.ScpCommandFactory;

/**
 *
 * @author rmaire
 */
public class ScpMockServer extends AbstractSshServer {

    public ScpMockServer() throws IOException {
    }

    public ScpMockServer(int port, String user, String password, String host) throws IOException {
        super(port, user, password, host);
    }
    
    

    @Override
    protected void setActualCommandFactory() {
        super.server.setFileSystemFactory(new VirtualFileSystemFactory() {
            @Override
            public Path getUserHomeDir(SessionContext session) throws IOException {
                return testFolder;
            }
        });
        
        System.out.println(testFolder.toAbsolutePath());
        
        super.server.setCommandFactory(new ScpCommandFactory());
    }
    
}
