package fan.mingwei.com.auto.Service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import fan.mingwei.com.auto.utils.AccessibilityLog;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.CLIPBOARD_SERVICE;

public class AccessibilityOperator {
private  static final String TAG=" AccessibilityOperator";
    private Context mContext;
    private static AccessibilityOperator mInstance ;
    private AccessibilityEvent mAccessibilityEvent;
    private AccessibilityService mAccessibilityService;
    private final Object mLock = new Object();

    private AccessibilityOperator() {
    }

    public static AccessibilityOperator getInstance() {

        if (mInstance== null) {
            synchronized (AccessibilityOperator.class) {
                if(mInstance==null){
                    mInstance=new AccessibilityOperator();
                    Log.e(TAG, "getInstance: "+mInstance );
                }
            }
        }
        return  mInstance;

    }

    public void init(Context context) {
        mContext = context;
    }

    public void updateEvent(AccessibilityService service, AccessibilityEvent event) {
        if (service != null && mAccessibilityService == null) {
            Log.e(TAG, "updateEvent: "+"i hava set the service" );
            mAccessibilityService = service;
            Log.e(TAG, "updateEvent: "+this.mAccessibilityService );
            Log.e(TAG, "updateEvent: "+this);
        }
        if (event != null) {
            mAccessibilityEvent = event;
        }
    }

    public boolean isServiceRunning() {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(Short.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo info : services) {
            if (info.service.getClassName().equals("fan.mingwei.com.auto.Service.AccessibilitySampleService")) {
                return true;
            }
        }

        return false;
    }

   public AccessibilityNodeInfo getRootNodeInfo() {
        AccessibilityEvent curEvent = mAccessibilityEvent;
        AccessibilityNodeInfo nodeInfo = null;
        if (Build.VERSION.SDK_INT >= 16) {
            // 建议使用getRootInActiveWindow，这样不依赖当前的事件类型
            if (mAccessibilityService != null) {
                Log.e(TAG, "getRootNodeInfo: "+this.mAccessibilityService );
                nodeInfo = mAccessibilityService.getRootInActiveWindow();
                AccessibilityLog.printLog("nodeInfo: " + nodeInfo);
            }
            // 下面这个必须依赖当前的AccessibilityEvent
//            nodeInfo = curEvent.getSource();
        } else {
            nodeInfo = curEvent.getSource();
        }
        return nodeInfo;
    }

    /**
     * 根据Text搜索所有符合条件的节点, 模糊搜索方式
     */
    public List<AccessibilityNodeInfo> findNodesByText(String text) {
        AccessibilityNodeInfo nodeInfo = getRootNodeInfo();
        if (nodeInfo != null) {
           return nodeInfo.findAccessibilityNodeInfosByText(text);
        }
        return null;
    }

    /**
     * 根据View的ID搜索符合条件的节点,精确搜索方式;
     * 这个只适用于自己写的界面，因为ID可能重复
     * api要求18及以上
     * @param viewId
     */
    public List<AccessibilityNodeInfo> findNodesById(String viewId) {
        AccessibilityNodeInfo nodeInfo = getRootNodeInfo();
        if (nodeInfo != null) {
            if (Build.VERSION.SDK_INT >= 18) {
                return nodeInfo.findAccessibilityNodeInfosByViewId(viewId);
            }
        }
        return null;
    }

    public boolean clickByText(String text) {
        return performClick(findNodesByText(text));
    }

    /**
     * 根据View的ID搜索符合条件的节点,精确搜索方式;
     * 这个只适用于自己写的界面，因为ID可能重复
     * api要求18及以上
     * @param viewId
     * @return 是否点击成功
     */
    public boolean clickById(String viewId) {
        return performClick(findNodesById(viewId));
    }

