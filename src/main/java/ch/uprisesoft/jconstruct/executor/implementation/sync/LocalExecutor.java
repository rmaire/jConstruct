package ch.uprisesoft.jconstruct.executor.implementation.sync;

import ch.uprisesoft.jconstruct.executor.ExecutorException;
import ch.uprisesoft.jconstruct.executor.OutputEntry;
import ch.uprisesoft.jconstruct.executor.OutputEntryType;
import ch.uprisesoft.jconstruct.executor.OutputObserver;
import ch.uprisesoft.jconstruct.executor.OutputSubject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import ch.uprisesoft.jconstruct.executor.Executor;
import java.util.HashSet;

/**
 *
 * @author Uprise Software <uprisesoft@gmail.com>
 */
public class LocalExecutor implements Executor, OutputSubject {

    private final List<OutputObserver> observers = new ArrayList<>();

    private final List<List<String>> commands;

    private final Path path;

    public LocalExecutor(List<List<String>> commands, Path path) {
        if (commands == null) {
            throw new ExecutorException("Null pointer command");
        }

        if (commands.isEmpty()) {
            throw new ExecutorException("Empty command");
        }

        this.commands = commands;
        this.path = path;
    }

    @Override
    public void runCommands() {

        for (List<String> command : commands) {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
//            processBuilder.directory(path.toFile());

            try {
                Process process = processBuilder.start();

                Thread inputThread = new Thread(() -> {

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String inputLine;
                        while ((inputLine = reader.readLine()) != null) {
                            OutputEntry info = new OutputEntry(inputLine, OutputEntryType.INFO, new HashSet<>());
                            notifyObservers(info);
                        }
                    } catch (Exception e) {
                        throw new ExecutorException("Could not read process output", e);
                    }
                });
                inputThread.start();

                Thread errorThread = new Thread(() -> {

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        String inputLine;
                        while ((inputLine = reader.readLine()) != null) {
                            OutputEntry error = new OutputEntry(inputLine, OutputEntryType.ERROR, new HashSet<>());
                            notifyObservers(error);
                        }
                    } catch (Exception e) {
                        throw new ExecutorException("Could not read process output", e);
                    }
                });
                errorThread.start();

                int exitCode = -1;
                try {
                    inputThread.join();
                    errorThread.join();
                    exitCode = process.waitFor();
                } catch (InterruptedException e) {
                    throw new ExecutorException("Thread interrupteds", e);
                }
            } catch (IOException e) {
                System.out.println(e.toString());
                throw new ExecutorException("Could not start process", e);
            }
        }
    }

    @Override
    public void register(OutputObserver observer) {
        this.observers.add(observer);
    }

    void notifyObservers(OutputEntry entry) {
        for (OutputObserver o : observers) {
            o.inform(entry);
        }
    }
}
