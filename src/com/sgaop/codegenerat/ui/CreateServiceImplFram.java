package com.sgaop.codegenerat.ui;

import com.intellij.ide.actions.OpenProjectFileChooserDescriptor;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.messages.impl.Message;
import com.sgaop.codegenerat.idea.ProjectPluginConfig;
import com.sgaop.codegenerat.templte.BeetlTemplteEngine;
import com.sgaop.codegenerat.templte.ITemplteEngine;
import com.sgaop.codegenerat.vo.JavaBaseVO;
import com.sgaop.codegenerat.vo.JavaFieldVO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author 黄川 306955302@qq.com
 * @date: 2018/5/30
 * 自动生成接口和实现类
 */
public class CreateServiceImplFram extends JDialog {
    ProjectPluginConfig pluginrInfo;
    String entityPackage;
    String entityName;
    String serviceFileName;
    String serviceImplFileName;
    String servicePackage;
    String serviceImplPackage;
    String actionPackage;
    String actionFileName;
    String htmlPaths;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField servicePackageText;
    private JTextField serviceImplPackageText;
    private JTextField actionPackageText;
    private JTextField htmlPathText;
    private JCheckBox actionCheckBox;
    private JCheckBox htmlPathCheckBox;
    private TextFieldWithBrowseButton basePathText;
    private JTextField funNameText;

