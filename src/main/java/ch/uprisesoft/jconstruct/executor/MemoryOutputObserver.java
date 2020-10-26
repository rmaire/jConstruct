package ch.uprisesoft.jconstruct.executor;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Uprise Software <uprisesoft@gmail.com>
 */
public class MemoryOutputObserver implements OutputObserver {
    
    private final List<OutputEntry> entries = new ArrayList<>();

    @Override
    public synchronized void inform(OutputEntry entry) {
        entries.add(entry);
    }

    public synchronized List<OutputEntry> getEntries() {
        return entries;
    }

}
