package ch.uprisesoft.jconstruct.forms;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.webfolder.ui4j.api.browser.BrowserEngine;
import io.webfolder.ui4j.api.browser.BrowserFactory;
import io.webfolder.ui4j.api.browser.Page;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FormAutomator {

    private final String properties;
    private final JavaPropsMapper mapper = new JavaPropsMapper();
    BrowserEngine browser = BrowserFactory.getWebKit();
    Page page;

    public FormAutomator(String properties, String url) {
        this.properties = properties;
        mapper.registerModule(new Jdk8Module());
        page = browser.navigate(url);
    }

    public FormAutomator(File propertiesFile, String url) throws FileNotFoundException {
        StringBuilder props = new StringBuilder();
        Scanner scanner = new Scanner(propertiesFile);
        while (scanner.hasNextLine()) {
            props.append(scanner.nextLine()).append("\n");
        }
        this.properties = props.toString();
        mapper.registerModule(new Jdk8Module());
        page = browser.navigate(url);
    }

    public void execute() throws IOException, InterruptedException {
        //page.show();
        System.setProperty("ui4j.headless", "true");
        ObjectNode node;
        node = mapper.readValue(properties, ObjectNode.class);

        Map<String, Object> result = mapper.convertValue(node, Map.class);
        ArrayList<Object> steps = (ArrayList) result.get("steps");

        for (Object o : steps) {
            FormStep fs = new FormStep((HashMap) o, page);
            //System.out.println(fs.toString());
            fs.execute();
        }
    }
}