    public CreateServiceImplFram(ProjectPluginConfig pluginEditorInfo, String entityPackage, String entityName) {
        this.pluginrInfo = pluginEditorInfo;
        this.entityPackage = entityPackage;
        this.entityName = entityName;
        this.serviceFileName = entityName + "Service";
        this.serviceImplFileName = entityName + "ServiceImpl";
        this.actionFileName = entityName + "Action";
        this.servicePackage = entityPackage.replace("entity", "service");
        this.serviceImplPackage = entityPackage.replace("entity", "service") + ".impl";
        this.actionPackage = entityPackage.replace("entity", "module");
        this.htmlPaths = "/" + entityName + "/";
        int w = 500, h = 400;
        int x = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - (w / 2));
        int y = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - (h / 2));
        setContentPane(contentPane);
        setTitle("代码生成");
        setModal(true);
        setBounds(x, y, w, h);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener((e) -> onOK());
        buttonCancel.addActionListener((e) -> onCancel());
        servicePackageText.setText(this.servicePackage + "." + serviceFileName);
        serviceImplPackageText.setText(this.serviceImplPackage + "." + serviceImplFileName);
        actionPackageText.setText(this.actionPackage + "." + actionFileName);
        htmlPathText.setText(this.htmlPaths);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(((e) -> onCancel()), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        basePathText.addActionListener((e -> {
            OpenProjectFileChooserDescriptor descriptor = new OpenProjectFileChooserDescriptor(false, false);
            descriptor.setHideIgnored(true);
            descriptor.setShowFileSystemRoots(false);
            descriptor.setRoots(pluginEditorInfo.getProject().getBaseDir());
            descriptor.setTitle("请选择WEB-INF下的目录");
            FileChooser.chooseFiles(descriptor, pluginEditorInfo.getProject(), null, virtualFiles -> {
                VirtualFile virtualFile = virtualFiles.get(0);
                String selectPath = virtualFile.getCanonicalPath();
                int start = selectPath.indexOf("WEB-INF");
                if (start == -1) {
                    JOptionPane.showMessageDialog(this.rootPane, "请选择WEB-INF下的目录", "错误提示", JOptionPane.ERROR_MESSAGE, null);
                } else {
                    selectPath = selectPath.replace("\\\\", "/").replace("\\", "/");
                    basePathText.setText(selectPath);
                }
            });
        }));
    }

    private void onOK() {
        try {
            if (this.htmlPathCheckBox.isSelected() && this.basePathText.getText().trim().length() == 0) {
                JOptionPane.showMessageDialog(this.rootPane, "请选择HTML目录", "错误提示", JOptionPane.ERROR_MESSAGE, null);
            } else if (this.funNameText.getText().trim().length() == 0) {
                JOptionPane.showMessageDialog(this.rootPane, "请输入功能名称", "错误提示", JOptionPane.ERROR_MESSAGE, null);
            } else {
                String moduleBasePath = pluginrInfo.getPsiFile().getVirtualFile().getCanonicalPath();
                String temp = entityPackage.replaceAll("\\.", "/");
                moduleBasePath = moduleBasePath.replace(temp, "");
                moduleBasePath = moduleBasePath.replace("/" + entityName + ".java", "");
                render(moduleBasePath, buildData(pluginrInfo.getJavaFields()));
                JOptionPane.showMessageDialog(this.rootPane, "生成完成", "生成完成", JOptionPane.INFORMATION_MESSAGE, null);
                dispose();
            }
        } catch (Throwable ex) {
            JOptionPane.showMessageDialog(this.rootPane, ex.getMessage(), "错误提示", JOptionPane.ERROR_MESSAGE, null);
        }
    }


    private HashMap buildData(List<JavaFieldVO> javaFields) {
        JavaBaseVO baseVO = new JavaBaseVO();
        baseVO.setEntityName(this.entityName);
        baseVO.setEntityPackage(this.entityPackage);
        baseVO.setServiceFileName(this.serviceFileName);
        baseVO.setServicePackage(this.servicePackage);
        baseVO.setServiceImplFileName(this.serviceImplFileName);
        baseVO.setServiceImplPackage(this.serviceImplPackage);
        baseVO.setActionFileName(this.actionFileName);
        baseVO.setActionPackage(this.actionPackage);
        baseVO.setFunName(this.funNameText.getText());
        Optional optional = javaFields.stream().filter(JavaFieldVO::isPrimaryKey).findFirst();
        if (optional.get() != null) {
            JavaFieldVO field = (JavaFieldVO) optional.get();
            baseVO.setPrimaryKey(field.getDbName());
            if ("uuid".equals(field.getDbName().toLowerCase())) {
                baseVO.setUuid(true);
            }
        }
        baseVO.setRichText(javaFields.stream().filter(javaFieldVO -> javaFieldVO.getText() == 4).findFirst() != null);
        baseVO.setAttachment(javaFields.stream().filter(JavaFieldVO::isAttachment).findFirst() != null);
        String templatePath = this.basePathText.getText();
        int start = templatePath.indexOf("WEB-INF");
        baseVO.setTemplatePath(templatePath.substring(start) + htmlPaths);
        baseVO.setUser(System.getProperties().getProperty("user.name"));

        HashMap bindData = getBindData(baseVO);
        bindData.put("base", baseVO);
        bindData.put("fields", pluginrInfo.getJavaFields());
        return bindData;
    }

    private void render(String moduleBasePath, HashMap bindData) {
        ITemplteEngine renderTemplte = new BeetlTemplteEngine();
        FileTemplateManager fileTemplateManager = FileTemplateManager.getInstance(pluginrInfo.getProject());
        FileTemplate service = fileTemplateManager.getJ2eeTemplate("Service");
        FileTemplate serviceImpl = fileTemplateManager.getJ2eeTemplate("ServiceImpl");
        FileTemplate actionImpl = fileTemplateManager.getJ2eeTemplate("Action");
        FileTemplate indexHtml = fileTemplateManager.getJ2eeTemplate("Index");
        FileTemplate editHtml = fileTemplateManager.getJ2eeTemplate("Edit");
        VirtualFile value = VirtualFileManager.getInstance().findFileByUrl(Paths.get(moduleBasePath).toUri().toString());
        renderTemplte.renderToFile(service.getText(), bindData, getPath(moduleBasePath, this.servicePackageText.getText()));
        renderTemplte.renderToFile(serviceImpl.getText(), bindData, getPath(moduleBasePath, this.serviceImplPackageText.getText()));
        if (this.actionCheckBox.isSelected()) {
            renderTemplte.renderToFile(actionImpl.getText(), bindData, getPath(moduleBasePath, this.actionPackageText.getText()));
        }
        if (this.htmlPathCheckBox.isSelected()) {
            renderTemplte.renderToFile(indexHtml.getText(), bindData, Paths.get(this.basePathText.getText(), this.htmlPaths, "index.html"));
            renderTemplte.renderToFile(editHtml.getText(), bindData, Paths.get(this.basePathText.getText(), this.htmlPaths, "edit.html"));
        }
        value.refresh(true, true);
    }


    private HashMap getBindData(JavaBaseVO baseVO) {
        HashMap bindData = new HashMap(11);
        bindData.put("entityName", baseVO.getEntityName());
        bindData.put("entityPackage", baseVO.getEntityPackage());
        bindData.put("serviceFileName", baseVO.getServiceFileName());
        bindData.put("servicePackage", baseVO.getServicePackage());
        bindData.put("serviceImplFileName", baseVO.getServiceImplFileName());
        bindData.put("serviceImplPackage", baseVO.getServiceImplPackage());
        bindData.put("actionFileName", baseVO.getActionFileName());
        bindData.put("actionPackage", baseVO.getActionPackage());
        bindData.put("funName", baseVO.getFunName());
        bindData.put("templatePath", baseVO.getTemplatePath());
        bindData.put("user", baseVO.getUser());
        bindData.put("uuid", baseVO.isUuid());
        bindData.put("primaryKey", baseVO.getPrimaryKey());
        bindData.put("richText", baseVO.isRichText());
        bindData.put("attachment", baseVO.isAttachment());
        return bindData;
    }

    private Path getPath(String basePath, String packages) {
        String[] s1 = packages.split("\\.");
        ArrayList<String> list = new ArrayList<>();
        int last = s1.length - 1;
        for (int i = 0; i < s1.length; i++) {
            if (i == last) {
                list.add(s1[i] + ".java");
            } else {
                list.add(s1[i]);
            }
        }
        return Paths.get(basePath, list.toArray(new String[0]));
    }

    private void onCancel() {
        dispose();
    }

}
