package cz.bouda.idea.resthelper;

import javax.swing.Icon;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.impl.ShowIntentionActionsHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;

public class ApplyIntentionAction extends AnAction {

    private final IntentionAction myAction;
    private final Editor myEditor;
    private final PsiFile myFile;

    public ApplyIntentionAction(IntentionAction action, Icon icon, String text, Editor editor, PsiFile file) {
        super(text, null, icon);
        myAction = action;
        myEditor = editor;
        myFile = file;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiDocumentManager.getInstance(myFile.getProject()).commitAllDocuments();
        ShowIntentionActionsHandler.chooseActionAndInvoke(myFile, myEditor, myAction, myAction.getText());
    }
}
