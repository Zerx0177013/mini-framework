package roro.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModAndView {
    private String view;
    private Map<String, List<?>> values;

    public ModAndView() {
        this.values = new HashMap<>();
    }

    public ModAndView(String view, Map<String, List<?>> values) {
        this.view = view;
        this.values = values;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public Map<String, List<?>> getValues() {
        return values;
    }

    public void setValues(Map<String, List<?>> values) {
        this.values = values;
    }

    public void addValue(String key, List<?> value) {
        this.values.put(key, value);
    }
}