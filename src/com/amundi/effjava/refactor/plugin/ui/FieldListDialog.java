package com.amundi.effjava.refactor.plugin.ui;

import javax.swing.*;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import org.jetbrains.annotations.Nullable;

public class FieldListDialog extends DialogWrapper {
	LabeledComponent component;

	public FieldListDialog(@Nullable PsiClass psiClass) {
		super(psiClass.getProject());
		CollectionListModel<PsiField> fieldList = new CollectionListModel<PsiField>(psiClass.getAllFields());
		JList jlist = new JList(fieldList);
		jlist.setCellRenderer(new DefaultPsiElementCellRenderer());
		ToolbarDecorator decorator = ToolbarDecorator.createDecorator(jlist);
		decorator.disableAddAction();
		component = LabeledComponent.create(decorator.createPanel(), "List of fields");
		init();
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return component;
	}
}
