package ch.uprisesoft.jconstruct.executor.implementation.sync;

import ch.uprisesoft.jconstruct.executor.Executor;
import ch.uprisesoft.jconstruct.executor.ExecutorException;
import ch.uprisesoft.jconstruct.executor.OutputEntry;
import ch.uprisesoft.jconstruct.executor.OutputEntryType;
import ch.uprisesoft.jconstruct.executor.OutputObserver;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.LoggerFactory;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.xfer.FileSystemFile;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author Uprise Software <uprisesoft@gmail.com>
 */
public class SshExecutor implements Executor {

    private final List<String> commands;
    private Session session;
    private final Integer timeout;
    private final SSHClient ssh;
    private final List<Pair<String, String>> uploads;
    private final ObserverStream sshOutputObserverInfoStream;
    private final ObserverStream sshOutputObserverErrorStream;

    protected SshExecutor(List<String> commands, List<OutputObserver> observers, Integer timeout, SSHClient ssh, List<Pair<String, String>> uploads, Optional<File> keyFile, Optional<String> keyString) {
        this.commands = commands;
        this.timeout = timeout;
        this.ssh = ssh;
        this.uploads = uploads;
        this.sshOutputObserverInfoStream = new ObserverStream(observers, new HashSet<>(), OutputEntryType.INFO);
        this.sshOutputObserverErrorStream = new ObserverStream(observers, new HashSet<>(), OutputEntryType.ERROR);
    }

    private SshExecutor(Builder builder) {
        this.commands = builder.commands;
        this.timeout = builder.timeout;
        this.ssh = builder.ssh;
        this.uploads = builder.uploads;
        this.sshOutputObserverInfoStream = new ObserverStream(builder.observers, new HashSet<>(), OutputEntryType.INFO);
        this.sshOutputObserverErrorStream = new ObserverStream(builder.observers, new HashSet<>(), OutputEntryType.ERROR);
    }

    @Override
    public void runCommands() {

        for (Pair<String, String> file : uploads) {
            File tempFile;
            try {
                tempFile = File.createTempFile("transfer", "tmp");
            } catch (IOException ex) {
                throw new ExecutorException("Could not create teporary file", ex);
            }

            try {
                // sshj needs a file to upload. If the file content is passed as string, a temp file has to be created
                FileWriter writer = new FileWriter(tempFile.getAbsoluteFile());
                writer.write(file.getLeft());
                writer.close();
                ssh.setTimeout(timeout);
                ssh.newSCPFileTransfer().upload(new FileSystemFile(tempFile.getAbsoluteFile()), file.getRight());
            } catch (IOException ex) {
                throw new ExecutorException("Could not upload file", ex);
            } finally {
                tempFile.delete();
            }
        }

        for (String c : commands) {
            Session.Command cmd;
            try {
                session = ssh.startSession();
                cmd = session.exec(c);
                new StreamCopier(cmd.getInputStream(), sshOutputObserverInfoStream, LoggerFactory.DEFAULT)
                        .bufSize(cmd.getLocalMaxPacketSize())
                        .spawn("stdout");

                new StreamCopier(cmd.getErrorStream(), sshOutputObserverErrorStream, LoggerFactory.DEFAULT)
                        .bufSize(cmd.getLocalMaxPacketSize())
                        .spawn("stderr");
                cmd.join(timeout, TimeUnit.SECONDS);
                cmd.close();
            } catch (ConnectionException | TransportException ex) {
                throw new ExecutorException("Could not connect to host", ex);
            }
        }
        try {
            if (session != null && session.isOpen()) {
                session.close();
                ssh.close();
            }
        } catch (IOException ex) {
            throw new ExecutorException("Could not close connection", ex);
        }
    }

    public static class Builder extends AbstractExecutorBuilder {

        private final Integer DEFAULT_PORT = 22;

        private final List<String> commandsWithEnv = new ArrayList<>();
        private SSHClient ssh;
        //private int timeout = 120;

