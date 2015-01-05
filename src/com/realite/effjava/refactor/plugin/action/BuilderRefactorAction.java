package com.realite.effjava.refactor.plugin.action;

import java.io.IOException;
import java.io.StringWriter;
import java.util.EnumSet;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.util.PsiTreeUtil;
import com.realite.effjava.refactor.plugin.ui.FieldListDialog;
import com.squareup.javawriter.JavaWriter;

public class BuilderRefactorAction extends AnAction {

	private static final String BUILDER_NAME = "Builder";

	public void actionPerformed(AnActionEvent e) {
		final PsiClass currentClass = getPsiClass(e);
		if (null == currentClass) {
			return;
		}
		final FieldListDialog dialog = new FieldListDialog(currentClass);
		dialog.show();
		if (dialog.isOK()) {
			final List<PsiField> selected = dialog.getSelectedFields();
			WriteCommandAction writeCommandAction = new WriteCommandAction.Simple(currentClass.getProject()) {
				@Override
				protected void run() throws Exception{
					PsiClass innerClass = generateInner(selected);
					if (builderExists(currentClass)) {
						PsiClass oldInnerClass = getInnerClass(currentClass);
						oldInnerClass.replace(innerClass);
					} else {
						currentClass.add(innerClass);
					}
				}

				private PsiClass generateInner(List<PsiField> selected) throws IOException{
					PsiClass innerClass;
					StringWriter stringWriter = new StringWriter();
					JavaWriter writer = new JavaWriter(stringWriter);
					writer.emitPackage("");
					for (PsiField field : selected) {
						String fieldType = field.getType().getPresentableText();
						String fieldName = field.getName();
						writer.emitField(fieldType, fieldName, EnumSet.of(Modifier.PRIVATE));
					}
					for (PsiField field : selected) {
						String fieldType = field.getType().getPresentableText();
						String fieldName = field.getName();
						writer.beginMethod(BUILDER_NAME, fieldName, EnumSet.of(Modifier.PUBLIC), fieldType, fieldName)
								.emitStatement("this." + fieldName + " = " + fieldName)
								.emitStatement("return this")
								.endMethod();
					}
					String classText = stringWriter.toString().replace("'", "");
					innerClass = JavaPsiFacade.getElementFactory(getProject()).createClassFromText(classText, currentClass);
					innerClass.setName(BUILDER_NAME);
					innerClass.getModifierList().setModifierProperty(PsiModifier.PUBLIC, true);
					innerClass.getModifierList().setModifierProperty(PsiModifier.STATIC, true);
					innerClass.getModifierList().setModifierProperty(PsiModifier.FINAL, true);
					return  innerClass;
				}

			};
			writeCommandAction.execute();
		}
	}

	private boolean builderExists(PsiClass clazz) {
		return getInnerClass(clazz) != null;
	}

	private PsiClass getInnerClass(PsiClass clazz) {
		return clazz.findInnerClassByName(BUILDER_NAME, false);
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
