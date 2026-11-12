package cn.rongcloud.um.ui.kit;

import org.junit.Test;
import java.util.EnumSet;
import static org.junit.Assert.*;

public class ImKitCatalogTest {
    @Test public void everyScenarioHasInstructionsAndAnAction() {
        EnumSet<ImKitCatalog.Group> groups = EnumSet.noneOf(ImKitCatalog.Group.class);
        for (ImKitCatalog.Scene scene : ImKitCatalog.Scene.values()) {
            assertSame(scene, ImKitCatalog.Scene.find(scene.name()));
            assertFalse(scene.title.isEmpty()); assertFalse(scene.description.isEmpty());
            assertFalse(scene.source.isEmpty()); assertFalse(scene.button.isEmpty());
            assertNotNull(scene.action); groups.add(scene.group);
        }
        assertEquals(EnumSet.allOf(ImKitCatalog.Group.class), groups);
        assertEquals(9, ImKitCatalog.Scene.values().length);
        assertNull(ImKitCatalog.Scene.find("unknown"));
        assertNull(ImKitCatalog.Scene.find(null));
    }
}
