package org.eclipse.oomph.console.core.util;

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.oomph.base.Annotation;
import org.eclipse.oomph.setup.AttributeRule;
import org.eclipse.oomph.setup.ProductVersion;
import org.eclipse.oomph.setup.Scope;
import org.eclipse.oomph.setup.SetupFactory;
import org.eclipse.oomph.setup.SetupPackage;
import org.eclipse.oomph.setup.SetupTask;
import org.eclipse.oomph.setup.User;
import org.eclipse.oomph.setup.VariableTask;
import org.eclipse.oomph.setup.internal.core.SetupTaskPerformer;
import org.eclipse.oomph.util.OS;

@SuppressWarnings("restriction")
public class ScopeAdjuster {

    private static final URI WORKSPACE_LOCATION_ATTRIBUTE_URI = SetupTaskPerformer
            .getAttributeURI(SetupPackage.Literals.WORKSPACE_TASK__LOCATION);
    private static final URI INSTALLATION_LOCATION_ATTRIBUTE_URI = SetupTaskPerformer
            .getAttributeURI(SetupPackage.Literals.INSTALLATION_TASK__LOCATION);
    private static final URI INSTALLATION_RELATIVE_PRODUCT_FOLDER_ATTRIBUTE_URI = SetupTaskPerformer
            .getAttributeURI(SetupPackage.Literals.INSTALLATION_TASK__RELATIVE_PRODUCT_FOLDER);

    public void setWorkspaceLacation(User user, String workspaceDir) {
        setPreferredAttributeRule(user.getAttributeRules(), WORKSPACE_LOCATION_ATTRIBUTE_URI, workspaceDir);
        setPreferredVariable(user, "workspace.location", workspaceDir);
    }

    public void setInstallationLocation(User user, String installDir) {
        setPreferredAttributeRule(user.getAttributeRules(), INSTALLATION_LOCATION_ATTRIBUTE_URI, installDir);
        setPreferredVariable(user, "installation.location", installDir);
        setPreferredVariable(user, "install.root", installDir);
    }

    public void setProductFolderName(User user, String folderName) {
        setPreferredAttributeRule(user.getAttributeRules(), INSTALLATION_RELATIVE_PRODUCT_FOLDER_ATTRIBUTE_URI,
                folderName);
        setPreferredVariable(user, "installation.id", folderName);
    }

    public void setProductFolderName(ProductVersion scope, String folderName) {
        String osgiOS = OS.INSTANCE.getOsgiOS();
        String osgiWS = OS.INSTANCE.getOsgiWS();
        String osgiArch = OS.INSTANCE.getOsgiArch();
        String[] keys = { "folderName." + osgiOS + '.' + osgiWS + '.' + osgiArch,
                "folderName." + osgiOS + '.' + osgiWS,
                "folderName." + osgiOS,
                "folderName" };
        setProductFolderName(scope, folderName, keys);
    }

    private void setProductFolderName(Scope scope, String value, String[] keys) {
        if (scope == null || value == null)
            return;
        Annotation annotation = scope.getAnnotation("http://www.eclipse.org/oomph/setup/BrandingInfo");
        if (annotation != null) {
            EMap<String, String> details = annotation.getDetails();
            for (int i = 0; i <= keys.length - 1; ++i) {
                String key = keys[i];
                if (details.get(key) != null) {
                    details.put(key, value);
                    return;
                }
                details.put(key, value);
            }
        }
        setProductFolderName(scope.getParentScope(), value, keys);
    }

    public static void setPreferredAttributeRule(EList<AttributeRule> attributeRules, URI uri, String value) {
        if (value == null)
            return;
        for (AttributeRule attributeRule : attributeRules) {
            if (uri.equals(attributeRule.getAttributeURI())) {
                attributeRule.setValue(value);
                return;
            }
        }
        AttributeRule attributeRule = SetupFactory.eINSTANCE.createAttributeRule();
        attributeRule.setAttributeURI(uri);
        attributeRule.setValue(value);
        attributeRules.add(attributeRule);
    }

    public void setPreferredVariable(User user, String name, String value) {
        setPreferredVariable(user.getSetupTasks(), name, value);
    }

    @SuppressWarnings("unchecked")
    public static void setPreferredVariable(List<? extends SetupTask> tasks, String name, String value) {
        if (value == null)
            return;
        for (SetupTask task : tasks) {
            if (task instanceof VariableTask) {
                VariableTask variable = (VariableTask) task;
                if (name.equals(variable.getName())) {
                    variable.setValue(value);
                    return;
                }
            }
        }

        VariableTask variable = SetupFactory.eINSTANCE.createVariableTask();
        variable.setName(name);
        variable.setValue(value);
        ((List<SetupTask>) tasks).add(variable);
    }

    public void setPreferredVariablesFromProperty(List<? extends SetupTask> tasks) {
        for (SetupTask task : tasks) {
            if (task instanceof VariableTask) {
                VariableTask variable = (VariableTask) task;
                String value = System.getProperty(variable.getName());
                if (value != null) {
                    variable.setValue(value);
                }
            }
        }
    }

}
