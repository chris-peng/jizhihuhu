package top.lcmatrix.jizhihuhu.common;

import java.util.ArrayList;
import java.util.List;
import cn.hutool.cache.impl.TimedCache;
public class MyCache {

    public static final int STATUS_WAIT = 0;
    public static final int STATUS_GENERATING = 1;
    public static final int STATUS_GENERATED= 2;
    public static final int STATUS_RETURNED = 3;

    /**
     * 0 - wait, 1 - generating, 2 - returned
     */
    public static final TimedCache<String, Integer> userMsgStatus = new TimedCache<>(60000);

    private static final TimedCache<String, Integer> msgRetryCount = new TimedCache<>(60000);

    public static final TimedCache<String, String> msgReplyCache = new TimedCache<>(60000);

    private static final TimedCache<String, FixedSizeList<String>> userMsgs = new TimedCache<>(60000 * 15);

    private static final TimedCache<String, Boolean> userLongSessionStatus = new TimedCache<>(60000 * 2);
    private static final TimedCache<String, List<String>> userLongSessionMsgs = new TimedCache<>(60000 * 2);

    public static int incrementRetryCount(String msgId){
        Integer count = msgRetryCount.get(msgId, false);
        if(count == null){
            count = 0;
        }
        count++;
        msgRetryCount.put(msgId, count);
        return count;
    }

    public static void addMsg(String userId, String msg){
        FixedSizeList<String> msgs = userMsgs.get(userId);
        if(msgs == null){
            msgs = new FixedSizeList<>(4);
        }
        msgs.push(msg);
        userMsgs.put(userId, msgs);
        Boolean userLongSessionStatus = MyCache.userLongSessionStatus.get(userId);
        if(userLongSessionStatus != null && userLongSessionStatus) {
            List<String> longSessionMsgs = userLongSessionMsgs.get(userId);
            if(longSessionMsgs == null){
                longSessionMsgs = new ArrayList<>();
            }
            longSessionMsgs.add(msg);
            userLongSessionMsgs.put(userId, longSessionMsgs);
        }
    }

    public static List<String> getMsgs(String userId){
        Boolean userLongSessionStatus = MyCache.userLongSessionStatus.get(userId);
        if(userLongSessionStatus == null || !userLongSessionStatus) {
            return userMsgs.get(userId);
        } else {
            return userLongSessionMsgs.get(userId);
        }
    }

    public static void clearMsgs(String userId){
        userMsgs.remove(userId);
        userLongSessionMsgs.remove(userId);
    }

    public static void setUserLongSessionStatus(String userId, boolean enabled){
        userLongSessionStatus.put(userId, enabled);
    }
}
