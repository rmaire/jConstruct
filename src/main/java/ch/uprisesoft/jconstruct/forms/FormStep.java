package ch.uprisesoft.jconstruct.forms;

import io.webfolder.ui4j.api.browser.Page;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FormStep {

    private Optional<Integer> waitBefore;
    private Optional<Integer> waitAfter;
    private Optional<String> submitType;
    private Optional<String> submitSelector;
    private Map<String, String> fields = new HashMap<>();
    private final Page page;

    public FormStep(final HashMap<String, Object> stepItems, Page page) {
        getWaitBefore(stepItems);
        getWaitAfter(stepItems);
        getSubmit(stepItems);
        if (stepItems.containsKey("fields")) {
            this.fields = getFields((ArrayList) stepItems.get("fields"));
        }
        this.page = page;
    }

    public void execute() throws InterruptedException {
        if (waitBefore.isPresent()) {
            Thread.sleep(waitBefore.get());
        }

        fillFields();
        Thread.sleep(1000);
        submit();

        if (waitAfter.isPresent()) {
            Thread.sleep(waitAfter.get());
        }
    }

    private void submit() {
        if (submitType.isPresent() && submitSelector.isPresent()) {
            if (submitType.get().equals("submit")) {
                if (submitSelector.isPresent()) {
                    page.getDocument().query(submitSelector.get()).getForm().submit();
                }
            } else if (submitType.get().equals("post")) {
                if (submitSelector.isPresent()) {
                    page.getDocument().query(submitSelector.get()).click();
                }
            }
        }
    }

    private void fillFields() {
        for (Map.Entry<String, String> field : fields.entrySet()) {
            fillField(field.getKey(), field.getValue());
        }
    }

    private void fillField(String selector, String value) {

        if (value.startsWith("(") && value.endsWith(")")) {
            String innerValue = value.substring(1, value.length() - 1);
            if (innerValue.matches("-?\\d+")) {
                page.getDocument().query(selector).getSelect().setSelectedIndex(Integer.parseInt(innerValue));
            } else if (innerValue.equals("click")) {
                page.getDocument().query(selector).click();
            }
        } else if (value.startsWith("'") && value.endsWith("'")) {
            String innerValue = value.substring(1, value.length() - 1);
            page.getDocument().query(selector).setText(innerValue);
        } else {
            page.getDocument().query(selector).setValue(value);
        }
    }

    private void getWaitBefore(HashMap<String, Object> stepItems) {
        if (stepItems.containsKey("waitBefore")) {
            this.waitBefore = Optional.of(Integer.parseInt((String) stepItems.get("waitBefore")));
        } else {
            this.waitBefore = Optional.empty();
        }
    }

    private void getWaitAfter(HashMap<String, Object> stepItems) {
        if (stepItems.containsKey("waitAfter")) {
            this.waitAfter = Optional.of(Integer.parseInt((String) stepItems.get("waitAfter")));
        } else {
            this.waitAfter = Optional.empty();
        }
    }

    private void getSubmit(HashMap<String, Object> stepItems) {
        if (stepItems.containsKey("submit")) {
            Map<String, String> submit = (Map) stepItems.get("submit");
            submitType = Optional.of(submit.get("type"));
            submitSelector = Optional.of(submit.get("selector"));
        } else {
            submitSelector = Optional.empty();
            submitType = Optional.empty();
        }
    }

    private Map<String, String> getFields(List<Object> items) {
        Map<String, String> localFields = new HashMap<>();

        for (Object f : items) {
            String value = null;
            String selector = null;
            Map<String, String> field = (HashMap) f;
            for (Map.Entry<String, String> e : field.entrySet()) {
                if (e.getKey().equals("value")) {
                    value = e.getValue();
                }
                if (e.getKey().equals("selector")) {
                    selector = e.getValue();
                }
                if (value != null && selector != null) {
                    localFields.put(selector, value);
                    selector = null;
                    value = null;
                }
            }
        }
        return localFields;
    }

    @Override
    public String toString() {
        return "FormStep{" + "waitBefore=" + waitBefore + ", waitAfter=" + waitAfter + ", submitType=" + submitType + ", submitSelector=" + submitSelector + ", fields=" + fields + '}';
    }
}
