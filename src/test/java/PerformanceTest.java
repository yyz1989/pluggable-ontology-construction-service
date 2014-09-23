import org.rug.yyz.ontologyservice.rabbitmq.EventReceiver;
import org.rug.yyz.ontologyservice.rabbitmq.EventSender;

/**
 * Created with IntelliJ IDEA.
 * User: yuanzhe
 * Date: 13-12-9
 * Time: 下午5:36
 * To change this template use File | Settings | File Templates.
 */

/**
 * The class for a performance test case which includes 400 variable creations and 800 state changes
 */
public class PerformanceTest {
    public static void main(String[] argv) {
/*        Runtime r = Runtime.getRuntime();
        long t1 = r.totalMemory()/1024;
        long f1 = r.freeMemory()/1024;
        System.out.println(t1+"KB");
        System.out.println(f1+"KB");*/
        EventSender.establishConnection();
        int vars=400;
        int calls=800;

/*        for(int i=1;i<=vars/2;i++) {
            String variable1=i+"|"+"jack"+"|"+"location"+i+"|"+"room1|false|string|false";
            String variable2=i+"|"+"lamp"+"|"+"switch"+i+"|"+"room1|true|string|false";
            EventSender.sendMessage(variable1);
            EventSender.sendMessage(variable2);
        }*/

        for(int j=1;j<=calls;j++) {
            if(j%2==1) EventSender.sendMessage("lamp|switch1|on");
            else EventSender.sendMessage("lamp|switch1|off");
        }

        EventSender.sendMessage("Test finished");

        System.out.println("Test started at "+System.currentTimeMillis());
        EventReceiver.establishConnection();

    }
}
