package cn.rongcloud.um.ui.api;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/** Copies SAF documents off the UI thread; owns cancellation and partial-file cleanup. */
public final class ApiFileImporter implements AutoCloseable {
    public interface Callback { void complete(String uri, String error); }
    private final Context context;
    private final Executor ui;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final AtomicLong generation = new AtomicLong();
    private volatile boolean closed;
    private Callback callback;

    public ApiFileImporter(Context context, Executor ui) {
        this.context = context.getApplicationContext();
        this.ui = ui;
    }

    public void importFile(Uri source, Callback callback) {
        if (closed) return;
        this.callback = callback;
        long id = generation.incrementAndGet();
        worker.execute(() -> copy(source, id));
    }
    private boolean active(long id) { return !closed && generation.get() == id; }
    private void copy(Uri source, long id) {
        if (!active(id)) return;
        File destination = null;
        try {
            String name = "media";
            try (Cursor cursor = context.getContentResolver().query(source, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) name = cursor.getString(0);
            }
            name = name.replaceAll("[^\\p{L}\\p{N}._-]", "_");
            if (name.length() > 100) name = name.substring(name.length() - 100);
            destination = File.createTempFile("im_api_", "_" + name, context.getCacheDir());
            try (InputStream input = context.getContentResolver().openInputStream(source);
                 FileOutputStream output = new FileOutputStream(destination)) {
                if (input == null) throw new IOException("无法读取所选文件");
                byte[] buffer = new byte[8192];
                int size;
                while ((size = input.read(buffer)) != -1) {
                    if (!active(id) || Thread.currentThread().isInterrupted()) throw new IOException("文件导入已取消");
                    output.write(buffer, 0, size);
                }
            }
            File complete = destination;
            ui.execute(() -> {
                if (!active(id)) { complete.delete(); return; }
                Callback receiver = this.callback;
                this.callback = null;
                if (receiver != null) receiver.complete(Uri.fromFile(complete).toString(), null);
            });
        } catch (Exception e) {
            if (destination != null) destination.delete();
            ui.execute(() -> {
                if (!active(id)) return;
                Callback receiver = this.callback;
                this.callback = null;
                if (receiver != null) receiver.complete(null, e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            });
        }
    }
    @Override public void close() {
        closed = true;
        generation.incrementAndGet();
        callback = null;
        worker.shutdownNow();
    }
}
