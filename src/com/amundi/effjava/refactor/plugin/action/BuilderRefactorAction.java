package com.amundi.effjava.refactor.plugin.action;

import com.amundi.effjava.refactor.plugin.ui.FieldListDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

public class BuilderRefactorAction extends AnAction {


	public void actionPerformed(AnActionEvent e) {
		final PsiClass currentClass = getPsiClass(e);
		if (null == currentClass) {
			return;
		}
		FieldListDialog dialog = new FieldListDialog(currentClass);
		dialog.show();
		if (dialog.isOK()) {
			WriteCommandAction writeCommandAction = new WriteCommandAction.Simple(currentClass.getProject()) {
				@Override
				protected void run() throws Throwable {
					StringBuilder builderBuilder = new StringBuilder();
					builderBuilder.append("public static class Builder {");
					builderBuilder.append("}");
					PsiClass innerClass = JavaPsiFacade.getElementFactory(getProject()).createClass(builderBuilder.toString());
					currentClass.add(innerClass);
				}
			};
			writeCommandAction.execute();
		}


	}

	public void update(AnActionEvent e) {
		getPsiClass(e);
	}

	private PsiClass getPsiClass(AnActionEvent e) {
		Editor editor = e.getData(PlatformDataKeys.EDITOR);
		PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
		if (psiFile == null || editor == null) {
			return null;
		}
		int offset = editor.getCaretModel().getOffset();
		PsiElement elementAt = psiFile.findElementAt(offset);
		if (elementAt == null || elementAt.getNode() == null) {
			return null;
		}
		return PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
	}


}
