import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Erik
 * Date: 17-02-13
 * Time: 15:44
 * To change this template use File | Settings | File Templates.
 */
public class TestRegexp {

    private static String pattern = "\\((((#appsync|#backlog|#bayer|#colgateapp|#garder|#kab|#nettoapp|#redas|#testproject|#UVApp)-\\d+, )*(#appsync|#backlog|#bayer|#colgateapp|#garder|#kab|#nettoapp|#redas|#testproject|#UVApp)-\\d)\\) (.*)";

    public static void main(String[] args) {
        Pattern compile = Pattern.compile(pattern);
        Matcher matcher = compile.matcher("(#appsync-1, #appsync-2) Fixed");
        boolean matches = matcher.matches();
        String group = matcher.group(1);

        String group1 = matcher.group(5);
        System.out.println("Last" + group1);
        String[] split = group.split(",");
        List<String> issues = new ArrayList<String>();
        for (String s : split) {
            issues.add(s.trim().substring(1));
        }

        System.out.println(issues);

        System.out.println(group);
        System.out.println(matches);
    }
}
