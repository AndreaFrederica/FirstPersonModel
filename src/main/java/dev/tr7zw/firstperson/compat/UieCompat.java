package dev.tr7zw.firstperson.compat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;

/** Read only public UIE state: never acquire a camera session or change its view. */
public final class UieCompat {
    private final List<Method> cameraChecks = new ArrayList<>();
    private Method zoom;
    private boolean cameraFailed, zoomFailed;

    public UieCompat() {
        this(find("neofontrender.addons.api.camera.CameraApi"),
                find("neofontrender.addons.zoom.ZoomModule"));
    }

    UieCompat(Class<?> cameraApi, Class<?> zoomApi) {
        if (cameraApi != null) {
            try {
                for (String name : new String[] {"isDroneActive", "isFreeLookActive", "isShoulderActive",
                        "isRenderOverrideActive"}) cameraChecks.add(cameraApi.getMethod(name));
            } catch (ReflectiveOperationException | LinkageError error) {
                cameraFailed = true;
                LogManager.getLogger("FirstPerson").warn("Incompatible UIE camera API; suppressing the extra body pass", error);
            }
        }
        if (zoomApi != null) {
            try { zoom = zoomApi.getMethod("isZoomActive"); }
            catch (ReflectiveOperationException | LinkageError error) {
                LogManager.getLogger("FirstPerson").warn("Incompatible UIE zoom API", error);
            }
        }
    }

    private static Class<?> find(String name) {
        try { return Class.forName(name, false, UieCompat.class.getClassLoader()); }
        catch (ClassNotFoundException ignored) { return null; }
        catch (LinkageError error) {
            LogManager.getLogger("FirstPerson").warn("Unable to load optional API {}", name, error);
            return null;
        }
    }

    public boolean ownsCamera() {
        if (cameraFailed) return true;
        try {
            for (Method method : cameraChecks) if ((boolean) method.invoke(null)) return true;
            return false;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError error) {
            cameraFailed = true;
            LogManager.getLogger("FirstPerson").warn("UIE camera query failed; suppressing the extra body pass", error);
            return true;
        }
    }

    public boolean isZooming() {
        if (zoom == null || zoomFailed) return false;
        try { return (boolean) zoom.invoke(null); }
        catch (ReflectiveOperationException | RuntimeException | LinkageError error) {
            zoomFailed = true;
            LogManager.getLogger("FirstPerson").warn("UIE zoom query failed; disabling zoom integration", error);
            return false;
        }
    }
}