        @Override
        public Executor build() {

            if (commands.isEmpty() && uploads.isEmpty()) {
                throw new IllegalStateException("No command given");
            }

            if (!target.getHost().isPresent()) {
                throw new IllegalStateException("Host must be set for Target");
            }

            if (!target.getUsername().isPresent()) {
                throw new IllegalStateException("Username must be set for Target");
            }

            if (!(target.getPassword().isPresent() || target.getKey().isPresent() || target.getKeyFile().isPresent())) {
                throw new IllegalStateException("Either password or key must be set for Target");
            }

            ssh = new SSHClient();
            ssh.setTimeout(timeout);

            try {
                ssh.addHostKeyVerifier(new PromiscuousVerifier());
                ssh.connect(target.getHost().get(), target.getPort().orElse(DEFAULT_PORT));
            } catch (IOException ex) {
                throw new ExecutorException("Could not connect to host", ex);
            }

            try {
                if (target.getKey().isPresent()) {

                    File tempFile;
                    try {
                        tempFile = File.createTempFile("key", "tmp");
                    } catch (IOException ex) {
                        throw new ExecutorException("Could not create teporary file", ex);
                    }
                    FileWriter writer = new FileWriter(tempFile.getAbsoluteFile());
                    writer.write(target.getKey().get());
                    writer.close();
                    ssh.authPublickey(target.getUsername().get(), ssh.loadKeys(tempFile.getAbsoluteFile().getAbsolutePath()));

                } else if (target.getKeyFile().isPresent()) {
                    System.out.println("File: " + target.getKeyFile());
                    //ssh.loadKeys(target.getKeyFile().get());
                    ssh.authPublickey(target.getUsername().get(), ssh.loadKeys(target.getKeyFile().get().getPath()));
                } else if (target.getPassword().isPresent()) {
                    ssh.authPassword(target.getUsername().get(), target.getPassword().get());
                }
            } catch (UserAuthException ex) {
                throw new ExecutorException("Invalid credentials", ex);
            } catch (TransportException ex) {
                throw new ExecutorException("Transport exception", ex);
            } catch (IOException ex) {
                throw new ExecutorException("Invalid key", ex);
            }

            if (environment.isPresent()) {
                for (String c : commands) {
                    commandsWithEnv.add(buildCommandWithEnv(c));
                }
                commands = new ArrayList<>(commandsWithEnv);
            }

            return new SshExecutor(this);
        }

        private String buildCommandWithEnv(String command) {
            StringBuilder commandWithEnv = new StringBuilder();

            for (Entry<String, String> env : environment.get().entrySet()) {
                commandWithEnv
                        .append(env.getKey())
                        .append("=")
                        .append(env.getValue())
                        .append("; ");
            }
            commandWithEnv.append(command);

            return commandWithEnv.toString();
        }

        @Override
        protected String platformDependendStringJoin(List<String> file) {
            return String.join("\n", file);
        }
    }

    class ObserverStream extends OutputStream {

        private StringBuffer buf = new StringBuffer();
        private final List<OutputObserver> observers;
        private final Set<String> tags;
        private final OutputEntryType type;

        public ObserverStream(List<OutputObserver> observers, Set<String> tags, OutputEntryType type) {
            this.observers = observers;
            this.tags = tags;
            this.type = type;
        }

        private OutputEntry createEntry(String entry, Set<String> tags) {
            return new OutputEntry(entry, type, tags);
        }

        private void notifyObservers(OutputEntry entry) {
            for (OutputObserver o : observers) {
                o.inform(entry);
            }
        }

        @Override
        public void write(int b) throws IOException {
            buf.append((char) b);
            if (buf.toString().endsWith("\n")) {
                notifyObservers(createEntry(buf.toString().substring(0, buf.length() - 1), tags));
                buf = new StringBuffer();
            }
        }
    }

}