    private boolean performClick(List<AccessibilityNodeInfo> nodeInfos) {
        if (nodeInfos != null && !nodeInfos.isEmpty()) {
            AccessibilityNodeInfo node;
            for (int i = 0; i < nodeInfos.size(); i++) {
                node = nodeInfos.get(i);
                // 获得点击View的类型
                AccessibilityLog.printLog("View类型：" + node.getClassName());
                // 进行模拟点击
                if (node.isEnabled()) {
                    return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
        return false;
    }
   //执行点击
    public boolean execlick(AccessibilityNodeInfo nodeInfo){
        boolean result=false;
        if(nodeInfo!=null){
            if(nodeInfo.isClickable()){
                result=nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }else {
                AccessibilityNodeInfo parent=nodeInfo.getParent();
               result=execlick(parent);
            }
        }
        return result;
    }


//执行向上滚动
    public boolean exeScrollforward(AccessibilityNodeInfo nodeInfo){
        boolean result=false;
        if(nodeInfo!=null){
            if(nodeInfo.isScrollable()){
                result=nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            }
        }
        return result;
    }

    //执行向上滚动
    public boolean exeScrollBackward(AccessibilityNodeInfo nodeInfo){
        boolean result=false;
        if(nodeInfo!=null){
            if(nodeInfo.isScrollable()){
                result=nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
            }
        }
        return result;
    }

    //执行回退
    public boolean exeBack() {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    private boolean performGlobalAction(int action) {
        return mAccessibilityService.performGlobalAction(action);
    }

    //用手势动作做长按
    public boolean click(int x,int y,int time){
        boolean result=false;
        synchronized (mLock) {
            Log.d(TAG, "gesture: x="+x+"  y="+y+" time="+time);
            Path mPath=new Path();//线性的path代表手势路径,点代表按下,封闭的没用
            mPath.moveTo(x, y);
            result=mAccessibilityService.dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription
                    (mPath,1000, time)).build(), null, null);
            Log.e(TAG, "run the gesture result" +result);
        }
        return  result;
    }

    //执行手势动作
    public boolean move(int x1,int y1,int x2, int y2,int time){
         boolean result=false;
        synchronized (mLock) {
            Log.d(TAG,"start: x="+x1+" y="+y1);
            Log.d(TAG,"end: x="+x2+" y="+y2);
            Path mPath=new Path();//线性的path代表手势路径,点代表按下,封闭的没用
            mPath.moveTo(x1, y1);
            mPath.lineTo(x2,y2);
            result=mAccessibilityService.dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription
                    (mPath,1000, time)).build(), null, null);
            Log.e(TAG, "run the gesture result" +result);
        }

        return result;
    }

    //打印当前界面的所有元素
   public void printAllNodeInfo(AccessibilityNodeInfo node) {
        Rect rect=new Rect();
        node.getBoundsInScreen(rect);
       Log.e(TAG, "text:" + (node.getText()==null ? "null" : node.getText().toString())
                + " desc:" + (node.getContentDescription()==null ? "null" : node.getContentDescription().toString())
               + " class:" + (node.getClassName()==null ? "null" : node.getClassName().toString())
                + " x:" + rect.centerX() + " y:" + rect.centerY()
        );

        int count = node.getChildCount();
        for (int i=0; i<count; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            printAllNodeInfo(child);
        }
        node.recycle();
    }

    //按照描述信息查找元素
    public List<AccessibilityNodeInfo> findNodeByTextInRoot(AccessibilityNodeInfo node,String Text) {
        List<AccessibilityNodeInfo> list=new ArrayList<>();
        if(node.getText()!=null){
            if(!node.getText().toString().isEmpty()&&node.getText().toString().contains(Text)){
                list.add(node);
            }
        }
        int count = node.getChildCount();
        for (int i=0; i<count; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            List<AccessibilityNodeInfo> list1=findNodeByTextInRoot(child,Text);
            if(list1!=null&&list1.size()!=0){
                for(int j=0;j<list1.size();++j){
                    AccessibilityNodeInfo chilenode=list1.get(j);
                    list.add(chilenode);
                }
            }
        }
        return list;
    }
    //按照描述信息查找元素
    public  List<AccessibilityNodeInfo> findNodeByDec(AccessibilityNodeInfo node,String dec) {
        List<AccessibilityNodeInfo> list=new ArrayList<>();
        if(node.getContentDescription()!=null){
            if(!node.getContentDescription().toString().isEmpty()&&node.getContentDescription().toString().contains(dec)){
                list.add(node);
            }
        }
        int count = node.getChildCount();
        for (int i=0; i<count; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            List<AccessibilityNodeInfo> list1=findNodeByDec(child,dec);
            if(list1!=null&&list1.size()!=0){
             for(int j=0;j<list1.size();++j){
                AccessibilityNodeInfo chilenode=list1.get(j);
                list.add(chilenode);
             }
            }
        }
        return list;
    }
    //按照类名信息查找元素
   public List<AccessibilityNodeInfo> findNodeByClass(AccessibilityNodeInfo node,String Class) {
        List<AccessibilityNodeInfo> list=new ArrayList<>();
        if(node.getClassName()!=null){
            if(!node.getClassName().toString().isEmpty()&&node.getClassName().toString().contains(Class)){
                list.add(node);
            }
        }
        int count = node.getChildCount();
        for (int i=0; i<count; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            List<AccessibilityNodeInfo> list1=findNodeByClass(child,Class);
            if(list1!=null&&list1.size()!=0){
                for(int j=0;j<list1.size();++j){
                    AccessibilityNodeInfo chilenode=list1.get(j);
                    list.add(chilenode);
                }
            }
        }
        return list;
    }


