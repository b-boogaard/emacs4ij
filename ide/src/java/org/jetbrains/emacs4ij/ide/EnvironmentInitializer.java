package org.jetbrains.emacs4ij.ide;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.keymap.impl.KeymapManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.emacs4ij.jelisp.DefinitionLoader;
import org.jetbrains.emacs4ij.jelisp.Environment;
import org.jetbrains.emacs4ij.jelisp.GlobalEnvironment;
import org.jetbrains.emacs4ij.jelisp.LogUtil;
import org.jetbrains.emacs4ij.jelisp.exception.Attention;
import org.jetbrains.emacs4ij.jelisp.exception.DoubleBufferException;
import org.jetbrains.emacs4ij.jelisp.exception.LispException;
import org.jetbrains.emacs4ij.jelisp.platformDependent.LispFrame;

public abstract class EnvironmentInitializer {
  private static boolean isGlobalInitialized = false;

  private EnvironmentInitializer() {}

  public static boolean isGlobalInitialized() {
    return isGlobalInitialized;
  }

  public static void reset() {
    isGlobalInitialized = false;
  }

  public static boolean silentInitGlobal() {
    EmacsHomeService emacsHomeService = ServiceManager.getService(EmacsHomeService.class);
    EmacsSourceService emacsSourceService = ServiceManager.getService(EmacsSourceService.class);
    if (emacsHomeService.isParameterSet() && emacsSourceService.isParameterSet()) {
      try {
        return init();
      } catch (LispException e) {
        LogUtil.log(e);
      }
    }
    return isGlobalInitialized;
  }

  public static boolean initGlobal() {
    try {
      return init();
    } catch (LispException e) {
      GlobalEnvironment.echo(e.getMessage(), GlobalEnvironment.MessageType.ERROR);
    }
    return isGlobalInitialized;
  }

  private static boolean init() {
    if (isGlobalInitialized)
      return true;

    EmacsIndexService indexService = ServiceManager.getService(EmacsIndexService.class);
    DefinitionLoader.initialize(indexService == null ? null : indexService.getEmacsIndex());

    Keymap userKeymap = KeymapManager.getInstance().getActiveKeymap();
    try {
      GlobalEnvironment.initialize(new KeymapCreator(), new BufferCreator(), new WindowCreator(), new IdeProvider(), new Runnable() {
        @Override
        public void run() {
          IdeaMiniBuffer.init(null, null);
        }
      });
      isGlobalInitialized = true;
    } catch (LispException e) {
      ((KeymapManagerImpl) KeymapManager.getInstance()).setActiveKeymap(userKeymap);
      throw e;
    }
    return isGlobalInitialized;
  }

  public static void initProjectEnv (final Project project, final Environment environment) {
    WindowManager windowManager = WindowManager.getInstance();
    for (IdeFrame frame: windowManager.getAllFrames()) {
      GlobalEnvironment.INSTANCE.onFrameOpened(new IdeaFrame((IdeFrameImpl) frame));
    }
    if (GlobalEnvironment.INSTANCE.getAllFrames().size() != 1) {
      LispFrame existing = GlobalEnvironment.INSTANCE.getExistingFrame(new IdeaFrame((IdeFrameImpl) windowManager.getIdeFrame(project)));
      GlobalEnvironment.INSTANCE.setSelectedFrame(existing);
    }

    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        ApplicationManager.getApplication().runReadAction(new Runnable() {
          @Override
          public void run() {
            final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);

            for (final VirtualFile virtualFile : fileEditorManager.getOpenFiles()) {
              ApplicationManager.getApplication().runReadAction(new Runnable() {
                @Override
                public void run() {
                  try {
                    new IdeaBuffer(environment, fileEditorManager, virtualFile);
                  } catch (DoubleBufferException exc) {
                    //due to event handling order the buffers were already initialized => skip here
                  }
                }
              });
            }
            Editor editor = fileEditorManager.getSelectedTextEditor();
            if (editor != null) {
              environment.switchToWindow(new IdeaEditorWrapper(fileEditorManager.getSelectedTextEditor()), true);
              return;
            }
            if (fileEditorManager.getOpenFiles().length != 0)
              throw new Attention();
          }
        });
      }
    });
  }
}
