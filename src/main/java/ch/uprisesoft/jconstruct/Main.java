package ch.uprisesoft.jconstruct;

import ch.uprisesoft.jconstruct.executor.ConsoleOutputObserver;
import ch.uprisesoft.jconstruct.executor.Executor;
import ch.uprisesoft.jconstruct.executor.ExecutorException;
import ch.uprisesoft.jconstruct.executor.OutputEntry;
import ch.uprisesoft.jconstruct.executor.OutputObserver;
import ch.uprisesoft.jconstruct.executor.implementation.async.AsyncExecutor;
import ch.uprisesoft.jconstruct.executor.implementation.sync.LocalExecutor;
import ch.uprisesoft.jconstruct.executor.implementation.sync.SshExecutor;
import ch.uprisesoft.jconstruct.executor.implementation.sync.WinRMExecutor;
import ch.uprisesoft.jconstruct.forms.FormAutomator;
import ch.uprisesoft.jconstruct.target.Target;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerMount;
import com.github.dockerjava.api.model.ContainerPort;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;
//import io.webfolder.ui4j.api.browser.BrowserEngine;
//import io.webfolder.ui4j.api.browser.BrowserFactory;
//import static io.webfolder.ui4j.api.browser.BrowserFactory.getWebKit;
//import io.webfolder.ui4j.api.browser.Page;
//import io.webfolder.ui4j.api.dom.Document;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import com.github.dockerjava.api.model.Event;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.command.EventsResultCallback;
//import io.webfolder.ui4j.api.dom.Element;
//import io.webfolder.ui4j.api.dom.RadioButton;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Optional;
import org.apache.sshd.server.SshServer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ProcessShellCommandFactory;
import org.apache.sshd.server.shell.ProcessShellFactory;

/**
 *
 * @author Uprise Software <uprisesoft@gmail.com>
 */
public class Main implements OutputObserver {

    private final List<String> file = new ArrayList();
    private final String linuxFile = "/home/vagrant/test.txt";
    private final String windowsFile = "c:/vagrant/test.txt";

    public static void main(String[] args) throws IOException, FileNotFoundException, InterruptedException {
//        try {
//            new Main().testUploadAndRun();
//        } catch (Exception e) {
//            System.out.println("Etwas ging schief: " + e.getMessage());
//        }

//        new Main().runLocal();
        //new Main().runUi4j();
//        new Main().runSsh();
//        new Main().runFormAutomator();
//        new Main().runWinRM();
//            new Main().startVagrant();
//            new Main().stopVagrant();
        new Main().testMinaLogger();
        System.exit(0);
    }

    public Main() {
        file.add("Hello");
        file.add("World!");
    }

    private void testMinaLogger() throws IOException, InterruptedException {
        Logger.getRootLogger().setLevel(Level.ALL);

        SshServer server;
        Target target;
        Executor executor;
        
        server = SshServer.setUpDefaultServer();
        server.setPort(2223);
        server.setHost("0.0.0.0");
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("hostkey.ser").toPath()));
        
        server.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String u, String p, ServerSession s) {
                return true;
            }
        });

        server.setShellFactory(new ProcessShellFactory(new String[]{"cmd.exe"}));
        ProcessShellCommandFactory pscf = new ProcessShellCommandFactory();
        server.setCommandFactory(pscf);
        server.start();
        
//        while(true) {
//            Thread.sleep(1000000);
//        }
//
        target = new Target.Builder()
