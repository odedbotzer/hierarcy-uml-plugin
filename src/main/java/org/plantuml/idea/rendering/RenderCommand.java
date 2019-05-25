package org.plantuml.idea.rendering;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.plantuml.PlantUmlIncludes;
import org.plantuml.idea.toolwindow.ExecutionStatusPanel;

import java.io.File;
import java.util.Map;


public abstract class RenderCommand implements Runnable {
    public static final Logger logger = Logger.getInstance(RenderCommand.class);

    protected Reason reason;
    protected String sourceFilePath;
    protected final String source;
    protected final File baseDir;
    protected final int page;
    protected int zoom;
    protected RenderCacheItem cachedItem;
    protected int version;
    protected boolean renderUrlLinks;
    protected LazyApplicationPoolExecutor.Delay delay;
    protected ExecutionStatusPanel label;

    public enum Reason {
        INCLUDES,
        FILE_SWITCHED,
        REFRESH,
        CARET,
        MANUAL_UPDATE, /* no function*/
        SOURCE_PAGE_ZOOM
    }

    public RenderCommand(Reason reason, String sourceFilePath, String source, File baseDir, int page, int zoom, RenderCacheItem cachedItem, int version, boolean renderUrlLinks, LazyApplicationPoolExecutor.Delay delay, ExecutionStatusPanel label) {
        this.reason = reason;
        this.sourceFilePath = sourceFilePath;
        this.source = source;
        this.baseDir = baseDir;
        this.page = page;
        this.zoom = zoom;
        this.cachedItem = cachedItem;
        this.version = version;
        this.renderUrlLinks = renderUrlLinks;
        this.delay = delay;
        this.label = label;
    }

    @Override
    public void run() {
        try {
            if (source.isEmpty()) {
                logger.debug("source is empty");
                return;
            }
            long start = System.currentTimeMillis();
            label.update(version, ExecutionStatusPanel.State.EXECUTING);

            final Map<File, Long> includedFiles = PlantUmlIncludes.commitIncludes(source, baseDir);
            logger.debug("includedFiles=", includedFiles);

            final RenderRequest renderRequest = new RenderRequest(baseDir, source, PlantUml.ImageFormat.PNG, page, zoom, version, renderUrlLinks, reason);
            final RenderResult result = PlantUmlRendererUtil.render(renderRequest, cachedItem);

            final RenderCacheItem newItem = new RenderCacheItem(renderRequest, sourceFilePath, source, baseDir, zoom, page, includedFiles, result, result.getImageItemsAsArray(), version);
            final long total = System.currentTimeMillis() - start;

            if (!Thread.currentThread().isInterrupted() && hasImages(newItem.getImageItems())) {

                ApplicationManager.getApplication().invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        postRenderOnEDT(newItem, total, result);
                    }
                });

            } else {
                logger.debug("no images rendered");
                label.update(version, ExecutionStatusPanel.State.DONE, total, result);
            }
        } catch (RenderingCancelledException e) {
            logger.info("command interrupted", e);
            label.update(version, ExecutionStatusPanel.State.CANCELLED);
        } catch (Throwable e) {
            label.update(version, ExecutionStatusPanel.State.ERROR);
            logger.error("Exception occurred rendering " + this, e);
        }
    }

    protected abstract void postRenderOnEDT(RenderCacheItem newItem, long total, RenderResult result);

    private boolean hasImages(ImageItem[] imageItems) {
        for (ImageItem imageItem : imageItems) {
            if (imageItem != null && imageItem.hasImage()) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("reason", reason)
                .append("sourceFilePath", sourceFilePath)
                .append("selectedDir", baseDir.getName())
                .append("page", page)
                .append("zoom", zoom)
                .append("cachedItem", cachedItem)
                .append("version", version)
                .toString();
    }
}
