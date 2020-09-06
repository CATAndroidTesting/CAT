package androidgraph;

import javassist.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.infoflow.android.resources.ARSCFileParser;
import soot.jimple.infoflow.android.resources.LayoutFileParser;
import soot.jimple.infoflow.android.resources.controls.AndroidLayoutControl;
import soot.jimple.infoflow.android.resources.controls.LayoutControlFactory;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.HashMultiMap;
import soot.util.MultiMap;

import javax.json.Json;
import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class APKAnalyser {
    private final AppInfo appInfo;
    private final MultiMap<SimplifiedCallGraphNode, SimplifiedCallGraphNode> simplifiedCallGraph;
    private final SetupApplication setupApplication;
    private final LayoutFileParser layoutFileParser;
    private final String appPath;
    private final String aaptPath;
    private final Logger logger;


    /**
     * Constructor of the APKAnalyser
     * @param _appPath Path to the APK file
     * @param _androidPlatformPath Path to the android platform folder
     * @throws IOException when APK file is not found
     * @throws XmlPullParserException when parsing the manifest file fails
     */
    public APKAnalyser(String _appPath, String _androidPlatformPath, String _aaptPath)
            throws IOException, XmlPullParserException {
        this.appPath = _appPath;
        this.aaptPath = _aaptPath;
        this.logger =  LoggerFactory.getLogger(getClass());

        // Setup FlowDroid application
        final File targetAPK = new File(_appPath);
        if (!targetAPK.exists()) {
            throw new RuntimeException(String.format("Target APK file %s does not exist", targetAPK.getCanonicalPath()));
        }
        // FlowDroid data
        ARSCFileParser resources = new ARSCFileParser();
        resources.parse(targetAPK.getAbsolutePath());
        ProcessManifest manifest = new ProcessManifest(targetAPK, resources);
        this.setupApplication = new SetupApplication(_androidPlatformPath, _appPath);
        this.layoutFileParser = new LayoutFileParser(manifest.getPackageName(), resources);

        this.appInfo = new AppInfo(manifest.getApplicationName(), manifest.getPackageName());
        this.simplifiedCallGraph = new HashMultiMap<>();
    }

    public void analyse() {
        processResourceIDMap();
        processApk();
    }

    private boolean determineBaseClass(SootClass targetClass, String classTypeStr) {
        if (targetClass != null) {
            if (targetClass.getName().equals(classTypeStr)) {
                return true;
            } else if (targetClass.hasOuterClass()) {
                return determineBaseClass(targetClass.getOuterClass(), classTypeStr);
            } else if (targetClass.hasSuperclass()) {
                return determineBaseClass(targetClass.getSuperclass(), classTypeStr);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Process the APK file to generate and refine the call graph, gather activities, views and their relationships
     */
    private void processApk() {
        setupApplication.constructCallgraph();
        CallGraph callGraph = Scene.v().getCallGraph();
        // Gather functions
        for (SootMethod sootMethod: Scene.v().getMethodNumberer()) {
            try {
                appInfo.addFunction(sootMethod.getDeclaringClass().getName(), sootMethod.toString());
                if (determineBaseClass(sootMethod.getDeclaringClass(), "android.app.Activity")) {
                    SimplifiedCallGraphNode functionNode = new SimplifiedCallGraphNode(SimplifiedCallGraphNode.NODE_TYPE_FUNCTION, sootMethod.toString());
                    SimplifiedCallGraphNode activityNode = new SimplifiedCallGraphNode(SimplifiedCallGraphNode.NODE_TYPE_ACTIVITY, sootMethod.getDeclaringClass().toString());
                    simplifiedCallGraph.put(functionNode, activityNode);
                    System.out.println(functionNode.toString() + "  " + activityNode.toString());
                }

            } catch (RuntimeException runtimeException) {
                appInfo.addFunction(sootMethod.getDeclaration()); //Runtime Exception is thrown if using toString()
            }
        }

        // Go through FlowDroid call graph to gather:
        // Function call to function call
        // Activity class to layout (by layout resource id)
        // Activity class to view (by view resource id)

        for (Edge edge : callGraph) {
            SootMethod smSrc = edge.src();
            SootMethod smDest = edge.tgt();
            SimplifiedCallGraphNode srcNode = new SimplifiedCallGraphNode(SimplifiedCallGraphNode.NODE_TYPE_FUNCTION, smSrc.toString());
            SimplifiedCallGraphNode destNode = new SimplifiedCallGraphNode(SimplifiedCallGraphNode.NODE_TYPE_FUNCTION, smDest.toString());
            simplifiedCallGraph.put(destNode, srcNode);

            if (edge.srcStmt() != null && edge.srcStmt().containsInvokeExpr()) {
                InvokeExpr invokeExpr = edge.srcStmt().getInvokeExpr();
                if (invokeExpr != null) {
                    String methodName = invokeExpr.getMethod().getName();
                    if (methodName.equals("findViewById")) {
                        List<Value> values = invokeExpr.getArgs();
                        try {
                            int viewID = Integer.parseInt(values.get(0).toString());
                            String viewName = appInfo.getResourceIDStr(viewID);
                            if (viewName != null) {
                                SimplifiedCallGraphNode activityNode = new SimplifiedCallGraphNode(SimplifiedCallGraphNode.NODE_TYPE_ACTIVITY, smSrc.getDeclaringClass().getName());
                                SimplifiedCallGraphNode viewNode = new SimplifiedCallGraphNode(SimplifiedCallGraphNode.NODE_TYPE_VIEW, viewName);
                                simplifiedCallGraph.put(viewNode, activityNode);
                            } else {
                                logger.info("resource id " + viewID + " not found in resources");
                            }
                        } catch (Exception e) {
                            logger.info("Ignoring a activity-view relation with unknown view id " + values.get(0).toString() + " in " + edge.srcStmt().toString());
                        }
                    } else if (methodName.equals("setContentView")) {
                        List<Value> values = invokeExpr.getArgs();
                        try {
                            int layoutID = Integer.parseInt(values.get(0).toString());
                            String layoutName = appInfo.getResourceIDStr(layoutID);
                            if (layoutName != null) {
                                SimplifiedCallGraphNode activityNode = new SimplifiedCallGraphNode(SimplifiedCallGraphNode.NODE_TYPE_ACTIVITY, smSrc.getDeclaringClass().getName());
                                SimplifiedCallGraphNode layoutNode = new SimplifiedCallGraphNode(SimplifiedCallGraphNode.NODE_TYPE_LAYOUT, layoutName);
                                simplifiedCallGraph.put(layoutNode, activityNode);
                            } else {
                                logger.info("resource id " + layoutID + " not found in resources");
                            }
                        } catch (Exception e) {
                            logger.info("Ignoring a activity-view relation with unknown view id " + values.get(0).toString() + " in " + edge.srcStmt().toString());
                        }
                    }
                }
            }

        }


        LayoutControlFactory layoutControlFactory = new LayoutControlFactory();
        layoutControlFactory.setLoadAdditionalAttributes(true);
        layoutFileParser.setControlFactory(layoutControlFactory);
        layoutFileParser.parseLayoutFileDirect(this.appPath);

        MultiMap<String, AndroidLayoutControl> userControls = layoutFileParser.getUserControls();

        for (String layoutFile: userControls.keySet()) {
            for (AndroidLayoutControl androidLayoutControl: userControls.get(layoutFile)) {
                appInfo.addLayoutView(layoutFile, androidLayoutControl.getID());
                appInfo.addView(androidLayoutControl.getID(), androidLayoutControl);
            }
        }
        for (String layout: appInfo.getLayouts()) {
            SimplifiedCallGraphNode layoutNode = new SimplifiedCallGraphNode(SimplifiedCallGraphNode.NODE_TYPE_LAYOUT, layout);
            for (int view: appInfo.getLayoutViews(layout)) {
                if (appInfo.getResourceIDStr(view) != null) {
                    SimplifiedCallGraphNode viewNode = new SimplifiedCallGraphNode(SimplifiedCallGraphNode.NODE_TYPE_VIEW, appInfo.getResourceIDStr(view));
                    simplifiedCallGraph.put(viewNode, layoutNode);
                }
            }
            for (String includedLayout: layoutFileParser.getLayoutInclusions().get(layout)) {
                SimplifiedCallGraphNode includedLayoutNode = new SimplifiedCallGraphNode(SimplifiedCallGraphNode.NODE_TYPE_LAYOUT, includedLayout);
                simplifiedCallGraph.put(includedLayoutNode, layoutNode);
            }
        }
    }

    private void processResourceIDMap() {
        // Run aapt command to dump resources
        ProcessBuilder processBuilder = new ProcessBuilder(aaptPath, "dump", "resources", this.appPath);
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] subStrings = line.strip().split(" ");
                int id;
                String strID;
                if (subStrings[0].equals("spec")) {
                    id = Integer.decode(subStrings[2]);
                    strID = subStrings[3].substring(0, subStrings[3].lastIndexOf(":"));
                } else if (subStrings[0].equals("resource")) {
                    id = Integer.decode(subStrings[1]);
                    strID = subStrings[2].substring(0, subStrings[2].lastIndexOf(":"));
                } else {
                    continue;
                }
                appInfo.addResourceID(id, strID);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private Set<SimplifiedCallGraphNode> traceBackFirst(SimplifiedCallGraphNode baseNode, int targetNodeType) {
        Set<SimplifiedCallGraphNode> collectedNodes = new HashSet<>();

        Set<SimplifiedCallGraphNode> nodesToProcess = new HashSet<>();
        Set<SimplifiedCallGraphNode> processedNodes = new HashSet<>();

        nodesToProcess.add(baseNode);
        while (!nodesToProcess.isEmpty()) {
            for (SimplifiedCallGraphNode nodeToProcess: nodesToProcess) {
                Set<SimplifiedCallGraphNode> relatedNodes = simplifiedCallGraph.get(nodeToProcess);
                for (SimplifiedCallGraphNode relatedNode: relatedNodes) {
                    if (relatedNode.getNodeType() == targetNodeType) {
                        collectedNodes.add(relatedNode);
                    } else if (relatedNode.getNodeType() < targetNodeType && !processedNodes.contains(relatedNode)) {
                        nodesToProcess.add(relatedNode);
                    }
                }
                processedNodes.add(nodeToProcess);
                nodesToProcess.remove(nodeToProcess);
            }
        }

        return collectedNodes;
    }

    public Set<SimplifiedCallGraphNode> getRelatedViews(SimplifiedCallGraphNode targetNode) {
        if (simplifiedCallGraph.keySet().contains(targetNode)) {
            return traceBackFirst(targetNode, SimplifiedCallGraphNode.NODE_TYPE_VIEW);
        } else {
            return null;
        }
    }

    public Set<SimplifiedCallGraphNode> getRelatedActivities(SimplifiedCallGraphNode targetView) {
        if (simplifiedCallGraph.keySet().contains(targetView)) {
            return traceBackFirst(targetView, SimplifiedCallGraphNode.NODE_TYPE_ACTIVITY);
        } else {
            return null;
        }
    }

    public void dumpCallGraphJSON(String path) {
        try {
            FileWriter fileWriter = new FileWriter(path);
            fileWriter.write("{\n    \"nodes\": {\n");
            for (SimplifiedCallGraphNode key: simplifiedCallGraph.keySet()) {
                fileWriter.write("        \"" + key.getUniqueID() + "\": ");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
