package androidgraph;

import org.xmlpull.v1.XmlPullParserException;
import soot.*;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

public class Main {
    private static final String appPath = "/Users/chaopeng/Project/droidbot/playground/01_Worldweather/worldweather.apk";
    private static final String androidPlatformPath = "/Users/chaopeng/Library/Android/sdk/platforms";
    private static final String aaptPath = "/Users/chaopeng/Library/Android/sdk/build-tools/30.0.1/aapt";
    public static void main(String[] args) {
        //SourceTraverser st = new SourceTraverser("/Users/chaopeng/Project/dreal/AmazeFileManager/043/AmazeFileManager-v.3.3.0-rc13/app/src/main/AndroidManifest.xml");
        //SetupApplication analyzer = new SetupApplication(androidPlatformPath, appPath);
        //analyzer.constructCallgraph();
        //CallGraph callGraph = Scene.v().getCallGraph();
        try {
            APKAnalyser apkAnalyser = new APKAnalyser(appPath, androidPlatformPath, aaptPath);
            apkAnalyser.analyse();
//            SimplifiedCallGraphNode nodeChanged = new SimplifiedCallGraphNode(SimplifiedCallGraphNode.NODE_TYPE_FUNCTION, "<com.amaze.filemanager.activities.AboutActivity: android.support.design.widget.CoordinatorLayout$LayoutParams calculateHeaderViewParams()>");
//            System.out.println("Start looking for controllers and activities...");
//            Set<SimplifiedCallGraphNode> relatedViews = apkAnalyser.getRelatedViews(nodeChanged);
//            System.out.println("Controllers found");
//            for (SimplifiedCallGraphNode view: relatedViews) {
//                System.out.println(view);
//                for (SimplifiedCallGraphNode activity: apkAnalyser.getRelatedActivities(view)) {
//                    System.out.println("    " + activity);
//                }
//            }
        } catch (IOException | XmlPullParserException exception) {
            exception.printStackTrace();
        }


        /*

        FileWriter fileWriter = new FileWriter("/Users/chaopeng/Playground/Android/filename.txt");

        for (SootMethod sootMethod : Scene.v().getMethodNumberer()) {
            try {
                fileWriter.write(sootMethod.getDeclaringClass().getName() + " DECLARES " + sootMethod.getName() + "\n");
            } catch (RuntimeException e) {
                fileWriter.write("NO CLASS DECLARES " + sootMethod.getName() + "\n");
            }
        }

        // Iterate over the callgraph and write to file

        int count = 0;
        for (Edge edge : callGraph) {
            SootMethod smSrc = edge.src();
            Unit uSrc = edge.srcStmt();
            SootMethod smDest = edge.tgt();

            fileWriter.write("Edge from " + uSrc + " in " + smSrc + " to " + smDest + "\n");
            // System.out.println("Edge from " + uSrc + " in " + smSrc + " to " + smDest);
        }
        fileWriter.close();

         */
    }
}