    //按照坐标做点击
    public boolean exeGusture(AccessibilityNodeInfo node,int time){
        boolean result=false;
        if(node!=null){
            Rect rect=new Rect();
            node.getBoundsInScreen(rect);
            int x=0; int y=0;
            x=rect.centerX(); y=rect.centerY();
           result=click(x,y,time);

        }
      return  result;
    }
    //输入文字
    public boolean inputText(AccessibilityNodeInfo nodeInfo,String Text){
        boolean result=false;
        ClipboardManager clipboard = (ClipboardManager) this.mContext.getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", Text);
        clipboard.setPrimaryClip(clip);
        CharSequence txt =nodeInfo.getText();
        if(txt==null){
            txt="";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Bundle arguments = new Bundle();
            arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, 0);
            arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, txt.length());
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, arguments);
            result=nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        }

     return result;
    }
    //按照Text信息点击
    public boolean clickForText(String Text,int instance){
        boolean result=false;
        AccessibilityNodeInfo root=getRootNodeInfo();
        List<AccessibilityNodeInfo> list_text=findNodeByTextInRoot(root,Text);
        if(list_text!=null&&!list_text.isEmpty()){
          if(instance<list_text.size()){
              AccessibilityNodeInfo nodeInfo=list_text.get(instance);
              nodeInfo=findclickable(nodeInfo);
              result=execlick(nodeInfo);
          }
        }
        root.recycle();
        return result;
    }
    //按照Text信息点击
    public boolean longClickForText(String Text,int instance,int time){
        boolean result=false;
        AccessibilityNodeInfo root=getRootNodeInfo();
        List<AccessibilityNodeInfo> list_text=findNodeByTextInRoot(root,Text);
        if(list_text!=null&&!list_text.isEmpty()){
            if(instance<list_text.size()){
                AccessibilityNodeInfo nodeInfo=list_text.get(instance);
                result=exeGusture(nodeInfo,time);
            }
        }
        root.recycle();
        return result;
    }
    public boolean clickForDec(String dec,int instance){
        boolean result=false;
        AccessibilityNodeInfo root=getRootNodeInfo();
        if(root!=null){
            List<AccessibilityNodeInfo> list_dec=findNodeByDec(root,dec);
            if(list_dec!=null&&!list_dec.isEmpty()){
                if(instance<list_dec.size()){
                    AccessibilityNodeInfo nodeInfo=list_dec.get(instance);
                    result=execlick(nodeInfo);
                    if(!result){
                        result=exeGusture(nodeInfo,150);
                    }
                }
            }
        }
root.recycle();
        return result;
    }
    public boolean longClickForDec(String dec,int instance,int time){
        boolean result=false;
        AccessibilityNodeInfo root=getRootNodeInfo();
        if(root!=null){
            List<AccessibilityNodeInfo> list_dec=findNodeByDec(root,dec);
            if(list_dec!=null&&!list_dec.isEmpty()){
                if(instance<list_dec.size()){
                    AccessibilityNodeInfo nodeInfo=list_dec.get(instance);
                    result=exeGusture(nodeInfo,time);

                }
            }
        }
        root.recycle();
        return result;
    }
    public boolean clickForClass(String Class,int instance){
        boolean result=false;
        AccessibilityNodeInfo root=getRootNodeInfo();
        if(root!=null){
            List<AccessibilityNodeInfo> list_Class=findNodeByClass(root,Class);
            if(list_Class!=null&&!list_Class.isEmpty()){
                if(instance<list_Class.size()){
                    AccessibilityNodeInfo nodeInfo=list_Class.get(instance);
                    result=execlick(nodeInfo);
                    if(!result){
                        result=exeGusture(nodeInfo,150);
                    }
                }
            }
        }
     root.recycle();
        return result;
    }
    public boolean longClickForClass(String Class,int instance,int time){
        boolean result=false;
        AccessibilityNodeInfo root=getRootNodeInfo();
        if(root!=null){
            List<AccessibilityNodeInfo> list_Class=findNodeByDec(root,Class);
            if(list_Class!=null&&!list_Class.isEmpty()){
                if(instance<list_Class.size()){
                    AccessibilityNodeInfo nodeInfo=list_Class.get(instance);
                    result=exeGusture(nodeInfo,time);

                }
            }
        }
        root.recycle();
        return result;
    }
    public AccessibilityNodeInfo findclickable(AccessibilityNodeInfo nodeInfo){
        AccessibilityNodeInfo clickable=null;
        if(nodeInfo.isClickable()==true){
            clickable=nodeInfo;
        }else {
            if(nodeInfo.getParent()!=null){
                clickable=findclickable(nodeInfo.getParent());

            }
        }
        return clickable;
    }

}
