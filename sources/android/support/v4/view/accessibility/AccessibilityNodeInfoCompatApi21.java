package android.support.v4.view.accessibility;

import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

class AccessibilityNodeInfoCompatApi21 {
    AccessibilityNodeInfoCompatApi21() {
    }

    static List<Object> getActionList(Object info) {
        return ((AccessibilityNodeInfo) info).getActionList();
    }

    static void addAction(Object info, int id, CharSequence label) {
        ((AccessibilityNodeInfo) info).addAction(new AccessibilityNodeInfo.AccessibilityAction(id, label));
    }

    public static Object obtainCollectionInfo(int rowCount, int columnCount, boolean hierarchical, int selectionMode) {
        return AccessibilityNodeInfo.CollectionInfo.obtain(rowCount, columnCount, hierarchical, selectionMode);
    }

    public static Object obtainCollectionItemInfo(int rowIndex, int rowSpan, int columnIndex, int columnSpan, boolean heading, boolean selected) {
        return AccessibilityNodeInfo.CollectionItemInfo.obtain(rowIndex, rowSpan, columnIndex, columnSpan, heading, selected);
    }

    static class CollectionItemInfo {
        CollectionItemInfo() {
        }

        public static boolean isSelected(Object info) {
            return ((AccessibilityNodeInfo.CollectionItemInfo) info).isSelected();
        }
    }

    static class AccessibilityAction {
        AccessibilityAction() {
        }

        static int getId(Object action) {
            return ((AccessibilityNodeInfo.AccessibilityAction) action).getId();
        }

        static CharSequence getLabel(Object action) {
            return ((AccessibilityNodeInfo.AccessibilityAction) action).getLabel();
        }
    }
}
