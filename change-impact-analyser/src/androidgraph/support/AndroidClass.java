package androidgraph.support;

import java.util.ArrayList;
import java.util.List;

public class AndroidClass {
    private String fullName;
    private List<String> functions;

    public AndroidClass(String _fullName) {
        fullName = _fullName;
        functions = new ArrayList<>();
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }
}
