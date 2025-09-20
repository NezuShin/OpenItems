package su.nezushin.openitems;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {


    @SafeVarargs
    public static <T> List<T> concatLists(List<T>... lists) {
        List<T> r = new ArrayList<>();

        for (var i : lists) {
            r.addAll(i);
        }

        return r;
    }
}
