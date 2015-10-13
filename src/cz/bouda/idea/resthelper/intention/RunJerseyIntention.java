package cz.bouda.idea.resthelper.intention;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.RunManager;
import com.intellij.psi.PsiElement;
import com.siyeh.ipp.base.Intention;
import com.siyeh.ipp.base.PsiElementPredicate;

public class RunJerseyIntention extends Intention {

    private final String name;
    private final Icon icon;

    public RunJerseyIntention(final String name, final Icon icon) {
        this.name = name;
        this.icon = icon;
    }

    @Override
    protected void processIntention(@NotNull final PsiElement element) {
        RunManager runManager = RunManager.getInstance(element.getProject());


    }

    @NotNull
    @Override
    protected PsiElementPredicate getElementPredicate() {
        return new PsiElementPredicate() {
            @Override
            public boolean satisfiedBy(final PsiElement element) {
                return true;
            }
        };
    }

    public String getName() {
        return name;
    }

    public Icon getIcon() {
        return icon;
    }
}
