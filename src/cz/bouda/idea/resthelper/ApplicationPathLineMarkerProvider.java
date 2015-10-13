package cz.bouda.idea.resthelper;

import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.ApplicationPath;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Function;
import com.intellij.util.NullableFunction;

import cz.bouda.idea.resthelper.icons.RestIcons;
import cz.bouda.idea.resthelper.intention.RunJerseyIntention;

import static java.util.Collections.singletonList;

public class ApplicationPathLineMarkerProvider implements LineMarkerProvider {

    private static List<RunJerseyIntention> runJerseyIntentions;

    static {
        runJerseyIntentions = Arrays.asList(
            new RunJerseyIntention("Run Jersey", AllIcons.Actions.Execute),
            new RunJerseyIntention("Debug Jersey", AllIcons.Actions.StartDebugger),
            new RunJerseyIntention("Generate Requests", AllIcons.Debugger.ThreadStates.Threaddump));
    }

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull final PsiElement element) {
        return null;
    }

    private boolean isApplicationPathAnnotated(final @NotNull PsiElement element) {
        return AnnotationUtil.isAnnotated((PsiClass) element, singletonList(ApplicationPath.class.getName()));
    }

    @Override
    public void collectSlowLineMarkers(List<PsiElement> elements, Collection<LineMarkerInfo> result) {
        for (PsiElement element : elements) {
            if (element instanceof PsiClass && isApplicationPathAnnotated(element)) {
                PsiClass clazz = (PsiClass) element;
                PsiElement range = clazz.getNameIdentifier() != null ? clazz.getNameIdentifier() : clazz;

                result.add(new LineMarkerInfo<PsiElement>(range, range.getTextRange(),
                    RestIcons.runIcon, Pass.UPDATE_OVERRIDEN_MARKERS, getTooltip(),
                    MyIconGutterHandler.INSTANCE, GutterIconRenderer.Alignment.RIGHT));
            }
        }
    }

    public Function<PsiElement, String> getTooltip() {
        return new NullableFunction<PsiElement, String>() {
            @Override
            public String fun(PsiElement element) {
                return "Actions corresponding to this Jersey Application Configuration.";
            }
        };
    }

    private static class MyIconGutterHandler implements GutterIconNavigationHandler<PsiElement> {

        static final MyIconGutterHandler INSTANCE = new MyIconGutterHandler();

        @Override
        public void navigate(MouseEvent e, PsiElement nameIdentifier) {
            final PsiElement listOwner = nameIdentifier.getParent();
            final PsiFile containingFile = listOwner.getContainingFile();
            final VirtualFile virtualFile = PsiUtilCore.getVirtualFile(listOwner);

            if (virtualFile != null && containingFile != null) {
                final Project project = listOwner.getProject();
                final Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

                if (editor != null) {
                    editor.getCaretModel().moveToOffset(nameIdentifier.getTextOffset());
                    final PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());

                    if (file != null && virtualFile.equals(file.getVirtualFile())) {
                        final JBPopup popup = createActionGroupPopup(containingFile, editor);
                        if (popup != null) {
                            popup.show(new RelativePoint(e));
                        }
                    }
                }
            }
        }

        @Nullable
        protected JBPopup createActionGroupPopup(PsiFile file, Editor editor) {
            DefaultActionGroup group = new DefaultActionGroup();

            for (RunJerseyIntention action : runJerseyIntentions) {
                group.add(new ApplyIntentionAction(action, action.getIcon(), action.getName(), editor, file));
            }

            if (group.getChildrenCount() > 0) {
                DataContext context = SimpleDataContext.getProjectContext(null);
                return JBPopupFactory.getInstance()
                    .createActionGroupPopup(null, group, context, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true);
            }

            return null;
        }
    }
}
