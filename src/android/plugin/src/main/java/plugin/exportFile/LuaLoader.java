//
//  LuaLoader.java
//  TemplateApp
//
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

// This corresponds to the name of the Lua library,
// e.g. [Lua] require "plugin.library"
package plugin.exportFile;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.ansca.corona.CoronaActivity;
import com.ansca.corona.CoronaEnvironment;
import com.ansca.corona.CoronaLua;
import com.ansca.corona.CoronaLuaEvent;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeTask;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.NamedJavaFunction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


@SuppressWarnings("unused")
public class LuaLoader implements JavaFunction {
    private static final String EVENT_NAME = "exportFile";


    @Override
    public int invoke(LuaState L) {
        NamedJavaFunction[] luaFunctions = new NamedJavaFunction[]{
                new Export(),
        };
        String libName = L.toString(1);
        L.register(libName, luaFunctions);
        return 1;
    }


    private static int dispatchEvent(final int listener, final String response, boolean isError) {
        if (listener == CoronaLua.REFNIL) return 0;
        CoronaEnvironment.getCoronaActivity().getRuntimeTaskDispatcher().send(new CoronaRuntimeTask() {
            @Override
            public void executeUsing(CoronaRuntime runtime) {
                LuaState L = runtime.getLuaState();

                CoronaLua.newEvent(L, EVENT_NAME);

                L.pushString(response);
                L.setField(-2, CoronaLuaEvent.RESPONSE_KEY);

                L.pushBoolean(isError);
                L.setField(-2, CoronaLuaEvent.ISERROR_KEY);

                try {
                    CoronaLua.dispatchEvent(L, listener, 0);
                } catch (Exception ignored) {
                }
                CoronaLua.deleteRef(L, listener);
            }
        });
        return 0;
    }


    private static class Export implements NamedJavaFunction {
        @Override
        public String getName() {
            return "export";
        }

        private int export(final int listener, final String filePath, final String fileName, final String fileType) {

            CoronaActivity activity = CoronaEnvironment.getCoronaActivity();
            if (activity == null) {
                return dispatchEvent(listener, "Null Activity", true);
            }

            int requestCode = activity.registerActivityResultHandler(new CoronaActivity.OnActivityResultHandler() {
                @Override
                public void onHandleActivityResult(CoronaActivity activity, int requestCode, int resultCode, android.content.Intent data) {
                    activity.unregisterActivityResultHandler(this);

                    Uri destination;
                    try {
                        destination = data.getData();
                    } catch (Throwable ignore) {
                        dispatchEvent(listener, "Error retrieving destination", true);
                        return;
                    }
                    if (destination == null) {
                        dispatchEvent(listener, "Destination is empty", true);
                        return;
                    }
                    OutputStream output;
                    try {
                        output = activity.getContentResolver().openOutputStream(destination);
                    } catch (Throwable e) {
                        dispatchEvent(listener, "Unable to open destination", true);
                        return;
                    }
                    if (output == null) {
                        dispatchEvent(listener, "Output stream is null", true);
                        return;
                    }

                    copyToStream(output, filePath, listener);
                }
            });

            if (android.os.Build.VERSION.SDK_INT >= 19) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(fileType);
                intent.putExtra(Intent.EXTRA_TITLE, fileName);
                activity.startActivityForResult(intent, requestCode);
            } else {
                OutputStream output;
                try {
                    File outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    output = new FileOutputStream(new File(outputDir, fileName));
                } catch (Throwable e) {
                    return dispatchEvent(listener, "Unable to open legacy destination", true);
                }
                copyToStream(output, filePath, listener);
            }
            return 0;
        }

        private void copyToStream(OutputStream output, String filePath, int listener) {
            InputStream input;
            try {
                input = new FileInputStream(filePath);
            } catch (Throwable e) {
                dispatchEvent(listener, "Cannot open input file", true);
                return;
            }

            try {
                byte[] buffer = new byte[100 * 1024];
                for (int length; (length = input.read(buffer)) > 0; ) {
                    output.write(buffer, 0, length);
                }
            } catch (Throwable e) {
                dispatchEvent(listener, "Error while copying data " + e.toString(), true);
                return;
            } finally {
                try {
                    input.close();
                    output.close();
                } catch (Throwable ignore) {
                }
            }
            dispatchEvent(listener, "Done!", false);
        }

        @Override
        public int invoke(LuaState L) {
            int listener = CoronaLua.REFNIL;
            String filePath = null;
            String fileName = null;
            String fileType = "*/*";

            if (L.isTable(1)) {
                L.getField(1, "path");
                if (L.isString(-1)) {
                    filePath = L.toString(-1);
                }
                L.pop(1);

                L.getField(1, "name");
                if (L.isString(-1)) {
                    fileName = L.toString(-1);
                }
                L.pop(1);

                L.getField(1, "type");
                if (L.isString(-1)) {
                    fileType = L.toString(-1);
                }
                L.pop(1);

                L.getField(1, "listener");
                if (CoronaLua.isListener(L, -1, EVENT_NAME)) {
                    listener = CoronaLua.newRef(L, -1);
                }
                L.pop(1);
            }

            if(filePath == null || filePath.isEmpty()) {
                return dispatchEvent(listener, "File path is not specified", true);
            }

            if(fileName == null) {
                fileName = new File(filePath).getName();
            }

            if(fileName.isEmpty()) {
                return dispatchEvent(listener, "File path is empty or invalid", true);
            }

            return export(listener, filePath, fileName, fileType);
        }
    }

}