//                .withHost("test.rebex.net")
//                .withUsername("demo")
//                .withPassword("password")
                .withHost("localhost")
                .withUsername("tester")
                .withPassword("test")
                .withPort(2223)
                .build();

        executor = new SshExecutor.Builder()
                .withTarget(target)
                .withCommand("ls")
                .register(this)
                .build();
        executor.runCommands();
    }

    private void runSshKeyString() throws InterruptedException {
        Target linux = new Target.Builder()
                .withHost("10.3.5.20")
                .withUsername("vagrant")
                //                .withPassword("vagrant")
                .withKey("-----BEGIN RSA PRIVATE KEY-----\n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\n-----END RSA PRIVATE KEY-----")
                //                                .withKeyFile(new File("key/private/id_test"))
                .build();

        Map<String, String> environment = new HashMap<>();
        environment.put("NAME", "World!");
        environment.put("BLA", "Blubb");

        Executor e = new SshExecutor.Builder()
                .withCommand("echo $NAME")
                .withCommand("sleep 2")
                .withCommand("echo $BLA")
                //                .withCommand("blrfg")
                .withTarget(linux)
                .withEnvironment(environment)
                .register(new ConsoleOutputObserver(true, true))
                .build();
//        e.runCommands();
        AsyncExecutor a = new AsyncExecutor(e);
        a.runCommands();
        while (!a.isDone()) {
            System.out.println("Not yet done");
            Thread.sleep(1000);
        }
        System.out.println("Done");
    }

    private void startVagrant() throws InterruptedException {

        try {
            List<List<String>> commands = new ArrayList<>();

            List<String> lc1 = new ArrayList<>();
            lc1.add("vagrant");
            lc1.add("up");
            lc1.add("windows");

            commands.add(lc1);

            System.out.println(Paths.get("."));
            LocalExecutor ex = new LocalExecutor(commands, Paths.get("."));
            ex.register(new ConsoleOutputObserver(true, true));
//        ex.runCommands();
            AsyncExecutor a = new AsyncExecutor(ex);
            a.runCommands();
            while (!a.isDone()) {
                Thread.sleep(1000);
            }
            System.out.println("Done");
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

    private void stopVagrant() {

        try {
            List<List<String>> commands = new ArrayList<>();

            List<String> lc1 = new ArrayList<>();
            lc1.add("vagrant");
            lc1.add("halt");
            lc1.add("windows");

            commands.add(lc1);

            System.out.println(Paths.get("."));
            LocalExecutor ex = new LocalExecutor(commands, Paths.get("."));
            ex.register(new ConsoleOutputObserver(true, true));
//        ex.runCommands();
            AsyncExecutor a = new AsyncExecutor(ex);
            a.runCommands();
            while (!a.isDone()) {
                Thread.sleep(1000);
            }
            System.out.println("Done");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void runFormAutomator() throws FileNotFoundException, IOException, InterruptedException {
        FormAutomator fa = new FormAutomator(new File(getClass().getClassLoader().getResource("jira.properties").getFile()), "http://mycloud.vm:8080");
        fa.execute();
    }

    public void runLocal() throws InterruptedException {
        List<List<String>> commands = new ArrayList<>();

        List<String> lc1 = new ArrayList<>();
        lc1.add("ls");
        List<String> lc2 = new ArrayList<>();
        lc2.add("sleep");
        lc2.add("5");
        List<String> lc3 = new ArrayList<>();
        lc3.add("ls");

        commands.add(lc1);
        commands.add(lc2);
        commands.add(lc3);

        LocalExecutor ex = new LocalExecutor(commands, Paths.get("."));
        ex.register(new ConsoleOutputObserver(true, true));
//        ex.runCommands();
        AsyncExecutor a = new AsyncExecutor(ex);
        a.runCommands();
        while (!a.isDone()) {
            System.out.println("Not yet done");
            Thread.sleep(1000);
        }
        System.out.println("Done");
    }

    public void uploadLinux() {
        Target linux = new Target.Builder()
                .withHost("10.3.5.20")
                .withUsername("vagrant")
                .withPassword("vagrant")
                .build();

        Executor e = new SshExecutor.Builder()
                .withUpload(file, linuxFile)
                .withTarget(linux)
                .register(new ConsoleOutputObserver(true, true))
                .build();
        e.runCommands();
    }

    public void uploadWindows() {
        Target win = new Target.Builder()
                .withHost("10.3.5.30")
                .withUsername("vagrant")
                .withPassword("vagrant")
                .build();

        file.add("One");

        Executor e = new WinRMExecutor.Builder()
                .withUpload(file, windowsFile)
                .withTarget(win)
                .register(new ConsoleOutputObserver(true, true))
                .build();
        e.runCommands();
    }

    public void runSsh() throws InterruptedException {
        Target linux = new Target.Builder()
                .withHost("10.3.5.20")
                .withUsername("vagrant")
                .withPassword("vagrant")
                //                .withKeyFile(new File("key/private/id_test"))
                .build();

        Map<String, String> environment = new HashMap<>();
        environment.put("NAME", "World!");
        environment.put("BLA", "Blubb");

        Executor e = new SshExecutor.Builder()
                .withCommand("echo $NAME")
                .withCommand("sleep 2")
                .withCommand("echo $BLA")
                //                .withCommand("blrfg")
                .withTarget(linux)
                .withEnvironment(environment)
                .register(new ConsoleOutputObserver(true, true))
                .build();
//        e.runCommands();
        AsyncExecutor a = new AsyncExecutor(e);
        a.runCommands();
        while (!a.isDone()) {
            System.out.println("Not yet done");
            Thread.sleep(1000);
        }
        System.out.println("Done");
    }

    public void runWinRM() throws InterruptedException {

        Target win = new Target.Builder()
                .withHost("10.3.5.30")
                .withUsername("vagrant")
                .withPassword("vagrant")
                .withPort(5985)
                .build();

        Map<String, String> environment = new HashMap<>();
        environment.put("NAME", "World!");
        environment.put("BLA", "BLUBB!");

        Executor e = new WinRMExecutor.Builder()
                .withCommand("Get-ChildItem Env:NAME")
                .withCommand("Start-Sleep -s 5")
                .withCommand("Get-ChildItem Env:BLA")
                .withEnvironment(environment)
                .withTarget(win)
                .register(new ConsoleOutputObserver(true, true))
                .build();
//        e.runCommands();
        AsyncExecutor a = new AsyncExecutor(e);
        a.runCommands();
        while (!a.isDone()) {
            System.out.println("Not yet done");
            Thread.sleep(1000);
        }
        System.out.println("Done");
    }

    public void testUploadAndRun() throws IOException {
        File script = new File("scripts/test.sh");

        Target linux = new Target.Builder()
                .withHost("10.3.5.20")
                .withUsername("vagrant")
                .withPassword("vagrant")
                .build();

        Command command = new Command(linux, OsType.LINUX);
        command.register(new ConsoleOutputObserver(true, true));
        command.addEnvironmentVariable("NAME", "World!");
        command.upload(script, "/home/vagrant/test.sh");
        command.makeExecutable("/home/vagrant/test.sh");
        command.callScript("/home/vagrant/test.sh");
    }

    public void runDocker() {
//        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
//                .withDockerHost("tcp://mycloud.vm:4243")
//                .withDockerTlsVerify(false)
//                .build();

        DockerCmdExecFactory dockerCmdExecFactory = new JerseyDockerCmdExecFactory()
                .withReadTimeout(1000)
                .withConnectTimeout(10000)
                .withMaxTotalConnections(100)
                .withMaxPerRouteConnections(10);

        // Client setup
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();

        DockerClient dockerClient = DockerClientBuilder.getInstance("tcp://mycloud.vm:4243").build();

        EventsResultCallback callback = new EventsResultCallback() {
            @Override
            public void onNext(Event event) {
                System.out.println("=====> Event: " + event.toString());
                //super.onNext(event);
            }
        };

//        Info info = dockerClient.infoCmd().exec();
        //System.out.print(info);
        PullImageResultCallback pirc = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
                System.out.println(item.getStatus());
            }
        };

        dockerClient.pullImageCmd("zuara/jira").withTag("7.13.3").exec(pirc);

        try {
            pirc.awaitCompletion();
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
//        ExposedPort tcp8080 = ExposedPort.tcp(8080);
        CreateContainerResponse container = dockerClient.createContainerCmd("zuara/jira:7.13.3")
                .withName("Testbla")
                .withHostConfig(HostConfig.newHostConfig()
                        .withPublishAllPorts(Boolean.TRUE))
                .withAttachStderr(false)
                .withAttachStdin(false)
                .withAttachStdout(false)
                .exec();

        dockerClient.startContainerCmd(container.getId())
                .exec();

        WaitContainerResultCallback wcrc = new WaitContainerResultCallback();

        dockerClient.waitContainerCmd(container.getId()).exec(wcrc);

        try {
            wcrc.awaitCompletion();
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
        LogContainerCmd lcc = dockerClient.logContainerCmd(container.getId());

        lcc.withStdOut(true).withStdErr(true);
        lcc.withTimestamps(Boolean.TRUE);

        try {
            lcc.exec(new LogContainerResultCallback() {
                @Override
                public void onNext(Frame item) {
                    System.out.println(item.toString());
                }
            }).awaitCompletion();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        List<Container> cl = dockerClient.listContainersCmd().withShowAll(Boolean.TRUE).exec();

        for (Container c : cl) {
            System.out.println(c.getId() + ": " + c.getNames()[0] + " / " + c.getState());
        }

        ArrayList<String> cname = new ArrayList<>();
        cname.add("Testbla");
        List<Container> bla = dockerClient.listContainersCmd().withNameFilter(cname).exec();
        for (Container c : bla) {
            System.out.println("Container " + c.getNames()[0].substring(1));
            List<ContainerMount> mnts = c.getMounts();

            for (ContainerMount mnt : mnts) {
                System.out.println("Mount " + mnt.getName() + " => " + mnt.getDestination());
            }

            List<ContainerPort> ports = Arrays.asList(c.getPorts());
            for (ContainerPort p : ports) {
                System.out.println("Port " + p.getPrivatePort() + ":" + p.getPublicPort());
            }
        }
        dockerClient.stopContainerCmd(container.getId()).exec();

        wcrc = new WaitContainerResultCallback();

        dockerClient.waitContainerCmd(container.getId()).exec(wcrc);

        try {
            wcrc.awaitCompletion();
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }

        dockerClient.removeContainerCmd(container.getId()).exec();
        //dockerClient.eventsCmd().exec(callback);

    }

    @Override
    public void inform(OutputEntry entry) {
        System.out.println(entry.getEntry());
    }
}
