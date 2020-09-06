package androidgraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class SourceTraverser {
    private final String axmlFilePath;
    private final String relativeRootPath;
    private final String packageName;
    private Map<String, List<String>> activityLayoutMap;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static class LayoutFileNameCollector extends VoidVisitorAdapter<List<String>> {
        @Override
        public void visit(MethodCallExpr n, List<String> arg) {
            super.visit(n, arg);
            if (n.getNameAsString().equals("setContentView")) {
                arg.add(n.getArgument(0).toString());
            }
        }
    }

    public SourceTraverser(String _axmlFilePath) throws Exception {
        activityLayoutMap = new HashMap<>();

        axmlFilePath = _axmlFilePath;
        relativeRootPath = axmlFilePath.substring(0, axmlFilePath.lastIndexOf("/") + 1);
        logger.info("Path to the source: " + relativeRootPath);
        File axmlFile = new File(axmlFilePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(axmlFile);
        doc.getDocumentElement().normalize();
        packageName = doc.getDocumentElement().getAttribute("package");
        logger.info("Package name: " + packageName);

        NodeList activityList = doc.getElementsByTagName("activity");
        for (int i = 0; i < activityList.getLength(); i++) {
            Node activityNode = activityList.item(i);
            if (activityNode.getNodeType() == Node.ELEMENT_NODE) {
                Element activityElement = (Element) activityNode;
                String activityName = activityElement.getAttribute("android:name");
                if (activityName.charAt(0) == '.') {
                    activityName = packageName + activityName;
                }
                CompilationUnit cu;
                try {
                    cu = StaticJavaParser.parse(new File(relativeRootPath + activityName.replace('.', '/') + ".java"));
                } catch (Exception e) {
                    cu = StaticJavaParser.parse(new File(relativeRootPath + "java/" + activityName.replace('.', '/') + ".java"));
                }
                List<String> layouts = new ArrayList<>();
                VoidVisitor<List<String>> layoutFileCollector = new LayoutFileNameCollector();
                layoutFileCollector.visit(cu, layouts);
                activityLayoutMap.put(activityName, layouts);
                logger.info("Activity " + i + " detected: " + activityName);
                layouts.forEach(layout -> logger.info("Layout detected: " + layout));
            }
        }


    }
}
