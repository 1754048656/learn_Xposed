package com.example.hoyn.example;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author glsite.com
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class App_dxys implements IXposedHookLoadPackage{

    private static final String TAG = "FATJSLOG";
    private String currentPackageName = "cn.dxy.android.aspirin";
    private String wrapperProxyApplication = "com.wrapper.proxyapplication.WrapperProxyApplication";

    private static File file;

    private Context context;
    private String fileName = "/question_妇科.txt"; //       /data/user/0/cn.dxy.android.aspirin/cache
    //Log.i(TAG, "com.stub.StubApp->"+i);


    /**************************************************************************************************/
    private void main(final ClassLoader classLoader) {

        XposedHelpers.findAndHookMethod("android.content.ContextWrapper", classLoader, "getApplicationContext", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if (context == null && file == null) {
                    context = (Application) param.getResult();
                    file = context.getCacheDir();
                    Log.i(TAG, "hooking...getApplicationContext.getCacheDir => " + file);
                }
            }
        });


        //CommonItemArray的getItems
        XposedHelpers.findAndHookMethod("cn.dxy.aspirin.bean.common.CommonItemArray", classLoader, "getItems", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                //Log.i(TAG, "afterHookedMethod: cn.dxy.aspirin.bean.common.CommonItemArray.getItems() res \n" + param.getResult() + "\n" + json);
                //cn.dxy.aspirin.bean.question.QuestionFlowBeanPublic
                if (param.getResult().toString().contains("QuestionFlowBeanPublic")) {
                    try {
                        parseDialogFormJson(param.getResult());
                    }catch (Exception e) {
                        Log.i(TAG, "Exception: " + e.toString());
                    }
                }
            }
        });
    }

    private void parseDialogFormJson(Object result) {
        List list = (List) JSONArray.toJSON(result);
        StringBuilder all = new StringBuilder();
        for (Object o : list) {
            String res = "";
            String str = String.valueOf(o);
            String jsonStr = String.valueOf(JSONObject.toJSON(str));
            if (jsonStr.contains("dialog_patient")) {//病人
                JSONObject jsonObject = JSONObject.parseObject(jsonStr);
                JSONObject dialog_patient = (JSONObject) jsonObject.get("dialog_patient");
                String content = dialog_patient.getString("content");
                res = printContent("dialog_patient", content, jsonStr);
            }
            if (jsonStr.contains("dialog_doctor")) {//医生
                JSONObject jsonObject = JSONObject.parseObject(jsonStr);
                JSONObject dialog_patient = (JSONObject) jsonObject.get("dialog_doctor");
                String content = dialog_patient.getString("content");
                res = printContent("dialog_doctor", content, jsonStr);
            }
            if (!res.equals(""))
                all.append(res).append(",");
        }
        all = new StringBuilder("[" + all.substring(0, all.length() - 1) + "]\n");
        AppendFile.writer(file + fileName, all.toString());
        Log.i(TAG, String.valueOf(all));
    }

    private String printContent(String user, String content, String jsonStr) {
        if (content.equals("")) {
            JSONObject jsonObject = JSONObject.parseObject(jsonStr);
            JSONObject dialog_patient = (JSONObject) jsonObject.get(user);
            JSONObject dialog_audio = (JSONObject) dialog_patient.get("dialog_audio");
            content = dialog_audio.getString("audio_text");
        }
        JSONObject json_tmp = new JSONObject();
        json_tmp.put(user,content);
        //String res = user + " => " + content;
        String res = json_tmp.toJSONString();
        res = res.replace("<br>", "");
        return res;
    }

    /**************************************************************************************************/
    public String printStack_1() {
        RuntimeException e = new RuntimeException("<Start dump Stack !>");
        e.fillInStackTrace();
        StackTraceElement[] stackTrace = e.getStackTrace();
        StringBuilder stackLog = new StringBuilder();
        for (StackTraceElement traceElement : stackTrace) {
            stackLog.append("\t\t").append(traceElement).append("\n");
        }
        return "\n++++++++++++++++\n" + stackLog;
    }

    public void GetClassLoaderClasslist(ClassLoader classLoader) {
        //private final DexPathList pathList;
        //public static java.lang.Object getObjectField(java.lang.Object obj, java.lang.String fieldName)
        //Log.i(TAG, "start dealwith classloader:" + classLoader);
        Object pathListObj = XposedHelpers.getObjectField(classLoader, "pathList");
        //private final Element[] dexElements;
        Object[] dexElementsObj = (Object[]) XposedHelpers.getObjectField(pathListObj, "dexElements");
        for (Object i : dexElementsObj) {
            //private final DexFile dexFile;
            Object dexFileObj = XposedHelpers.getObjectField(i, "dexFile");
            //private Object mCookie;
            Object mCookieObj = XposedHelpers.getObjectField(dexFileObj, "mCookie");
            //private static native String[] getClassNameList(Object cookie);
            //    public static java.lang.Object callStaticMethod(java.lang.Class<?> clazz, java.lang.String methodName, java.lang.Object... args) { /* compiled code */ }
            Class DexFileClass = XposedHelpers.findClass("dalvik.system.DexFile", classLoader);

            String[] classlist = (String[]) XposedHelpers.callStaticMethod(DexFileClass, "getClassNameList", mCookieObj);
//            for (String classname : classlist) {
//                Log.i(TAG, dexFileObj + "---" + classname);
//            }
        }
        //Log.i(TAG, "end dealwith classloader:" + classLoader);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        //Log.i(TAG, loadPackageParam.packageName);
        //Log.i(TAG, "HookJava->app packagename" + loadPackageParam.packageName);
        if (loadPackageParam.packageName.equals(currentPackageName)) {
            //Log.i(TAG, "kanxue " + loadPackageParam.packageName);
            /* public static de.robv.android.xposed.XC_MethodHook.Unhook findAndHookMethod(java.lang.Class<?> clazz, java.lang.String methodName, java.lang.Object... parameterTypesAndCallback) { *//* compiled code *//* }

            public static de.robv.android.xposed.XC_MethodHook.Unhook findAndHookMethod(java.lang.String className, java.lang.ClassLoader classLoader, java.lang.String methodName, java.lang.Object... parameterTypesAndCallback) { *//* compiled code *//* }
             */
            ClassLoader classLoader = loadPackageParam.classLoader;
            //Log.i(TAG, "loadPackageParam.classLoader->" + classLoader);
            GetClassLoaderClasslist(classLoader);

            ClassLoader parent = classLoader.getParent();
            while (parent != null) {
                //Log.i(TAG, "parent->" + parent);
                if (parent.toString().contains("BootClassLoader")) {

                } else {
                    GetClassLoaderClasslist(parent);
                }
                parent = parent.getParent();
            }

            Class StubAppClass=XposedHelpers.findClass(wrapperProxyApplication,loadPackageParam.classLoader);
            Method[] methods=StubAppClass.getDeclaredMethods();
//            for(Method i:methods){
//                Log.i(TAG, "com.stub.StubApp->"+i);
//            }
            XposedHelpers.findAndHookMethod(wrapperProxyApplication, loadPackageParam.classLoader, "onCreate", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    ClassLoader finalClassLoader=getClassloader();
                    //Log.i(TAG, "finalClassLoader->" + finalClassLoader);
                    //GetClassLoaderClasslist(finalClassLoader);//展示classLoader List
                    main(finalClassLoader);
                }
            });
        }
    }

    public static Object invokeStaticMethod(String class_name,
                                            String method_name, Class[] pareTyple, Object[] pareVaules) {
        try {
            Class obj_class = Class.forName(class_name);
            Method method = obj_class.getMethod(method_name, pareTyple);
            return method.invoke(null, pareVaules);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getFieldOjbect(String class_name, Object obj,
                                        String filedName) {
        try {
            Class obj_class = Class.forName(class_name);
            Field field = obj_class.getDeclaredField(filedName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ClassLoader getClassloader() {
        ClassLoader resultClassloader = null;
        Object currentActivityThread = invokeStaticMethod(
                "android.app.ActivityThread", "currentActivityThread",
                new Class[]{}, new Object[]{});
        Object mBoundApplication = getFieldOjbect(
                "android.app.ActivityThread", currentActivityThread,
                "mBoundApplication");
        Application mInitialApplication = (Application) getFieldOjbect("android.app.ActivityThread",
                currentActivityThread, "mInitialApplication");
        Object loadedApkInfo = getFieldOjbect(
                "android.app.ActivityThread$AppBindData",
                mBoundApplication, "info");
        Application mApplication = (Application) getFieldOjbect("android.app.LoadedApk", loadedApkInfo, "mApplication");
        resultClassloader = mApplication.getClassLoader();
        return resultClassloader;
    }

    /* ********************************************************************************************** */

    public static Field getClassField(ClassLoader classloader, String class_name,
                                      String filedName) {
        try {
            Class obj_class = classloader.loadClass(class_name);//Class.forName(class_name);
            Field field = obj_class.getDeclaredField(filedName);
            field.setAccessible(true);
            return field;
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getClassFieldObject(ClassLoader classloader, String class_name, Object obj,
                                             String filedName) {
        try {
            Class obj_class = classloader.loadClass(class_name);//Class.forName(class_name);
            Field field = obj_class.getDeclaredField(filedName);
            field.setAccessible(true);
            Object result = null;
            result = field.get(obj);
            return result;
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}


