package ch.uprisesoft.jconstruct.executor;

/**
 *
 * @author Uprise Software <uprisesoft@gmail.com>
 */
public class ConsoleOutputObserver implements OutputObserver {

    private final boolean withOutputType;
    private final boolean withTimestamp;

    public ConsoleOutputObserver(boolean withOutputType, boolean withTimestamp) {
        this.withOutputType = withOutputType;
        this.withTimestamp = withTimestamp;
    }
    
    @Override
    public void inform(OutputEntry entry) {
        StringBuilder printableEntry = new StringBuilder();
        
        if(withOutputType) {
            printableEntry
                    .append(entry.getType())
                    .append(" ");
        }
        
        if(withTimestamp) {
            printableEntry
                    .append(entry.getTimestamp())
                    .append(" ");
        }
        
        printableEntry.append(entry.getEntry());
        
        System.out.println(printableEntry);
        
    }

}
