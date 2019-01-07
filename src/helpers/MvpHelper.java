package helpers;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import enums.CodeType;
import enums.PackageType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ui.MvpCreatorDialog;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;

public class MvpHelper {
    private Project project;
    private String packageName;
    private String moduleName;

    public MvpHelper(Project project) {
        this.project = project;
        this.packageName = getPackageName(project);
    }

    public void createPattern(AnActionEvent e, PackageType packageType) {
        refreshProject(e);
        if (packageType == PackageType.Module) {
            MvpCreatorDialog dialog = new MvpCreatorDialog((moduleName) -> {
                this.moduleName = moduleName;
                createClassFiles(PackageType.Module);
                Messages.showInfoMessage(project, "Create mvp module success", "Mvp Creator");
                refreshProject(e);
            });
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        } else if (packageType == PackageType.Base) {
            createClassFiles(PackageType.Base);
            Messages.showInfoMessage(project, "Create base success", "Mvp Creator");
            refreshProject(e);
        }
    }

    private void createClassFiles(PackageType packageType) {
        switch (packageType) {
            case Base:
                createClassFile(packageType, CodeType.BaseActivity);
                createClassFile(packageType, CodeType.BasePresenter);
                createClassFile(packageType, CodeType.MvpPresenter);
                createClassFile(packageType, CodeType.MvpView);
                break;
            case Module:
                createClassFile(packageType, CodeType.Activity);
                createClassFile(packageType, CodeType.IPresenter);
                createClassFile(packageType, CodeType.Presenter);
                createClassFile(packageType, CodeType.IView);
                break;
        }
    }

    /**
     * Generate mvp framework code
     */

    private void createClassFile(PackageType packageType, CodeType codeType) {
        String fileName = "";
        String childName = "";
        String content;
        String appPath = getAppPath(packageType);

        switch (codeType) {
            case BaseActivity:
                childName = "BaseActivity";
                fileName = childName;
                break;
            case BasePresenter:
                childName = "BasePresenter";
                fileName = childName;
                break;
            case MvpPresenter:
                childName = "MvpPresenter";
                fileName = childName;
                break;
            case MvpView:
                childName = "MvpView";
                fileName = childName;
                break;
            case Activity:
                childName = "Activity";
                fileName = getNameActivity();
                break;
            case IPresenter:
                childName = "IPresenter";
                fileName = getNameIPresenter();
                break;
            case Presenter:
                childName = "Presenter";
                fileName = getNamePresenter();
                break;
            case IView:
                childName = "IView";
                fileName = getNameIView();
                break;
        }

        content = readTemplateFile(packageType, childName + ".txt");
        content = dealTemplateContent(packageType, content);
        writeToFile(content, appPath, fileName + ".java");
    }

    private String getNameActivity() {
        return moduleName + "Activity";
    }

    private String getNamePresenter() {
        return moduleName + "Presenter";
    }

    private String getNameIPresenter() {
        return "I" + moduleName + "Presenter";
    }

    private String getNameIView() {
        return "I" + moduleName + "View";
    }

    private void refreshProject(AnActionEvent e) {
        if (e.getProject() != null) {
            e.getProject().getBaseDir().refresh(false, true);
        }
    }

    private String getPackageName(Project project) {
        String packageName = "";
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(project.getBasePath() + "/app/src/main/AndroidManifest.xml");

            NodeList nodeList = doc.getElementsByTagName("manifest");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                Element element = (Element) node;
                packageName = element.getAttribute("package");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packageName;
    }

    private String getAppPath(PackageType packageType) {
        String packagePath = packageName.replace(".", "/");
        return project.getBasePath() + "/app/src/main/java/" + packagePath + "/" + (packageType == PackageType.Base ? "base/" : (packageType == PackageType.Module ? moduleName.toLowerCase() + "/" : ""));
    }

    private String dealTemplateContent(PackageType packageType, String content) {
        if (packageType == PackageType.Module) {
            content = content.replace("$moduleName", moduleName);
        }
        if (content.contains("$packageName")) {
            content = content.replace("$packageName", packageName + "." + moduleName.toLowerCase());
        }
        if (content.contains("$basePackageName")) {
            content = content.replace("$basePackageName", packageName + ".base");
        }
        return content;
    }

    private void writeToFile(String content, String classPath, String className) {
        try {
            File folder = new File(classPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(classPath + "/" + className);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readTemplateFile(PackageType packageType, String fileName) {
        InputStream in;
        String resourcePath = "/templates/" + (packageType == PackageType.Base ? "base/" : packageType == PackageType.Module ? "module/" : "") + fileName;
        in = this.getClass().getResourceAsStream(resourcePath);
        String content = "";
        try {
            content = new String(readStream(in));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    private byte[] readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            outputStream.close();
            inputStream.close();
        }

        return outputStream.toByteArray();
    }
}
