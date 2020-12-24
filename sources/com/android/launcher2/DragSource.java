package com.android.launcher2;

import android.view.View;
import com.android.launcher2.DropTarget;

public interface DragSource {
    void onDropCompleted(View view, DropTarget.DragObject dragObject, boolean z, boolean z2);

    void onFlingToDeleteCompleted();

    boolean supportsFlingToDelete();
}
