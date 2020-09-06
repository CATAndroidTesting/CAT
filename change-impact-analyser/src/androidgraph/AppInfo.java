package androidgraph;

import soot.jimple.infoflow.android.resources.controls.AndroidLayoutControl;
import soot.util.HashMultiMap;
import soot.util.MultiMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AppInfo {
    private final MultiMap<String, String> classes;
    private final MultiMap<String, Integer> layoutViews;
    private final Map<Integer, AndroidLayoutControl> views;
    private final Map<Integer, String> resourceIDMapInt2Str;
    private final Map<String, Integer> resourceIDMapStr2Int;
    private final String packageName;
    private final String appName;

    public AppInfo(String _appName, String _packageName) {
        this.classes = new HashMultiMap<>();
        this.layoutViews = new HashMultiMap<>();
        this.views = new HashMap<>();
        this.resourceIDMapInt2Str = new HashMap<>();
        this.resourceIDMapStr2Int = new HashMap<>();
        this.appName = _appName;
        this.packageName = _packageName;
    }

    public void addFunction(String className, String functionSignature) {
        classes.put(className, functionSignature);
    }

    public void addFunction(String functionSignature) {
        String className = "NOT_KNOWN";
        addFunction(className, functionSignature);
    }

    @Override
    public String toString() {
        StringBuilder _string = new StringBuilder();
        for (String key: classes.keySet()) {
            _string.append("class: ").append(key).append('\n');
            for (String functionSignature: classes.get(key)) {
                _string.append("    ").append(functionSignature).append('\n');
            }
        }
        return _string.toString();
    }

    public Set<String> getClassFunctions(String className) {
        return classes.get(className);
    }

    public Set<Integer> getLayoutViews(String activity) {
        return layoutViews.get(activity);
    }

    public void addLayoutView(String activity, Integer view) {
        layoutViews.put(activity, view);
    }

    public Set<String> getLayouts() {
        return layoutViews.keySet();
    }

    public void addView(int id, AndroidLayoutControl androidLayoutControl) {
        this.views.put(id, androidLayoutControl);
    }

    public AndroidLayoutControl getView(int id) {
        return views.get(id);
    }

    public Map<Integer, AndroidLayoutControl> getViews() {
        return this.views;
    }

    public Set<String> getClasses() {
        return classes.keySet();
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void addResourceID(int id, String strID) throws RuntimeException {
        if (resourceIDMapInt2Str.containsKey(id) && !resourceIDMapInt2Str.get(id).equals(strID)) {
            throw new RuntimeException("Parsing resource map fails: existing id with different values " + strID + " and " +
                    resourceIDMapInt2Str.get(id));
        }
        this.resourceIDMapInt2Str.put(id, strID);
        this.resourceIDMapStr2Int.put(strID, id);
    }

    public void addResourceID(String strID, int id) {
        addResourceID(id, strID);
    }

    public int getResourceIDInt(String strID) {
        return this.resourceIDMapStr2Int.get(strID);
    }

    public String getResourceIDStr (int id) {
        return this.resourceIDMapInt2Str.get(id);
    }
}
